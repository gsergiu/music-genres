package eu.europeana.dbpedia.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import eu.europeana.api.client.connection.HttpConnector;


public class DBPediaApiClient {

	private static final Log log = LogFactory.getLog(DBPediaApiClient.class);

	private String apiKey;
	public static final String DEFAULT_DBPEDIA_SEARCH_URI = "https://wdq.wmflabs.org/api?";

	private String wikidataApiUri = "";
	private HttpConnector http = new HttpConnector();

	public final static String SEARCH_RESULTS_FOLDER = "./src/test/resources/search/dbpedia-results";

	
	/**
	 * Create a new connection to the DBPedia API.
	 * 
	 * @param apiKey
	 *            API Key provided by DBPedia to access the API
	 */
	public DBPediaApiClient(String apiUri, String apiKey) {
		this.apiKey = apiKey;
//		if(apiKey != null)
			this.setApiUri(apiUri);
//		else 
//			setApiKey(getDefaultApiKey());
	}

	public DBPediaApiClient() {
		this(DEFAULT_DBPEDIA_SEARCH_URI, null);
		setApiKey(getDefaultApiKey());

	}


	String getJSONResult(String url) throws IOException {
		log.trace("Call to Europeana API: " + url);
		return http.getURLContent(url);

	}

	/**
	 * Returns the DBPedia API URI for JSON calls
	 * e.g. https://wdq.wmflabs.org/api?q=string[1709:%27http://dbpedia.org/ontology/Musical%27]
	 * @param searchString
	 * @return search URL
	 * @throws UnsupportedEncodingException 
	 */
//	public String getDBPediaSearchUrl(String searchString) throws UnsupportedEncodingException {
//		String searchUrl = getApiUri();
//		searchUrl += "q=" + URLEncoder.encode("string[1709:'" + searchString + "']", "UTF-8");
//		return searchUrl;
//	}

	
    /**
     * Sample query: "http://dbpedia.org/page/Chicago_blues"
     * @param label
     * @return DBPedia response
     */
    public String queryDBPedia(String label) {
        ParameterizedSparqlString qs = new ParameterizedSparqlString( "" +
                "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select ?resource where {\n" +
                "  ?resource rdfs:label ?label\n" +
                "}" );

        Literal labelLiteral = ResourceFactory.createLangLiteral( label, "en" );
        qs.setParam("label", labelLiteral);

        System.out.println( qs );

        QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());

        // Normally you'd just do results = exec.execSelect(), but I want to 
        // use this ResultSet twice, so I'm making a copy of it.  
        ResultSet results = ResultSetFactory.copyResults( exec.execSelect() );

        while ( results.hasNext() ) {
            // As RobV pointed out, don't use the `?` in the variable
            // name here. Use *just* the name of the variable.
            System.out.println( results.next().get( "resource" ));
        }

        // A simpler way of printing the results.
        ResultSetFormatter.out( results );
        return results.toString();
    }
	    
	    
	/**
	 * Modifies the DBPedia API URI for JSON calls. The default value points
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
		return DBPediaClientConfiguration.getInstance().getApiKey();
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
	 * This method stores DBPedia JSON data for related DBPedia ID (query) 
	 * in JSON files for given fileName.
	 * @param query
	 * @param folder
	 * @param fileName
	 * @throws IOException
	 */
	public void saveSearchResults(String query, String folder, String fileName)
			throws IOException {
		
//		if (fileName.startsWith("m.")) {
			File queryResultsFile = new File(folder, fileName + ".json");
			// create parent dirs
			queryResultsFile.getParentFile().mkdirs();
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(queryResultsFile));
//				String searchUrl = getDBPediaSearchUrl(query);
//				String searchResult = getJSONResult(searchUrl);
				String searchResult = queryDBPedia(query);
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
//		}
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
}
