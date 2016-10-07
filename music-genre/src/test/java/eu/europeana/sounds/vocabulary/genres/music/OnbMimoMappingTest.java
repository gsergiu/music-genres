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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import eu.europeana.search.connection.EuropeanaSearchApiClient;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.skos.BaseSkosTest;

/**
 * This class implements ONB MIMO mapping testing.
 */
public class OnbMimoMappingTest extends BaseSkosTest {

	
	String mappingFolder = "./src/test/resources/MIMO/onb/mapping";
	
	public final String VARIATIONS_INSTRUMENT_LIST_FILE_PATH =
			mappingFolder + "/ONB_MIMO_keyword_mapping_master_variations.csv";
	public final String ENRICHED_INSTRUMENT_VARIATIONS_FILE_PATH = 
			mappingFolder + "/Enriched_ONB_MIMO_keyword_mapping_master_variations.csv";
	

	EuropeanaSearchApiClient apiClient = new EuropeanaSearchApiClient();
	
	
	/**
	 * 
	 * ENRICH VARIATIONS FOR INSTRUMENT LIST
	 * 
	 * Having an ONB variations instrument list we match instrument data by music genre DDB IDs, 
	 * and enrich instrument list in CSV format, enriched matches. 
	 * @throws IOException
	 */
	@Test
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
	
	
}
