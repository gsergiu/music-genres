package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.dbpedia.connection.DBPediaApiClient;
import eu.europeana.musicbrainz.connection.MusicbrainzApiClient;
import eu.europeana.sounds.skos.BaseSkosTest;
import eu.europeana.wikidata.connection.WikidataApiClient;


public class SearchWikidataForDataset extends BaseSkosTest {


	String searchAnalysisFodler = "./src/test/resources/analysis/dataset/";
	String datasetFolder = "C:\\europeana-client\\datasets\\collections\\metadata\\full\\";

	String EXTENDED_OVERVIEW_CSV = "extended-overview.csv"; 
	String EXTENDED_WIKIDATA_OVERVIEW_CSV = "extended-wikidata-overview.csv"; 
	String EXTENDED_TITLE_OVERVIEW_CSV = "extended-title-overview.csv"; 
	String EXTENDED_IA_WIKIDATA_OVERVIEW_CSV = "extended-ia-wikidata-overview.csv"; 
	String EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV = "extended-ia-wikidata-musicbrainz-overview.csv"; 
	
	String DBPEDIA_ID_BASE_URL = "http://dbpedia.org/resource/";
	String IA_ID_BASE_URL = "https://archive.org/details/";
	String EUROPEANA_ID_BASE_DELIMITER = "data_sounds_";
	
	int EUROPEANA_ID_COL_POS    = 0;
	int TITLE_COL_POS           = 1;
	int DESC_COL_POS            = 2;
	int CREATOR_ID_COL_POS      = 3;
	int DBPEDIA_ID_COL_POS      = 4;
	int IA_ID_COL_POS           = 5;
	int WIKIDATA_ID_COL_POS     = 6;
	int MUSICBRAINZ_ID_COL_POS  = 7;
	
	String DBPEDIA_ID_STR       = "DBPedia ID";
	String IA_ID_STR            = "IA ID";
	String WIKIDATA_ID_STR      = "Wikidata ID";
	String MUSICBRAINZ_ID_STR   = "Musicbrainz ID";
	
	String IA_ID_PROP           = "724";
	String MUSICBRAINZ_ID_PROP  = "434";
	
	WikidataApiClient apiClient = new WikidataApiClient();
	DBPediaApiClient dbpediaApiClient = new DBPediaApiClient();
	MusicbrainzApiClient musicbrainzApiClient = new MusicbrainzApiClient();

	protected Logger log = Logger.getLogger(getClass());
	
	
//	@Test
	public void enrichMetadataForDatasetDiscoveringWikidataIdByInternetArchiveId() throws IOException{

		File datasetFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
		
		File recordFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_WIKIDATA_OVERVIEW_CSV);
		
		while (iterator.hasNext()) {
			
			String europeanaId = "";
			String title = "";
			String description = "";
			String creator = "";
			String dbpediaId = "";
			String iaId = ""; // Internet Archive ID
			String wikidataId = ""; 

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				String row = new StringBuilder().append(line).append(cellSeparator)
						.append(WIKIDATA_ID_STR).append(lineBreak)
						.toString();
				if (cnt > 0) {
					europeanaId = items[EUROPEANA_ID_COL_POS];
					//ignore comments
					if(europeanaId.isEmpty() || !europeanaId.startsWith("/"))
						continue;
					title = items[TITLE_COL_POS];
					description = items[DESC_COL_POS];
					creator = items[CREATOR_ID_COL_POS];
					iaId = items[IA_ID_COL_POS];
					if (iaId.length() > 0) {
						log.info("Count: " + cnt + ", IA ID: " + iaId);
						String[] iaIdAsArray = iaId.split("/");
						String iaIdQuery = iaIdAsArray[iaIdAsArray.length-1].replace("\t", "");
						String jsonResponse = apiClient.queryWikidataByProperty(IA_ID_PROP, iaIdQuery);
						try {
							JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
							JsonArray arr = (JsonArray) jsonObject.get("items");							
							if (arr.size() > 0) {
								wikidataId = arr.get(0).toString();
								log.info("Wikidata ID: " + wikidataId);
							}
						} catch (Exception e) {
							log.error(e.getMessage());
						}
					}

					row = new StringBuilder().append(europeanaId).append(cellSeparator)
							.append(title).append(cellSeparator)
							.append(description).append(cellSeparator)
							.append(creator).append(cellSeparator)
							.append(dbpediaId).append(cellSeparator)
							.append(iaId).append(cellSeparator)
							.append(wikidataId).append(lineBreak)
							.toString();
					FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);
				} else {
					FileUtils.writeStringToFile(recordFile, row, "UTF-8");					
				}
				cnt++;
			}
		}		
		log.info("Successfully enriched items: " + cnt);		
	}
	
	
	/**
	 * @param obj
	 * @param objName
	 * @return
	 */
	private String parseJsonValueFromArrayInObject(JSONObject obj, String objName) {
		JSONArray arr = (JSONArray) obj.get(objName);							
        String value = (String) arr.get(0);
		return value;
	}

	
	/**
	 * @param inputStr
	 * @return
	 */
	private String normalizeStr(String inputStr) {
		return inputStr.replace("\r\n", "").replace("\n", "");
	}
	
	
//	@Test
	public void enrichMetadataForDatasetDiscoveringTitleByInternetArchiveId() throws IOException{

		File datasetFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_WIKIDATA_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
        JSONParser parser = new JSONParser();
		
		File recordFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_TITLE_OVERVIEW_CSV);
		
		while (iterator.hasNext()) {
			
			String europeanaId = "";
			String title = "";
			String description = "";
			String creator = "";
			String dbpediaId = "";
			String iaId = ""; // Internet Archive ID
			String wikidataId = ""; 
			String musicbrainzId = ""; 
			String europeanaIdentifier = "";

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				String row = new StringBuilder().append(line).append(cellSeparator)
						.append(MUSICBRAINZ_ID_STR).append(lineBreak)
						.toString();
				if (cnt > 0) {
					europeanaId = items[EUROPEANA_ID_COL_POS];		
					//ignore comments
					if(europeanaId.isEmpty() || !europeanaId.startsWith("/"))
						continue;

	                title = items[TITLE_COL_POS];
					if (title.startsWith("Venue: ")) { // in case of corrupted title
						description = "";
						creator = "";
						iaId = "";
						title = "";
						
						// Read europeana full JSON file by europeana ID
						// Load results object from JSON file
				        try {
				            Object obj = parser.parse(new FileReader(datasetFolder + europeanaId + ".json"));
				            // webresource
		                    JSONObject jsonObject = (JSONObject) obj;
							JSONObject o1 = (JSONObject) jsonObject.get("object");							
							JSONArray o2 = (JSONArray) o1.get("proxies");							
							JSONObject dc = (JSONObject) ((JSONObject) o2.get(0)).get("dcIdentifier");
							europeanaIdentifier = parseJsonValueFromArrayInObject(dc, "def");
		                    log.info("europeana identifier: " + europeanaIdentifier);
				        } catch (FileNotFoundException e) {
				            e.printStackTrace();
				        } catch (IOException e) {
				            e.printStackTrace();
				        } catch (ParseException e) {
				            e.printStackTrace();
				        }

				        String iaJsonResponse = apiClient.getJSONResult(
								"https://archive.org/details/" + europeanaIdentifier + "&output=json");
		                Object obj;
		                try {
		                    obj = parser.parse(iaJsonResponse);
		                    JSONObject jsonObject = (JSONObject) obj;
		                    JSONObject o1 = (JSONObject) jsonObject.get("metadata");
		                    String titleRaw = parseJsonValueFromArrayInObject(o1, "title");
		                    title = normalizeStr(titleRaw);
		                    String creatorRaw = parseJsonValueFromArrayInObject(o1, "creator");
		                    creator = normalizeStr(creatorRaw);
		                    String descriptionRaw = parseJsonValueFromArrayInObject(o1, "description");
		                    description = normalizeStr(descriptionRaw);
		                } catch (ParseException e) {
		                    log.error(e.getMessage());
		                }						
					} else {
						description = items[DESC_COL_POS];
						creator = items[CREATOR_ID_COL_POS];
						iaId = items[IA_ID_COL_POS];
					}
					if (items != null && items.length > WIKIDATA_ID_COL_POS) {
						wikidataId = items[WIKIDATA_ID_COL_POS];
					}
					if (wikidataId.length() > 0) {
						log.info("Count: " + cnt + ", Wikidata ID: " + wikidataId);
						String wikidataJsonResponse = apiClient.getMusicBrainzIdFromWikidataById(wikidataId);
		                try {
		                    Object obj = parser.parse(wikidataJsonResponse);
		                    JSONObject jsonObject = (JSONObject) obj;
		                    JSONObject o1 = (JSONObject) jsonObject.get("props");
		            		JSONArray arr = (JSONArray) o1.get(MUSICBRAINZ_ID_PROP);							
		            		musicbrainzId = (String) arr.get(0).toString().split(",")[2].replace("\"", "").replace("]", "");		                    
		                    log.info("musicbrainz ID: " + musicbrainzId);
		                } catch (ParseException pe) {
		                    log.error(pe.getMessage());
		                } catch (Exception e) {
		                    log.error(e.getMessage());
		                }						
					}

					row = new StringBuilder().append(europeanaId).append(cellSeparator)
							.append(title).append(cellSeparator)
							.append(description).append(cellSeparator)
							.append(creator).append(cellSeparator)
							.append(dbpediaId).append(cellSeparator)
							.append(iaId).append(cellSeparator)
							.append(wikidataId).append(cellSeparator)
							.append(musicbrainzId).append(lineBreak)
							.toString();
					FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);
				} else {
					FileUtils.writeStringToFile(recordFile, row, "UTF-8");					
				}
				cnt++;
			}
		}		
		log.info("Successfully enriched items: " + cnt);		
	}
	

	@Test
	public void enrichMetadataForDatasetMusicbrainzIdUsingCreatorName() throws IOException{

		File datasetFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_IA_WIKIDATA_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
        JSONParser parser = new JSONParser();
		
		File recordFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV);
		
		while (iterator.hasNext()) {
			
			String europeanaId = "";
			String title = "";
			String description = "";
			String creator = "";
			String dbpediaId = "";
			String iaId = ""; // Internet Archive ID
			String wikidataId = ""; 
			String musicbrainzId = ""; 

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				String row = new StringBuilder().append(line).append(lineBreak).toString();
				if (cnt > 0) {
					europeanaId = items[EUROPEANA_ID_COL_POS];		
					//ignore comments
					if(europeanaId.isEmpty() || !europeanaId.startsWith("/"))
						continue;

	                title = items[TITLE_COL_POS];
					description = items[DESC_COL_POS];
					creator = items[CREATOR_ID_COL_POS];
					if (items != null && items.length > DBPEDIA_ID_COL_POS) {
						dbpediaId = items[DBPEDIA_ID_COL_POS];
					}
					if (items != null && items.length > IA_ID_COL_POS) {
						iaId = items[IA_ID_COL_POS];
					}
					if (items != null && items.length > WIKIDATA_ID_COL_POS) {
						wikidataId = items[WIKIDATA_ID_COL_POS];
					}
					if (items != null && items.length > MUSICBRAINZ_ID_COL_POS) {
						musicbrainzId = items[MUSICBRAINZ_ID_COL_POS];
					}
					if (StringUtils.isEmpty(musicbrainzId)) {
						log.info("Count: " + cnt + ", creator: " + creator);						
						String musicbrainzJsonResponse = musicbrainzApiClient.getMusicbrainzEntryByArtistName(creator);
		                try {
		                    Object obj = parser.parse(musicbrainzJsonResponse);
		                    JSONObject jsonObject = (JSONObject) obj;
		            		JSONArray artistsArray = (JSONArray) jsonObject.get("artists");							
		            		musicbrainzId = (String) ((JSONObject) artistsArray.get(0)).get("id");		                    
		                    log.info("musicbrainz ID: " + musicbrainzId);
		                } catch (ParseException pe) {
		                    log.error(pe.getMessage());
		                } catch (Exception e) {
		                    log.error(e.getMessage());
		                }						
					}

					row = new StringBuilder().append(europeanaId).append(cellSeparator)
							.append(title).append(cellSeparator)
							.append(description).append(cellSeparator)
							.append(creator).append(cellSeparator)
							.append(dbpediaId).append(cellSeparator)
							.append(iaId).append(cellSeparator)
							.append(wikidataId).append(cellSeparator)
							.append(musicbrainzId).append(lineBreak)
							.toString();
					FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);
				} else {
					FileUtils.writeStringToFile(recordFile, row, "UTF-8");					
				}
				cnt++;
			}
		}		
		log.info("Successfully enriched items: " + cnt);		
	}
	

}
