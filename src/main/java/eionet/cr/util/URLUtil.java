/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URLUtil {

    /** */
    private static final Logger LOGGER = Logger.getLogger(URLUtil.class);

    /** */
    private static final String DEFAULT_CHARACTAER_ENCODING = "UTF-8";

    /** */
    private static final List<String> sessionIdentifiers = Arrays.asList("JSESSIONID", "PHPSESSID", "ASPSESSIONID");

    /**
     *
     * @param s
     * @return boolean
     */
    public static boolean isURL(String s) {

        if (s == null || s.trim().length() == 0)
            return false;

        try {
            URL url = new URL(s);
            return url != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Create the User-agent header value from config file.
     *
     * @return the User-agent value.
     */
    public static String userAgentHeader() {

        String ua = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_USERAGENT);
        Object[] args = new String[1];
        args[0] = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_VERSION);
        return MessageFormat.format(ua, args);
    }

    /**
     * Connect to the URL and check if it is modified since the timestamp argument. Returns null, if it cannot be determined for
     * sure.
     *
     * @param urlString
     * @param timestamp
     * @return true if it is modified.
     */
    public static Boolean isModifiedSince(String urlString, long timestamp) {

        if (!URLUtil.isURL(urlString)) {
            return Boolean.FALSE;
        }

        if (timestamp == 0) {
            return Boolean.TRUE;
        }

        URLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlString, "#"));
            urlConnection = replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("User-Agent", userAgentHeader());
            urlConnection.setIfModifiedSince(timestamp);

            int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return Boolean.FALSE;
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                return Boolean.TRUE;
            } else {
                // Return null to indicate if it's unclear whether the source has
                // been modified or not.
                return null;
            }
        } catch (IOException ioe) {
            return null;
        } finally {
            URLUtil.disconnect(urlConnection);
        }
    }

    /**
     * Connect to the URL and check if it exists at the remote end. Local ids are removed (the part after the '#') before
     * connecting.
     *
     * @param urlStr
     *            the URL to check.
     * @return true is the URL does <b>NOT</b> exist.
     */
    public static boolean isNotExisting(String urlStr) {

        int responseCode = -1;
        IOException ioe = null;
        URLConnection urlConnection = null;
        try {
            URL url = new URL(StringUtils.substringBefore(urlStr, "#"));
            urlConnection = replaceURLSpaces(url).openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
        } catch (IOException e) {
            ioe = e;
        } finally {
            URLUtil.disconnect(urlConnection);
        }

        return ioe instanceof MalformedURLException || ioe instanceof UnknownHostException
                || (responseCode >= 400 && responseCode <= 499) || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED
                || responseCode == HttpURLConnection.HTTP_VERSION;
    }

    /**
     * A cross-the-stream-to-get-water replacement for {@link java.net.URL#getHost()}.
     *
     * @param uri
     * @param dflt
     * @return String
     */
    public static String extractUrlHost(String uri) {

        if (URLUtil.isURL(uri)) {
            String host = "";
            URL url;
            try {
                url = new URL(StringUtils.substringBefore(uri, "#"));
                host = uri.substring(0, uri.indexOf(url.getPath()));

            } catch (Exception ex) {

            }
            return host;
        } else {
            return null;
        }
    }

    /**
     *
     * @param url
     * @return URL
     * @throws MalformedURLException
     */
    public static URL replaceURLSpaces(URL url) throws MalformedURLException {

        return url == null ? null : new URL(replaceURLSpaces(url.toString()));
    }

    /**
     *
     * @param url
     * @return URL
     */
    public static String replaceURLSpaces(String url) {

        return url == null ? null : StringUtils.replace(url.toString(), " ", "%20");
    }

    /**
     *
     * @param urlConnection
     */
    public static void disconnect(URLConnection urlConnection) {

        if (urlConnection != null && urlConnection instanceof HttpURLConnection) {
            try {
                ((HttpURLConnection) urlConnection).disconnect();
            } catch (Exception e) {
                LOGGER.warn("Error when disconnection from " + urlConnection.getURL() + ": " + e.toString());
            }
        }
    }

    /**
     *
     * @param url1
     * @param url2
     * @return
     */
    public static boolean equalUrls(String url1, String url2) {

        return normalizeUrl(url1).equals(normalizeUrl(url2));
    }

    /**
     *
     * @param urlString
     * @return
     */
    public static String normalizeUrl(String urlString) {

        // if given URL string is null, return it as it is
        if (urlString == null) {
            return urlString;
        }

        // we're going to need both the URL and URI wrappers
        URL url = null;
        URI uri = null;
        try {
            url = new URL(urlString.trim());
            uri = url.toURI();
        } catch (MalformedURLException e) {
            return urlString;
        } catch (URISyntaxException e) {
            return urlString;
        }

        // get all the various parts of this URL
        String protocol = url.getProtocol();
        String userInfo = url.getUserInfo();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        String query = url.getQuery();
        String reference = url.getRef();

        // start building the result, processing each of the above-found URL parts

        StringBuilder result = new StringBuilder();

        try {
            if (!StringUtils.isEmpty(protocol)) {
                result.append(decodeEncode(protocol.toLowerCase())).append("://");
            }

            if (!StringUtils.isEmpty(userInfo)) {
                result.append(decodeEncode(userInfo, ":")).append("@");
            }

            if (!StringUtils.isEmpty(host)) {
                result.append(decodeEncode(host.toLowerCase()));
            }

            if (port != -1 && port != 80) {
                result.append(":").append(port);
            }

            if (!StringUtils.isEmpty(path)) {
                result.append(normalizePath(path));
            }

            if (!StringUtils.isEmpty(query)) {
                String normalizedQuery = normalizeQueryString(uri);
                if (!StringUtils.isBlank(normalizedQuery)) {
                    result.append("?").append(normalizedQuery);
                }
            }

            if (!StringUtils.isEmpty(reference)) {
                result.append("#").append(decodeEncode(reference));
            }
        } catch (UnsupportedEncodingException e) {
            throw new CRRuntimeException("Unsupported encoding: " + e.getMessage(), e);
        }

        return result.toString();
    }

    /**
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String decodeEncode(String str) throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(str)) {
            return str;
        } else {
            String decoded = URLDecoder.decode(str, DEFAULT_CHARACTAER_ENCODING);
            return URLEncoder.encode(decoded, DEFAULT_CHARACTAER_ENCODING);
        }
    }

    /**
     *
     * @param str
     * @param exceptions
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String decodeEncode(String str, String exceptions) throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(str)) {
            return str;
        } else if (StringUtils.isEmpty(exceptions)) {
            return decodeEncode(str);
        }

        StringBuilder result = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(str, exceptions, true);
        while (tokenizer.hasMoreTokens()) {

            String token = tokenizer.nextToken();
            if (!token.isEmpty() && exceptions.contains(token)) {
                result.append(token);
            } else {
                result.append(decodeEncode(token));
            }
        }

        return result.toString();
    }

    /**
     *
     * @param uri
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String normalizeQueryString(URI uri) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        List<NameValuePair> paramPairs = URLEncodedUtils.parse(uri, DEFAULT_CHARACTAER_ENCODING);
        for (NameValuePair paramPair : paramPairs) {

            String name = paramPair.getName();
            String value = paramPair.getValue();
            String normalizedName = decodeEncode(name);
            if (!isSessionId(normalizedName)) {

                if (result.length() > 0) {
                    result.append("&");
                }
                result.append(normalizedName);
                if (value != null) {
                    result.append("=").append(decodeEncode(value));
                }
            }
        }

        return result.toString();
    }

    /**
     *
     * @param str
     * @return
     */
    private static boolean isSessionId(String str) {

        if (str == null) {
            return false;
        }

        return sessionIdentifiers.contains(str.toUpperCase());
    }

    /**
     *
     * @param path
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String normalizePath(String path) throws UnsupportedEncodingException {

        for (String sessionId : sessionIdentifiers) {

            int i = path.indexOf(";" + sessionId + "=");
            if (i >= 0) {
                path = path.substring(0, i);
            }
        }

        return decodeEncode(path, "/;");
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String urlString =
                "http://usern%68me:password@d%69main:123/path;ASPSESSIONID=93AE727EADF5D5351219360F1952051F?jaanus=onu&phpsessid=999#fragment_%69d";
        System.out.println(urlString);
        System.out.println(normalizeUrl(urlString));
    }
}
