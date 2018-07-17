/*
 * Created on 23.04.2010
 */
package eionet.cr.util.exporter;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;

import eionet.cr.util.export.XlsExporter;
import eionet.cr.util.export.XmlExporter;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS ExporterTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ExporterTest {

    @Test
    public void testGetRowsLimit() {
        int limit = XlsExporter.getRowsLimit();
        assertTrue(limit > 100);

        int limit2 = XmlExporter.getRowsLimit();
        assertTrue(limit2 == -1);
    }
}
