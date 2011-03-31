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
package ORG.oclc.oai.server.verb;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

/**
 * This class represents an BadVerb response on either the server or
 * on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class BadVerb extends ServerVerb {
    /**
     * Construct the xml response on the server side.
     *
     * @param context the servlet context
     * @param request the servlet request
     * @param response 
     * @param serverTransformer 
     * @return a String containing the xml response
     * @throws TransformerException 
     */
    public static String construct(HashMap context,
                                   HttpServletRequest request, HttpServletResponse response,
                                   Transformer serverTransformer)
        throws TransformerException {
        Properties properties =
	    (Properties)context.get("OAIHandler.properties");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	String styleSheet = properties.getProperty("OAIHandler.styleSheet");
	if (styleSheet != null) {
	    sb.append("<?xml-stylesheet type=\"text/xsl\" href=\"");
	    sb.append(styleSheet);
	    sb.append("\"?>");
	}
        sb.append("<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/");
        sb.append(" http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">");
	sb.append("<responseDate>");
	sb.append(createResponseDate(new Date()));
	sb.append("</responseDate>");
// 	sb.append("<requestURL>");
//         sb.append(getRequestURL(request));
// 	sb.append("</requestURL>");
	sb.append("<request>");
	try {
	    sb.append(request.getRequestURL().toString());
	} catch (java.lang.NoSuchMethodError e) {
	    sb.append(request.getRequestURL().toString());
//	    sb.append(HttpUtils.getRequestURL(request).toString());
	}
	sb.append("</request>");
	sb.append("<error code=\"badVerb\">Illegal verb</error>");
        sb.append("</OAI-PMH>");
	return render(response, "text/xml; charset=UTF-8", sb.toString(), serverTransformer);
    }
}
