package eu.europeana.sounds.vocabulary.genres.music;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.dbpedia.connection.DBPediaApiClient;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.skos.BaseSkosTest;
import eu.europeana.wikidata.connection.WikidataApiClient;

public class SearchWikidataForGenres extends BaseSkosTest {


	String searchAnalysisFodler = "./src/test/resources/analysis";
	String WIKIDATA_ID_KEY = "wikidataId";

	WikidataApiClient apiClient = new WikidataApiClient();
	DBPediaApiClient dbpediaApiClient = new DBPediaApiClient();

	

	/**
	 * This method reads XML entries from a file in a given folder TEST_RDF_VOCABULARY_FILE_PATH.
	 * For example, we use Music-Genres.rdf file in folder 'src/test/resources'.
	 * From result Freebase ID is extracted and normalized.
	 * Using extracted Freebase ID we search in Wikidata repository and store related JSON objects 
	 * in files in SEARCH_RESULTS_FOLDER. 
	 */
//	@Test
	public void saveSearchResults() {
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(TEST_RDF_VOCABULARY_FILE_PATH); 
    	Iterator<Concept> itrConcept = concepts.iterator();
    	while (itrConcept.hasNext()) {
			try {
		    	String freebaseId = getSkosUtils().extractFreebaseIdFromConceptCloseMatch(itrConcept.next());
		    	System.out.println(freebaseId);
				apiClient.saveSearchResults(
						getSkosUtils().normalizeFreebaseId(freebaseId)
						, WikidataApiClient.SEARCH_RESULTS_FOLDER
				        , freebaseId);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}


	/**
	 * This method reads XML entries from a file in a given folder TEST_RDF_VOCABULARY_FILE_PATH.
	 * For example, we use Music-Genres.rdf file in folder 'src/test/resources'.
	 * From result DBPedia ID is extracted and normalized.
	 * Using extracted DBPedia ID we search in DBPedia repository and store related JSON objects 
	 * in files in SEARCH_RESULTS_FOLDER. 
	 */
	@Test
	public void saveDBPediaSearchResults() {
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(TEST_RDF_VOCABULARY_FILE_PATH); 
    	Iterator<Concept> itrConcept = concepts.iterator();
    	while (itrConcept.hasNext()) {
			try {
		    	String dbpediaId = getSkosUtils().extractDBPediaIdFromConceptExactMatch(itrConcept.next());
		    	System.out.println(dbpediaId);
				dbpediaApiClient.saveSearchResults(
						dbpediaId
						, DBPediaApiClient.SEARCH_RESULTS_FOLDER
				        , getSkosUtils().getLastChunk(dbpediaId, "/"));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}


	/**
	 * This method reads XML entries from RDF file. Also it extracts Wikidata IDs from 
	 * JSON files in the search results folder and maps them to related Freebase IDs.
	 * In the next step we enrich original XML files by extracted Wikidata IDs and 
	 * store the result in new RDF file in folder 'src/test/resources/analysis'.
	 */
	@Test
	public void parseSearchResults() {
		String json = null;
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(TEST_RDF_VOCABULARY_FILE_PATH); 
    	Iterator<Concept> itrConcept = concepts.iterator();
    	while (itrConcept.hasNext()) {
			try {
				Concept concept = itrConcept.next();
		    	String freebaseId = getSkosUtils().extractFreebaseIdFromConceptCloseMatch(concept);
		    	System.out.println(freebaseId);
				json = apiClient.getSearchResultFromFile(freebaseId);
				String wikidataId = parseResult(json);
				if(wikidataId != null && wikidataId.length() > 0)
					concept.addCloseMatchInMapping(WIKIDATA_ID_KEY, wikidataId);
				else{
					System.out.println("NOT FOUND: " + freebaseId);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
    	getSkosUtils().writeConceptsToRdf(concepts, TEST_RDF_VOCABULARY_FILE_PATH, searchAnalysisFodler);
	}

	
	/**
	 * This method extracts Wikidata ID from JSON file looking up in
	 * field 'items'
	 * @param json The JSON string
	 * @return Wikidata ID string
	 */
	private String parseResult(String json) {
		String res = "";
		JsonParser parser = new JsonParser();
		JsonObject json_data = (JsonObject) parser.parse(json);

		JsonArray results = (JsonArray) json_data.getAsJsonArray("items");
		if (results.size() > 0) {
			res = results.get(0).toString();
		}
		return res;
	}

}
