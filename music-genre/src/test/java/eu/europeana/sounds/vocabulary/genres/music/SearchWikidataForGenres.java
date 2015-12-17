package eu.europeana.sounds.vocabulary.genres.music;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.skos.BaseSkosTest;
import eu.europeana.wikidata.connection.WikidataApiClient;

public class SearchWikidataForGenres extends BaseSkosTest {


	String searchAnalysisFodler = "./src/test/resources/analysis";

	WikidataApiClient apiClient = new WikidataApiClient();
	String MUSIC_GENRE = "/music/genre";

	BufferedWriter othersFileWriter = null;
	BufferedWriter genresFileWriter = null;

	

	/**
	 * This method reads XML entries from a file in a given folder TEST_RDF_VOCABULARY_FILE_PATH.
	 * For example, we use Music-Genres.rdf file in folder 'src/test/resources'.
	 * From result Freebase ID is extracted and normalized.
	 * Using extracted Freebase ID we search in Wikidata repository and store related JSON objects 
	 * in files in SEARCH_RESULTS_FOLDER. 
	 */
	@Test
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
	 * This method reads XML entries from RDF file. Also it extracts Wikidata IDs from 
	 * JSON files in the search results folder and maps them to related Freebase IDs.
	 * In the next step we enrich original XML files by extracted Wikidata IDs and 
	 * store the result in new RDF file in folder 'src/test/resources'.
	 */
//	@Test
	public void parseSearchResults() {
		String json = null;
		Map<String, String> results;
    	List<Concept> concepts = getSkosUtils().parseSkosRdfXmlToConceptCollection(TEST_RDF_VOCABULARY_FILE_PATH); 
    	Iterator<Concept> itrConcept = concepts.iterator();
    	while (itrConcept.hasNext()) {
			try {
		    	String freebaseId = getSkosUtils().extractFreebaseIdFromConceptCloseMatch(itrConcept.next());
		    	System.out.println(freebaseId);
				json = apiClient.getSearchResultFromFile(freebaseId);
				results = parseResult(json);
				if(results != null && !results.isEmpty())
					writeToFiles(freebaseId, results);
				else{
					System.out.println("NOT FOUND: " + freebaseId);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally{
				closeWriters();
			}
		}
	}

	private void writeToFiles(String query, Map<String, String> results)
			throws IOException {
		if (results.containsKey(MUSIC_GENRE))
			writeToGenresFile(query, results);
		else
			writeToOthersFile(query, results);

	}

	private void writeToOthersFile(String query, Map<String, String> results)
			throws IOException {

		if (othersFileWriter == null) {
			File othersFile = new File(searchAnalysisFodler, "others.csv");
			// create parent dirs
			othersFile.getParentFile().mkdirs();
			othersFileWriter = new BufferedWriter(new FileWriter(othersFile));
		}

		othersFileWriter.append(query);
		othersFileWriter.append(";");
		for (Map.Entry<String, String> entry : results.entrySet()) {
			othersFileWriter.append(entry.getKey());
			othersFileWriter.append(";");
			othersFileWriter.append(entry.getValue());
		}
		othersFileWriter.append("\n");

	}

	private void writeToGenresFile(String query, Map<String, String> results)
			throws IOException {
		if (genresFileWriter == null) {
			File genresFile = new File(searchAnalysisFodler, "genres.csv");
			// create parent dirs
			genresFile.getParentFile().mkdirs();
			genresFileWriter = new BufferedWriter(new FileWriter(genresFile));
		}

		genresFileWriter.append(query);
		genresFileWriter.append(";");
		
		String genres = results.get(MUSIC_GENRE);
		genresFileWriter.append(genres);
		genresFileWriter.append(";");
		genresFileWriter.append(";");
		
		for (Map.Entry<String, String> entry : results.entrySet()) {
			genresFileWriter.append(entry.getKey());
			genresFileWriter.append(";");
			genresFileWriter.append(entry.getValue());
		}
		genresFileWriter.append("\n");
	}

	private void closeWriters() {
		try {
			genresFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			othersFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, String> parseResult(String json) {
		JsonParser parser = new JsonParser();
		JsonObject json_data = (JsonObject) parser.parse(json);

		JsonArray results = (JsonArray) json_data.getAsJsonArray("result");
		String mid;
		String id;
		String name;
		String type;
		JsonObject notable;

		Map<String, String> res = new HashMap<String, String>();
		for (JsonElement jsonElement : results) {
			if ((((JsonObject) jsonElement).get("mid")) != null)
				mid = (((JsonObject) jsonElement).get("mid")).getAsString();
			else
				mid = null;

			if ((((JsonObject) jsonElement).get("id")) != null)
				id = (((JsonObject) jsonElement).get("id")).getAsString();
			else
				id = null;

			if ((((JsonObject) jsonElement).get("name")) != null)
				name = (((JsonObject) jsonElement).get("name")).getAsString();
			else
				name = null;

			if ((((JsonObject) jsonElement).get("notable")) != null) {
				notable = (((JsonObject) jsonElement).get("notable"))
						.getAsJsonObject();
				type = notable.get("id").getAsString();
			} else{
				type = null;
			}

			if (!res.containsKey(type))
				res.put(type, name + "," + id + "," + mid);
			else
				res.put(type, res.get(type)+ "," + name  + ", " + id + "," + mid);

			if (MUSIC_GENRE.equals(type))
				System.out.println(id + "-" + mid);
		}

		return res;
	}

}
