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
package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.readers.HarvestSourceDTOReader;
import eionet.cr.dto.readers.SingleIntColumnReader;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * @author altnyris
 *
 */
public class MySQLHarvestSourceDAO extends MySQLBaseDAO implements HarvestSourceDAO {
	
	/**
	 * 
	 */
	public MySQLHarvestSourceDAO() {
	}
	
	/** */
	private static final String getSourcesSQL = "select * from HARVEST_SOURCE order by TYPE desc, URL";
		
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#getHarvestSources()
     */
    public List<HarvestSourceDTO> getHarvestSources() throws DAOException {
    	List<Object> values = new ArrayList<Object>();
				
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return list;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
    }

    /** */
    private static final String getSourcesByTypeSQL = "select * from HARVEST_SOURCE where TYPE=? order by TYPE desc, URL";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesByType()
     */
    public List<HarvestSourceDTO> getHarvestSourcesByType(String type) throws DAOException {
    	
    	List<Object> values = new ArrayList<Object>();
    	values.add(type);
				
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByTypeSQL, values, rsReader, conn);
			return rsReader.getResultList();
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}
    
    /** */
	private static final String getSourcesByIdSQL = "select * from HARVEST_SOURCE where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#getHarvestSourceById()
     */
    public HarvestSourceDTO getHarvestSourceById(Integer harvestSourceID) throws DAOException {
    	List<Object> values = new ArrayList<Object>();
    	values.add(harvestSourceID);
				
		Connection conn = null;
		HarvestSourceDTO source = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByIdSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return (list!=null && !list.isEmpty()) ? list.get(0) : null;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
    }

    /** */
	private static final String getSourcesByUrlSQL = "select * from HARVEST_SOURCE where URL=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourceByUrl(java.lang.String)
     */
	public HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {
		
    	List<Object> values = new ArrayList<Object>();
    	values.add(url);
				
		Connection conn = null;
		HarvestSourceDTO source = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getSourcesByUrlSQL, values, rsReader, conn);
			List<HarvestSourceDTO>  list = rsReader.getResultList();
			return (list!=null && !list.isEmpty()) ? list.get(0) : null;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}

    /** */
	private static final String addSourceSQL = "insert into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,TIME_CREATED,CREATOR,INTERVAL_MINUTES) VALUES (?,?,?,?,NOW(),?,?)";
	private static final String addSourceIgnoreSQL = "insert into HARVEST_SOURCE (NAME,URL,TYPE,EMAILS,TIME_CREATED,CREATOR,INTERVAL_MINUTES) VALUES (?,?,?,?,NOW(),?,?) on duplicate key update HARVEST_SOURCE_ID=LAST_INSERT_ID(HARVEST_SOURCE_ID)";

	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#addSource()
     */
    public Integer addSource(HarvestSourceDTO source, String user) throws DAOException {
    	return addSource(addSourceSQL, source, user);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIgnoreDuplicate(eionet.cr.dto.HarvestSourceDTO, java.lang.String)
     */
	public Integer addSourceIgnoreDuplicate(HarvestSourceDTO source, String user) throws DAOException {
		return addSource(addSourceIgnoreSQL, source, user);
	}

	/**
	 * 
	 * @param sql
	 * @param source
	 * @param user
	 * @return
	 * @throws DAOException 
	 */
	private Integer addSource(String sql, HarvestSourceDTO source, String user) throws DAOException{
		
		Integer harvestSourceID = null;
    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getName());
		values.add(source.getUrl());
		values.add(source.getType());
		values.add(source.getEmails());
		values.add(user);
		values.add(source.getIntervalMinutes());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(sql, values, conn);
			harvestSourceID = getLastInsertID(conn);
			return harvestSourceID;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

    /** */
	private static final String editSourceSQL = "update HARVEST_SOURCE set NAME=?,URL=?,TYPE=?,EMAILS=?,INTERVAL_MINUTES=? where HARVEST_SOURCE_ID=?";
	
	/*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDao#editSource()
     */
    public void editSource(HarvestSourceDTO source) throws DAOException {
    	    	
    	List<Object> values = new ArrayList<Object>();
		values.add(source.getName());
		values.add(source.getUrl());
		values.add(source.getType());
		values.add(source.getEmails());
		values.add(source.getIntervalMinutes());
		values.add(source.getSourceId());
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(editSourceSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourcesByUrl(java.util.List)
     */
    public void deleteSourcesByUrl(List<String> urls) throws DAOException {

    	/* prepare question marks and url hashes */
    	
    	String questionMark = "?";
    	List<Long> urlHashes = new ArrayList<Long>();
    	List<String> questionMarks = new ArrayList<String>();
    	for (String url:urls){
    		urlHashes.add(Hashes.spoHash(url));
    		questionMarks.add(questionMark);
    	}
    	String inClause = " in (" + Util.toCSV(questionMarks) + ")";
    	
    	/* execute deletion queries */
    	
		Connection conn = null;
		try{
			conn = getConnection();
			
			/* get harvest source ids, delete harvests and harvest messages by them */
			
			SingleIntColumnReader reader = new SingleIntColumnReader();
			SQLUtil.executeQuery("select HARVEST_SOURCE_ID from HARVEST_SOURCE where URL" + inClause, urls, reader, conn);
			String harvestSourceIdsCSV = Util.toCSV(reader.getResultList());
			
			reader = new SingleIntColumnReader();
			SQLUtil.executeQuery("select HARVEST_ID from HARVEST where HARVEST_SOURCE_ID in (" + harvestSourceIdsCSV + ")", reader, conn);
			String harvestIdsCSV = Util.toCSV(reader.getResultList());
			if (harvestIdsCSV.trim().length()>0){
				SQLUtil.executeUpdate("delete from HARVEST_MESSAGE where HARVEST_ID in (" + harvestIdsCSV + ")", conn);
			}
			SQLUtil.executeUpdate("delete from HARVEST where HARVEST_SOURCE_ID in (" + harvestSourceIdsCSV + ")", conn);
			
			/* delete various stuff by harvest source urls or url hashes */
			
			SQLUtil.executeUpdate("delete from HARVEST_SOURCE where URL" + inClause, urls, conn);
			SQLUtil.executeUpdate("delete from HARVEST_SOURCE where SOURCE" + inClause, urlHashes, conn);
			SQLUtil.executeUpdate("delete from SPO where SOURCE" + inClause, urlHashes, conn);
			SQLUtil.executeUpdate("delete from SPO where OBJ_DERIV_SOURCE" + inClause, urlHashes, conn);
			SQLUtil.executeUpdate("delete from UNFINISHED_HARVEST where SOURCE" + inClause, urlHashes, conn);
			SQLUtil.executeUpdate("delete from URGENT_HARVEST_QUEUE where URL" + inClause, urls, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
    }

    /** */
    private static final String updateHarvestFinishedSQL = "update HARVEST_SOURCE set STATEMENTS=?, RESOURCES=? where HARVEST_SOURCE_ID=?";
    private static final String updateHarvestFinishedSQL_avail = "update HARVEST_SOURCE set STATEMENTS=?, RESOURCES=?, COUNT_UNAVAIL=if(?,0,(COUNT_UNAVAIL+1)) where HARVEST_SOURCE_ID=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestSourceDAO#updateHarvestFinished(int, Integer, Integer)
     */
	public void updateHarvestFinished(int sourceId, Integer numStatements, Integer numResources, Boolean sourceAvailable) throws DAOException {
		
		List<Object> values = new ArrayList<Object>();
		values.add(numStatements);
		values.add(numResources);
		if (sourceAvailable!=null)
			values.add(sourceAvailable.booleanValue()==true ? new Integer(1) : new Integer(0));
		values.add(new Integer(sourceId));		
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(sourceAvailable!=null ? updateHarvestFinishedSQL_avail : updateHarvestFinishedSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/** */
	private static final String updateHarvestStartedSQL = "update HARVEST_SOURCE set LAST_HARVEST=NOW() where HARVEST_SOURCE_ID=?";
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#updateHarvestStarted(int)
	 */
	public void updateHarvestStarted(int sourceId) throws DAOException {

		List<Object> values = new ArrayList<Object>();
		values.add(new Integer(sourceId));		
		
		Connection conn = null;
		try{
			conn = getConnection();
			SQLUtil.executeUpdate(updateHarvestStartedSQL, values, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/** */
	private static final String getHarvestSourcesUnavailableSQL =
		"select * from HARVEST_SOURCE where COUNT_UNAVAIL > " + HarvestSourceDTO.COUNT_UNAVAIL_THRESHOLD;
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getHarvestSourcesUnavailable()
	 */
	public List<HarvestSourceDTO> getHarvestSourcesUnavailable() throws DAOException {
		
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			SQLUtil.executeQuery(getHarvestSourcesUnavailableSQL, new ArrayList<Object>(), rsReader, conn);
			return rsReader.getResultList();
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	private static final String getNextScheduledSourcesSQL =
		
		"select * from HARVEST_SOURCE where INTERVAL_MINUTES>0"
		+ " and timestampdiff(MINUTE,ifnull(LAST_HARVEST,timestampadd(MINUTE,-1*INTERVAL_MINUTES,TIME_CREATED)),NOW()) >= INTERVAL_MINUTES"
		+ " order by timestampdiff(MINUTE,ifnull(LAST_HARVEST,timestampadd(MINUTE,-1*INTERVAL_MINUTES,TIME_CREATED)),NOW())/INTERVAL_MINUTES desc"
		+ " limit ?";

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HarvestSourceDAO#getNextScheduledSources(int)
	 */
	public List<HarvestSourceDTO> getNextScheduledSources(int numOfSegments) throws DAOException {
		
		Connection conn = null;
		HarvestSourceDTOReader rsReader = new HarvestSourceDTOReader();
		try{
			conn = getConnection();
			
			Object o = SQLUtil.executeSingleReturnValueQuery("select count(*) from HARVEST_SOURCE", conn);
			int numOfSources = o==null ? 0 : Integer.parseInt(o.toString());
			int limit = Math.round((float)numOfSources/(float)numOfSegments);

			List<Object> values = new ArrayList<Object>();
	    	values.add(new Integer(limit));
			SQLUtil.executeQuery(getNextScheduledSourcesSQL, values, rsReader, conn);
			return rsReader.getResultList();
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}
}
