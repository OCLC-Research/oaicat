/*
 * The contents of this file, as updated from time to time by the OCLC Office
 * of Research, are subject to the OCLC Office of Research Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a current copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.html.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the OCLC Office of Research. For more information on the OCLC
 * Office of Research, please see http://www.oclc.org/oclc/research/.
 *
 * This is Original Code.
 *
 * The Initial Developer(s) of the Original Code is (are):
 *  - Ralph LeVan <levan@oclc.org>
 * 
 * Portions created by OCLC are Copyright (C) 2001.
 *
 * 2002-04-09 Created
 */

package edu.getty.oai.server.catalog;
import ORG.oclc.oai.server.catalog.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;



/**
 * GettyFileSystemOAICatalog is an implementation of AbstractCatalog interface
 * with the data sitting in a directory on a filesystem.
 *
 * @author Cristian Grunca, J. Paul Getty Trust
 */

public class GettyFileSystemOAICatalog extends AbstractCatalog {
    static final boolean debug=false;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat();
    protected String           homeDir;
    private HashMap          fileDateMap = new HashMap();
    private HashMap setMap = new HashMap();
    private HashMap          resumptionResults=new HashMap();
    private int              maxListSize;
    private ArrayList sets = null;

    public GettyFileSystemOAICatalog(Properties properties) throws IOException {
        String          temp;

        dateFormatter.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        temp=properties.getProperty("GettyFileSystemOAICatalog.maxListSize");
        if (temp==null)
            throw new IllegalArgumentException("GettyFileSystemOAICatalog."+
                "maxListSize is missing from the properties file");
        maxListSize = Integer.parseInt(temp);
        if(debug)
            System.out.println("in GettyFileSystemOAICatalog(): maxListSize="+
                maxListSize);

        homeDir=properties.getProperty("GettyFileSystemOAICatalog.homeDir");
        if (homeDir==null)
            throw new IllegalArgumentException("GettyFileSystemOAICatalog."+
                "homeDir is missing from the properties file");
        if(debug)
            System.out.println("in GettyFileSystemOAICatalog(): homeDir="+
                homeDir);


	File homeFile = new File(homeDir);
	int homeDirLen = homeFile.getPath().length()+1;
	loadFileMap(homeDirLen, homeFile);
// 	Iterator iterator = fileDateMap.entrySet().iterator();
// 	while (iterator.hasNext()) {
// 	    Map.Entry entry = (Map.Entry)iterator.next();
// 	    System.out.println(entry.getKey() + ":" + entry.getValue());
// 	}
	sets = getSets(properties);
    }

    private static ArrayList getSets(Properties properties) {
        TreeMap treeMap = new TreeMap();
	String propertyPrefix = "Sets.";
	Enumeration propNames = properties.propertyNames();
	while (propNames.hasMoreElements()) {
	    String propertyName = (String)propNames.nextElement();
	    if (propertyName.startsWith(propertyPrefix)) {
                treeMap.put(propertyName, properties.get(propertyName));
	    }
	}
 	return new ArrayList(treeMap.values());
    }
    
    private void loadFileMap(int homeDirLen, File currentDir)
        throws IOException {
        try {
            String[] list = currentDir.list();
            //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //DocumentBuilder builder = factory.newDocumentBuilder();
            for (int i=0; i<list.length; ++i) {
                File child = new File(currentDir, list[i]);
                if (child.isDirectory() && !"CVS".equals(child.getName())) {
                    loadFileMap(homeDirLen, child);
                } else if (isMetadataFile(child)) {
                    String path = file2path(homeDirLen, child);
                    System.out.println("parsing : " + path);
                    String datestamp = date2OAIDatestamp(new Date(child.lastModified()));

					fileDateMap.put(path, datestamp);
                                                        
                    String setSpec = file2OAISetSpec(child);
                    if(setSpec != null)
                    {
						ArrayList setSpecList = (ArrayList)setMap.get(setSpec);
						if (setSpecList == null) {
							setSpecList = new ArrayList();
							setMap.put(setSpec, setSpecList);
						}
						
						setSpecList.add(path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Override this method if some files exist in the
     * filesystem that aren't metadata records.
     *
     * @param child the File to be investigated
     * @return true if it contains metadata, false otherwise
     */
    protected boolean isMetadataFile(File child) {
        return true;
    }

    /**
     * Override this method if you don't like the default localIdentifiers.
     * @param homeDirLen the length of the home directory path
     * @param file the File object containing the native record
     * @return localIdentifier
     */
    protected String file2path(int homeDirLen, File file) {
	String fileName = file.getPath().substring(homeDirLen).replace(File.separatorChar, '/');
	return fileName;
    }
    /**
     * Override this method if you don't like the default localIdentifiers.
     * @param file the File object containing the native record
     * @return localIdentifier
     */
    protected String file2OAISetSpec(File file) {
	String fileName = file.getName();
	int lastIndex = fileName.lastIndexOf(".");
	if(lastIndex < 0)
		return null;
	lastIndex = fileName.lastIndexOf(".", lastIndex - 1);	
	if(lastIndex < 0)
		return null;
	int firstIndex = fileName.lastIndexOf(".", lastIndex - 1);
	if(firstIndex < 0)
		return null;	
	String setSpec = fileName.substring(firstIndex+1,lastIndex);
	
	return setSpec;
    }

    /**
     * Override this method if you don't like the default localIdentifiers.
     * @param localIdentifier the localIdentifier as parsed from the OAI identifier
     * @return the File object containing the native record
     */
    protected File localIdentifier2File(String localIdentifier) {
        String fileName = localIdentifier.replace('/', File.separatorChar);
	return new File(homeDir, fileName);
    }

    private String date2OAIDatestamp(Date date) {
         return dateFormatter.format(date);
    }

    private HashMap getNativeHeader(String path)
        throws IOException {
        HashMap recordMap = null;
        if (fileDateMap.containsKey(path)) {
            recordMap = new HashMap();
            recordMap.put("localIdentifier", path.substring(0, path.lastIndexOf(".")));
            recordMap.put("lastModified", fileDateMap.get(path));
            ArrayList setSpecs = new ArrayList();
            Iterator keySet = setMap.keySet().iterator();
            while (keySet.hasNext()) {
                String key = (String)keySet.next();
                ArrayList identifierList = (ArrayList)setMap.get(key);
                if (identifierList.contains(path)) {
                    setSpecs.add(key);
                }
            }
            recordMap.put("setSpecs", setSpecs.iterator());
            return recordMap;
        }
        return recordMap;
    }

    private ArrayList getExtensionList(String localIdentifier) {
//         System.out.println("getExtensionList: localIdentifier="
//                            + localIdentifier);
        ArrayList list = new ArrayList();
        Iterator iterator = fileDateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
//             System.out.println("entry.getKey()=" + entry.getKey());
            if (((String)entry.getKey()).startsWith(localIdentifier)) {
                list.add(((String)entry.getKey()).substring(localIdentifier.length()+1));
            }
        }
        return list;
    }
    
    private HashMap getNativeRecord(String path)
        throws IOException {
        HashMap recordMap = getNativeHeader(path);
        if (recordMap == null) {
            return null;
        } else {
            File file = localIdentifier2File(path);
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                byte[] buffer = new byte[(int)file.length()];
                bis.read(buffer, 0, (int)file.length());
                recordMap.put("recordBytes", buffer);
                bis.close();
                fis.close();
                return recordMap;
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }
    
    /**
     * Retrieve the specified metadata for the specified oaiIdentifier
     *
     * @param     oaiIdentifier the OAI identifier
     * @param     metadataPrefix the OAI metadataPrefix
     * @return    the Record object containing the result.
     * @exception CannotDisseminateFormatException signals an http status
     *                code 400 problem
     * @exception IdDoesNotExistException signals an http status code 404
     *                problem
     * @exception OAIInternalServerError signals an http status code 500
     *                problem
     */
    public String getRecord(String oaiIdentifier, String metadataPrefix)
        throws IdDoesNotExistException, CannotDisseminateFormatException,
               OAIInternalServerError {
        HashMap nativeItem = null;
        try {
	    String localIdentifier
		= getRecordFactory().fromOAIIdentifier(oaiIdentifier);

            nativeItem = getNativeRecord(localIdentifier + "." + metadataPrefix);
            if (nativeItem == null)
                throw new IdDoesNotExistException(oaiIdentifier);
            return constructRecord(nativeItem, metadataPrefix);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OAIInternalServerError("Database Failure");
        }
    }


    /**
     * Retrieve a list of schemaLocation values associated with the specified
     * oaiIdentifier.
     *
     * We get passed the ID for a record and are supposed to return a list
     * of the formats that we can deliver the record in.  Since we are assuming
     * that all the records in the directory have the same format, the
     * response to this is static;
     *
     * @param oaiIdentifier the OAI identifier
     * @return a Vector containing schemaLocation Strings
     * @exception OAIBadRequestException signals an http status code 400
     *            problem
     * @exception OAINotFoundException signals an http status code 404 problem
     * @exception OAIInternalServerError signals an http status code 500
     *            problem
     */
    public Vector getSchemaLocations(String oaiIdentifier)
      throws IdDoesNotExistException, OAIInternalServerError, NoMetadataFormatsException {
        ArrayList extensionList = null;
         try {
	    String localIdentifier
		= getRecordFactory().fromOAIIdentifier(oaiIdentifier);
            extensionList = getExtensionList(localIdentifier);
         } catch (Exception e) {
             e.printStackTrace();
             throw new OAIInternalServerError("Database Failure");
         }
        
        if (extensionList != null) {
            return getRecordFactory().getSchemaLocations(extensionList);
        } else {
            throw new IdDoesNotExistException(oaiIdentifier);
        }
    }


    /**
     * Retrieve a list of Identifiers that satisfy the criteria parameters
     *
     * @param from beginning date in the form of YYYY-MM-DD or null if earliest
     * date is desired
     * @param until ending date in the form of YYYY-MM-DD or null if latest
     * date is desired
     * @param set set name or null if no set is desired
     * @return a Map object containing an optional "resumptionToken" key/value
     * pair and an "identifiers" Map object. The "identifiers" Map contains OAI
     * identifier keys with corresponding values of "true" or null depending on
     * whether the identifier is deleted or not.
     * @exception OAIBadRequestException signals an http status code 400
     *            problem
     * @exception OAIInternalServerError signals an http status code 500
     *            problem
     */
    public Map listIdentifiers(String from, String until, String set, String metadataPrefix)
        throws BadArgumentException, CannotDisseminateFormatException, OAIInternalServerError,
               NoItemsMatchException {
        purge(); // clean out old resumptionTokens
        Map listIdentifiersMap = new HashMap();
        ArrayList headers = new ArrayList();
        ArrayList identifiers = new ArrayList();
	Iterator iterator = fileDateMap.entrySet().iterator();
	int numRows = fileDateMap.entrySet().size();
	int count = 0;
        ArrayList setIdentifiers = (ArrayList)setMap.get(set);
	while (count < maxListSize && iterator.hasNext()) {
	    Map.Entry entryDateMap = (Map.Entry)iterator.next();
            String fileDate = (String)entryDateMap.getValue();
            String path = (String)entryDateMap.getKey();
            if (fileDate.compareTo(from) >= 0
                && fileDate.compareTo(until) <= 0
                && (set == null || setIdentifiers != null && setIdentifiers.contains(path))) {
                try {
                    HashMap nativeHeader = getNativeHeader((String)entryDateMap.getKey());
                    String[] header = getRecordFactory().createHeader(nativeHeader);
                    headers.add(header[0]);
                    identifiers.add(header[1]);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new OAIInternalServerError(e.getMessage());
                }
            }
	}

        if (count == 0)
            throw new NoItemsMatchException();
        
	/* decide if you're done */
	if (iterator.hasNext()) {
	    String resumptionId = getRSName();
	    resumptionResults.put(resumptionId, iterator);

	    /*****************************************************************
	     * Construct the resumptionToken String however you see fit.
	     *****************************************************************/
	    StringBuffer resumptionTokenSb = new StringBuffer();
	    resumptionTokenSb.append(resumptionId);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(count));
	    resumptionTokenSb.append(":");
 	    resumptionTokenSb.append(Integer.toString(numRows));
 	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(metadataPrefix);
 	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(set);
            
	    /*****************************************************************
	     * Use the following line if you wish to include the optional
	     * resumptionToken attributes in the response. Otherwise, use the
	     * line after it that I've commented out.
	     *****************************************************************/
 	    listIdentifiersMap.put("resumptionMap",
 				   getResumptionMap(resumptionTokenSb.toString(),
 						    numRows,
 						    0));
// 	    listIdentifiersMap.put("resumptionMap",
// 				   getResumptionMap(resumptionTokenSb.toString()));
	}
        listIdentifiersMap.put("headers", headers.iterator());
        listIdentifiersMap.put("identifiers", identifiers.iterator());
        return listIdentifiersMap;
    }

    /**
     * Retrieve the next set of Identifiers associated with the resumptionToken
     *
     * @param resumptionToken implementation-dependent format taken from the
     * previous listIdentifiers() Map result.
     * @return a Map object containing an optional "resumptionToken" key/value
     * pair and an "identifiers" Map object. The "identifiers" Map contains OAI
     * identifier keys with corresponding values of "true" or null depending on
     * whether the identifier is deleted or not.
     * @exception OAIBadRequestException signals an http status code 400
     *            problem
     * @exception OAIInternalServerError signals an http status code 500
     *            problem
     */
    public Map listIdentifiers(String resumptionToken)
      throws BadResumptionTokenException, OAIInternalServerError {
        purge(); // clean out old resumptionTokens
        Map listIdentifiersMap = new HashMap();
        ArrayList headers = new ArrayList();
        ArrayList identifiers = new ArrayList();
        
        /**********************************************************************
         * parse your resumptionToken and look it up in the resumptionResults,
         * if necessary
         **********************************************************************/
        StringTokenizer tokenizer = new StringTokenizer(resumptionToken, ":");
        String resumptionId;
        int oldCount;
        String metadataPrefix;
	int numRows;
        String set;
        try {
            resumptionId = tokenizer.nextToken();
            oldCount = Integer.parseInt(tokenizer.nextToken());
	    numRows = Integer.parseInt(tokenizer.nextToken());
            metadataPrefix = tokenizer.nextToken();
            set = tokenizer.nextToken();
            if(set.equals("null")) set = null;            
        } catch (NoSuchElementException e) {
            throw new BadResumptionTokenException();
        }

	/* Get some more records from your database */
	Iterator iterator = (Iterator)resumptionResults.remove(resumptionId);
	if (iterator == null) {
	    System.out.println("GettyFileSystemOAICatalog.listIdentifiers: reuse of old resumptionToken?");
	    iterator = fileDateMap.entrySet().iterator();
	    for (int i = 0; i<oldCount; ++i)
		iterator.next();
	}
        
	/* load the headers and identifiers ArrayLists. */
	int count = 0;
        ArrayList setIdentifiers = (ArrayList)setMap.get(set);
	while (count < maxListSize && iterator.hasNext()) {
	    Map.Entry entryDateMap = (Map.Entry)iterator.next();
            String path = (String)entryDateMap.getKey();
            if (set == null || setIdentifiers != null && setIdentifiers.contains(path)) {
                try {
                    HashMap nativeHeader = getNativeHeader((String)entryDateMap.getKey());
                    String[] header = getRecordFactory().createHeader(nativeHeader);
                    headers.add(header[0]);
                    identifiers.add(header[1]);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new OAIInternalServerError(e.getMessage());
                }
            }
	}
        
	/* decide if you're done. */
	if (iterator.hasNext()) {
	    resumptionId = getRSName();
	    resumptionResults.put(resumptionId, iterator);
            
	    /*****************************************************************
	     * Construct the resumptionToken String however you see fit.
	     *****************************************************************/
	    StringBuffer resumptionTokenSb = new StringBuffer();
	    resumptionTokenSb.append(resumptionId);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(oldCount + count));
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(numRows));
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(metadataPrefix);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(set);
            
	    /*****************************************************************
	     * Use the following line if you wish to include the optional
	     * resumptionToken attributes in the response. Otherwise, use the
	     * line after it that I've commented out.
	     *****************************************************************/
	    listIdentifiersMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
								     numRows,
								     oldCount));
	    //          listIdentifiersMap.put("resumptionMap",
	    //                                 getResumptionMap(resumptionTokenSb.toString()));
	}

        listIdentifiersMap.put("headers", headers.iterator());
        listIdentifiersMap.put("identifiers", identifiers.iterator());
        return listIdentifiersMap;
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
    private String constructRecord(HashMap nativeItem, String metadataPrefix)
        throws CannotDisseminateFormatException, OAIInternalServerError {
        String schemaURL = null;
	Iterator setSpecs = getSetSpecs(nativeItem);
	Iterator abouts = getAbouts(nativeItem);

        if (metadataPrefix != null) {
            if ((schemaURL = getCrosswalks().getSchemaURL(metadataPrefix)) == null)
                throw new CannotDisseminateFormatException(metadataPrefix);
        }
        return getRecordFactory().create(nativeItem, schemaURL, metadataPrefix, setSpecs, abouts);
    }

    /**
     * get an Iterator containing the setSpecs for the nativeItem
     *
     * @param rs ResultSet containing the nativeItem
     * @return an Iterator containing the list of setSpec values for this nativeItem
     */
    private Iterator getSetSpecs(HashMap nativeItem)
	throws OAIInternalServerError {
        return null;
    }

    /**
     * get an Iterator containing the abouts for the nativeItem
     *
     * @param rs ResultSet containing the nativeItem
     * @return an Iterator containing the list of about values for this nativeItem
     */
    private Iterator getAbouts(HashMap nativeItem)
	throws OAIInternalServerError {
        return null;
    }

    /**
     * Retrieve a list of records that satisfy the specified criteria
     *
     * @param from beginning date in the form of YYYY-MM-DD or null if earliest
     * date is desired
     * @param until ending date in the form of YYYY-MM-DD or null if latest
     * date is desired
     * @param set set name or null if no set is desired
     * @param metadataPrefix the OAI metadataPrefix
     * @return a Map object containing an optional "resumptionToken" key/value
     * pair and a "records" Iterator object. The "records" Iterator contains a
     * set of Records objects.
     * @exception OAIBadRequestException signals an http status code 400
     *            problem
     * @exception OAIInternalServerError signals an http status code 500
     *            problem
     */
    public Map listRecords(String from, String until, String set,
                                    String metadataPrefix)
      throws BadArgumentException, CannotDisseminateFormatException,
      OAIInternalServerError, NoItemsMatchException {
        purge(); // clean out old resumptionTokens
        Map listRecordsMap = new HashMap();
        ArrayList records = new ArrayList();
	Iterator iterator = fileDateMap.entrySet().iterator();
	int numRows = fileDateMap.entrySet().size();
	int count = 0;
        ArrayList setIdentifiers = (ArrayList)setMap.get(set);
	while (count < maxListSize && iterator.hasNext()) {
	    Map.Entry entryDateMap = (Map.Entry)iterator.next();
            String fileDate = (String)entryDateMap.getValue();
            String path = (String)entryDateMap.getKey();
            String extension = path.substring(path.lastIndexOf(".")+1);
            if (fileDate.compareTo(from) >= 0
                && fileDate.compareTo(until) <= 0
                && extension.equals(metadataPrefix)
                && (set == null || setIdentifiers != null && setIdentifiers.contains(path))) {
                try {
                    HashMap nativeItem = getNativeRecord((String)entryDateMap.getKey());
                    String record = constructRecord(nativeItem, metadataPrefix);
                    records.add(record);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new OAIInternalServerError(e.getMessage());
                }
            }
	}
        
        if (count == 0)
            throw new NoItemsMatchException();
        
	/* decide if you're done */
	if (iterator.hasNext()) {
	    String resumptionId = getRSName();
	    resumptionResults.put(resumptionId, iterator);

	    /*****************************************************************
	     * Construct the resumptionToken String however you see fit.
	     *****************************************************************/
	    StringBuffer resumptionTokenSb = new StringBuffer();
	    resumptionTokenSb.append(resumptionId);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(count));
	    resumptionTokenSb.append(":");
 	    resumptionTokenSb.append(Integer.toString(numRows));
 	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(metadataPrefix);
 	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(set);
            
	    /*****************************************************************
	     * Use the following line if you wish to include the optional
	     * resumptionToken attributes in the response. Otherwise, use the
	     * line after it that I've commented out.
	     *****************************************************************/
 	    listRecordsMap.put("resumptionMap",
 				   getResumptionMap(resumptionTokenSb.toString(),
 						    numRows,
 						    0));
// 	    listRecordsMap.put("resumptionMap",
// 				   getResumptionMap(resumptionTokenSb.toString()));
	}
        listRecordsMap.put("records", records.iterator());
        return listRecordsMap;
    }


    /**
     * Retrieve the next set of records associated with the resumptionToken
     *
     * @param resumptionToken implementation-dependent format taken from the
     * previous listRecords() Map result.
     * @return a Map object containing an optional "resumptionToken" key/value
     * pair and a "records" Iterator object. The "records" Iterator contains a
     * set of Records objects.
     * @exception OAIBadRequestException signals an http status code 400
     *            problem
     * @exception OAIInternalServerError signals an http status code 500
     *            problem
     */
    public Map listRecords(String resumptionToken)
      throws BadResumptionTokenException,
      OAIInternalServerError {
        purge(); // clean out old resumptionTokens
        Map listRecordsMap = new HashMap();
        ArrayList records = new ArrayList();
        
        /**********************************************************************
         * parse your resumptionToken and look it up in the resumptionResults,
         * if necessary
         **********************************************************************/
        StringTokenizer tokenizer = new StringTokenizer(resumptionToken, ":");
        String resumptionId;
        int oldCount;
        String metadataPrefix;
	int numRows;
        String set;
        try {
            resumptionId = tokenizer.nextToken();
            oldCount = Integer.parseInt(tokenizer.nextToken());
	    numRows = Integer.parseInt(tokenizer.nextToken());
            metadataPrefix = tokenizer.nextToken();
            set = tokenizer.nextToken();
            if(set.equals("null")) set = null;
        } catch (NoSuchElementException e) {
            throw new BadResumptionTokenException();
        }

	/* Get some more records from your database */
	Iterator iterator = (Iterator)resumptionResults.remove(resumptionId);
	if (iterator == null) {
	    System.out.println("GettyFileSystemOAICatalog.listRecords: reuse of old resumptionToken?");
	    iterator = fileDateMap.entrySet().iterator();
	    for (int i = 0; i<oldCount; ++i)
		iterator.next();
	}
        
	/* load the records ArrayLists. */
	int count = 0;
        ArrayList setIdentifiers = (ArrayList)setMap.get(set);
	while (count < maxListSize && iterator.hasNext()) {
	    Map.Entry entryDateMap = (Map.Entry)iterator.next();
            String path = (String)entryDateMap.getKey();
            if (set == null || setIdentifiers != null && setIdentifiers.contains(path)) {
                try {
                    HashMap nativeItem = getNativeRecord((String)entryDateMap.getKey());
                    String record = constructRecord(nativeItem, metadataPrefix);
                    records.add(record);
                    count++;
                } catch (CannotDisseminateFormatException e) {
                    /* the client hacked the resumptionToken beyond repair */
                    throw new BadResumptionTokenException();
                } catch (IOException e) {
                    /* the file is probably missing */
                    throw new BadResumptionTokenException();
                }
            }
	}
        
	/* decide if you're done. */
	if (iterator.hasNext()) {
	    resumptionId = getRSName();
	    resumptionResults.put(resumptionId, iterator);
            
	    /*****************************************************************
	     * Construct the resumptionToken String however you see fit.
	     *****************************************************************/
	    StringBuffer resumptionTokenSb = new StringBuffer();
	    resumptionTokenSb.append(resumptionId);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(oldCount + count));
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(Integer.toString(numRows));
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(metadataPrefix);
	    resumptionTokenSb.append(":");
	    resumptionTokenSb.append(set);
            
	    /*****************************************************************
	     * Use the following line if you wish to include the optional
	     * resumptionToken attributes in the response. Otherwise, use the
	     * line after it that I've commented out.
	     *****************************************************************/
	    listRecordsMap.put("resumptionMap", getResumptionMap(resumptionTokenSb.toString(),
								     numRows,
								     oldCount));
	    //          listRecordsMap.put("resumptionMap",
	    //                                 getResumptionMap(resumptionTokenSb.toString()));
	}

        listRecordsMap.put("records", records.iterator());
        return listRecordsMap;
    }


    public Map listSets() throws OAIInternalServerError,
				 NoSetHierarchyException {
        if (sets.size() == 0)
            throw new NoSetHierarchyException();
         Map listSetsMap = new LinkedHashMap();
         listSetsMap.put("sets", sets.iterator());
         return listSetsMap;
    }


    public Map listSets(String resumptionToken)
      throws BadResumptionTokenException, OAIInternalServerError {
	throw new BadResumptionTokenException();
    }


    /**
     * close the repository
     */
    public void close() { }


    /**
     * Purge tokens that are older than the time-to-live.
     */
    private void purge() {
        ArrayList old = new ArrayList();
        Date      then, now = new Date();
        Iterator  keySet = resumptionResults.keySet().iterator();
        String    key;

        while (keySet.hasNext()) {
            key=(String)keySet.next();
            then=new Date(Long.parseLong(key)+getMillisecondsToLive());
            if (now.after(then)) {
                old.add(key);
            }
        }
        Iterator iterator = old.iterator();
        while (iterator.hasNext()) {
            key = (String)iterator.next();
            resumptionResults.remove(key);
        }
    }


    /**
     * Use the current date as the basis for the resumptiontoken
     *
     * @return a long integer version of the current time
     */
    private synchronized static String getRSName() {
        Date now = new Date();
        return Long.toString(now.getTime());
    }
}
