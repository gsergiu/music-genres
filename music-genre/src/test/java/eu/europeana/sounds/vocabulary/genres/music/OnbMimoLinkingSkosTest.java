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
import eu.europeana.sounds.skos.BaseSkosTest;

/**
 * This class implements different SKOS testing scenarios.
 */
public class OnbMimoLinkingSkosTest extends BaseSkosTest {

	public final String ONB_INSTRUMENTS_SKOS_FILE_PATH = 
			"C:/git/music-genres/music-genre/src/test/resources/MIMO/onb/Instruments of the ONB_list_SKOS.xml";

	EuropeanaSearchApiClient apiClient = new EuropeanaSearchApiClient();
	
	
	/**
	 * Having an ONB instrument list in SKOS format, we query Europeana Search API 
	 * for additional instrument data, store this data and create instrument list in CSV format, 
	 * enriched by Title, Description and Europeana ID. 
	 * This test employs Europeana search API to find ONB items that match the SKOS labels 
     * (preffered and alternative) in Title or Description (title and dcDescription).
	 * @throws IOException
	 */
	@Test
	public void mapONBMusicInstrumentsToEuropeanaSerachApi() throws IOException {
		
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(
    			ONB_INSTRUMENTS_SKOS_FILE_PATH);    	
		int numberOfMappedInstruments = apiClient.mapOnbMimo(concepts);
		
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
