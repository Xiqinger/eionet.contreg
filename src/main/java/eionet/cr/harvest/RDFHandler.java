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
package eionet.cr.harvest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.persist.IHarvestPersister;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.harvest.persist.PersisterException;
import eionet.cr.harvest.persist.PersisterFactory;
import eionet.cr.harvest.persist.mysql.MySQLDefaultPersister;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.harvest.util.arp.AResourceImpl;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.UnicodeUtils;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class RDFHandler implements StatementHandler{
	
	/** */
	public static final String URN_UUID_PREFIX = "urn:uuid:";
	
	private Log logger;

	/** */
	private boolean parsingStarted = false;
	
	/** */
	private int tripleCounter = 0;
	private static final int BULK_INSERT_SIZE = 50000;
	
	/** */
	private static final String EMPTY_STRING = "";
	
	/** */
	private HashSet<Long> addedSubjects = new HashSet<Long>();
	private HashSet<Long> addedPredicates = new HashSet<Long>();
	
	/** */
	private Map<String, List<Pair<String,String>>> rdfValues = new HashMap<String, List<Pair<String,String>>>();
	
	/** */
	private boolean rdfContentFound = false;
	
	/** */
	private IHarvestPersister persister;

	/** */
	private int storedTriplesCount;
	private int distinctSubjectsCount;

	
	/**
	 * 
	 * @param sourceUrl
	 * @param genTime
	 */
	public RDFHandler(PersisterConfig config){

		/* argument validations */
		
		if (StringUtils.isBlank(config.getSourceUrl()))
			throw new IllegalArgumentException("Source URL must not be null or blank!");
		else if (config.getGenTime() <= 0)
			throw new IllegalArgumentException("Gen-time must be > 0");
		
		/* field assignments */
		this.logger = new HarvestLog(config.getSourceUrl(), config.getGenTime(), LogFactory.getLog(this.getClass()));
		
		persister = PersisterFactory.getPersister(config);
	}
	
	/** 
	 * @see eionet.cr.harvest.IRDFHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 * {@inheritDoc}
	 */
	public void statement(AResource subject, AResource predicate, AResource object){
		
		if (rdfContentFound==false){
			rdfContentFound = true;
		}
		
		statement(subject, predicate, object.isAnonymous() ? object.getAnonymousID() : object.getURI(), EMPTY_STRING, false, object.isAnonymous());
	}

	/** 
	 * @see eionet.cr.harvest.IRDFHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 * {@inheritDoc}
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object){
		
		if (rdfContentFound==false){
			rdfContentFound = true;
		}
		
		statement(subject, predicate, object.toString(), object.getLang(), true, false);
	}
	
	/** 
	 * @see eionet.cr.harvest.IRDFHandler#addSourceMetadata(eionet.cr.dto.SubjectDTO)
	 * {@inheritDoc}
	 */
	public void addSourceMetadata(SubjectDTO subjectDTO){
		
		if (subjectDTO!=null && subjectDTO.getPredicateCount()>0){
			
			AResource subject = new AResourceImpl(subjectDTO.getUri());
			for (String predicateUri:subjectDTO.getPredicates().keySet()){
				
				Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
				if (objects!=null && !objects.isEmpty()){
					
					AResource predicate = new AResourceImpl(predicateUri);
					for (ObjectDTO object:objects){
						
						statement(subject, predicate, object.toString(), object.getLanguage(), object.isLiteral(), object.isAnonymous());
					}
				}
			}
		}

	}
	
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 */
	private void statement(AResource subject, AResource predicate, String object, String objectLang, boolean litObject, boolean anonObject){

		tripleCounter++;
		
		try{
			// if this is the first statement, perform certain "startup" actions
			if (parsingStarted==false){
				onParsingStarted();
				parsingStarted = true;
			}
			
			// ignore statements with anonymous predicates
			if (predicate.isAnonymous()){
				return;
			}

			// ignore literal objects with length==0
			if (litObject && object.length()==0){
				return;
			}
			
			// set up subject URI, and subject and predicate hashes
			boolean anonSubject = subject.isAnonymous();
			String subjectUri = anonSubject ? generateUUID(subject.getAnonymousID()) : subject.getURI();
			long subjectHash = Hashes.spoHash(subjectUri);
			long predicateHash = Hashes.spoHash(predicate.getURI());
			
			// replace entity references in the object if it's a literal
			if (litObject){
				object = UnicodeUtils.replaceEntityReferences(object);
			}
			
			// replace object with its UUID, if it's an anonymous resource
			if (anonObject && !litObject){
				object = generateUUID(object);
			}

			// no more modifications of object are expected below this line, so prepare object hash
			long objectHash = Hashes.spoHash(object);
			
			// we remember rdfValues			
			if (anonSubject && predicate.getURI().equals(Predicates.RDF_VALUE)){
				List<Pair<String,String>> subjectRdfValues = rdfValues.get(subject.getAnonymousID());
				if (subjectRdfValues==null){
					subjectRdfValues = new ArrayList<Pair<String,String>>();
					rdfValues.put(subjectUri, subjectRdfValues);
				}
				subjectRdfValues.add(new Pair<String,String>(object, objectLang));
			}

			// add the triple to the SQL insert batch
			addTriple(subjectHash, anonSubject, predicateHash, object, objectHash, objectLang, litObject, anonObject);
			
			// if the object represents an anonymous subject, lookup the rdf:value(s) of the latter and insert it(them) as derived
			if (anonObject && !litObject){
				List<Pair<String,String>> objectRdfValues = rdfValues.get(object);
				if (objectRdfValues!=null){
					for (Pair<String,String> objectLangPair : objectRdfValues){
						addTriple(subjectHash, anonSubject, predicateHash, objectLangPair.getLeft(), objectHash, objectLangPair.getRight(),
								true, false, Hashes.spoHash(object));
					}
				}
			}

			// if subject not already added into resources, then do so
			if (!resourceAlreadyAdded(subjectHash)){
				addResource(subjectUri, subjectHash);
				addedSubjects.add(subjectHash);
			}

			// if predicate not already added into resources, then do so
			if (!resourceAlreadyAdded(predicateHash)){
				addResource(predicate.getURI(), predicateHash);
				addedSubjects.add(predicateHash);
			}
			
			// if object is a resource and it's not already added into resources, then do so
			if (litObject==false && !resourceAlreadyAdded(objectHash)){
				addResource(object, objectHash);
				addedPredicates.add(subjectHash); // TODO
			}

			// if at BULK_INSERT_SIZE, execute the batch
			if (tripleCounter % BULK_INSERT_SIZE == 0){
				executeBatch();
			}
		}
		catch (Exception e){
			throw new LoadException(e.toString(), e);
		}
	}

	/**
	 * 
	 * @param resourceHash
	 * @return
	 */
	private boolean resourceAlreadyAdded(long resourceHash){
		Long l = Long.valueOf(resourceHash);
		return addedSubjects.contains(l) || addedPredicates.contains(l);
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void onParsingStarted() throws PersisterException{
		persister.openResources();
	}
	
	/**
	 * 
	 * @param subjectHash
	 * @param anonSubject
	 * @param predicateHash
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 * @throws SQLException
	 */
	private void addTriple(long subjectHash, boolean anonSubject, long predicateHash,
			String object, long objectHash, String objectLang, boolean litObject, boolean anonObject) throws PersisterException {
		
		addTriple(subjectHash, anonSubject, predicateHash, object, objectHash, objectLang, litObject, anonObject, 0);
	}

	/**
	 * 
	 * @param subjectHash
	 * @param anonSubject
	 * @param predicateHash
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 * @param objSourceObject
	 * @throws SQLException
	 */
	private void addTriple(long subjectHash, boolean anonSubject, long predicateHash,
			String object, long objectHash, String objectLang, boolean litObject, boolean anonObject, long objSourceObject) throws PersisterException {
		
		persister.addTriple(subjectHash, anonSubject, predicateHash, object, objectHash, objectLang, litObject, anonObject, objSourceObject);
		storedTriplesCount++;
	}
	
	/**
	 * Generates a name-based (i.e. version-3) UUID from the given name, that is unique across all harvests.
	 * 
	 * @param name
	 * @return
	 */
	private String generateUUID(String name){
		return generateUUID(URN_UUID_PREFIX, name);
	}

	/**
	 * 
	 * @param uuidNamePrefix
	 * @param name
	 * @return
	 */
	public static String generateUUID(String uuidNamePrefix, String name){
		
		String uuid = UUID.nameUUIDFromBytes(new StringBuilder(uuidNamePrefix).append(name).toString().getBytes()).toString();
		return new StringBuilder(URN_UUID_PREFIX).append(uuid).toString();
	}
	
	/**
	 * 
	 * @param sourceHash
	 * @param genTime
	 * @return
	 */
	public static String createUuidNamePrefix(String sourceHash, String genTime){
		
		return new StringBuilder(sourceHash).append(":").append(genTime).append(":").toString();
	}

	/**
	 * @param uri
	 * @param uriHash
	 * @throws PersisterException 
	 */
	private void addResource(String uri, long uriHash) throws PersisterException {
		persister.addResource(uri, uriHash);
	}

	/**
	 * 
	 */
	private void executeBatch() throws PersisterException{
		persister.tempCommit();
	}

	/**
	 * 
	 * @throws PersisterException 
	 */
	public void endOfFile() throws PersisterException{
		persister.endOfFile();
	}

	/**
	 * @throws PersisterException
	 */
	public void commit() throws PersisterException {
		persister.commit();
	}

	/**
	 * 
	 * @throws SQLException
	 */
	public void rollback() throws PersisterException{
		logger.debug("Doing harvest rollback");
		persister.rollback();
	}

	/**
	 * @return return stored triple count.
	 */
	public int getStoredTriplesCount() {
		//return persister.getStoredTripleCount();
		return storedTriplesCount;
	}

	/**
	 * @return distinct subject count.
	 */
	public int getDistinctSubjectsCount() {
		//return persister.getDistinctSubjectCount();
		return addedSubjects.size();
	}

	/**
	 * @return triple counter.
	 */
	public int getTripleCounter() {
		return tripleCounter;
	}

	/**
	 * @return
	 */
	public boolean isRdfContentFound() {
		return rdfContentFound;
	}

	/**
	 * @throws PersisterException 
	 * 
	 */
	public void closeResources() throws PersisterException  {
		persister.closeResources();
	}
}