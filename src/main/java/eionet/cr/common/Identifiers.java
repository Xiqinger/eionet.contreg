package eionet.cr.common;

/**
 * 
 * @author heinljab
 *
 */
public interface Identifiers {

	/** */
	public static final String DOC_ID = "DOC_ID";
	public static final String SOURCE_ID = "SOURCE_ID";
	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String ANON_ID_PREFIX = "http://cr.eionet.europa.eu/anonymous/";
	public static final String IS_ENCODING_SCHEME = "IS_ENCODING_SCHEME";
	public static final String ALL_LITERAL_CONTENT = "CONTENT";
	public static final String FIRST_SEEN_TIMESTAMP = "urn:contreg:first-seen-timestamp";	
	
	public static final String DC_TITLE = "http://purl.org/dc/elements/1.1/title";
	public static final String DC_DATE = "http://purl.org/dc/elements/1.1/date";
	public static final String DC_COVERAGE = "http://purl.org/dc/elements/1.1/coverage";
	public static final String DC_IDENTIFIER = "http://purl.org/dc/elements/1.1/identifier";
	public static final String DC_SOURCE = "http://purl.org/dc/elements/1.1/source";
	public static final String DC_SUBJECT = "http://purl.org/dc/elements/1.1/subject";
	
	public static final String RSS_ITEM_CLASS = "http://purl.org/rss/1.0/Item";
	
	public static final String ROD_OBLIGATION_CLASS = "http://rod.eionet.eu.int/schema.rdf#Obligation";
	public static final String ROD_OBLIGATION_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#obligation";
	
	public static final String ROD_INSTRUMENT_CLASS = "http://rod.eionet.eu.int/schema.rdf#Instrument";
	public static final String ROD_INSTRUMENT_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#instrument";
	
	public static final String ROD_LOCALITY_CLASS = "http://rod.eionet.eu.int/schema.rdf#Locality";
	public static final String ROD_LOCALITY_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#locality";
	
	public static final String ROD_ISSUE_CLASS = "http://rod.eionet.eu.int/schema.rdf#Issue";
	public static final String ROD_ISSUE_PROPERTY = "http://rod.eionet.eu.int/schema.rdf#issue";
	
	public static final String ROD_DELIVERY_CLASS = "http://rod.eionet.eu.int/schema.rdf#Delivery";
	
	public static final String RDFS_SUB_PROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
	
	public static final String FULL_REPORT_CLASS = "http://reports.eea.europa.eu/reports_rdf#FullReport";
}
