/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.lanl.myProject.oaicat.crosswalk;

import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import ORG.oclc.oai.server.crosswalk.XSLTCrosswalk;
import ORG.oclc.oai.server.verb.OAIInternalServerError;

/**
 * Convert native "item" to pro. In this case, the native "item"
 * is assumed to already be formatted as an OAI <record> element,
 * with the possible exception that multiple metadataFormats may
 * be present in the <metadata> element. The "crosswalk", merely
 * involves pulling out the one that is requested.
 */
public class XSLToai_wdCrosswalk extends XSLTCrosswalk {
    /**
     * The constructor assigns the schemaLocation associated with this crosswalk. Since
     * the crosswalk is trivial in this case, no properties are utilized.
     *
     * @param properties properties that are needed to configure the crosswalk.
     */
    public XSLToai_wdCrosswalk(Properties properties)
    throws OAIInternalServerError {
        super(properties, "http://www.lanl.gov/nisac/schemas/watchdog/ http://www.lanl.gov/nisac/schemas/oai_wd.xsd", null);
        try {
            String xsltName = properties.getProperty("XSLToai_wdCrosswalk.xsltName");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            if (xsltName != null) {
                StreamSource xslSource = new StreamSource(new FileInputStream(xsltName));
                this.transformer = tFactory.newTransformer(xslSource);
            } else {
                this.transformer = tFactory.newTransformer();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OAIInternalServerError(e.getMessage());
        }
    }
}
