package eionet.cr.harvest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import eionet.cr.common.LabelPredicates;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.DedicatedHarvestSourceTypes;
import eionet.cr.util.Hashes;
import eionet.cr.util.StringUtils;
import eionet.cr.util.UnicodeUtils;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class NewRDFHandler implements StatementHandler, ErrorHandler{

	/** */
	private static Log logger = LogFactory.getLog(NewRDFHandler.class);
	
	/** */
	private List<SAXParseException> saxErrors = new ArrayList<SAXParseException>();
	private List<SAXParseException> saxWarnings = new ArrayList<SAXParseException>();
	
	/** */
	private long anonIdSeed;
	
	/** */
	private HashSet<String> usedNamespaces = new HashSet();
	
	/** */
	private String sourceUrl;
	private long sourceUrlHash;
	private long genTime;
	
	/** */
	private PreparedStatement preparedStatementForTriples;
	private PreparedStatement preparedStatementForResources;
	
	/** */
	private Connection connection;

	/** */
	private int storedTriplesCount = 0;

	/**
	 * 
	 */
	public NewRDFHandler(String sourceUrl, long genTime){
		
		if (sourceUrl==null || sourceUrl.length()==0 || genTime<=0)
			throw new IllegalArgumentException();
		
		this.sourceUrl = sourceUrl;
		this.sourceUrlHash = Hashes.spoHash(sourceUrl);
		this.genTime = genTime;
		
		// set the hash-seed for anonymous ids
		anonIdSeed = Hashes.spoHash(sourceUrl + String.valueOf(genTime));
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 */
	public void statement(AResource subject, AResource predicate, AResource object){
		
		statement(subject, predicate, object.isAnonymous() ? object.getAnonymousID() : object.getURI(), "", false, object.isAnonymous());
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object){
		
		statement(subject, predicate, object.toString(), object.getLang(), true, false);
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
	private void statement(AResource subject, AResource predicate,
							String object, String objectLang, boolean litObject, boolean anonObject){

		if (!predicate.isAnonymous()){ // we ignore statements with anonymous predicates
			
			try{
				parseForUsedNamespaces(predicate); // FIXME - extract used namespaces before this handler is called (for better performance)
				
				long subjectHash = spoHash(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI(), subject.isAnonymous());
				long predicateHash = spoHash(predicate.getURI(), false);
				if (litObject)
					object = UnicodeUtils.replaceEntityReferences(object);
				
				int i = storeTriple(subjectHash, subject.isAnonymous(), predicateHash, object, objectLang, litObject, anonObject);
				if (i>0){
					storeResource(predicate.getURI(), predicateHash);
					if (!subject.isAnonymous()){
						storeResource(subject.getURI(), subjectHash);
					}
				}
			}
			catch (Exception e){
				throw new LoadException(e.toString(), e);
			}
		}
	}

	/**
	 * 
	 * @param subject
	 * @param anonSubject
	 * @param predicate
	 * @param anonPredicate
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 * @throws SQLException 
	 */
	private int storeTriple(long subjectHash, boolean anonSubject, long predicateHash,
							String object, String objectLang, boolean litObject, boolean anonObject) throws SQLException{
		
		if (preparedStatementForTriples==null){
			prepareForTriples();
			logger.debug("Started storing triples from " + sourceUrl);
		}
		
		preparedStatementForTriples.setLong  ( 1, subjectHash);
		preparedStatementForTriples.setLong  ( 2, predicateHash);
		preparedStatementForTriples.setString( 3, object);
		preparedStatementForTriples.setLong  ( 4, spoHash(object, anonObject));
		preparedStatementForTriples.setString( 5, YesNoBoolean.format(anonSubject));
		preparedStatementForTriples.setString( 6, YesNoBoolean.format(anonObject));
		preparedStatementForTriples.setString( 7, YesNoBoolean.format(litObject));
		preparedStatementForTriples.setString( 8, objectLang==null ? "" : objectLang);
		
		return preparedStatementForTriples.executeUpdate();
	}

	/**
	 * 
	 * @param uri
	 * @param uriHash
	 * @param type
	 * @throws SQLException 
	 */
	private int storeResource(String uri, long uriHash) throws SQLException{

		if (preparedStatementForResources==null){
			prepareForResources();
			logger.debug("Started storing resources from " + sourceUrl);
		}
		
		preparedStatementForResources.setString(1, uri);
		preparedStatementForResources.setLong(2, uriHash);
		
		return preparedStatementForResources.executeUpdate();
	}
	
	/**
	 * 
	 * @param s
	 * @param isAnonymous
	 * @return
	 */
	private long spoHash(String s, boolean isAnonymous){
		return isAnonymous ? Hashes.spoHash(s, anonIdSeed) : Hashes.spoHash(s);
	}

	/**
	 * 
	 * @param predicate
	 */
	private void parseForUsedNamespaces(AResource predicate){
		
		if (!predicate.isAnonymous()){
			String predicateUri = predicate.getURI();
			int i = predicateUri.lastIndexOf("#");
            if (i<0)
            	i = predicateUri.lastIndexOf("/");
            if (i>0){
                if (predicateUri.charAt(i)=='/')
                	i++;
                usedNamespaces.add(predicateUri.substring(0, i));
            }
		}
	}

	/**
	 * Does "delete from SPO_TEMP" and then prepares this.preparedStatementForTriples.
	 * 
	 * @throws SQLException
	 */
	private void prepareForTriples() throws SQLException{
		
		// make sure SPO_TEMP is empty, let exception be thrown if this does not succeed
		// (because we do only one harvest at a time, so possible leftovers from previous harvest must be deleted)
		SQLUtil.executeUpdate("delete from SPO_TEMP", getConnection());
		
		// prepare this.preparedStatementForTriples
		StringBuffer buf = new StringBuffer();
        buf.append("insert into SPO_TEMP (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ").
        append("ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatementForTriples = getConnection().prepareStatement(buf.toString());
	}

	/**
	 * First does "delete from RESOURCE_TEMP" and then prepares this.preparedStatementForResources.
	 * 
	 * @throws SQLException 
	 */
	private void prepareForResources() throws SQLException{

		// make sure RESOURCE_TEMP is empty, let exception be thrown if this does not succeed
		// (because we do only one harvest at a time, so possible leftovers from previous harvest must be deleted)
		SQLUtil.executeUpdate("delete from RESOURCE_TEMP", getConnection());

		// prepare this.preparedStatementForResources
        preparedStatementForResources = getConnection().prepareStatement("insert ignore into RESOURCE_TEMP (URI, URI_HASH) VALUES (?, ?)");
	}

	/**
	 * @return the connection
	 * @throws SQLException 
	 */
	private Connection getConnection() throws SQLException {
		
		if (connection==null){
			connection = ConnectionUtil.getConnection();
		}
		return connection;
	}
	
	/**
	 * 
	 */
	protected void closeResources(){
		
		SQLUtil.close(preparedStatementForTriples);
		SQLUtil.close(preparedStatementForResources);
		SQLUtil.close(connection);
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	protected void commit() throws SQLException{
		
		int i = commitTriples();
		if (i>0){
			commitResources();
			storedTriplesCount = storedTriplesCount + i;
		}
		
		clearTemporaries();
		
		resolveLabels();
		extractNewHarvestSources();
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private int commitTriples() throws SQLException{

		/* copy triples from SPO_TEMP into SPO */

		logger.debug("Copying triples from SPO_TEMP into SPO, sourceUrl: " + sourceUrl);
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert high_priority into SPO (").
		append("SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, SOURCE, GEN_TIME").
		append(") select distinct SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append(sourceUrlHash).append(", ").append(genTime).append(" from SPO_TEMP");
		
		int committedTriplesCount = SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(committedTriplesCount + " triples inserted into SPO from " + sourceUrl);
		
		/* delete SPO records from previous harvests of this source */
		
		logger.debug("Deleting SPO rows of previous harvests of " + sourceUrl);
		
		buf = new StringBuffer("delete from SPO where SOURCE=");
		buf.append(sourceUrlHash).append(" and GEN_TIME<").append(genTime);
		
		SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		return committedTriplesCount;
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private int commitResources() throws SQLException{
		
		logger.debug("Copying resources from RESOURCE_TEMP into RESOURCE, sourceUrl: " + sourceUrl);
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert high_priority ignore into RESOURCE (URI, URI_HASH, FIRSTSEEN_SOURCE, FIRSTSEEN_TIME) ").
		append("select URI, URI_HASH, ").append(sourceUrlHash).append(", ").append(genTime).append(" from RESOURCE_TEMP");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());
		logger.debug(i + " resources inserted into RESOURCE from " + sourceUrl);
		
		return i;
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void resolveLabels() throws SQLException{
		
		logger.debug("Deriving labels for and from " + sourceUrl);
		
		/* Find and insert labels for freshly harvested non-literal objects. */
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_DERIV_SOURCE, OBJ_LANG, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SPO_FRESH.PREDICATE, SPO_LABEL.OBJECT, SPO_LABEL.OBJECT_HASH, SPO_FRESH.ANON_SUBJ, ").
		append("'N' as ANON_OBJ, 'Y' as LIT_OBJ, SPO_LABEL.SOURCE as OBJ_DERIV_SOURCE, SPO_LABEL.OBJ_LANG, SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SPO_LABEL ").
		append("where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.ANON_OBJ='N' and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.OBJECT_HASH=SPO_LABEL.SUBJECT and ").
		append("SPO_LABEL.LIT_OBJ='Y' and SPO_LABEL.ANON_OBJ='N' and ").
		append("SPO_LABEL.PREDICATE in (").
		append(LabelPredicates.getCommaSeparatedHashes()).
		append(")");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* Insert freshly harvested labels for all non-literal objects in SPO across all sources. */
		
		buf = new StringBuffer().
		append("insert ignore into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, ").
		append("OBJ_DERIV_SOURCE, OBJ_LANG, SOURCE, GEN_TIME) ").
		append("select distinct SPO_ALL.SUBJECT, SPO_ALL.PREDICATE, SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, SPO_ALL.ANON_SUBJ, ").
		append("'N' as ANON_OBJ, 'Y' as LIT_OBJ, SPO_FRESH.SOURCE as OBJ_DERIV_SOURCE, SPO_FRESH.OBJ_LANG, 	SPO_ALL.SOURCE, SPO_ALL.GEN_TIME ").
		append("from SPO as SPO_ALL, SPO as SPO_FRESH ").
		append("where SPO_ALL.LIT_OBJ='N' and SPO_ALL.OBJECT_HASH=SPO_FRESH.SUBJECT and ").
		append("SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.LIT_OBJ='Y' and SPO_FRESH.ANON_OBJ='N' and ").
		append("SPO_FRESH.PREDICATE in (").
		append(LabelPredicates.getCommaSeparatedHashes()).
		append(")");
		
		int j = SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " labels derived FOR and " + j + " labels derived FROM " + sourceUrl);
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void extractNewHarvestSources() throws SQLException{

		logger.debug("Extracting new harvest sources from SPO rows harvested from " + sourceUrl);
		
		/* handle qaw sources */
		
		String cronExpr = GeneralConfig.getProperty(GeneralConfig.HARVESTER_DEDICATED_SOURCES_CRON_EXPRESSION,
				HarvestSourceDTO.DEDICATED_HARVEST_SOURCE_DEFAULT_CRON);
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into HARVEST_SOURCE (NAME, URL, TYPE, DATE_CREATED, CREATOR, SCHEDULE_CRON) ").
		append("select OBJECT, OBJECT, '").append(DedicatedHarvestSourceTypes.qawSource).
		append("', now(), '").append(CRUser.application.getUserName()).
		append("', '").append(cronExpr).
		append("' from SPO where ANON_OBJ='N' and LIT_OBJ='N' and PREDICATE=").
		append(Hashes.spoHash(Predicates.DC_SOURCE)).
		append(" and SUBJECT in (select distinct SUBJECT from SPO where PREDICATE=").
		append(Hashes.spoHash(Predicates.RDF_TYPE)).append(" and ANON_OBJ='N' and LIT_OBJ='N' and OBJECT_HASH in (").
		append(Hashes.spoHash(Subjects.QA_REPORT_CLASS)).append(", ").append(Hashes.spoHash(Subjects.QAW_RESOURCE_CLASS)).append("))");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* handle delivered files */
		
		buf = new StringBuffer().
		append("insert ignore into HARVEST_SOURCE (NAME, URL, TYPE, DATE_CREATED, CREATOR, SCHEDULE_CRON) ").
		append("select URI, URI, '").append(DedicatedHarvestSourceTypes.deliveredFile).
		append("', now(), '").append(CRUser.application.getUserName()).
		append("', '").append(cronExpr).
		append("' from RESOURCE where URI_HASH in (select distinct SUBJECT from SPO where PREDICATE=").
		append(Hashes.spoHash(Predicates.RDF_TYPE)).append(" and ANON_OBJ='N' and LIT_OBJ='N' and OBJECT_HASH in (").
		append(Hashes.spoHash(Subjects.ROD_DELIVERY_CLASS)).append(", ").append(Hashes.spoHash(Subjects.DCTYPE_DATASET_CLASS)).append("))");
		
		i = i + SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " new harvest sources extracted and inserted from " + sourceUrl);
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void clearTemporaries() throws SQLException{
		
		logger.debug("Cleaning SPO_TEMP and RESOURCE_TEMP after harvesting " + sourceUrl);
		
		try{
			SQLUtil.executeUpdate("delete from SPO_TEMP", getConnection());
		}
		catch (Exception e){}
		try{
			SQLUtil.executeUpdate("delete from RESOURCE_TEMP", getConnection());
		}
		catch (Exception e){}
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	protected void rollback() throws SQLException{

		// delete rows of current harvest from SPO
		StringBuffer buf = new StringBuffer("delete from SPO where SOURCE=");
		buf.append(sourceUrlHash).append(" and GEN_TIME=").append(genTime);
		SQLUtil.executeUpdate(buf.toString(), getConnection());

		// delete rows of current harvest from RESOURCE
		buf = new StringBuffer("delete from RESOURCE where FIRSTSEEN_SOURCE=");
		buf.append(sourceUrlHash).append(" and FIRSTSEEN_TIME=").append(genTime);
		SQLUtil.executeUpdate(buf.toString(), getConnection());

		// delete all rows from SPO_TEMP and RESOURCE_TEMP
		clearTemporaries();
	}

    /*
     * (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
	public void error(SAXParseException e) throws SAXException {
		saxErrors.add(e);
		logger.error("SAX error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		throw new LoadException(e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		saxWarnings.add(e);
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/**
	 * @return the saxErrors
	 */
	public List<SAXParseException> getSaxErrors() {
		return saxErrors;
	}

	/**
	 * @return the saxWarnings
	 */
	public List<SAXParseException> getSaxWarnings() {
		return saxWarnings;
	}

	/**
	 * @return the storedTriplesCount
	 */
	public int getStoredTriplesCount() {
		return storedTriplesCount;
	}
}