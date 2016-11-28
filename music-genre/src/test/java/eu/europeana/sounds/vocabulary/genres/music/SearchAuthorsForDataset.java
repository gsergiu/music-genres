package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.assertTrue;
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
import eu.europeana.skos.SkosApiClient;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.definitions.model.concept.impl.MimoMappingView;
import eu.europeana.sounds.definitions.model.utils.TypeUtils;
import eu.europeana.sounds.skos.BaseSkosTest;
import eu.europeana.wikidata.connection.WikidataApiClient;


public class SearchAuthorsForDataset extends BaseSkosTest {


	String searchAnalysisFodler = "./src/test/resources/analysis/dataset/";

	String EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV = "extended-ia-wikidata-musicbrainz-overview.csv"; 
	String VIAF_DBPEDIA_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV = "viaf-dbpedia-ia-wikidata-musicbrainz-overview.csv"; 
	
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
	
	int INSTRUMENT_ID_COL_POS   = 0;
	int INSTRUMENT_NAME_COL_POS = 1;

	String VIAF_ID_STR = "VIAF ID";
	
	String DBPEDIA_BASE_URL = "http://dbpedia.org/resource/";
	
	WikidataApiClient wikidataApiClient = new WikidataApiClient();
	MusicbrainzApiClient musicbrainzApiClient = new MusicbrainzApiClient();

	protected Logger log = Logger.getLogger(getClass());	

	
	@Test
	public void searchInstrumentsForDataset() throws IOException {
		
		File datasetFile = FileUtils.getFile(
				searchAnalysisFodler + EXTENDED_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV);
		if(!datasetFile.exists())
			fail("required dataset file doesn't exist" + datasetFile);
		
		LineIterator iterator = FileUtils.lineIterator(datasetFile);
		String line;
		int cnt = 0;
		final String cellSeparator = ";"; 
		final String lineBreak = "\n"; 
		
		File recordFile = FileUtils.getFile(
				searchAnalysisFodler + VIAF_DBPEDIA_IA_WIKIDATA_MUSICBRAINZ_OVERVIEW_CSV);
		
		while (iterator.hasNext()) {
			
			String europeanaId = "";
			String title = "";
			String description = "";
			String creator = "";
			String dbpediaId = "";
			String iaId = ""; // Internet Archive ID
			String wikidataId = ""; 
			String musicbrainzId = ""; 
			String viafId = ""; 

			line = (String) iterator.next();
			String[] items = line.split(cellSeparator);
			if (items != null && items.length >= CREATOR_ID_COL_POS && items[CREATOR_ID_COL_POS] != null) {
				String row = new StringBuilder().append(line).append(cellSeparator)
						.append(VIAF_ID_STR).append(lineBreak).toString();
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

					if (!StringUtils.isEmpty(musicbrainzId)) {
						String musicbrainzEntity = musicbrainzApiClient.getEntity(
								musicbrainzId + musicbrainzApiClient.RELATIONSHIPS);
						
						List<String> viafIdList = 
								TypeUtils.extractMultipleStringsBetweenPrefixAndEnding(
										musicbrainzEntity
										, musicbrainzApiClient.BDI_TAG_BEGIN + musicbrainzApiClient.VIAF_POINTER
										, musicbrainzApiClient.BDI_TAG_END);
						if (viafIdList.size() > 0)
							viafId = viafIdList.get(0);

						List<String> dbpediaIdList = 
								TypeUtils.extractMultipleStringsBetweenPrefixAndEnding(
										musicbrainzEntity
										, musicbrainzApiClient.BDI_TAG_BEGIN + musicbrainzApiClient.DBPEDIA_POINTER
										, musicbrainzApiClient.BDI_TAG_END);
						if (dbpediaIdList.size() > 0)
							dbpediaId = DBPEDIA_BASE_URL + dbpediaIdList.get(0).replace(" ", "_");

						List<String> wikidataIdList = 
								TypeUtils.extractMultipleStringsBetweenPrefixAndEnding(
										musicbrainzEntity
										, musicbrainzApiClient.BDI_TAG_BEGIN + musicbrainzApiClient.WIKIDATA_POINTER
										, musicbrainzApiClient.BDI_TAG_END);
						if (wikidataIdList.size() > 0)
							wikidataId = wikidataIdList.get(0);

						log.info("count: " + cnt + ", viafId: " + viafId + ", dbpediaId: " + dbpediaId + ", wikidataId: " + wikidataId);
					}

					row = new StringBuilder().append(europeanaId).append(cellSeparator)
							.append(title).append(cellSeparator)
							.append(description).append(cellSeparator)
							.append(creator).append(cellSeparator)
							.append(dbpediaId).append(cellSeparator)
							.append(iaId).append(cellSeparator)
							.append(wikidataId).append(cellSeparator)
							.append(musicbrainzId).append(cellSeparator)
							.append(viafId).append(lineBreak)
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
