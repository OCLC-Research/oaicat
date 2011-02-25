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
package ORG.oclc.oai.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Utility methods for OAICat and OAIHarvester
 */
public class OAIUtil {
    private static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private static TransformerFactory tFactory = TransformerFactory.newInstance();
    static {
        dbFactory.setNamespaceAware(true);
    }
    
    /**
     * XML encode a string.
     * @param s any String
     * @return the String with &amp;, &lt;, and &gt; encoded for use in XML.
     */
    public static String xmlEncode(String s) {
        StringBuffer sb = new StringBuffer();

        for (int i=0; i<s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Convert a packed LCCN String to MARC display format.
     * @param packedLCCN an LCCN String in packed storage format (e.g. 'n&nbsp;&nbsp;2001050268').
     * @return  an LCCN String in MARC display format (e.g. 'n2001-50268').
     */
    public static String toLCCNDisplay(String packedLCCN) {
	StringBuffer sb = new StringBuffer();
	if (Character.isDigit(packedLCCN.charAt(2))) {
	    sb.append(packedLCCN.substring(0, 2).trim());
	    sb.append(packedLCCN.substring(2, 6));
	    sb.append("-");
	    int i = Integer.parseInt(packedLCCN.substring(6).trim());
	    sb.append(Integer.toString(i));
	} else {
	    sb.append(packedLCCN.substring(0, 3).trim());
	    sb.append(packedLCCN.substring(3, 5));
	    sb.append("-");
	    int i = Integer.parseInt(packedLCCN.substring(5).trim());
	    sb.append(Integer.toString(i));
	}
	return sb.toString();
    }

    /**
     * convert a packed LCCN to display format.
     * @param packedLCCN an LCCN String in packed storage format (e.g. 'n&nbsp;&nbsp;2001050268').
     * @return  an LCCN String in MARC display format (e.g. 'n2001-50268').
     * @deprecated use toLCCNDisplay() instead.
     */
    public static String getLCCN(String packedLCCN) {
	return toLCCNDisplay(packedLCCN);
    }

    public static Document parse(InputStream is)
    throws SAXException, IOException, ParserConfigurationException {
        return getThreadedDocumentBuilder().parse(is);
    }

    public static DocumentBuilder getThreadedDocumentBuilder()
    throws ParserConfigurationException {
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        return builder;
    }
    
    /**
     * Transform a DOM Node into an XML String.
     * @param node
     * @return an XML String representation of the specified Node
     * @throws TransformerException
     */
    public static String toString(Node node)
    throws TransformerException {
        return toString(node, true);
    }
    
    /**
     * Transform a DOM Node into an XML String
     * @param node
     * @param omitXMLDeclaration
     * @return an XML String representation of the specified Node
     * @throws TransformerException
     */
    public static String toString(Node node, boolean omitXMLDeclaration)
    throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer =
            getThreadedIdentityTransformer(omitXMLDeclaration);
        Source source = new DOMSource(node);
        Result result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }
    
    /**
     * Get a thread-safe Transformer without an assigned transform. This is useful
     * for transforming a DOM Document into XML text.
     * @param omitXmlDeclaration 
     * @return an "identity" Transformer assigned to the current thread
     * @throws TransformerConfigurationException
     */
    public static Transformer getThreadedIdentityTransformer(
            boolean omitXmlDeclaration)
    throws TransformerConfigurationException {
        return getTransformer(omitXmlDeclaration, (String) null);
    }
    
    /**
     * Get a thread-safe Transformer.
     * @param omitXmlDeclaration
     * @param xslURL
     * @return a thread-safe Transformer
     * @throws TransformerConfigurationException
     */
    public static Transformer getTransformer(boolean omitXmlDeclaration,
            String xslURL)
    throws TransformerConfigurationException {
        return getTransformer(omitXmlDeclaration, true, xslURL);
    }
    
    /**
     * @param omitXmlDeclaration
     * @param standalone
     * @param xslURL 
     * @return a Transformer for the specified XSL document
     * @throws TransformerConfigurationException
     */
    public static Transformer getTransformer(
            boolean omitXmlDeclaration,
            boolean standalone, String xslURL)
    throws TransformerConfigurationException {
        Transformer transformer = null;
        if (xslURL == null) {
            transformer = tFactory.newTransformer(); // "never null"
        } else {
            Source xslSource = null;
            if (xslURL.startsWith("file://")) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslURL.substring(6));
                xslSource = new StreamSource(is);
            } else {
                xslSource = new StreamSource(xslURL);
            }
            transformer = tFactory.newTransformer(xslSource);
        }
//      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE,
                standalone?"yes":"no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                omitXmlDeclaration?"yes":"no");
        return transformer;
    }
    
}
