/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.util;

import java.net.MalformedURLException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URIUtilTest extends TestCase{

	/**
	 * 
	 */
	public void testExtractURILabel(){
		
		assertEquals(null, URIUtil.extractURILabel("http://sws.geonames.org/6255148/"));
		assertEquals("6255148", URIUtil.extractURILabel("http://sws.geonames.org/6255148"));
		assertEquals("Name", URIUtil.extractURILabel("http://sws.geonames.org/6255148#Name"));
		assertEquals(null, URIUtil.extractURILabel(null));
		assertEquals(null, URIUtil.extractURILabel(""));
		assertEquals(null, URIUtil.extractURILabel(" \t "));
		assertEquals(null, URIUtil.extractURILabel("Name"));
		
		assertEquals("dflt", URIUtil.extractURILabel("http://sws.geonames.org/6255148/", "dflt"));
		assertEquals(null, URIUtil.extractURILabel("http://sws.geonames.org/6255148/", null));
	}
	
	/**
	 * 
	 */
	public void testFixRelativeUrl(){
		assertEquals("http://sws.geonames.org", URLUtil.extractUrlHost("http://sws.geonames.org/6255148/"));
		try {
			assertEquals("http://rod.eionet.europa.eu/clients.rdf", UrlRedirectAnalyzer.constructFullUrl("http://rod.eionet.europa.eu/clients/10", "/clients.rdf"));
			assertEquals("http://rod.eionet.europa.eu/clients/clients.rdf", UrlRedirectAnalyzer.constructFullUrl("http://rod.eionet.europa.eu/clients/10", "clients.rdf"));
			assertEquals("http://rod.eionet.europa.eu/clients/clients.rdf", UrlRedirectAnalyzer.constructFullUrl("http://rod.eionet.europa.eu/clients/", "clients.rdf"));
		} catch (MalformedURLException ex ){
			fail("Did not expect this exception: " + ex.toString());
		}
	}
	
}
