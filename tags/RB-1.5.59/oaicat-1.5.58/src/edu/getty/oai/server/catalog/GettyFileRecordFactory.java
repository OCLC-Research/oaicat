
/**
*Copyright (c) 2000-2002 OCLC Online Computer Library Center,
*Inc. and other contributors. All rights reserved.  The contents of this file, as updated
*from time to time by the OCLC Office of Research, are subject to OCLC Research
*Public License Version 2.0 (the "License"); you may not use this file except in
*compliance with the License. You may obtain a current copy of the License at
*http://www.apache.org/licenses/LICENSE-2.0.html.  Software distributed under the License is
*distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express
*or implied. See the License for the specific language governing rights and limitations
*under the License.  This software consists of voluntary contributions made by many
*individuals on behalf of OCLC Research. For more information on OCLC Research,
*please see http://www.oclc.org/oclc/research/.
*
*The Original Code is NewFileRecordFactory.java.
*The Initial Developer of the Original Code is Jeff Young.
*Portions created by Cristian Grunca 
*    CDWALite support
*    File name encoding
*Copyright (C) 2005 J. Paul Getty. All Rights Reserved.
*Contributor(s): Ted Dancescu, Steven Swimmer.
*/

package edu.getty.oai.server.catalog;

import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
//import java.util.StringTokenizer;
import ORG.oclc.oai.server.catalog.*;

/**
 * GettyFileRecordFactory converts native XML "items" to "record" Strings.
 * This factory assumes the native XML item looks exactly like the <record>
 * element of an OAI GetRecord response, with the possible exception that the
 * <metadata> element contains multiple metadataFormats from which to choose.
 */
public class GettyFileRecordFactory extends RecordFactory {
    private String repositoryIdentifier = null;
    
    /**
     * Construct an GettyFileRecordFactory capable of producing the Crosswalk(s)
     * specified in the properties file.
     * @param properties Contains information to configure the factory:
     *                   specifically, the names of the crosswalk(s) supported
     * @exception IllegalArgumentException Something is wrong with the argument.
     */
    public GettyFileRecordFactory(Properties properties)
	throws IllegalArgumentException {
	super(properties);
	repositoryIdentifier = properties.getProperty("GettyFileRecordFactory.repositoryIdentifier");
	if (repositoryIdentifier == null) {
	    throw new IllegalArgumentException("GettyFileRecordFactory.repositoryIdentifier is missing from the properties file");
	}
    }

    /**
     * Utility method to parse the 'local identifier' from the OAI identifier
     *
     * @param identifier OAI identifier (e.g. oai:oaicat.oclc.org:ID/12345)
     * @return local identifier (e.g. ID/12345).
     */
    public String fromOAIIdentifier(String identifier) {
        return identifier;
    }

    /**
     * Construct an OAI identifier from the native item
     *
     * @param nativeItem native Item object
     * @return OAI identifier
     */
    public String getOAIIdentifier(Object nativeItem) {
	StringBuffer sb = new StringBuffer();
	sb.append(getLocalIdentifier(nativeItem));
	return sb.toString();
    }

    /**
     * Extract the local identifier from the native item
     *
     * @param nativeItem native Item object
     * @return local identifier
     */
    public String getLocalIdentifier(Object nativeItem) {
        return (String)((HashMap)nativeItem).get("localIdentifier");
    }

    /**
     * get the datestamp from the item
     *
     * @param nativeItem a native item presumably containing a datestamp somewhere
     * @return a String containing the datestamp for the item
     * @exception IllegalArgumentException Something is wrong with the argument.
     */
    public String getDatestamp(Object nativeItem)
	throws IllegalArgumentException  {
        return (String)((HashMap)nativeItem).get("lastModified");
    }

    /**
     * get the setspec from the item
     *
     * @param nativeItem a native item presumably containing a setspec somewhere
     * @return a String containing the setspec for the item
     * @exception IllegalArgumentException Something is wrong with the argument.
     */
    public Iterator getSetSpecs(Object nativeItem)
	throws IllegalArgumentException  {
        return (Iterator)((HashMap)nativeItem).get("setSpecs");
    }

    /**
     * Get the about elements from the item
     *
     * @param nativeItem a native item presumably containing about information somewhere
     * @return a Iterator of Strings containing &lt;about&gt;s for the item
     * @exception IllegalArgumentException Something is wrong with the argument.
     */
    public Iterator getAbouts(Object nativeItem) throws IllegalArgumentException {
	return null;
    }

    /**
     * Is the record deleted?
     *
     * @param nativeItem a native item presumably containing a possible delete indicator
     * @return true if record is deleted, false if not
     * @exception IllegalArgumentException Something is wrong with the argument.
     */
    public boolean isDeleted(Object nativeItem)
	throws IllegalArgumentException {
	return false;
    }

    /**
     * Allows classes that implement RecordFactory to override the default create() method.
     * This is useful, for example, if the entire &lt;record&gt; is already packaged as the native
     * record. Return null if you want the default handler to create it by calling the methods
     * above individually.
     * 
     * @param nativeItem the native record
     * @return a String containing the OAI &lt;record&gt; or null if the default method should be
     * used.
     */
    public String quickCreate(Object nativeItem, String schemaLocation, String metadataPrefix) {
	// Don't perform quick creates
        try {
            String result = new String((byte[])((HashMap)nativeItem).get("recordBytes"), "UTF-8");
            if (result.startsWith("<?")) {
                int offset = result.indexOf("?>");
                result = result.substring(offset+2);
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
// 	return null;
    }
}
