package eionet.cr.web.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.config.GeneralConfig;
import eionet.cr.search.SearchException;
import eionet.cr.search.SimpleSearch;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.util.URIUtil;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/references.action")
public class ReferencesActionBean extends AbstractSearchActionBean{

	/** */
	private String to;
	private SearchExpression searchExpression;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {

		searchExpression = new SearchExpression(to);
		SimpleSearch simpleSearch = new SimpleSearch(searchExpression); // validation assures that to!=null
		simpleSearch.setPageNumber(getPageN());
		simpleSearch.setSorting(getSortP(), getSortO());
		
		simpleSearch.execute();
		resultList = simpleSearch.getResultList();
		matchCount = simpleSearch.getTotalMatchCount();
		
		return new ForwardResolution("/pages/references.jsp");
	}

	/**
	 * 
	 * @param errors
	 */
    @ValidationMethod(on="search")
    public void validateSearch(ValidationErrors errors) {
    	
        if (StringUtils.isBlank(to)) {
            handleCrException("No search criteria specified!", GeneralConfig.SEVERITY_CAUTION);
        }
    }

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		return getDefaultColumns();
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the searchExpression
	 */
	public SearchExpression getSearchExpression() {
		return searchExpression;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}
}
