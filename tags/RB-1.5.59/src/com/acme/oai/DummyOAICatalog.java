
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

package com.acme.oai;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;

/**
 * DummyOAICatalog is an example of how to implement the AbstractCatalog interface.
 * Pattern an implementation of the AbstractCatalog interface after this class
 * to have OAICat work with your database. Your effort may be minimized by confining
 * your changes to areas identified by "YOUR CODE GOES HERE" comments. In truth, though,
 * you can do things however you want, as long as the non-private methods return
 * what they're supposed to.
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class DummyOAICatalog extends AbstractCatalog {
    /**
     * maximum number of entries to return for ListRecords and ListIdentifiers
     */
    private static int maxListSize;

    /**
     * pending resumption tokens
     */
    private HashMap resumptionResults = new HashMap();

    /**************************************************************
     * YOUR CODE GOES HERE
     * delete dummyDb and create new private variables
     * to manage your database here
     **************************************************************/
    
    /**
     * Dummy database. Note that the two items each contain two metadataFormats;
     * oai_dc and oai_etdms
     */
    private String[] dummyDb = {
        "<record><header><identifier>oai:oaicat.oclc.org:OCLCNo/ocm00000012</identifier><datestamp>2001-02-02</datestamp><setSpec>music</setSpec><setSpec>music:(muzak)</setSpec></header><metadata><oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:language>eng</dc:language><dc:date>1964</dc:date><dc:type>Text data</dc:type><dc:identifier>ocm00000012</dc:identifier><dc:identifier>64063999 //r83</dc:identifier><dc:coverage>n-us---</dc:coverage><dc:subject>KF6369.3--.G3</dc:subject><dc:subject>340</dc:subject><dc:creator>Galles, George Raymond,--1918-</dc:creator><dc:title>Involuntary conversions under the Federal income tax laws.</dc:title><dc:publisher>[University of Idaho, Bureau of Business and Economic Research]</dc:publisher><dc:publisher>Moscow</dc:publisher><dc:format>v, 46 l.</dc:format><dc:format>28 cm.</dc:format><dc:relation>Idaho BBER--monograph no. 2</dc:relation><dc:subject>Income tax--Law and legislation--United States.</dc:subject><dc:relation>University of Idaho.--Bureau of Business and Economic Research.--Idaho BBER monograph ;--no. 2.</dc:relation></oai_dc:dc><oai_etdms:thesis xmlns:oai_etdms=\"http://www.ndltd.org/standards/metadata/etdms/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ndltd.org/standards/metadata/etdms/1.0/ http://www.ndltd.org/standards/metadata/etdms/1.0/etdms.xsd\"><oai_etdms:title>Involuntary conversions under the Federal income tax laws.</oai_etdms:title><oai_etdms:creator>Galles, George Raymond,--1918-</oai_etdms:creator><oai_etdms:subject>KF6369.3--.G3</oai_etdms:subject><oai_etdms:subject>340</oai_etdms:subject><oai_etdms:subject>Income tax--Law and legislation--United States.</oai_etdms:subject><oai_etdms:publisher>[University of Idaho, Bureau of Business and Economic Research]</oai_etdms:publisher><oai_etdms:publisher>Moscow</oai_etdms:publisher><oai_etdms:date>1964</oai_etdms:date><oai_etdms:type>Text data</oai_etdms:type><oai_etdms:format>v, 46 l.--28 cm.</oai_etdms:format><oai_etdms:identifier>ocm00000012</oai_etdms:identifier><oai_etdms:identifier>64063999 //r83</oai_etdms:identifier><oai_etdms:language>eng</oai_etdms:language><oai_etdms:coverage>n-us---</oai_etdms:coverage></oai_etdms:thesis></metadata></record>",
        "<rec><header><identifier>oai:oaicat.oclc.org:OCLCNo/ocm00003601</identifier><datestamp>2001-02-02</datestamp><setSpec>music</setSpec></header><metadata><oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:language>eng</dc:language><dc:date>1967</dc:date><dc:type>Text data</dc:type><dc:identifier>ocm00003600</dc:identifier><dc:identifier>68066972 //r915</dc:identifier><dc:subject>E78.W3--W3 no. 25</dc:subject><dc:subject>970.4/96/37</dc:subject><dc:creator>Barnes, Paul L.</dc:creator><dc:title>Archaeology of the Dean Site, Twin Falls County, Idaho,</dc:title><dc:publisher>[Pullman,</dc:publisher><dc:format>vii, 89 p.--illus., maps.</dc:format><dc:format>28 cm.</dc:format><dc:relation>Laboratory of Anthropology, Washington State University. Report of investigations,--no. 25</dc:relation><dc:subject>Dean site.</dc:subject><dc:subject>Idaho--Antiquities.</dc:subject><dc:relation>Reports of investigations (Washington State University. Laboratory of Anthropology) ;--no. 25.</dc:relation></oai_dc:dc><oai_etdms:thesis xmlns:oai_etdms=\"http://www.ndltd.org/standards/metadata/etdms/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ndltd.org/standards/metadata/etdms/1.0/ http://www.ndltd.org/standards/metadata/etdms/1.0/etdms.xsd\"><oai_etdms:title>Archaeology of the Dean Site, Twin Falls County, Idaho,</oai_etdms:title><oai_etdms:creator>Barnes, Paul L.</oai_etdms:creator><oai_etdms:subject>E78.W3--W3 no. 25</oai_etdms:subject><oai_etdms:subject>970.4/96/37</oai_etdms:subject><oai_etdms:subject>Dean site.</oai_etdms:subject><oai_etdms:subject>Idaho--Antiquities.</oai_etdms:subject><oai_etdms:publisher>[Pullman,</oai_etdms:publisher><oai_etdms:date>1967</oai_etdms:date><oai_etdms:type>Text data</oai_etdms:type><oai_etdms:format>vii, 89 p.--illus., maps.--28 cm.</oai_etdms:format><oai_etdms:identifier>ocm00003600</oai_etdms:identifier><oai_etdms:identifier>68066972 //r915</oai_etdms:identifier><oai_etdms:language>eng</oai_etdms:language></oai_etdms:thesis></metadata></rec>"
    };
    
    /**************************************************************
     * YOUR CODE GOES HERE
     * delete dummySets and then figure out an alternative way
     * to identify the sets to be returned to ListSets requests.
     * One way might be to include them in the oaicat.properties
     * file and then have the constructor grab them. The ideal
     * solution, though,  is probably to query your database
     * somehow for any sets that exist there.
     **************************************************************/
    
    /**
     * Dummy sets.
     */
    private String[] dummySets = {
        "<set><setSpec>music</setSpec><setName>Music collection</setName></set>",
        "<set><setSpec>music:(muzak)</setSpec><setName>Muzak collection</setName></set>",
        "<set><setSpec>music:(elec)</setSpec><setName>Electronic Music Collection</setName><setDescription><oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.epenarchives.org/OAI/2.0/oai_dc.xsd\"><dc:description>This set contains metadata describing electronic music recordings made during the 1950ies</dc:description></oai_dc:dc></setDescription></set>"
    };
    
    /**
     * Construct a DummyOAICatalog object
     *
     * @param properties a properties object containing initialization parameters
     */
    public DummyOAICatalog(Properties properties) {
        String maxListSize = properties.getProperty("DummyOAICatalog.maxListSize");
        if (maxListSize == null) {
            throw new IllegalArgumentException("DummyOAICatalog.maxListSize is missing from the properties file");
        } else {
            DummyOAICatalog.maxListSize = Integer.parseInt(maxListSize);
        }
        
        /************************************************************
         * YOUR CODE GOES HERE
         * Load other properties you need to initialize your database
         * (and perhaps sets) and store them in private instance
         * variables.
         ************************************************************/
        /************************************************************
         * YOUR CODE GOES HERE
         * Given the configuration properties above, open your
         * database here and store any necessary variables
         * to manage it in private instance variables.
         ************************************************************/
    }
    
    /**
     * Retrieve a list of schemaLocation values associated with the specified
     * identifier.
     *
     * @param identifier the OAI identifier
     * @return a Vector containing schemaLocation Strings
     * @exception IdDoesNotExistException the specified identifier can't be found
     * @exception NoMetadataFormatsException the specified identifier was found
     * but the item is flagged as deleted and thus no schemaLocations (i.e.
     * metadataFormats) can be produced.
     */
    public Vector getSchemaLocations(String identifier)
        throws IdDoesNotExistException, NoMetadataFormatsException {
        /**********************************************************************
         * YOUR CODE GOES HERE
         * Retrieve the specified native item from your database.
         **********************************************************************/
        Object nativeItem = dummyDb[0];
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        /*
         * Let your recordFactory decide which schemaLocations
         * (i.e. metadataFormats) it can produce from the record.
         * Doing so will preserve the separation of database access
         * (which happens here) from the record content interpretation
         * (which is the responsibility of the RecordFactory implementation).
         */
        if (nativeItem == null) {
            throw new IdDoesNotExistException(identifier);
        } else {
            return getRecordFactory().getSchemaLocations(nativeItem);
        }
    }

    /**
     * Retrieve a list of identifiers that satisfy the specified criteria
     *
     * @param from beginning date using the proper granularity
     * @param until ending date using the proper granularity
     * @param set the set name or null if no such limit is requested
     * @param metadataPrefix the OAI metadataPrefix or null if no such limit is requested
     * @return a Map object containing entries for "headers" and "identifiers" Iterators
     * (both containing Strings) as well as an optional "resumptionMap" Map.
     * It may seem strange for the map to include both "headers" and "identifiers"
     * since the identifiers can be obtained from the headers. This may be true, but
     * AbstractCatalog.listRecords() can operate quicker if it doesn't
     * need to parse identifiers from the XML headers itself. Better
     * still, do like I do below and override AbstractCatalog.listRecords().
     * AbstractCatalog.listRecords() is relatively inefficient because given the list
     * of identifiers, it must call getRecord() individually for each as it constructs
     * its response. It's much more efficient to construct the entire response in one fell
     * swoop by overriding listRecords() as I've done here.
     */
    public Map listIdentifiers(String from, String until, String set, String metadataPrefix) {
        purge(); // clean out old resumptionTokens
        Map listIdentifiersMap = new HashMap();
        ArrayList headers = new ArrayList();
        ArrayList identifiers = new ArrayList();
        
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/
        
        /* Get some records from your database */
        Object[] nativeItems = dummyDb;
        int count;
        
        /* load the headers and identifiers ArrayLists. */
        for (count=0; count < maxListSize && count < nativeItems.length; ++count) {
            /* Use the RecordFactory to extract header/identifier pairs for each item */
            String[] header = getRecordFactory().createHeader(nativeItems[count]);
            headers.add(header[0]);
            identifiers.add(header[1]);
        }
        
        /* decide if you're done */
        if (count < nativeItems.length) {
            String resumptionId = getResumptionId();
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, nativeItems);

            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(count));
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(metadataPrefix);
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listIdentifiersMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                                     nativeItems.length,
                                                                     0));
            //          listIdentifiersMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listIdentifiersMap.put("headers", headers.iterator());
        listIdentifiersMap.put("identifiers", identifiers.iterator());
        return listIdentifiersMap;
    }

    /**
     * Retrieve the next set of identifiers associated with the resumptionToken
     *
     * @param resumptionToken implementation-dependent format taken from the
     * previous listIdentifiers() Map result.
     * @return a Map object containing entries for "headers" and "identifiers" Iterators
     * (both containing Strings) as well as an optional "resumptionMap" Map.
     * @exception BadResumptionTokenException the value of the resumptionToken
     * is invalid or expired.
     */
    public Map listIdentifiers(String resumptionToken)
        throws BadResumptionTokenException {
        purge(); // clean out old resumptionTokens
        Map listIdentifiersMap = new HashMap();
        ArrayList headers = new ArrayList();
        ArrayList identifiers = new ArrayList();
        
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/
        /**********************************************************************
         * parse your resumptionToken and look it up in the resumptionResults,
         * if necessary
         **********************************************************************/
        StringTokenizer tokenizer = new StringTokenizer(resumptionToken, ":");
        String resumptionId;
        int oldCount;
        String metadataPrefix;
        try {
            resumptionId = tokenizer.nextToken();
            oldCount = Integer.parseInt(tokenizer.nextToken());
            metadataPrefix = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new BadResumptionTokenException();
        }
        
        /* Get some more records from your database */
        Object[] nativeItems = (Object[])resumptionResults.remove(resumptionId);
        if (nativeItems == null) {
            throw new BadResumptionTokenException();
        }
        int count;
        
        /* load the headers and identifiers ArrayLists. */
        for (count = 0; count < maxListSize && count+oldCount < nativeItems.length; ++count) {
            /* Use the RecordFactory to extract header/identifier pairs for each item */
            String[] header = getRecordFactory().createHeader(nativeItems[count+oldCount]);
            headers.add(header[0]);
            identifiers.add(header[1]);
        }
        
        /* decide if you're done. */
        if (count+oldCount < nativeItems.length) {
            resumptionId = getResumptionId();
            
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, nativeItems);
            
            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(oldCount + count));
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(metadataPrefix);
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listIdentifiersMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                                     nativeItems.length,
                                                                     oldCount));
            //          listIdentifiersMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listIdentifiersMap.put("headers", headers.iterator());
        listIdentifiersMap.put("identifiers", identifiers.iterator());
        return listIdentifiersMap;
    }

    /**
     * Retrieve the specified metadata for the specified identifier
     *
     * @param identifier the OAI identifier
     * @param metadataPrefix the OAI metadataPrefix
     * @return the <record/> portion of the XML response.
     * @exception CannotDisseminateFormatException the metadataPrefix is not
     * supported by the item.
     * @exception IdDoesNotExistException the identifier wasn't found
     */
    public String getRecord(String identifier, String metadataPrefix)
        throws CannotDisseminateFormatException,
               IdDoesNotExistException {
        /**********************************************************************
         * YOUR CODE GOES HERE
         * Replace this nativeItem assignment with your database API code.
         **********************************************************************/
        Object nativeItem = dummyDb[0];
        if (nativeItem == null)
            throw new IdDoesNotExistException(identifier);
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        return constructRecord(nativeItem, metadataPrefix);
    }

    /**
     * Retrieve a list of records that satisfy the specified criteria. Note, though,
     * that unlike the other OAI verb type methods implemented here, both of the
     * listRecords methods are already implemented in AbstractCatalog rather than
     * abstracted. This is because it is possible to implement ListRecords as a
     * combination of ListIdentifiers and GetRecord combinations. Nevertheless,
     * I suggest that you override both the AbstractCatalog.listRecords methods
     * here since it will probably improve the performance if you create the
     * response in one fell swoop rather than construct it one GetRecord at a time.
     *
     * @param from beginning date using the proper granularity
     * @param until ending date using the proper granularity
     * @param set the set name or null if no such limit is requested
     * @param metadataPrefix the OAI metadataPrefix or null if no such limit is requested
     * @return a Map object containing entries for a "records" Iterator object
     * (containing XML <record/> Strings) and an optional "resumptionMap" Map.
     * @exception CannotDisseminateFormatException the metadataPrefix isn't
     * supported by the item.
     */
    public Map listRecords(String from, String until, String set, String metadataPrefix)
        throws CannotDisseminateFormatException {
        purge(); // clean out old resumptionTokens
        Map listRecordsMap = new HashMap();
        ArrayList records = new ArrayList();
        
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/

        /* Get some records from your database */
        Object[] nativeItem = dummyDb;
        int count;

        /* load the records ArrayList */
        for (count=0; count < maxListSize && count < nativeItem.length; ++count) {
            String record = constructRecord(nativeItem[count], metadataPrefix);
            records.add(record);
        }

        /* decide if you're done */
        if (count < nativeItem.length) {
            String resumptionId = getResumptionId();
            
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, nativeItem);

            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(count));
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(metadataPrefix);
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listRecordsMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                                 nativeItem.length,
                                                                 0));
            //          listRecordsMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSbSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listRecordsMap.put("records", records.iterator());
        return listRecordsMap;
    }

    /**
     * Retrieve the next set of records associated with the resumptionToken
     *
     * @param resumptionToken implementation-dependent format taken from the
     * previous listRecords() Map result.
     * @return a Map object containing entries for "headers" and "identifiers" Iterators
     * (both containing Strings) as well as an optional "resumptionMap" Map.
     * @exception BadResumptionTokenException the value of the resumptionToken argument
     * is invalid or expired.
     */
    public Map listRecords(String resumptionToken)
        throws BadResumptionTokenException {
        Map listRecordsMap = new HashMap();
        ArrayList records = new ArrayList();
        purge(); // clean out old resumptionTokens
        
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/
        /**********************************************************************
         * parse your resumptionToken and look it up in the resumptionResults,
         * if necessary
         **********************************************************************/
        StringTokenizer tokenizer = new StringTokenizer(resumptionToken, ":");
        String resumptionId;
        int oldCount;
        String metadataPrefix;
        try {
            resumptionId = tokenizer.nextToken();
            oldCount = Integer.parseInt(tokenizer.nextToken());
            metadataPrefix = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new BadResumptionTokenException();
        }
        
        /* Get some more records from your database */
        Object[] nativeItem = (Object[])resumptionResults.remove(resumptionId);
        if (nativeItem == null) {
            throw new BadResumptionTokenException();
        }
        int count;
        
        /* load the headers and identifiers ArrayLists. */
        for (count = 0; count < maxListSize && count+oldCount < nativeItem.length; ++count) {
            try {
                String record = constructRecord(nativeItem[count+oldCount], metadataPrefix);
                records.add(record);
            } catch (CannotDisseminateFormatException e) {
                /* the client hacked the resumptionToken beyond repair */
                throw new BadResumptionTokenException();
            }
        }
        
        /* decide if you're done */
        if (count+oldCount < nativeItem.length) {
            resumptionId = getResumptionId();
            
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, nativeItem);
            
            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(oldCount + count));
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(metadataPrefix);
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listRecordsMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                                 nativeItem.length,
                                                                 oldCount));
            //          listRecordsMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listRecordsMap.put("records", records.iterator());
        return listRecordsMap;
    }

    /**
     * Utility method to construct a Record object for a specified
     * metadataFormat from a native record
     *
     * @param nativeItem native item from the dataase
     * @param metadataPrefix the desired metadataPrefix for performing the crosswalk
     * @return the <record/> String
     * @exception CannotDisseminateFormatException the record is not available
     * for the specified metadataPrefix.
     */
    private String constructRecord(Object nativeItem, String metadataPrefix)
        throws CannotDisseminateFormatException {
        String schemaURL = null;

        if (metadataPrefix != null) {
            if ((schemaURL = getCrosswalks().getSchemaURL(metadataPrefix)) == null)
                throw new CannotDisseminateFormatException(metadataPrefix);
        }
        return getRecordFactory().create(nativeItem, schemaURL, metadataPrefix);
    }

    /**
     * Retrieve a list of sets that satisfy the specified criteria
     *
     * @return a Map object containing "sets" Iterator object (contains
     * <setSpec/> XML Strings) as well as an optional resumptionMap Map.
     * @exception OAIBadRequestException signals an http status code 400 problem
     */
    public Map listSets() {
        purge(); // clean out old resumptionTokens
        Map listSetsMap = new HashMap();
        ArrayList sets = new ArrayList();
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/

        /* decide which sets you're going to support */
        String[] dbSets = dummySets;
        int count;

        /* load the sets ArrayList */
        for (count=0; count < maxListSize && count < dbSets.length; ++count) {
            sets.add(dbSets[count]);
        }

        /* decide if you're done */
        if (count < dbSets.length) {
            String resumptionId = getResumptionId();
            
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, dbSets);

            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(count));
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listSetsMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                              dbSets.length,
                                                              0));
            //          listSetsMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSbSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listSetsMap.put("sets", sets.iterator());
        return listSetsMap;
    }

    /**
     * Retrieve the next set of sets associated with the resumptionToken
     *
     * @param resumptionToken implementation-dependent format taken from the
     * previous listSets() Map result.
     * @return a Map object containing "sets" Iterator object (contains
     * <setSpec/> XML Strings) as well as an optional resumptionMap Map.
     * @exception BadResumptionTokenException the value of the resumptionToken
     * is invalid or expired.
     */
    public Map listSets(String resumptionToken)
        throws BadResumptionTokenException {
        Map listSetsMap = new HashMap();
        ArrayList sets = new ArrayList();
        purge(); // clean out old resumptionTokens
        
        /**********************************************************************
         * YOUR CODE GOES HERE
         **********************************************************************/
        /**********************************************************************
         * parse your resumptionToken and look it up in the resumptionResults,
         * if necessary
         **********************************************************************/
        StringTokenizer tokenizer = new StringTokenizer(resumptionToken, ":");
        String resumptionId;
        int oldCount;
        try {
            resumptionId = tokenizer.nextToken();
            oldCount = Integer.parseInt(tokenizer.nextToken());
        } catch (NoSuchElementException e) {
            throw new BadResumptionTokenException();
        }

        /* Get some more sets */
        String[] dbSets = (String[])resumptionResults.remove(resumptionId);
        if (dbSets == null) {
            throw new BadResumptionTokenException();
        }
        int count;

        /* load the sets ArrayList */
        for (count = 0; count < maxListSize && count+oldCount < dbSets.length; ++count) {
            sets.add(dbSets[count+oldCount]);
        }

        /* decide if we're done */
        if (count+oldCount < dbSets.length) {
            resumptionId = getResumptionId();
            
            /*****************************************************************
             * Store an object appropriate for your database API in the
             * resumptionResults Map in place of nativeItems. This object
             * should probably encapsulate the information necessary to
             * perform the next resumption of ListIdentifiers. It might even
             * be possible to encode everything you need in the
             * resumptionToken, in which case you won't need the
             * resumptionResults Map. Here, I've done a silly combination
             * of the two. Stateless resumptionTokens have some advantages.
             *****************************************************************/
            resumptionResults.put(resumptionId, dbSets);
            
            /*****************************************************************
             * Construct the resumptionToken String however you see fit.
             *****************************************************************/
            StringBuffer resumptionTokenSb = new StringBuffer();
            resumptionTokenSb.append(resumptionId);
            resumptionTokenSb.append(":");
            resumptionTokenSb.append(Integer.toString(oldCount + count));
            
            /*****************************************************************
             * Use the following line if you wish to include the optional
             * resumptionToken attributes in the response. Otherwise, use the
             * line after it that I've commented out.
             *****************************************************************/
            listSetsMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
                                                              dbSets.length,
                                                              oldCount));
            //          listSetsMap.put("resumptionMap",
            //                                 getResumptionMap(resumptionTokenSb.toString()));
        }
        /***********************************************************************
         * END OF CUSTOM CODE SECTION
         ***********************************************************************/
        listSetsMap.put("sets", sets.iterator());
        return listSetsMap;
    }

    /**
     * close the repository
     */
    public void close() { }
    
    /**
     * Purge tokens that are older than the configured time-to-live.
     */
    private void purge() {
        ArrayList old = new ArrayList();
        Date now = new Date();
        Iterator keySet = resumptionResults.keySet().iterator();
        while (keySet.hasNext()) {
            String key = (String)keySet.next();
            Date then = new Date(Long.parseLong(key) + getMillisecondsToLive());
            if (now.after(then)) {
                old.add(key);
            }
        }
        Iterator iterator = old.iterator();
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            resumptionResults.remove(key);
        }
    }
    
    /**
     * Use the current date as the basis for the resumptiontoken
     *
     * @return a String version of the current time
     */
    private synchronized static String getResumptionId() {
        Date now = new Date();
        return Long.toString(now.getTime());
    }
}
