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
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import eu.europeana.api.client.connection.EuropeanaConnection;
import eu.europeana.api.client.connection.HttpConnector;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.search.query.EuropeanaQueryInterface;
import eu.europeana.sounds.definitions.model.concept.Instrument;
import eu.europeana.sounds.definitions.model.concept.impl.BaseInstrument;

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
	public final static String ONB_INSTRUMENTS_FOLDER = "./src/test/resources/MIMO/onb";
	public final static String ONB_INSTRUMENTS_JSON_FOLDER = "./src/test/resources/MIMO/onb/json";
	
	public final static String ONB_INSTRUMENTS_FILE = "Instruments of the ONB_list.csv";
	public final static String INSTRUMENTS_FILE_EXTENDED = "Enriched instruments of the ONB list.csv";
	
	public final static String CSV_DELIMITER = ";";
	 

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


	/*****************************
	 * MUSIC INSTRUMENTS MAPPING *
	 ****************************/
	
	/**
	 * This method is used to access ONB music instruments CSV file.
	 * @param filename
	 * @return
	 */
	private File getONBInstrumentsFile(String filename) {
		File queryResultsFile = new File(ONB_INSTRUMENTS_FOLDER, filename);
		return queryResultsFile;
	}


	/**
	 * This method prepares file for storing music instrument JSON data from Freebase.
	 * @param filename
	 * @return
	 */
	private File getInstrumentJsonFile(String filename) {
		File queryResultsFile = new File(ONB_INSTRUMENTS_JSON_FOLDER, filename + ".json");
		return queryResultsFile;
	}


	public EuropeanaApi2Results search(EuropeanaQueryInterface search, long offset) 
			throws IOException {
		
	    String json = this.searchJsonPage(search, 12, offset);
	
	    // Load results object from JSON
	    Gson gson = new Gson();
	    EuropeanaApi2Results res = gson.fromJson(json,
	    EuropeanaApi2Results.class);
	
	    return res;
	}
	

	private Instrument saveInstrumentToJsonFile(File queryResultsFile, String url)
			throws IOException {
		
		Instrument instrument = new BaseInstrument();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryResultsFile), "UTF-8"));
			String jsonResult = getJSONResult(url);
			writer.write(jsonResult);

			// Load results object from JSON
			if (!jsonResult.contains("error") && !jsonResult.contains("Not Found") && !jsonResult.contains("\"result\": []")) {
			    Gson gson = new Gson();
	//		    instrument = gson.fromJson(jsonResult, BaseInstrument.class);
				JsonElement element = gson.fromJson (jsonResult, JsonElement.class);
				JsonObject jsonObj = element.getAsJsonObject();		
				JsonArray resultJsonArray = jsonObj.get("result").getAsJsonArray();
				JsonObject resultJsonObject = resultJsonArray.get(0).getAsJsonObject();
				String mid = resultJsonObject.get("mid").getAsString();
				instrument.setMid(mid);
				Type listType = new TypeToken<List<String>>() {}.getType();
				List<String> familyList = new Gson().fromJson(resultJsonObject.get("/music/instrument/family"), listType);
				instrument.setInstrumentFamily(familyList);
				String name = resultJsonObject.get("key").getAsJsonArray().get(0)
						.getAsJsonObject().get("value").getAsString();
				instrument.setName(name);
			}
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for file: "
						+ queryResultsFile);
			}
		}
		
		return instrument;
	}

	
	/**
	 * Execute a query to Europeana and return one results page as JSON
	 *
	 * @param search
	 * @param limit
	 * @param offset
	 * @return The results as a JSON string
	 * @throws IOException
	 */
	public String searchJsonPage(EuropeanaQueryInterface search, long limit, long offset) 
			throws IOException {
		
	    String url = "";//search.getQueryUrl(this, limit, offset);
	    return this.getJSONResult(url);
	}
	
	
    public List<Instrument> readFromCsvFile(File csvFile) throws Exception {
    	
    	List<Instrument> instrumentList = new ArrayList<Instrument>();
        String splitBy = ";";
        FileReader fr = new FileReader(csvFile);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while ((line = br.readLine()) !=null) {
		     String[] b = line.split(splitBy);
		     System.out.println(b[0]);
		     BaseInstrument instrument = new BaseInstrument();
		     instrument.setName(b[0]);
		     instrumentList.add(instrument);		     
		}
		br.close();	
		return instrumentList;
	}
    
    
    public Instrument saveInstrumentFreebaseFile(String instrumentName) throws IOException {

		File queryResultsFile = getInstrumentJsonFile(instrumentName);
		// do not read the file multiple times
//		if (queryResultsFile.exists())
//			return;

		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();
    	
    	String url = DEFAULT_FREEBASE_MQLREAD_URI;
		
		String query = "[{\"mid\":null," 
		        + "\"id\":null,"
				+ "\"type\":\"/music/instrument\", "
				+ "\"/music/instrument/family\":[], "
				+ "\"key\": [{\"namespace\": null,"
				+ "\"value\": \"" + instrumentName + "\"}] " + "}]";
		url += URLEncoder.encode(query, "UTF-8");
		url += "&key=";
		url += getApiKey();

		return saveInstrumentToJsonFile(queryResultsFile, url);    
    }
    
    
    public void generateInstrumentCsvFile(List<Instrument> instrumentList, String sFileName) {

    	try {
    		FileWriter writer = new FileWriter(ONB_INSTRUMENTS_FOLDER + "/" + sFileName);

    		writer.append("Name");
    		writer.append(CSV_DELIMITER);
    		writer.append("MID");
    		writer.append(CSV_DELIMITER);
    		writer.append("Family");
    		writer.append('\n');

    		Iterator<Instrument> itr = instrumentList.iterator();
    		while (itr.hasNext()) {
    			Instrument instrument = itr.next();
    			writer.append(instrument.getName());
        		writer.append(CSV_DELIMITER);
    			writer.append(instrument.getMid());
        		writer.append(CSV_DELIMITER);
    			writer.append(instrument.getInstrumentFamily().toString());
    			writer.append('\n');
    		}
    		writer.flush();
    		writer.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    
	/**
	 * This method maps ONB music instrument terms to Freebase.
	 * @throws IOException
	 */
	public int mapONBInstruments() throws IOException {
		
		return mapONBInstruments(ONB_INSTRUMENTS_FILE, INSTRUMENTS_FILE_EXTENDED);
	}

	
	/**
	 * This method maps ONB music instrument terms to Freebase.
	 * @throws IOException
	 */
	public int mapONBInstruments(String inputFilePath, String outputFilePath) throws IOException {
		
		int mappedInstrumetCount = 0;
		
		List<Instrument> enrichedInstrumentList = new ArrayList<Instrument>();

		// read ONB instrument list
		File queryResultsFile = getONBInstrumentsFile(inputFilePath);
		
		// create parent dirs
		queryResultsFile.getParentFile().mkdirs();

		// load Freebase instrument data and store it in JSON files
	    try {
	    	List<Instrument> instrumentList = readFromCsvFile(queryResultsFile);
	    	Iterator<Instrument> itr = instrumentList.iterator();
	    	while (itr.hasNext()) {
	    		Instrument enrichedInstrument = saveInstrumentFreebaseFile(itr.next().getName());
	    		if (StringUtils.isNotEmpty(enrichedInstrument.getName()))
	    			enrichedInstrumentList.add(enrichedInstrument);
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    // parse mid and instrument family and store it in CSV comma separated files
	    generateInstrumentCsvFile(enrichedInstrumentList, outputFilePath);

    	mappedInstrumetCount = enrichedInstrumentList.size();

	    return mappedInstrumetCount;
	}

}
