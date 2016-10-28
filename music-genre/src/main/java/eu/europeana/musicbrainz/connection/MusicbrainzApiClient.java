package eu.europeana.musicbrainz.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.europeana.api.client.connection.HttpConnector;



public class MusicbrainzApiClient {

	private static final Log log = LogFactory.getLog(MusicbrainzApiClient.class);

	public static final String DEFAULT_MUSICBRAINZ_SEARCH_URI = "http://musicbrainz.org/ws/2/";
	
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
	
	
}
