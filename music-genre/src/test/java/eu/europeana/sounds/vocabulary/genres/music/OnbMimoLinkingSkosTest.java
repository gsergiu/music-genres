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
import java.util.List;

import org.junit.Test;

import eu.europeana.search.connection.EuropeanaSearchApiClient;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.definitions.model.vocabulary.MatchTypes;
import eu.europeana.sounds.skos.BaseSkosTest;

/**
 * This class implements different SKOS testing scenarios.
 */
public class OnbMimoLinkingSkosTest extends BaseSkosTest {

	public final String ONB_INSTRUMENTS_SKOS_FILE_PATH = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Instruments of the ONB_list_SKOS.xml";
	public final static String CONCEPTS_FILE_EXTENDED = "Linked instruments of the ONB-MIMO list.csv";

	public final String ONB_INSTRUMENTS_SET_FLAT_ALL_CSV_FILE_PATH = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Instrument_set_flat_all_with_stamm.csv";
	public final String ENRICHED_ONB_INSTRUMENTS_SET_FLAT_ALL_CSV_FILE_PATH = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Enriched_instrument_set_flat_all_with_stamm.csv";
	public final String ONB_MIMO_MATCH_INSTRUMENTS_CSV_FILE_PATH = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/ONB_MIMO_match_instrument_set.csv";
	public final String ONB_MIMO_MAPPING_CSV_FILE_PATH =
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/ONB-MIMO-mapping.csv";

	public final String VARIATIONS_INSTRUMENT_LIST_FILE_PATH =
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Variations instruments of the ONB.csv";
	public final String ENRICHED_INSTRUMENT_VARIATIONS_FILE = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Enriched instrument variations of the ONB-MIMO list.csv";
	
	public final String SHORTENINGS_INSTRUMENT_LIST_FILE_PATH =
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Shortenings for instruments of the ONB.csv";
	public final String ENRICHED_INSTRUMENT_SHORTENINGS_FILE = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Enriched instrument shortenings of the ONB-MIMO list.csv";
	

	EuropeanaSearchApiClient apiClient = new EuropeanaSearchApiClient();
	
	
	/**
	 * 
	 * MAIN INSTRUMENTS LIST
	 * 
	 * Having an ONB all flat instrument list and file with matches (exact, close, narrow) in CSV format, 
	 * we match instrument data by music genre DDB IDs, and enrich instrument list in CSV format, 
	 * enriched matches. 
	 * @throws IOException
	 */
//	@Test
	public void matchONBFlatAllStemMusicInstruments() throws IOException {
		
		List<Concept> mimoConceptList = getSkosUtils().retrieveMatchesFromCsv(ONB_MIMO_MAPPING_CSV_FILE_PATH);
		
		
		int numberOfMappedInstruments = apiClient.matchOnbMimo(
				ENRICHED_ONB_INSTRUMENTS_SET_FLAT_ALL_CSV_FILE_PATH
				, mimoConceptList
				, ONB_MIMO_MATCH_INSTRUMENTS_CSV_FILE_PATH
				, MatchTypes.BROAD.name()
				);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
	/**
	 * 
	 * VARIATIONS INSTRUMENTS LIST
	 * 
	 * Having an ONB variations instrument list we match instrument data by music genre DDB IDs, 
	 * and enrich instrument list in CSV format, enriched matches. 
	 * @throws IOException
	 */
//	@Test
	public void matchONBInstrumentsVariations() throws IOException {
		
		List<Concept> mimoConceptList = getSkosUtils().retrieveConceptWithUriFromFile(VARIATIONS_INSTRUMENT_LIST_FILE_PATH);
		
		int numberOfMappedInstruments = apiClient.mapOnbMimo(mimoConceptList, ENRICHED_INSTRUMENT_VARIATIONS_FILE);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
	/**
	 * 
	 * SHORTENINGS FOR INSTRUMENT LIST
	 * 
	 * Having an ONB shortenings instrument list we match instrument data by music genre DDB IDs, 
	 * and enrich instrument list in CSV format, enriched matches. 
	 * @throws IOException
	 */
	@Test
	public void matchONBInstrumentsShortenings() throws IOException {
		
		List<Concept> mimoConceptList = getSkosUtils().retrieveConceptWithUriFromFile(SHORTENINGS_INSTRUMENT_LIST_FILE_PATH);
		
		int numberOfMappedInstruments = apiClient.mapOnbMimo(mimoConceptList, ENRICHED_INSTRUMENT_SHORTENINGS_FILE);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
	/**
	 * Having an ONB all flat instrument list in CSV format, we query Europeana Search API 
	 * for additional instrument data, store this data and create instrument list in CSV format, 
	 * enriched by Title, Description and Europeana ID. 
	 * This test employs Europeana search API to find ONB items that match the SKOS labels 
     * (preffered and alternative) in Title or Description (title and dcDescription).
	 * @throws IOException
	 */
//	@Test
	public void mapONBFlatAllMusicInstrumentsToEuropeanaSerachApi() throws IOException {
		
    	List<Concept> concepts = getSkosUtils().retrieveConceptsFromCsv(
    			ONB_INSTRUMENTS_SET_FLAT_ALL_CSV_FILE_PATH);    	
		int numberOfMappedInstruments = apiClient.mapOnbMimo(concepts, ENRICHED_ONB_INSTRUMENTS_SET_FLAT_ALL_CSV_FILE_PATH);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
	/**
	 * Having an ONB instrument list in SKOS format, we query Europeana Search API 
	 * for additional instrument data, store this data and create instrument list in CSV format, 
	 * enriched by Title, Description and Europeana ID. 
	 * This test employs Europeana search API to find ONB items that match the SKOS labels 
     * (preffered and alternative) in Title or Description (title and dcDescription).
	 * @throws IOException
	 */
//	@Test
	public void mapONBMusicInstrumentsToEuropeanaSerachApi() throws IOException {
		
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(
    			ONB_INSTRUMENTS_SKOS_FILE_PATH);    	
		int numberOfMappedInstruments = apiClient.mapOnbMimo(concepts, CONCEPTS_FILE_EXTENDED);
		
		assertTrue(numberOfMappedInstruments > 0);
	}
	
	
    /**
     * This test parses ONB intrument file into Concept objects.
     */
//    @Test
    public void testParseOnbInstrumentsFile() {
    	
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(ONB_INSTRUMENTS_SKOS_FILE_PATH);    	
    	assertTrue(concepts.size() > 0);
    }

    
}
