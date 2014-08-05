package eu.europeana.freebase.connection;

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

import eu.europeana.api.client.connection.EuropeanaConnection;
import eu.europeana.api.client.connection.HttpConnector;

public class FreebaseApiClient {

	private static final Log log = LogFactory.getLog(EuropeanaConnection.class);

	// private static final int MAX_RESULTS_PAGE = 100;

	private String apiKey;
	public static final String DEFAULT_FREEBASE_SEARCH_URI = "https://www.googleapis.com/freebase/v1/search?query=";
	public static final String DEFAULT_FREEBASE_MQLREAD_URI = "https://www.googleapis.com/freebase/v1/mqlread?query=";
	public static final String DEFAULT_FREEBASE_TOPIC_URI = "https://www.googleapis.com/freebase/v1/topic";

	String NAME_FILTER = "?filter=/type/object/name&lang=en";
	String DESCRIPTION_FILTER = "?filter=/common/topic/description&lang=en";

	//public static String DEFAULT_API_KEY = "";
	private String freebaseApiUri = "";
	private HttpConnector http = new HttpConnector();

	public final static String SEARCH_RESULTS_FOLDER = "./src/test/resources/search/results";
	public final static String SEARCH_DESCRIPTIONS_FOLDER = "./src/test/resources/search/descriptions";
	public final static String SEARCH_NAMES_FOLDER = "./src/test/resources/search/names";
	public final static String SEARCH_ALIASES_FOLDER = "./src/test/resources/search/aliases";
	public final static String SEARCH_I18N_FOLDER = "./src/test/resources/search/i18n";
	public final static String SEARCH_PARENT_SUBGENRES_FOLDER = "./src/test/resources/search/parentandsubgerens";
	public final static String SEARCH_WIKIPEDIATITLE_FOLDER = "./src/test/resources/search/wikipediatitle";

	/**
	 * Create a new connection to the Freebase API.
	 * 
	 * @param apiKey
	 *            API Key provided by Freebase to access the API
	 */
	public FreebaseApiClient(String apiUri, String apiKey) {
		this.apiKey = apiKey;
		if(apiKey != null)
			this.setApiUri(apiUri);
		else 
			setApiKey(getDefaultApiKey());
	}

	public FreebaseApiClient() {
		this(DEFAULT_FREEBASE_SEARCH_URI, null);
		setApiKey(getDefaultApiKey());

	}

	//
	// public EuropeanaApi2Results search(EuropeanaQueryInterface search, long
	// offset) throws IOException {
	// String json = this.searchJsonPage(search, 12, offset);
	//
	// // Load results object from JSON
	// Gson gson = new Gson();
	// EuropeanaApi2Results res = gson.fromJson(json,
	// EuropeanaApi2Results.class);
	//
	// return res;
	// }

	// /**
	// * Execute a query to Europeana and return one results page as JSON
	// *
	// * @param search
	// * @param limit
	// * @param offset
	// * @return The results as a JSON string
	// * @throws IOException
	// */
	// public String searchJsonPage(EuropeanaQueryInterface search, long limit,
	// long offset) throws IOException {
	// String url = search.getQueryUrl(this, limit, offset);
	// return this.getJSONResult(url);
	// }

	String getJSONResult(String url) throws IOException {
		log.trace("Call to Europeana API: " + url);
		return http.getURLContent(url);

	}

	/**
	 * Returns the Freebase API URI for JSON calls
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String getFreebaseSearchUrl(String searchString) throws UnsupportedEncodingException {
		return getApiUri() + URLEncoder.encode(searchString, "UTF-8") + "&key="
				+ getApiKey();
	}

	/**
	 * Modifies the Europeana API URI for JSON calls. The default value points
	 * to the "http://api.europeana.eu/api/opensearch.json"
	 * 
	 * @param apiUri
	 */
	public void setApiUri(String apiUri) {
		this.freebaseApiUri = apiUri;
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
		return FreebaseClientConfiguration.getInstance().getApiKey();
	}
	
	/**
	 * @param apiKey
	 *            the Europeana apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiUri() {
		return freebaseApiUri;
	}

	public void saveSearchResults(String query, String folder)
			throws IOException {
		File queryResultsFile = new File(folder, query + ".json");
		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(queryResultsFile));
			writer.write(getJSONResult(getFreebaseSearchUrl(query)));
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for file: "
						+ queryResultsFile);
			}
		}
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

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonDescription(String label, String freebaseId)
			throws IOException {

		File queryResultsFile = getDescriptionFile(label);

		saveDescription(freebaseId, queryResultsFile);

		return readJsonFile(queryResultsFile);
	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonDescription(String freebaseId) throws IOException {

		File queryResultsFile = getDescriptionFile(freebaseId);

		saveDescription(freebaseId, queryResultsFile);

		return readJsonFile(queryResultsFile);
	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonLabel(String freebaseId) throws IOException {

		File queryResultsFile = getLabelFile(freebaseId);

		saveLabel(freebaseId, queryResultsFile);

		return readJsonFile(queryResultsFile);
	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonAliases(String freebaseId) throws IOException {

		File queryResultsFile = getAliasesFile(freebaseId);

		saveAliases(freebaseId, queryResultsFile);

		return readJsonFile(queryResultsFile);
	}

	private void saveAliases(String freebaseId, File queryResultsFile)
			throws IOException {
		// do not read the file multiple times
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();

		String url = DEFAULT_FREEBASE_MQLREAD_URI;
		String query = "{\"mid\":\"" + freebaseId + "\","
				+ "\"/common/topic/alias\": " +
				"[{\"value\":null, \"lang\":\"/lang/en\"}]" 
				+ "}";

		url += URLEncoder.encode(query, "UTF-8");
		url += "&key=";
		url += getApiKey();

		savetoJsonFile(queryResultsFile, url);

	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonI18NLabels(String freebaseId) throws IOException {

		File queryResultsFile = getI18NFile(freebaseId);

		saveI18NLabels(freebaseId, queryResultsFile);

		return readJsonFile(queryResultsFile);
	}

	private void saveI18NLabels(String freebaseId, File queryResultsFile) throws IOException {
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();

		String url = DEFAULT_FREEBASE_MQLREAD_URI;
		String query = "{\"mid\":\"" + freebaseId + "\","
				+ "\"name\": [{}]" + "}";

		url += URLEncoder.encode(query, "UTF-8");
		url += "&key=";
		url += getApiKey();

		savetoJsonFile(queryResultsFile, url);

	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getJsonParentGenreAndSubgenres(String label, String freebaseId)
			throws IOException {

		saveParentGenreAndSubgenres(label, freebaseId);

		File queryResultsFile = getParentAndSubgenresFile(label);

		// create parent dirs
		return readJsonFile(queryResultsFile);

	}

	/**
	 * 
	 * @param freebaseId
	 *            - mid attribute in freebase
	 * @return
	 * @throws IOException
	 */
	public String getWikipediaTitle(String freebaseId) throws IOException {

		saveWikipediaTitle(freebaseId);

		File queryResultsFile = getWikipediaTitleFile(freebaseId);

		// create parent dirs
		return readJsonFile(queryResultsFile);

	}

	public void saveWikipediaTitle(String freebaseId) throws IOException {
		File queryResultsFile = getWikipediaTitleFile(freebaseId);
		// do not read the file multiple times
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
		String url = DEFAULT_FREEBASE_MQLREAD_URI;
		String query = "[{\"mid\":\"" + freebaseId + "\"," + "\"id\":null,"
				+ "\"name\":null, "
				+ "\"key\": [{\"namespace\": \"/wikipedia/en_title\","
				+ "\"value\": null}] " + "}]";
		url += URLEncoder.encode(query, "UTF-8");
		url += "&key=";
		url += getApiKey();

		savetoJsonFile(queryResultsFile, url);
	}

	private File getParentAndSubgenresFile(String label) {
		return new File(SEARCH_PARENT_SUBGENRES_FOLDER, label + ".json");
	}

	public void saveParentGenreAndSubgenres(String label, String freebaseId)
			throws IOException {
		File queryResultsFile = getParentAndSubgenresFile(label);
		// do not read the file multiple times
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
		String url = DEFAULT_FREEBASE_MQLREAD_URI;
		String query = "[{\"mid\":\"" + freebaseId + "\"," + "\"id\":null,"
				+ "\"name\":null, " + "\"type\":\"/music/genre\", "
				+ "\"subgenre\":[{}]," + "\"parent_genre\":[{}]" + "}]";
		url += URLEncoder.encode(query, "UTF-8");
		url += "&key=";
		url += getApiKey();

		savetoJsonFile(queryResultsFile, url);
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

	private void saveDescription(String freebaseId, File queryResultsFile)
			throws IOException {
		// do not read the file multiple times
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
		String url = DEFAULT_FREEBASE_TOPIC_URI + freebaseId
				+ DESCRIPTION_FILTER + "&key=" + getApiKey();

		savetoJsonFile(queryResultsFile, url);
	}

	private void saveLabel(String freebaseId, File queryResultsFile)
			throws IOException {
		// do not read the file multiple times
		if (queryResultsFile.exists())
			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
		String url = DEFAULT_FREEBASE_TOPIC_URI + freebaseId + NAME_FILTER
				+ "&key=" + getApiKey();

		savetoJsonFile(queryResultsFile, url);
	}

	private File getDescriptionFile(String fileName) {
		return new File(SEARCH_DESCRIPTIONS_FOLDER, fileName + ".json");
	}

	private File getLabelFile(String fileName) {
		return new File(SEARCH_NAMES_FOLDER, fileName + ".json");
	}

	private File getWikipediaTitleFile(String mid) {
		return new File(SEARCH_WIKIPEDIATITLE_FOLDER, mid + ".json");
	}

	private File getAliasesFile(String mid) {
		return new File(SEARCH_ALIASES_FOLDER, mid + ".json");
	}

	private File getI18NFile(String mid) {
		return new File(SEARCH_I18N_FOLDER, mid + ".json");
	}

	private File getSearchResultsFile(String filename) {
		File queryResultsFile = new File(SEARCH_RESULTS_FOLDER, filename
				+ ".json");
		return queryResultsFile;
	}
}
