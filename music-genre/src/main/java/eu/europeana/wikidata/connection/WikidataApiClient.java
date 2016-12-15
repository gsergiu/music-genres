package eu.europeana.wikidata.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

import eu.europeana.api.client.connection.HttpConnector;



public class WikidataApiClient {

	private static final Log log = LogFactory.getLog(WikidataApiClient.class);

	private String apiKey;
	public static final String DEFAULT_WIKIDATA_SEARCH_URI = "https://wdq.wmflabs.org/api?";
	
	public final String BASE_URI = "www.wikidata.org/wiki/Q";
	public final String HTTP_BASE_URI = "http://www.wikidata.org/wiki/Q";

	private String wikidataApiUri = "";
	private HttpConnector http = new HttpConnector();

	public final static String SEARCH_RESULTS_FOLDER = "./src/test/resources/search/results";
	
	public final String MUSIC_BRAINZ_PROP = "434";
	public final String INSTRUMENT_PROP = "1303";

	public final String SPARQL_ENDPOINT = "https://query.wikidata.org/";
	
	
	/**
	 * Create a new connection to the Wikidata API.
	 * 
	 * @param apiKey
	 *            API Key provided by Wikidata to access the API
	 */
	public WikidataApiClient(String apiUri, String apiKey) {
		this.apiKey = apiKey;
//		if(apiKey != null)
			this.setApiUri(apiUri);
//		else 
//			setApiKey(getDefaultApiKey());
	}

	public WikidataApiClient() {
		this(DEFAULT_WIKIDATA_SEARCH_URI, null);
		setApiKey(getDefaultApiKey());

	}


	public String getJSONResult(String url) throws IOException {
		log.trace("Call to Europeana API: " + url);
		return http.getURLContent(url);

	}

	
	/**
	 * Returns the Wikidata API URI for JSON calls
	 * e.g. https://wdq.wmflabs.org/api?q=string[646:%27/m/0557q%27]
	 * @param searchString
	 * @return search URL
	 * @throws UnsupportedEncodingException 
	 */
	public String getWikidataSearchUrl(String searchString) throws UnsupportedEncodingException {
		String searchUrl = getApiUri();
//		searchUrl += "q=string[646:%27" + URLEncoder.encode(searchString, "UTF-8") + "%27]";
//		searchUrl += URLEncoder.encode("q=string[646:'" + searchString + "']", "UTF-8");
		searchUrl += "q=" + URLEncoder.encode("string[646:'" + searchString + "']", "UTF-8");
		return searchUrl;
	}

	
	/**
	 * Returns the MusicBrainz ID by Wikidata ID
	 * e.g. https://www.wikidata.org/wiki/Q435330 for 'Kristin Hersh' in browser that has
	 * MusicBrainz artist ID 0b461f11-c7af-4ddb-a30e-5cb0aabb3e7f
	 *      https://wdq.wmflabs.org/api?q=items[435330]&props=434 in API
	 * @param searchWikidataId
	 * @return MusicBrainz ID
	 * @throws UnsupportedEncodingException 
	 */
	public String getMusicBrainzIdFromWikidataById(String searchWikidataId) throws UnsupportedEncodingException {
		String searchUrl = getApiUri();
		searchUrl += "q=" + URLEncoder.encode("items['" + searchWikidataId + "']", "UTF-8") + "&props=" + MUSIC_BRAINZ_PROP;
		String searchResult = "";
		try {
			searchResult = getJSONResult(searchUrl);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}

	
	/**
	 * Returns the instrument ID by Wikidata ID
	 * e.g. https://www.wikidata.org/wiki/Q435330 for 'Kristin Hersh' in browser that has
	 * instrument ID 6607
	 *      https://wdq.wmflabs.org/api?q=items[435330]&props=1303 in API
	 * 
	 * !!! This method suffers an Gateway timeout from Wikidata side.
	 * 
	 * @param searchWikidataId
	 * @return MusicBrainz ID
	 * @throws UnsupportedEncodingException 
	 */
	public String getInstrumentIdFromWikidataById(String searchWikidataId) throws UnsupportedEncodingException {
		String searchUrl = getApiUri();
		searchUrl += "q=" + URLEncoder.encode("items['" + searchWikidataId + "']", "UTF-8") + "&props=" + INSTRUMENT_PROP;
		String searchResult = "";
		try {
			searchResult = getJSONResult(searchUrl);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}

	
	/**
	 * Returns the Wikidata API URI for passed property number and query string
	 * e.g. https://wdq.wmflabs.org/api?q=string[724:%27PhilLeshandFriends%27] for Internet Archive ID property
	 * @param propertyNumber The Wikidata property number
	 * @param searchString The Internet Archive ID
	 * @return Wikidata URL
	 * @throws UnsupportedEncodingException 
	 */
	public String getWikidataSearchUrlByProp(String propertyNumber, String searchString) throws UnsupportedEncodingException {
		String searchUrl = getApiUri();
		searchUrl += "q=" + URLEncoder.encode("string[" + propertyNumber + ":'" + searchString + "']", "UTF-8");
		return searchUrl;
	}

	
	/**
	 * Modifies the Wikidata API URI for JSON calls. The default value points
	 * to the "https://wdq.wmflabs.org/api?"
	 * 
	 * @param apiUri
	 */
	public void setApiUri(String apiUri) {
		this.wikidataApiUri = apiUri;
	}

	
	public String getApiUri() {
		return wikidataApiUri;
	}


	/**
	 * @return the Europeana apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @return the Europeana apiKey
	 */
	public String getDefaultApiKey() {
		return WikidataClientConfiguration.getInstance().getApiKey();
	}
	
	/**
	 * @param apiKey
	 *            the Europeana apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	/**
	 * To indent JSON string, bind it as Object and write it output with indentation.
	 * @param input The input JSON string
	 * @return indented JSON string
	 */
	public String convertJsonStringToPrettyPrintJsonOutput(String input) {

		String res = input;
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(input, Object.class);
			res = mapper.defaultPrettyPrintingWriter().writeValueAsString(json);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	/**
	 * This method stores Wikidata JSON data for related Freebase ID (query) 
	 * in JSON files for given fileName.
	 * @param query
	 * @param folder
	 * @param fileName
	 * @throws IOException
	 */
	public void saveSearchResults(String query, String folder, String fileName)
			throws IOException {
		
		if (fileName.startsWith("m.")) {
			File queryResultsFile = new File(folder, fileName + ".json");
			// create parent dirs
			queryResultsFile.getParentFile().mkdirs();
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(queryResultsFile));
				String searchUrl = getWikidataSearchUrl(query);
				String searchResult = getJSONResult(searchUrl);
				String indentedSearchResult = convertJsonStringToPrettyPrintJsonOutput(searchResult);
				writer.write(indentedSearchResult);
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					log.warn("cannot close results writer for file: "
							+ queryResultsFile);
				}
			}
		}
	}


	public String queryWikidata(String query)
			throws IOException {
		
		String searchUrl = getWikidataSearchUrl(query);
		String searchResult = getJSONResult(searchUrl);
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}
	
	
	/**
	 * This method queries Wikidata by property number and query string.
	 * @param propertyNumber
	 * @param query
	 * @return
	 * @throws IOException
	 */
	public String queryWikidataByProperty(String propertyNumber, String query)
			throws IOException {
		
		String searchUrl = getWikidataSearchUrlByProp(propertyNumber, query);
		String searchResult = getJSONResult(searchUrl);
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}
	
	
	public String queryWikidataIdFromDump(String query, String dumpFile)
			throws IOException {
		
		String res = "";
		
		if (query.startsWith("/"))
			query = query.substring(1);
		query = query.replace("/", ".");
		
		int FREEBASE_POS = 0;
		int WIKIDATA_POS = 2;
    	String splitBy = "\t";	    
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(dumpFile));
			String line = br.readLine();			
			while ((line = br.readLine()) !=null) {
			    String[] b = line.split(splitBy);
			    if (b[FREEBASE_POS].contains(query)) {
			    	String[] wikidata_path = b[WIKIDATA_POS].split("/");
			    	res = wikidata_path[wikidata_path.length - 1].substring(1).replace("> .", ""); // to remove starting 'Q'
			    	break;
			    }
			}
		    br.close();
		} catch (FileNotFoundException e1) {
			log.error("File not found. " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			log.error("IO error. " + e.getMessage());
			e.printStackTrace();
		}
		
		return res;
	}
	
	
    public String getHtmlContent(String query) throws IOException {

    	HttpConnector httpConnection = new HttpConnector();
    	String url = "https://www.wikidata.org/wiki/Q" + query;
    	return httpConnection.getURLContent(url);
    }
	

    public String getEntity(String id) {

    	String res = "";
    	try {
	    	if(id != null && id.length() > 0) {
	    		res = getHtmlContent(id);
	    	}
		} catch (IOException e) {
			log.error("Error during Wikidata search by ID: " + id + ". " + e.getMessage());
		}
		return res;
	    	
	}
    

	public String getSearchResultFromFile(String query) throws IOException {
		String localFolder = SEARCH_RESULTS_FOLDER;
		return readJsonResultFromFile(query, localFolder);

	}

	private String readJsonResultFromFile(String filename, String localFolder)
			throws FileNotFoundException, IOException {
		File queryResultsFile = new File(localFolder, filename + ".json");
		return readJsonFile(queryResultsFile);
	}


	private void savetoJsonFile(File queryResultsFile, String url)
			throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryResultsFile), "UTF-8"));
					//new FileWriter(queryResultsFile));
			writer.write(getJSONResult(url));
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for file: "
						+ queryResultsFile);
			}
		}
	}

	private String readJsonFile(File queryResultsFile)
			throws FileNotFoundException, IOException {
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(queryResultsFile), "UTF-8"));
			// new FileReader(queryResultsFile));
			while ((line = reader.readLine()) != null)
				builder.append(line);

		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for file: "
						+ queryResultsFile);
			}
		}

		return builder.toString();
	}


	private File getSearchResultsFile(String filename) {
		File queryResultsFile = new File(SEARCH_RESULTS_FOLDER, filename
				+ ".json");
		return queryResultsFile;
	}
	
	
    /**
     * Sample query: "https://www.wikidata.org/wiki/Q6607" for guitar
     * @param id
     * @return DBPedia response
     */
    public String getWikidataLabelById(String id) {
    	
    	String res = "";
    	
        ParameterizedSparqlString qs = new ParameterizedSparqlString( "" +
                "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix wd: <http://www.wikidata.org/entity/>\n" +
                "\n" +
                "select *\n" +
                "where {\n" +
				"         wd:Q" + id + " rdfs:label ?label .\n" +
                "	      FILTER (LANG(?label) = 'en') .\n" + 
                "      }\n" + 
                "LIMIT 1" 
                );

//        Literal labelLiteral = ResourceFactory.createLangLiteral( label, "en" );
//        qs.setParam("label", labelLiteral);

        log.debug( qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, qs.asQuery());

        ResultSet resultSet = exec.execSelect();
        
        // Normally you'd just do results = exec.execSelect(), but I want to 
        // use this ResultSet twice, so I'm making a copy of it.  
        ResultSet results = ResultSetFactory.copyResults( resultSet );

        while ( results.hasNext() ) {
            // As RobV pointed out, don't use the `?` in the variable
            // name here. Use *just* the name of the variable.
//            System.out.println( results.next().get( "resource" ));
        	QuerySolution resQs = results.next();
            res = res + resQs.get( "resource" ) + "#";
            res = res + resQs.get( "description" );
//            System.out.println( resQs.get( "resource" ));
//            System.out.println( resQs.get( "description" ));
        }

        // A simpler way of printing the results.
        ResultSetFormatter.out( results );
//        return results.toString();
        if (res.equals(""))
        	res = "#";
        return res;
    }
	    
	    
	
}
