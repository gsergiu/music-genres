package eu.europeana.musicbrainz.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.europeana.api.client.connection.HttpConnector;



/**
 * This class makes use of Musicbrainz API provided by 
 * 
 * http://musicbrainz.org/doc/Development/XML_Web_Service/Version_2/Search
 * 
 * @author GrafR
 *
 */
public class MusicbrainzApiClient {

	private static final Log log = LogFactory.getLog(MusicbrainzApiClient.class);

	public final static String DEFAULT_MUSICBRAINZ_SEARCH_URI = "http://musicbrainz.org/ws/2/";
	public final String BASE_ARTIST_URL = "https://musicbrainz.org/artist/"; 
	public final String BASE_INSTRUMENT_URL = "https://musicbrainz.org/instrument/"; 
	public final String RELATIONSHIPS = "/relationships";	
	public final String INSTRUMENT = "/instrument";	
	
	String SEARCH_FIELD_ARTIST = "artist";

	private String musicbrainzApiUri = "";
	private HttpConnector http = new HttpConnector();

	public final static String SEARCH_RESULTS_FOLDER = "./src/test/resources/search/results";
	
	/**
	 * Create a new connection to the Musicbrainz API.
	 */
	public MusicbrainzApiClient(String apiUri) {
		this.setApiUri(apiUri);
	}

	public MusicbrainzApiClient() {
		this(DEFAULT_MUSICBRAINZ_SEARCH_URI);
	}


	public String getJSONResult(String url) throws IOException {
		log.trace("Call to Europeana API: " + url);
		return http.getURLContent(url);

	}

	
	/**
	 * Returns the Musicbrainz API URI for JSON calls
	 * e.g. http://musicbrainz.org/ws/2/artist/?query=Mekons&fmt=json
	 * @param searchField e.g. artist
	 * @param searchValue e.g. Mekons
	 * @return search URL
	 * @throws UnsupportedEncodingException
	 */
	public String getSearchUriByFieldAndValue(String searchField, String searchValue) throws UnsupportedEncodingException {
		String searchUrl = getApiUri();
		searchUrl += searchField;
		searchUrl += "/?query=" + URLEncoder.encode(searchValue.replace(" ", "+"), "UTF-8");
		searchUrl += "&fmt=json";
		return searchUrl;
	}

	
	/**
	 * Returns the Musicbrainz API artist data in JSON format
	 * e.g. http://musicbrainz.org/ws/2/artist/?query=Mekons&fmt=json
	 * @param artistName
	 * @return search URL
	 * @throws UnsupportedEncodingException 
	 */
	public String getMusicbrainzEntryByArtistName(String artistName) throws UnsupportedEncodingException {
		String searchUrl = getSearchUriByFieldAndValue(SEARCH_FIELD_ARTIST, artistName);
		String searchResult = "";
		try {
			searchResult = getJSONResult(searchUrl);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}

	
	/**
	 * Returns the Musicbrainz API artist data in JSON format
	 * e.g. http://musicbrainz.org/ws/2/artist/?query=Mekons&fmt=json
	 * @param artistName
	 * @return search URL
	 * @throws UnsupportedEncodingException 
	 */
	public String getMusicbrainzEntryById(String id) throws UnsupportedEncodingException {
		String searchUrl = getSearchUriByFieldAndValue(SEARCH_FIELD_ARTIST, id);
		String searchResult = "";
		try {
			searchResult = getJSONResult(searchUrl);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return convertJsonStringToPrettyPrintJsonOutput(searchResult);
	}

	
	/**
	 * Modifies the Musicbrainz API URI for JSON calls. 
	 * @param apiUri
	 */
	public void setApiUri(String apiUri) {
		this.musicbrainzApiUri = apiUri;
	}

	
	public String getApiUri() {
		return musicbrainzApiUri;
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
    	String url = BASE_ARTIST_URL + query;
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
	 * This method extracts instrument ID from the Musicbrainz entity string.
	 * @param entityStr
	 * @return instrument ID
	 */
	public String getInstrumentIdFromMusicbrainzEntityString(String entityStr) {
		String res = "";
		if (StringUtils.isNotEmpty(entityStr) && entityStr.contains(INSTRUMENT)) {
			Pattern pattern = Pattern.compile(INSTRUMENT + "/(.*?)</a>");
			Matcher matcher = pattern.matcher(entityStr);
			if (matcher.find()) {
			    res = matcher.group(1);
			}		
		}
		return res;
	}
	

}
