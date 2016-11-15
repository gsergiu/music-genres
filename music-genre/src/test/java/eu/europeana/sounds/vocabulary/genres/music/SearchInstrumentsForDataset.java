package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.europeana.musicbrainz.connection.MusicbrainzApiClient;
import eu.europeana.sounds.definitions.model.utils.TypeUtils;
import eu.europeana.sounds.skos.BaseSkosTest;
import eu.europeana.wikidata.connection.WikidataApiClient;


public class SearchInstrumentsForDataset extends BaseSkosTest {


	String searchAnalysisFodler = "./src/test/resources/analysis/dataset/";

	String EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV = "extended-ia-wikidata-musicbrainz-overview.csv"; 
	String INSTRUMENTS_OVERVIEW_CSV = "instruments-overview.csv"; 
	String UNIQUE_INSTRUMENTS_CSV = "unique-instruments.csv"; 
	
	int EUROPEANA_ID_COL_POS    = 0;
	int TITLE_COL_POS           = 1;
	int DESC_COL_POS            = 2;
	int CREATOR_ID_COL_POS      = 3;
	int DBPEDIA_ID_COL_POS      = 4;
	int IA_ID_COL_POS           = 5;
	int WIKIDATA_ID_COL_POS     = 6;
	int MUSICBRAINZ_ID_COL_POS  = 7;
	int MUSICBRAINZ_INSTRUMENT_ID_COL_POS    = 8;
	int MUSICBRAINZ_INSTRUMENT_NAME_COL_POS  = 9;
	
	String MUSICBRAINZ_INSTRUMENT_NAME_STR = "Musicbrainz Instrument Name";
	String MUSICBRAINZ_INSTRUMENT_ID_STR   = "Musicbrainz Instrument ID";
	String WIKIDATA_INSTRUMENT_NAME_STR    = "Wikidata Instrument Name";
	String WIKIDATA_INSTRUMENT_ID_STR      = "Wikidata Instrument ID";
	
	WikidataApiClient wikidataApiClient = new WikidataApiClient();
	MusicbrainzApiClient musicbrainzApiClient = new MusicbrainzApiClient();

	protected Logger log = Logger.getLogger(getClass());	
	
	
	/**
	 * This is a helper class for caching to improve performance
	 * 
	 * @author GrafR
	 *
	 */
	private class InstrumentIds {
		
		String musicbrainzInstrumentId = ""; 
		String musicbrainzInstrumentName = ""; 
		String wikidataInstrumentId = ""; 
		String wikidataInstrumentName = ""; 

		public String getMusicbrainzInstrumentId() {
			return musicbrainzInstrumentId;
		}

		public void setMusicbrainzInstrumentId(String musicbrainzInstrumentId) {
			this.musicbrainzInstrumentId = musicbrainzInstrumentId;
		}

		public String getMusicbrainzInstrumentName() {
			return musicbrainzInstrumentName;
		}

		public void setMusicbrainzInstrumentName(String musicbrainzInstrumentName) {
			this.musicbrainzInstrumentName = musicbrainzInstrumentName;
		}

		public String getWikidataInstrumentId() {
			return wikidataInstrumentId;
		}

		public void setWikidataInstrumentId(String wikidataInstrumentId) {
			this.wikidataInstrumentId = wikidataInstrumentId;
		}

		public String getWikidataInstrumentName() {
			return wikidataInstrumentName;
		}

		public void setWikidataInstrumentName(String wikidataInstrumentName) {
			this.wikidataInstrumentName = wikidataInstrumentName;
		}

		public InstrumentIds (			
				String musicbrainzInstrumentId 
				, String musicbrainzInstrumentName 
				, String wikidataInstrumentId 
				, String wikidataInstrumentName 
				) {
			this.musicbrainzInstrumentId = musicbrainzInstrumentId; 
			this.musicbrainzInstrumentName = musicbrainzInstrumentName; 
			this.wikidataInstrumentId = wikidataInstrumentId; 
			this.wikidataInstrumentName = wikidataInstrumentName; 			
		}
	}

	
//	@Test
	public void searchInstrumentsForDataset() throws IOException {
		
		Map<String, InstrumentIds> cacheMap = new HashMap<String, InstrumentIds>();

		File datasetFile = FileUtils.getFile(searchAnalysisFodler + EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
		
		File recordFile = FileUtils.getFile(searchAnalysisFodler + INSTRUMENTS_OVERVIEW_CSV);
		
		while (iterator.hasNext()) {
			
			String europeanaId = "";
			String title = "";
			String description = "";
			String creator = "";
			String dbpediaId = "";
			String iaId = ""; // Internet Archive ID
			String wikidataId = ""; 
			String musicbrainzId = ""; 
			String musicbrainzInstrumentId = ""; 
			String musicbrainzInstrumentName = ""; 
			String wikidataInstrumentId = ""; 
			String wikidataInstrumentName = ""; 

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				String row = new StringBuilder().append(line).append(cellSeparator)
						.append(MUSICBRAINZ_INSTRUMENT_ID_STR).append(cellSeparator)
						.append(MUSICBRAINZ_INSTRUMENT_NAME_STR).append(cellSeparator)
						.append(WIKIDATA_INSTRUMENT_ID_STR).append(cellSeparator)
						.append(WIKIDATA_INSTRUMENT_NAME_STR).append(lineBreak).toString();
				if (cnt > 0) {
					europeanaId = items[EUROPEANA_ID_COL_POS];		
					//ignore comments
					if(europeanaId.isEmpty() || !europeanaId.startsWith("/"))
						continue;

	                title = items[TITLE_COL_POS];
					description = items[DESC_COL_POS];
					creator = items[CREATOR_ID_COL_POS].replace(" ", "").replace("/t", "");
					log.info("Count: " + cnt + ", creator: " + creator);	
										
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
					
					if (cacheMap.containsKey(creator)) {
						InstrumentIds instrumentIds = cacheMap.get(creator);
						musicbrainzInstrumentId = instrumentIds.getMusicbrainzInstrumentId(); 
						musicbrainzInstrumentName = instrumentIds.getMusicbrainzInstrumentName(); 
						wikidataInstrumentId = instrumentIds.getWikidataInstrumentId(); 
						wikidataInstrumentName = instrumentIds.getWikidataInstrumentName(); 			
					} else {					
						if (!StringUtils.isEmpty(wikidataId)) {
							String wikidataHtmlResponse = wikidataApiClient.getEntity(wikidataId);
							int beginPos = wikidataHtmlResponse.indexOf("P" + wikidataApiClient.INSTRUMENT_PROP);
							if (beginPos > 0) {
								wikidataInstrumentId = TypeUtils.extractStringBetweenPrefixAndEnding(
										wikidataHtmlResponse.substring(beginPos), "numeric-id", ",").replace("\\\":", "") ;
								wikidataHtmlResponse = wikidataApiClient.getEntity(wikidataInstrumentId);
								wikidataInstrumentName = TypeUtils.extractStringBetweenPrefixAndEnding(
										wikidataHtmlResponse, "<title>", "<") ;
			                    log.info("wikidataInstrumentId: " + wikidataInstrumentId);
			                    log.info("wikidataInstrumentName: " + wikidataInstrumentName);						}				
						}
	
						if (!StringUtils.isEmpty(musicbrainzId)) {
							String musicbrainzEntity = musicbrainzApiClient.getEntity(
									musicbrainzId + musicbrainzApiClient.RELATIONSHIPS);
	//						log.info(musicbrainzEntity);
							
							List<String> musicbrainzInstrumentIdDefList = 
									TypeUtils.extractMultipleStringsBetweenPrefixAndEnding(
											musicbrainzEntity, musicbrainzApiClient.INSTRUMENT + "/", "</a>");
	
	//						String musicbrainzInstrumentDef = musicbrainzApiClient
	//								.getInstrumentIdFromMusicbrainzEntityString(musicbrainzEntity);
							List<String> musicbrainzInstrumentIdList = new ArrayList<String>();
							List<String> musicbrainzInstrumentNameList = new ArrayList<String>();
							for (String musicbrainzInstrumentDef : musicbrainzInstrumentIdDefList) {
								if (StringUtils.isNotEmpty(musicbrainzInstrumentDef)) {
									String[] instrumentArr = musicbrainzInstrumentDef.split("\">");
									String musicbrainzInstrumentIdCur = instrumentArr[0];
									String musicbrainzInstrumentNameCur = instrumentArr[1];
								    if (!musicbrainzInstrumentIdList.contains(musicbrainzInstrumentIdCur)) {
								    	musicbrainzInstrumentIdList.add(musicbrainzInstrumentIdCur);
								    }
								    if (!musicbrainzInstrumentNameList.contains(musicbrainzInstrumentNameCur)) {
								    	musicbrainzInstrumentNameList.add(musicbrainzInstrumentNameCur);
								    }
								}
							}
	
							musicbrainzInstrumentId = TypeUtils.convertListToString(musicbrainzInstrumentIdList, "#");
							musicbrainzInstrumentName = TypeUtils.convertListToString(musicbrainzInstrumentNameList, "#");
							log.info("musicbrainzInstrumentId: " + musicbrainzInstrumentId);
							log.info("musicbrainzInstrumentName: " + musicbrainzInstrumentName);							
						}
						
						cacheMap.put(creator, 
								new InstrumentIds(
										musicbrainzInstrumentId
										, musicbrainzInstrumentName
										, wikidataInstrumentId
										, wikidataInstrumentName
								)
						);						
					}

					row = new StringBuilder().append(europeanaId).append(cellSeparator)
							.append(title).append(cellSeparator)
							.append(description).append(cellSeparator)
							.append(creator).append(cellSeparator)
							.append(dbpediaId).append(cellSeparator)
							.append(iaId).append(cellSeparator)
							.append(wikidataId).append(cellSeparator)
							.append(musicbrainzId).append(cellSeparator)
							.append(musicbrainzInstrumentId).append(cellSeparator)
							.append(musicbrainzInstrumentName).append(cellSeparator)
							.append(wikidataInstrumentId).append(cellSeparator)
							.append(wikidataInstrumentName).append(lineBreak)
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
	public void createUniqueInstrumentList() throws IOException {
		
		Map<String, String> instrumentMap = new HashMap<String, String>();
		
		File datasetFile = FileUtils.getFile(searchAnalysisFodler + INSTRUMENTS_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
		
		File recordFile = FileUtils.getFile(searchAnalysisFodler + UNIQUE_INSTRUMENTS_CSV);
		
		while (iterator.hasNext()) {
			
			String musicbrainzInstrumentId = ""; 
			String musicbrainzInstrumentName = ""; 

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				if (cnt > 0) {
					if (items != null && items.length > MUSICBRAINZ_INSTRUMENT_ID_COL_POS) {
						musicbrainzInstrumentId = items[MUSICBRAINZ_INSTRUMENT_ID_COL_POS];
						List<String> curInstruments = TypeUtils.convertStringToList(musicbrainzInstrumentId, "#");
						if (items != null && items.length > MUSICBRAINZ_INSTRUMENT_NAME_COL_POS) {
							musicbrainzInstrumentName = items[MUSICBRAINZ_INSTRUMENT_NAME_COL_POS];
						}
						List<String> curInstrumentNames = TypeUtils.convertStringToList(musicbrainzInstrumentName, "#");
						int count = 0;
						for (String instrumentId : curInstruments) {
							if (!instrumentMap.containsKey(instrumentId)) {
								instrumentMap.put(instrumentId, curInstrumentNames.get(count));
							}
							count = count +1;
						}
					}
				}
				cnt++;
			}
		}

		String row = new StringBuilder()
				.append(MUSICBRAINZ_INSTRUMENT_ID_STR).append(cellSeparator)
				.append(MUSICBRAINZ_INSTRUMENT_NAME_STR).append(lineBreak).toString();
		FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);

		for (String key: instrumentMap.keySet()) {
			row = new StringBuilder()
				.append(key).append(cellSeparator)
				.append(instrumentMap.get(key)).append(lineBreak)
				.toString();
			FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);
		}		
	}
	

}
