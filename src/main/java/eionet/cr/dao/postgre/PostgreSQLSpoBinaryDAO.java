/*
* The contents of this file are subject to the Mozilla Public
*
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
*
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.postgre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dto.SpoBinaryDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLSpoBinaryDAO extends PostgreSQLBaseDAO implements SpoBinaryDAO {

    /** */
    private static final String sqlAdd = "insert into SPO_BINARY "
            + "(SUBJECT,OBJ_LANG,DATATYPE,MUST_EMBED) values (?,?,?,?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#add(eionet.cr.dto.SpoBinaryDTO, long)
     */
    public void add(SpoBinaryDTO dto, long contentSize) throws DAOException {

        if (dto == null) {
            throw new IllegalArgumentException("DTO object must not be null");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getSQLConnection();
            stmt = conn.prepareStatement(sqlAdd);

            stmt.setLong(1, dto.getSubjectHash());

            String lang = dto.getLanguage();
            stmt.setString(2, lang == null ? "" : lang);

            String contentType = dto.getContentType();
            stmt.setString(3, contentType == null ? "" : contentType);

            stmt.setBoolean(4, dto.isMustEmbed());

            stmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new DAOException(sqle.getMessage(), sqle);
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String sqlGet = "select * from SPO_BINARY where SUBJECT=?";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#get(java.lang.String)
     */
    public SpoBinaryDTO get(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            throw new IllegalArgumentException("Subject uri must not be blank");
        }

        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getSQLConnection();
            stmt = conn.prepareStatement(sqlGet);
            stmt.setLong(1, Hashes.spoHash(subjectUri));
            rs = stmt.executeQuery();
            if (rs != null && rs.next()) {

                SpoBinaryDTO dto = new SpoBinaryDTO(
                        rs.getLong("SUBJECT"));
                dto.setContentType(rs.getString("DATATYPE"));
                dto.setLanguage(rs.getString("OBJ_LANG"));
                dto.setMustEmbed(rs.getBoolean("MUST_EMBED"));

                return dto;
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return null;
    }

    /** */
    private static final String sqlExists = "select count(*) from SPO_BINARY where SUBJECT=?";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoBinaryDAO#exists(java.lang.String)
     */
    public boolean exists(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri)) {
            throw new IllegalArgumentException("Subject uri must not be blank");
        }

        Connection conn = null;
        try {
            conn = getSQLConnection();
            Object o = SQLUtil.executeSingleReturnValueQuery(sqlExists,
                    Collections.singletonList(Long.valueOf(Hashes.spoHash(subjectUri))), conn);
            return o != null && Integer.parseInt(o.toString()) > 0;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }
}
