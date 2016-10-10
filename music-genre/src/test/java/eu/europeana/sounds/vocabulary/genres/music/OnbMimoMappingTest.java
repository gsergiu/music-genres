/*
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import eu.europeana.api.client.exception.EuropeanaApiProblem;
import eu.europeana.search.connection.EuropeanaSearchApiClient;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.skos.BaseSkosTest;

/**
 * This class implements ONB MIMO mapping testing.
 */
public class OnbMimoMappingTest extends BaseSkosTest {

	
	int EUROPEANA_ID_COL_POS = 3;
	int EXACT_MATCH_COL_POS = 4;
	int BROAD_MATCH_COL_POS = 5;
	String ID_DELIMITER = "_";
	String CSV_LINE_DELIMITER = ";";

	String mappingFolder = "./src/test/resources/MIMO/onb/mapping";
	
	public final String VARIATIONS_INSTRUMENT_LIST_FILE_PATH =
			mappingFolder + "/ONB_MIMO_keyword_mapping_master_variations.csv";
	public final String ENRICHED_INSTRUMENT_VARIATIONS_FILE_PATH = 
			mappingFolder + "/Enriched_ONB_MIMO_keyword_mapping_master_variations.csv";
	public final String SHORTENINGS_INSTRUMENT_LIST_FILE_PATH =
			mappingFolder + "/ONB_MIMO_keyword_mapping_master_shortenings.csv";
	public final String ENRICHED_INSTRUMENT_SHORTENINGS_FILE_PATH = 
			mappingFolder + "/Enriched_ONB_MIMO_keyword_mapping_master_shortenings.csv";

	public final String ENRICHED_INSTRUMENT_VARIATIONS_V1_FILE_PATH = 
			mappingFolder + "/Enriched_ONB_MIMO_keyword_mapping_master_variations_V1.csv";
	public final String ENRICHED_INSTRUMENT_SHORTENINGS_V1_FILE_PATH = 
			mappingFolder + "/Enriched_ONB_MIMO_keyword_mapping_master_shortenings_V1.csv";
	public final String INPUT_ENRICHED_INSTRUMENT_LIST_FILE_PATH = 
			mappingFolder + "/Enrichments_V1.csv";
	public final String OUTPUT_ENRICHED_INSTRUMENT_LIST_FILE_PATH = 
			mappingFolder + "/Enrichments_V2.csv";
	

	EuropeanaSearchApiClient apiClient = new EuropeanaSearchApiClient();
	
	
	/**
	 * 
	 * ENRICH VARIATIONS FOR INSTRUMENT LIST
	 * 
	 * Having an ONB variations instrument list we match instrument data by music genre labels, 
	 * and enrich instrument list in CSV format, enriched matches. 
	 * @throws IOException
	 */
//	@Test
	public void mapONBInstrumentsVariations() throws IOException {
		
		int ID_COLUMN_POS = 3;
		int EXACT_MATCH_COLUMN_POS = 1;
		int BROAD_MATCH_COLUMN_POS = 2;
				
		Map<String, Integer> fields = new HashMap<String, Integer>();
		fields.put(getSkosUtils().EXACT_MATCH_FIELD, EXACT_MATCH_COLUMN_POS);
		fields.put(getSkosUtils().BROAD_MATCH_FIELD, BROAD_MATCH_COLUMN_POS);
		List<Concept> mimoConceptList = getSkosUtils().retrieveConceptWithUriFromFile(
				VARIATIONS_INSTRUMENT_LIST_FILE_PATH, ID_COLUMN_POS, fields);
		
		int numberOfMappedInstruments = apiClient.mapOnbMimo(
				mimoConceptList, ENRICHED_INSTRUMENT_VARIATIONS_FILE_PATH);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
	/**
	 * 
	 * ENRICH SHORTENINGS FOR INSTRUMENT LIST
	 * 
	 * Having an ONB shortenings instrument list we match instrument data by music genre labels, 
	 * and enrich instrument list in CSV format, enriched matches. 
	 * @throws IOException
	 */
//	@Test
	public void mapONBInstrumentsShortenings() throws IOException {
		
		int ID_COLUMN_POS = 3;
		int EXACT_MATCH_COLUMN_POS = 1;
		int BROAD_MATCH_COLUMN_POS = 2;
				
		Map<String, Integer> fields = new HashMap<String, Integer>();
		fields.put(getSkosUtils().EXACT_MATCH_FIELD, EXACT_MATCH_COLUMN_POS);
		fields.put(getSkosUtils().BROAD_MATCH_FIELD, BROAD_MATCH_COLUMN_POS);
		List<Concept> mimoConceptList = getSkosUtils().retrieveConceptWithUriFromFile(
				SHORTENINGS_INSTRUMENT_LIST_FILE_PATH, ID_COLUMN_POS, fields);
		
		int numberOfMappedInstruments = apiClient.mapOnbMimo(
				mimoConceptList, ENRICHED_INSTRUMENT_SHORTENINGS_FILE_PATH);
		
		assertTrue(numberOfMappedInstruments > 0);
	}

	
	/**
	 * This method validates mapping parameters and
	 * notices mapping ID in form <EuropeanaId_matchLink>
	 * @param instrumentsArr
	 * @param instrumentLine
	 * @param enrichedIds
	 * @param mapIdToLine
	 * @param idColPos The location of the Europeana ID column in CSV file
	 * @param matchColPos The location of the matching column (e.g. exact or broad match) in CSV file
	 */
	private void noteEnrichedId(String[] instrumentsArr, String instrumentLine, 
			List<String> enrichedIds, Map<String,String> mapIdToLine, int idColPos, int matchColPos) {
		
		if (instrumentsArr != null && instrumentsArr.length > matchColPos && StringUtils.isNotBlank(instrumentsArr[idColPos]) 
				&& instrumentsArr[matchColPos] != null && StringUtils.isNotBlank(instrumentsArr[matchColPos])) {
			String instrumentIdStr = instrumentsArr[idColPos] + ID_DELIMITER 
					+ instrumentsArr[matchColPos];
			if (!enrichedIds.contains(instrumentIdStr)) {
				enrichedIds.add(instrumentIdStr);
				mapIdToLine.put(instrumentIdStr, instrumentLine);
			}
		}
	}
	
	
	/**
	 * This method extracts mapping IDs in form <EuropeanaId_matchLink> for given input CSV file. 
	 * @param filePath
	 * @param enrichedIds
	 * @param mapIdToLine
	 * @throws IOException
	 */
	private void extractMappingIds(String filePath, List<String> enrichedIds, Map<String,String> mapIdToLine) 
			 throws IOException {
		
		File enrichedFile = new File(filePath);
		List<String> instrumentLines = FileUtils.readLines(enrichedFile);
		for (String instrumentLine : instrumentLines.subList(1, instrumentLines.size())) {
			String[] instrumentsArr = instrumentLine.split(CSV_LINE_DELIMITER);
			noteEnrichedId(instrumentsArr, instrumentLine, enrichedIds, mapIdToLine
					, EUROPEANA_ID_COL_POS, EXACT_MATCH_COL_POS);
			noteEnrichedId(instrumentsArr, instrumentLine, enrichedIds, mapIdToLine
					, EUROPEANA_ID_COL_POS, BROAD_MATCH_COL_POS);
		}		
	}
	
	
	@Test
	public void mergeEnrichmentsAndRemoveOnbMimoMappingDuplicates() throws IOException, EuropeanaApiProblem {

		Map<String,String> mapIdToLine = new HashMap<String,String>();
		List<String> enrichedIds = new ArrayList<String>();

		// read variations and extract ID in form <EuropeanaId_matchLink>
		extractMappingIds(ENRICHED_INSTRUMENT_VARIATIONS_V1_FILE_PATH, enrichedIds, mapIdToLine); 
		
		// read shortenings and extract ID in form <EuropeanaId_matchLink>
		extractMappingIds(ENRICHED_INSTRUMENT_SHORTENINGS_V1_FILE_PATH, enrichedIds, mapIdToLine); 
		
		// write out existing input extentions in the output file
		File outputFile = new File(OUTPUT_ENRICHED_INSTRUMENT_LIST_FILE_PATH);	
		File inputEnrichedInstrumentsFile = new File(INPUT_ENRICHED_INSTRUMENT_LIST_FILE_PATH);
		List<String> inputEnrichedIds = new ArrayList<String>();
		List<String> enrichedInstrumentsLines = FileUtils.readLines(inputEnrichedInstrumentsFile);
		for (String enrichedInstrumentLine : enrichedInstrumentsLines.subList(1, enrichedInstrumentsLines.size()) ) {
			String[] enrichedInstrumentArr = enrichedInstrumentLine.split(CSV_LINE_DELIMITER);
			String enrichedInstrumentIdStr = enrichedInstrumentArr[EUROPEANA_ID_COL_POS] + ID_DELIMITER 
					+ enrichedInstrumentArr[EXACT_MATCH_COL_POS];
			if (!inputEnrichedIds.contains(enrichedInstrumentIdStr))
				inputEnrichedIds.add(enrichedInstrumentIdStr);			
		}
		
		// identifying by ID in form <EuropeanaId_matchLink>, write out additional mappings 
		// from variations and shortenings in output file
		for (String enrichedIdLine : enrichedIds) {
			if (!inputEnrichedIds.contains(enrichedIdLine)) {
				enrichedInstrumentsLines.add(mapIdToLine.get(enrichedIdLine));
			}
		}
		FileUtils.writeLines(outputFile, "UTF-8", enrichedInstrumentsLines);
	}
	
	
}
