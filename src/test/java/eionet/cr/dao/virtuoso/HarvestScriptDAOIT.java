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
 *        Enriko Käsper
 */

package eionet.cr.dao.virtuoso;

//import com.ibm.icu.util.Calendar;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 *
 *
 * @author Enriko Käsper
 */
public class HarvestScriptDAOIT extends CRDatabaseTestCase {

    /** */
    String script =
            "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> INSERT INTO ?harvestedSource  { ?subject a cr:File } FROM ?harvestedSource";

    /** */
    String script2 =
            "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> INSERT INTO ?harvestedSource  { ?subject <http://www.w3.org/2000/01/rdf-schema#label> \"new soruce\" } FROM ?harvestedSource";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /**
     *
     * @throws DAOException
     */
    @Test
    public void testInsert() throws DAOException {
        String targetUrl = "http://rod.eionet.europa.eu/schema.rdf#Delivery";
        String title = "Post harvest script";
        HarvestScriptDAO postHarvestDao = DAOFactory.get().getDao(HarvestScriptDAO.class);
        postHarvestDao.insert(TargetType.SOURCE, targetUrl, title, script, true, false, null, HarvestScriptType.POST_HARVEST,
                null, null);

        assertTrue(postHarvestDao.exists(TargetType.SOURCE, targetUrl, title));
        assertEquals(1, postHarvestDao.listActive(TargetType.SOURCE, targetUrl, null).size());
    }

    @Test
    public void testActivate() throws DAOException {
        String targetUrl = "http://rod.eionet.europa.eu/schema.rdf#Delivery";
        String title = "Post harvest script";
        HarvestScriptDAO postHarvestDao = DAOFactory.get().getDao(HarvestScriptDAO.class);
        postHarvestDao.insert(TargetType.SOURCE, targetUrl, title, script, true, false, null, HarvestScriptType.POST_HARVEST, 
                null, null);

        List<HarvestScriptDTO> scriptsList = postHarvestDao.listActive(TargetType.SOURCE, targetUrl, null);

        assertEquals(1, postHarvestDao.listActive(TargetType.SOURCE, targetUrl, null).size());

        HarvestScriptDTO script = scriptsList.get(0);
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(script.getId());
        postHarvestDao.activateDeactivate(ids);

        assertEquals(0, postHarvestDao.listActive(TargetType.SOURCE, targetUrl, null).size());
    }

    @Test
    public void testIsScriptsModified() throws DAOException, InterruptedException {
        String targetUrl = "http://rod.eionet.europa.eu/schema.rdf";
        String title1 = "Post harvest script 1";
        String title2 = "Post harvest script 2";
        HarvestScriptDAO postHarvestDao = DAOFactory.get().getDao(HarvestScriptDAO.class);
        postHarvestDao.insert(TargetType.SOURCE, targetUrl, title1, script, true, false, null, 
                HarvestScriptType.POST_HARVEST, null, null);

        // There must definitely be no script newer than two days in the future.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        assertFalse(postHarvestDao.isScriptsModified(cal.getTime(), targetUrl));

        // The above-inserted script is definitely modified after two days in the past.
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        assertTrue(postHarvestDao.isScriptsModified(cal.getTime(), targetUrl));

        // Sleep one second and insert one more script. Now there must be one script newer
        // then the moment we started sleeping.
        Calendar calNow = Calendar.getInstance();
        Thread.sleep(1000);
        postHarvestDao.insert(TargetType.SOURCE, targetUrl, title2, script2, true, false, null, 
                HarvestScriptType.POST_HARVEST, null, null);
        assertTrue(postHarvestDao.isScriptsModified(calNow.getTime(), targetUrl));
    }
}
