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
package ORG.oclc.oai.server.crosswalk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ORG.oclc.oai.server.verb.OAIInternalServerError;

public class CrosswalkItem {
    private String contentType = null;
    private String docType = null;
    private String encoding = null;
    private String nativeRecordSchema = null;
    private String metadataPrefix = null;
    private String schema = null;
    private String metadataNamespace = null;
    private String xsltName = null;
    private Crosswalk crosswalk = null;
    private int rank = -1;
    
    public static final int RANK_DIRECTLY_AVAILABLE = 0;
    public static final int RANK_DERIVED = 1;

    public CrosswalkItem(String nativeRecordSchema, String metadataPrefix,
			 String schema, String metadataNamespace,
			 Crosswalk crosswalk, int rank) {
	this(nativeRecordSchema, metadataPrefix, schema, metadataNamespace,
	     rank);
	this.crosswalk = crosswalk;
    }
    
    public CrosswalkItem(String nativeRecordSchema, String metadataPrefix,
			 String schema, String metadataNamespace,
			 int rank) {
	this.nativeRecordSchema = nativeRecordSchema;
	this.metadataPrefix = metadataPrefix;
	this.schema = schema;
	this.metadataNamespace = metadataNamespace;
	this.rank = rank;
    }
    
    public CrosswalkItem(String metadataPrefix, String schema,
			 String metadataNamespace, Crosswalk crosswalk) {
	this(metadataPrefix, metadataPrefix, schema, metadataNamespace,
	     RANK_DIRECTLY_AVAILABLE);
	this.crosswalk = crosswalk;
    }
    
    public CrosswalkItem(String metadataPrefix, String schema,
			 String metadataNamespace, Class crosswalkClass)
	throws OAIInternalServerError {
	this(metadataPrefix, metadataPrefix, schema, metadataNamespace,
	     RANK_DIRECTLY_AVAILABLE);
	try {
	    this.crosswalk = getCrosswalk(crosswalkClass);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new OAIInternalServerError(e.getMessage());
	}
    }
    
    public CrosswalkItem(String nativeRecordSchema, String metadataPrefix,
			 String schema, String metadataNamespace,
			 Class crosswalkClass, String xsltName)
	throws OAIInternalServerError {
	this(nativeRecordSchema, metadataPrefix, schema, metadataNamespace,
	     RANK_DERIVED);
	this.xsltName = xsltName;
	try {
	    this.crosswalk = getCrosswalk(crosswalkClass);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new OAIInternalServerError(e.getMessage());
	}
    }
    
    public CrosswalkItem(String nativeRecordSchema, String metadataPrefix,
			 String schema, String metadataNamespace,
			 Class crosswalkClass)
	throws OAIInternalServerError {
	this(nativeRecordSchema, metadataPrefix, schema, metadataNamespace,
	     RANK_DIRECTLY_AVAILABLE);
	try {
	    this.crosswalk = getCrosswalk(crosswalkClass);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new OAIInternalServerError(e.getMessage());
	}
    }

    private Crosswalk getCrosswalk(Class crosswalkClass)
	throws NoSuchMethodException, InstantiationException,
	       IllegalAccessException, InvocationTargetException {
	Constructor constructor = crosswalkClass.getConstructor(new Class[] {CrosswalkItem.class});
	return (Crosswalk)constructor.newInstance(new Object[] {this});
    }
	
    public String getNativeRecordSchema() { return nativeRecordSchema; }
    public String getMetadataPrefix() { return metadataPrefix; }
    public String getMetadataNamespace() { return metadataNamespace; }
    public String getSchema() { return schema; }
    public Crosswalk getCrosswalk() { return crosswalk; }
    public String getContentType() { return contentType; }
    public String getDocType() { return docType; }
    public String getEncoding() { return encoding; }
    public String getXSLTName() { return xsltName; }
    
    public int getRank() { return rank; }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("CrosswalkItem: " );
	sb.append(getNativeRecordSchema());
	sb.append(":");
 	sb.append(getMetadataPrefix());
	sb.append(":");
	sb.append(getMetadataNamespace());
	sb.append(":");
	sb.append(getSchema());
	sb.append(":");
	sb.append(getCrosswalk());
	sb.append(":");
	sb.append(getRank());
	return sb.toString();
    }
}