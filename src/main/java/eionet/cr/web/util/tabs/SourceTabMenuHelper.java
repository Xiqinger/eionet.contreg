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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.web.util.tabs;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab menu helper for source page.
 *
 * @author Juhan Voolaid
 */
public class SourceTabMenuHelper {

    /** Source uri. */
    private String uri;

    /**
     * Class constructor.
     *
     * @param uri
     */
    public SourceTabMenuHelper(String uri) {
        this.uri = uri;
    }

    /**
     * Returns tabs.
     *
     * @param selected
     *            - selected tab's title
     * @return
     */
    public List<TabElement> getTabs(String selected) {
        List<TabElement> result = new ArrayList<TabElement>();

        TabElement te1 = new TabElement(TabTitle.VIEW, "/sourceView.action", selected);
        te1.addParam("uri", uri);
        result.add(te1);

        TabElement te2 = new TabElement(TabTitle.EDIT, "/sourceEdit.action", selected);
        te2.addParam("uri", uri);
        result.add(te2);

        TabElement te3 = new TabElement(TabTitle.SAMPLE_TRIPLES, "/sourceTriples.action", selected);
        te3.addParam("uri", uri);
        result.add(te3);

        return result;
    }

    /**
     * Tab titles.
     */
    public static class TabTitle {
        public static final String VIEW = "View";
        public static final String EDIT = "Edit";
        public static final String SAMPLE_TRIPLES = "Sample triples";
    }
}
