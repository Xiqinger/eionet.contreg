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
package eionet.cr.util.xml;

import java.io.File;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ConversionsParserTest extends TestCase {

    /**
     *
     */
    @Test
    public void testConversionsParser() {

        ConversionsParser conversionsParser = new ConversionsParser();
        try {
            conversionsParser.parse(new File(this.getClass().getClassLoader().getResource("test-conversions.xml").getFile()));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Was not expecting this exception: " + t.toString());
        }

        assertNotNull(conversionsParser.getRdfConversionId());
        assertEquals(conversionsParser.getRdfConversionId(), "89");

        assertNotNull(conversionsParser.getRdfConversionXslFileName());
        assertEquals(conversionsParser.getRdfConversionXslFileName(), "art17-habitat-rdf.xsl");
    }
}
