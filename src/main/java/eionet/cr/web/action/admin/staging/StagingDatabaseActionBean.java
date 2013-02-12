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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.web.action.admin.staging;

import java.io.File;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.AnnotatedClassActionResolver;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.dto.StagingDatabaseTableColumnDTO;
import eionet.cr.staging.FileDownloader;
import eionet.cr.staging.imp.ImportRunner;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

// TODO: Auto-generated Javadoc
/**
 * Action bean for operations with a single staging database.
 *
 * @author jaanus
 */
@UrlBinding("/admin/stagingDb.action")
public class StagingDatabaseActionBean extends AbstractActionBean {

    /** */
    private static final String VIEW_STAGING_DATABASE_JSP = "/pages/admin/staging/viewDatabase.jsp";

    /** */
    private static final String EDIT_STAGING_DATABASE_JSP = "/pages/admin/staging/editDatabase.jsp";

    /** */
    private static final String ADD_STAGING_DATABASE_JSP = "/pages/admin/staging/addDatabase.jsp";

    /** Static logger. */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabaseActionBean.class);

    /** */
    private StagingDatabaseDTO database;

    /** */
    private int dbId;
    private String fileName;
    private String dbName;
    private String dbDescription;
    private String defaultQuery;
    private File file;

    /** */
    private StagingDatabaseDTO dbDTO;

    /** */
    private List<StagingDatabaseTableColumnDTO> tablesColumns;

    /**
     * The bean's default event handler method.
     *
     * @return Resolution to go to.
     * @throws DAOException the dAO exception
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        StagingDatabaseDAO dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        if (dbId > 0) {
            dbDTO = dao.getDatabaseById(dbId);
        } else if (!StringUtils.isBlank(dbName)) {
            dbDTO = dao.getDatabaseByName(dbName);
        }

        if (dbDTO == null) {
            addWarningMessage("Found no database by the given id or name!");
        }
        else{
            tablesColumns = dao.getTablesColumns(dbDTO.getName());
        }

        return new ForwardResolution(VIEW_STAGING_DATABASE_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution edit() throws DAOException {


        StagingDatabaseDAO dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        if (dbId > 0) {
            dbDTO = dao.getDatabaseById(dbId);
        } else if (!StringUtils.isBlank(dbName)) {
            dbDTO = dao.getDatabaseByName(dbName);
        }

        if (dbDTO == null) {
            addWarningMessage("Found no database by the given id or name!");
        }
        else{
            dbName = dbDTO.getName();
            dbDescription = dbDTO.getDescription();
            defaultQuery = dbDTO.getDefaultQuery();
            tablesColumns = dao.getTablesColumns(dbDTO.getName());
        }

        return new ForwardResolution(EDIT_STAGING_DATABASE_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution save() throws DAOException {

        DAOFactory.get().getDao(StagingDatabaseDAO.class).updateDatabaseMetadata(dbId, dbDescription, defaultQuery);
        addSystemMessage("Metadata saved successfully!");
        return new RedirectResolution(this.getClass(), "edit").addParameter("dbId", dbId);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution saveAndClose() throws DAOException {

        DAOFactory.get().getDao(StagingDatabaseDAO.class).updateDatabaseMetadata(dbId, dbDescription, defaultQuery);
        addSystemMessage("Metadata saved successfully!");
        return new RedirectResolution(this.getClass(), "view").addParameter("dbId", dbId);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution cancelEdit() throws DAOException {

        return new RedirectResolution(this.getClass(), "view").addParameter("dbId", dbId);
    }

    /**
     * Adds the.
     *
     * @return the resolution
     * @throws DAOException the dAO exception
     */
    public Resolution add() throws DAOException {

        LOGGER.debug("file: " + file);

        if (getContext().getRequest().getMethod().equalsIgnoreCase("GET")) {
            return new ForwardResolution(ADD_STAGING_DATABASE_JSP);
        }

        LOGGER.debug("dbName: " + dbName);
        LOGGER.debug("dbDescription: " + dbDescription);

        // Create the database record.
        StagingDatabaseDTO dto = new StagingDatabaseDTO();
        dto.setName(dbName);
        dto.setDescription(dbDescription);
        int id = DAOFactory.get().getDao(StagingDatabaseDAO.class).createRecord(dto, getUserName());
        dto.setId(id);
        LOGGER.debug("New staging database record created, id = " + id);

        // Create the database in DBMS and populate it from the uploaded file.
        ImportRunner.start(dto, file);

        addSystemMessage("Database created and import started in the background!");
        return new RedirectResolution(StagingDatabasesActionBean.class);
    }

    /**
     * Back to db list.
     *
     * @return the resolution
     */
    public Resolution backToDbList() {
        return new RedirectResolution(StagingDatabasesActionBean.class);
    }

    /**
     * @return the database
     */
    public StagingDatabaseDTO getDatabase() {
        return database;
    }

    /**
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the dbDescription
     */
    public String getDbDescription() {
        return dbDescription;
    }

    /**
     * @param dbDescription the dbDescription to set
     */
    public void setDbDescription(String dbDescription) {
        this.dbDescription = dbDescription;
    }

    /**
     * Validate request on "add" event.
     *
     * @throws DAOException the dAO exception
     */
    @ValidationMethod(on = {"add"})
    public void validateAdd() throws DAOException {

        if (StringUtils.isBlank(fileName)) {
            addGlobalValidationError("No file name supplied!");
        } else {
            file = new File(FileDownloader.FILES_DIR, fileName);
            if (!file.exists()) {
                addGlobalValidationError("No such file found in available files: " + fileName);
            }
        }

        // Validations for POST method only.
        if (getContext().getRequest().getMethod().equalsIgnoreCase("POST")) {

            if (StringUtils.isBlank(dbName)) {
                addGlobalValidationError("Database name must not be blank!");
            } else if (DAOFactory.get().getDao(StagingDatabaseDAO.class).exists(dbName)) {
                addGlobalValidationError("A database with this name already exists: " + dbName);
            }
        }

        getContext().setSourcePageResolution(new ForwardResolution(ADD_STAGING_DATABASE_JSP));
    }

    /**
     * Validates the the user is authorised for any operations on this action bean. If user not authorised, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events, since no specific events
     * specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @return
     */
    public long getFileSize() {
        return file == null ? 0L : file.length();
    }

    /**
     *
     * @return
     */
    public String getSuggestedDbName() {
        return StringUtils.isBlank(fileName) ? "" : StringUtils.substringBefore(fileName, ".");
    }

    /**
     * @return the dbDTO
     */
    public StagingDatabaseDTO getDbDTO() {
        return dbDTO;
    }

    /**
     * @param dbId the dbId to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     *
     * @return
     */
    public String getStagingDatabasesActionBeanUrlBinding() {
        AnnotatedClassActionResolver resolver = new AnnotatedClassActionResolver();
        return resolver.getUrlBinding(StagingDatabasesActionBean.class);
    }

    /**
     *
     * @return
     */
    public Class getStagingDatabasesActionBeanClass() {
        return StagingDatabasesActionBean.class;
    }

    /**
     *
     * @return
     */
    public Class getRdfExportWizardActionBeanClass() {
        return RDFExportWizardActionBean.class;
    }

    /**
     *
     * @return
     */
    public Class getRdfExportsActionBeanClass() {
        return RDFExportsActionBean.class;
    }

    /**
     * @return the tablesColumns
     */
    public List<StagingDatabaseTableColumnDTO> getTablesColumns() {
        return tablesColumns;
    }

    /**
     * @return the defaultQuery
     */
    public String getDefaultQuery() {
        return defaultQuery;
    }

    /**
     * @param defaultQuery the defaultQuery to set
     */
    public void setDefaultQuery(String defaultQuery) {
        this.defaultQuery = defaultQuery;
    }
}
