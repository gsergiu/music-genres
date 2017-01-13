package eu.europeana.mimo.connection;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.api.client.connection.HttpConnector;
import eu.europeana.sounds.vocabulary.genres.music.MimoJsonLdInstruments.SupportedLanguages;



/**
 * This class provides MIMO API
 * 
 * @author GrafR
 *
 */
public class MimoApiClient {

	private static final Log log = LogFactory.getLog(MimoApiClient.class);

	static String MIMO_BASE_URI = "http://data.mimo-db.eu/sparql/describe?uri=";
	String CSV_LINE_DELIMITER = ";";
	
	public String EXACT_MATCH = "exactMatch";
	public String BROAD_MATCH = "broadMatch";	
	
	public final String cellSeparator = ";"; 
	public final String lineBreak = "\n"; 
	
	final String ID         = "@id";
	final String LANGUAGE   = "@language";
	final String VALUE      = "@value";
	final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
	final String ALT_LABEL  = "http://www.w3.org/2004/02/skos/core#altLabel";
	
	private String mimoApiUri = "";
	private HttpConnector http = new HttpConnector();

	public final static String SEARCH_RESULTS_FOLDER = "./src/test/resources/search/results";
	
	/**
	 * Create a new connection to the MIMO API.
	 */
	public MimoApiClient(String apiUri) {
		this.setApiUri(apiUri);
	}

	public MimoApiClient() {
		this(MIMO_BASE_URI);
	}


	public String getJSONResult(String url) throws IOException {
		log.trace("Call to Europeana API: " + url);
		return http.getURLContent(url);

	}

	
	/**
	 * Modifies the MIMO API URI for JSON calls. 
	 * @param apiUri
	 */
	public void setApiUri(String apiUri) {
		this.mimoApiUri = apiUri;
	}

	
	public String getApiUri() {
		return mimoApiUri;
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
	

    public String getHtmlContent(String query) throws IOException {

    	HttpConnector httpConnection = new HttpConnector();
    	String url = MIMO_BASE_URI + query;
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
	
    
    /**
     * @param matchMap
     * @param matchKey
     * @return
     */
    public List<String> extractMimoJsonContentFromMap(Map<String, String> matchMap, String matchKey) {
		 
    	List<String> res = new ArrayList<String>();
    	
		if (matchMap != null && matchMap.size() > 0) {

			for (String key: matchMap.keySet()) {
				if (key.contains(matchKey)) {
					String mimoId = matchMap.get(key);
					String jsonLdString = extractMimoJsonContentFromId(mimoId);
					res.add(jsonLdString);
				}
			}
    	}
        
		return res;
    }
        

    /**
     * @param id
     * @return
     */
    public String extractMimoJsonContentFromId(String id) {
    	
    	String res = "";
    	
		HttpResponse response = queryMimoApi(id);
		try {
			String jsonLdString = readJsonHttpResponse(response);
			res = jsonLdString;
		} catch (IOException e) {
			log.error("Can't read JSON HTTP response for MIMO id: " + id + ". " + e.getMessage());
		}
		
		return res;    
    }
    
    
	/**
	 * This method queries MIMO API by ID.
	 * e.g. retrieve data by id e.g. http://data.mimo-db.eu/sparql/describe?
	 *     uri=http%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F2251&type=Resource&default-graph-uri=data
	 * @param id
	 * @return HttpResponse
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public HttpResponse queryMimoApi(String id) {
		
		HttpResponse response = null;
		
		try {
			String searchUrl = MIMO_BASE_URI;
			searchUrl += id.replace(" ", "");
			searchUrl += "&type=Resource&default-graph-uri=data";
			log.trace("Call to MIMO API: " + searchUrl);
		
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(searchUrl);
			httpGet.addHeader("Accept" , "application/ld+json");
	
			response = httpclient.execute(httpGet);
		} catch (ClientProtocolException e) {
			log.error("Can't read MIMO API response." + e.getMessage());
		} catch (IOException e) {
			log.error("Can't read MIMO API response." + e.getMessage());
		}	
		
		return response;
	}
	
	
	/**
	 * This methods reads JSON content from HTTP response.
	 * @param response
	 * @return JSON content string
	 * @throws IOException
	 */
	public String readJsonHttpResponse(HttpResponse response)
			throws IOException {
		
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			while ((line = reader.readLine()) != null)
				builder.append(line);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for response: "
						+ response.toString());
			}
		}

		return builder.toString();
	}
	
	
	/**
	 * This method reads a given field from JSON file to String.
	 * @param fileName
	 * @param fieldName The name of a field in JSON object
	 * @return The value of the given field
	 * @throws Throwable
	 */
	public String parseJsonLdFile(String fileName) throws Throwable {
		
		String res = "";
		
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(
            		new InputStreamReader(new FileInputStream(fileName), "UTF8"));            		
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (final JsonElement elem : jsonArray) {
                JsonObject jsonObject = elem.getAsJsonObject();
                String id = jsonObject.get(ID).getAsString();
                String [] filePathArr = fileName.split("\\\\");
                String fileNameStr = filePathArr[filePathArr.length - 1];
                String fileNameId = fileNameStr.replace(".json", "");
                if (id.contains(fileNameId)) {
                	String prefLabelStr = extractLabelsFromJsonConcept(jsonObject, PREF_LABEL);
                	String altLabelStr = extractLabelsFromJsonConcept(jsonObject, ALT_LABEL);
					res = new StringBuilder().append(id).append(cellSeparator)
						.append(prefLabelStr).append(cellSeparator)
						.append(altLabelStr).append(lineBreak)
						.toString();
                }
            }
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return res;
	}

	
	/**
	 * @param content
	 * @return
	 * @throws Throwable
	 */
	public String parsePrefLabelJsonLdString(String content) {
		
		String res = "";
		
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(content);            		
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (final JsonElement elem : jsonArray) {
                JsonObject jsonObject = elem.getAsJsonObject();
                String id = jsonObject.get(ID).getAsString();
            	String prefLabelStr = extractLabelsFromJsonConcept(jsonObject, PREF_LABEL);
				res = new StringBuilder().append(id).append(cellSeparator)
					.append(prefLabelStr)
					.toString();
            }
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return res;
	}

	
	public String parsePrefLabelAnyLanguageJsonLdString(String content) {
		
		String res = "";
		
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(content);            		
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (final JsonElement elem : jsonArray) {
                JsonObject jsonObject = elem.getAsJsonObject();
                String id = jsonObject.get(ID).getAsString();
            	String prefLabelStr = extractLabelsAnyLanguageFromJsonConcept(jsonObject, PREF_LABEL);
				res = new StringBuilder().append(id).append(cellSeparator)
					.append(prefLabelStr)
					.toString();
            }
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return res;
	}

	
	/**
	 * This method presents Concept labels as a string.
	 * @param jsonObject
	 * @param type e.g. prefLabel
	 * @return label string
	 */
	private String extractLabelsFromJsonConcept(JsonObject jsonObject, String type) {
		
		String labelStr = "";
		
	    if (jsonObject.has(type)) {
			JsonArray labelArray = jsonObject.get(type).getAsJsonArray();
			for (final JsonElement subElem : labelArray) {
			    JsonObject subElemJsonObject = subElem.getAsJsonObject();
			    if (subElemJsonObject.has(LANGUAGE)) {
				    String language = subElemJsonObject.get(LANGUAGE).getAsString();
					for (SupportedLanguages sl : SupportedLanguages.values()) {
						if (sl.name().toLowerCase().equals(language)) {
						    if (subElemJsonObject.has(VALUE)) {
					            String value = subElemJsonObject.get(VALUE).getAsString();
					            labelStr = labelStr + value + "@" + language + ",";
						    }
						}
					}
			    }
			}
			
			// remove last comma
			if (labelStr != null && labelStr.length() > 0 && labelStr.charAt(labelStr.length()-1)==',') {
				labelStr = labelStr.substring(0, labelStr.length()-1);
			}	
	    }
	    
		return labelStr;
	}
	
	
	private String extractLabelsAnyLanguageFromJsonConcept(JsonObject jsonObject, String type) {
		
		String labelStr = "";
		
	    if (jsonObject.has(type)) {
			JsonArray labelArray = jsonObject.get(type).getAsJsonArray();
			for (final JsonElement subElem : labelArray) {
			    JsonObject subElemJsonObject = subElem.getAsJsonObject();
			    if (subElemJsonObject.has(LANGUAGE)) {
				    String language = subElemJsonObject.get(LANGUAGE).getAsString();
				    if (subElemJsonObject.has(VALUE)) {
			            String value = subElemJsonObject.get(VALUE).getAsString();
			            labelStr = labelStr + value + "@" + language + ",";
				    }
			    }
			}
			
			// remove last comma
			if (labelStr != null && labelStr.length() > 0 && labelStr.charAt(labelStr.length()-1)==',') {
				labelStr = labelStr.substring(0, labelStr.length()-1);
			}	
	    }
	    
		return labelStr;
	}
	
	
    /**
     * @param url
     * @return
     */
    public String getNumberFromIdUrl(String url) {
    	String res = "";
		if (StringUtils.isNotEmpty(url)) {
			String[] urlArr = url.split("/");
			res = urlArr[urlArr.length-1];
		}
		return res;
    }
}
