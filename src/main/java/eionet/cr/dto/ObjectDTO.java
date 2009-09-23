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
package eionet.cr.dto;

import eionet.cr.util.Hashes;
import eionet.cr.web.util.FactsheetObjectId;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ObjectDTO {
	
	/** */
	public enum Type{LITERAL, RESOURCE;}

	/** */
	private String value;
	private long hash;
	
	private boolean anonymous;
	private boolean literal;
	private String language;
	
	private String derivSourceUri;
	private long derivSourceHash;
	private long derivSourceGenTime;
	
	private long sourceObjectHash;
	
	private String sourceUri;
	private long sourceHash;
	
	/**
	 * 
	 * @param value
	 * @param language
	 * @param literal
	 * @param anonymous
	 */
	public ObjectDTO(String value, String language, boolean literal, boolean anonymous){
		
		this.value = value;
		this.language = language;
		this.literal = literal;
		this.anonymous = anonymous;
		this.hash = Hashes.spoHash(value);
	}
	
	/**
	 * 
	 * @param value
	 * @param literal
	 */
	public ObjectDTO(String value, boolean literal){
		this(value, null, literal, false);	
	}
	
	/**
	 * 
	 * @param hash
	 * @param sourceHash
	 * @param derivSourceHash
	 * @param sourceObjectHash
	 */
	private ObjectDTO(long hash, long sourceHash, long derivSourceHash, long sourceObjectHash){
		
		this.hash = hash;
		this.sourceHash = sourceHash;
		this.derivSourceHash = derivSourceHash;
		this.sourceObjectHash = sourceObjectHash;
	}
	
	/**
	 * 
	 * @param hash
	 * @param sourceHash
	 * @param derivSourceHash
	 * @param sourceObjectHash
	 * @return
	 */
	public static ObjectDTO create(long hash, long sourceHash, long derivSourceHash, long sourceObjectHash){
		
		return new ObjectDTO(hash, sourceHash, derivSourceHash, sourceObjectHash);
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @return the literal
	 */
	public boolean isLiteral() {
		return literal;
	}
	/**
	 * @return the anonymous
	 */
	public boolean isAnonymous() {
		return anonymous;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		
		if (this==other)
			return true;
		
		if (!(other instanceof ObjectDTO))
			return false;
		
		
		String otherValue = ((ObjectDTO)other).getValue();
		return getValue()==null ? otherValue==null : getValue().equals(otherValue);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){
		return getValue()==null ? 0 : getValue().hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public long getHash(){
		return hash;
	}

	/**
	 * @return the derivSourceUri
	 */
	public String getDerivSourceUri() {
		return derivSourceUri;
	}
	
	/**
	 * @param derivSourceUri the derivSourceUri to set
	 */
	public void setDerivSourceUri(String derivSource) {
		this.derivSourceUri = derivSource;
	}

	/**
	 * @return the sourceUri
	 */
	public String getSourceUri() {
		return sourceUri;
	}

	/**
	 * @param sourceUri the sourceUri to set
	 */
	public void setSourceUri(String source) {
		this.sourceUri = source;
	}

	/**
	 * 
	 * @return
	 */
	public String getSourceSmart() {
		
		if (derivSourceUri!=null && derivSourceUri.trim().length()>0)
			return derivSourceUri;
		else if (sourceUri!=null && sourceUri.trim().length()>0)
			return sourceUri;
		else
			return null;
	}

	/**
	 * @return the sourceObjectHash
	 */
	public long getSourceObjectHash() {
		return sourceObjectHash;
	}

	/**
	 * @param sourceObjectHash the sourceObjectHash to set
	 */
	public void setSourceObjectHash(long sourceObjectHash) {
		this.sourceObjectHash = sourceObjectHash;
	}
	
	/**
	 * 
	 * @param hash
	 */
	public void setHash(long hash) {
		this.hash = hash;
	}

	/**
	 * @return the derivSourceGenTime
	 */
	public long getDerivSourceGenTime() {
		return derivSourceGenTime;
	}

	/**
	 * @param derivSourceGenTime the derivSourceGenTime to set
	 */
	public void setDerivSourceGenTime(long derivSourceGenTime) {
		this.derivSourceGenTime = derivSourceGenTime;
	}

	/**
	 * @return the derivSourceHash
	 */
	public long getDerivSourceHash() {
		return derivSourceHash;
	}

	/**
	 * @param derivSourceHash the derivSourceHash to set
	 */
	public void setDerivSourceHash(long derivSourceHash) {
		this.derivSourceHash = derivSourceHash;
	}

	/**
	 * @return the sourceHash
	 */
	public long getSourceHash() {
		return sourceHash;
	}

	/**
	 * 
	 * @return
	 */
	public long getSourceHashSmart() {
		return derivSourceHash!=0 ? derivSourceHash : sourceHash;
	}

	/**
	 * @param sourceHash the sourceHash to set
	 */
	public void setSourceHash(long sourceHash) {
		this.sourceHash = sourceHash;
	}

	/**
	 * 
	 * @return
	 */
	public String getId(){

		return FactsheetObjectId.format(this);
	}

}
