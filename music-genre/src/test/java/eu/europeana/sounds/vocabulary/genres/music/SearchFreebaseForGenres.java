package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.freebase.connection.FreebaseApiClient;
import eu.europeana.sounds.definitions.model.WebAnnotationFields;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.skos.BaseSkosTest;

public class SearchFreebaseForGenres extends BaseSkosTest {

	public final String DUMP_FILE = "E:/freebase-rdf-latest/freebase-rdf-latest";
	public final String ONLY_COMPOSITIONS_CSV_FILE_PATH = "/only_classical_composition_types.csv";
	public final String ENRICHED_ONLY_COMPOSITIONS_CSV_FILE_PATH = "/enriched_only_classical_composition_types.csv";
	public final String FREEBASE_DUMP_OUTPUT_FILE_PATH = "/freebaseDumpOutput.txt";
	
	private String missingGenres = "Albazo,Aliwen,Ambient jazz,Arabesk,Ayarachi,Ayataki,Bailecito,Balada,"
			+ "Ballads,Ballet,Beguine,Boli,Bomba,Bomba del Chota,Cachua,Carnaval,Carnavalito,Cha-cha-chá,Chakri,"
			+ "Chamamé,Charleston,Chôro,Concert,Danzón,Dastgah,Epics,Festejo,Filin,Folk Blues,Forró,Fox incaico,"
			+ "Free improvised music,Fugue,Giddha,Golpe larense,Guaguancó,Guaranía,Gypsy music,Hora,Huayno,"
			+ "Jazz Rock,Jenkka,Jota,Jùjú,Kaluyo,Khoomi,Kpalongo,Kullawada,Kupletti,Laments,Landó,Lazgi,"
			+ "Ljuljane,Lullabies,Madrigal,Malouf,Manas-Epos,Mapalé,Maqam,Marinera,Mass,Min'ge,"
			+ "Mississippi Delta blues,Moda da viola,Motet,Music theatre,Música caipira,Música sertaneja,"
			+ "New Orleans,New wave,Nursery rhymes,Onestep,Pansori,Pasacalle,Polca,Punk,Rai,Rap,"
			+ "Rautalanka,Redoble,Revue,Rock steady,Rondo,Sanjuanito,Santiago,Saya,Sevillana,"
			+ "Shan'ge,Shimmy,Shomyo,Sicuri,Sinawe,Sonata,Soulblues,Symphony,Tambora,Taquirari,Tonada";

	String searchAnalysisFodler = "./src/test/resources/analysis";

	FreebaseApiClient apiClient = new FreebaseApiClient();
	String MUSIC_GENRE = "/music/genre";

	BufferedWriter othersFileWriter = null;
	BufferedWriter genresFileWriter = null;

	// private String missingGenres = "Rautalanka";

	// @Test
	public void saveSearchResults() {
		String[] queries = missingGenres.split(",");
		String query = null;
		for (int i = 0; i < queries.length; i++) {
			query = queries[i];
			try {
				apiClient.saveSearchResults(query, FreebaseApiClient.SEARCH_RESULTS_FOLDER);
			} catch (IOException e) {
				System.out.println(query);
				System.out.println(e.getMessage());
			}
		}
	}

	// parse search results
//	@Test
	public void parseSearchResults() {
		String[] queries = missingGenres.split(",");
		String query = null;
		String json = null;
		Map<String, String> results;
		try {

			for (int i = 0; i < queries.length; i++) {
				query = queries[i];

				json = apiClient.getSearchResultFromFile(query);
				System.out.println(query);
				results = parseResult(json);
				if(results != null && !results.isEmpty())
					writeToFiles(query, results);
				else{
					System.out.println("NOT FOUND: " + query);
				}

			}
		} catch (IOException e) {
			System.out.println(query);
			System.out.println(e.getMessage());
		} finally{
			closeWriters();
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

	
	/**
	 * Having a classical composition list in CSV format, we query Freebase dump 
	 * for additional data, store this data and extend composition list in CSV format, 
	 * enriched by Description, Freebase ID and preferred labels in different languages. 
	 * @throws IOException
	 */
//	@Test
	public void retrieveDescriptionsFromDump() throws IOException {
				
    	List<Concept> concepts = getSkosUtils().retrieveCompositionConceptsFromCsv(
    			searchAnalysisFodler + ONLY_COMPOSITIONS_CSV_FILE_PATH);    	

    	List<String> freebaseIdList = new ArrayList<String>();
    	Iterator<Concept> itrConcepts = concepts.iterator();
    	while (itrConcepts.hasNext()) {
			Concept concept = itrConcepts.next();
			String freebaseId = concept.getUri();
			System.out.println("freebaseId from CSV file: " + freebaseId);
			String queryFreebaseId = getSkosUtils().freebaseIdToFileName(freebaseId);
			freebaseIdList.add(queryFreebaseId);
		}

		Map<String, String> mapFreebaseIdToDescription = apiClient.queryDescriptionFromDump(freebaseIdList, DUMP_FILE);		    	
    	while (itrConcepts.hasNext()) {
			Concept concept = itrConcepts.next();
			String freebaseId = concept.getUri();
			String queryFreebaseId = getSkosUtils().freebaseIdToFileName(freebaseId);
			for (Map.Entry<String, String> entry : mapFreebaseIdToDescription.entrySet()) { 
				if (queryFreebaseId.equals(entry.getKey()) && entry.getValue().length() > 1) {
					concept.addDefinitionInMapping(getSkosUtils().EN, entry.getValue());
					System.out.println("Add Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
				}
			}
		}
		
		boolean res = getSkosUtils().generateCsvForConcepts(concepts, ONLY_COMPOSITIONS_CSV_FILE_PATH,
				ENRICHED_ONLY_COMPOSITIONS_CSV_FILE_PATH, searchAnalysisFodler);	    	    	
		assertTrue(res);
	}
	
	
//	@Test
	public void extractDBPediaIdFromFreebaseOutput() throws IOException {
	
    	List<Concept> concepts = getSkosUtils().retrieveCompositionConceptsFromCsv(
    			searchAnalysisFodler + ONLY_COMPOSITIONS_CSV_FILE_PATH);    	

    	List<String> freebaseIdList = new ArrayList<String>();
    	Iterator<Concept> itrConcepts = concepts.iterator();
    	while (itrConcepts.hasNext()) {
			Concept concept = itrConcepts.next();
			String freebaseId = concept.getUri();
			System.out.println("freebaseId from CSV file: " + freebaseId);
			String queryFreebaseId = getSkosUtils().freebaseIdToFileName(freebaseId);
			freebaseIdList.add(queryFreebaseId);
		}

    	int DESCRIPTION_POS = 2;
    	String splitBy = "\t";	    
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(searchAnalysisFodler + FREEBASE_DUMP_OUTPUT_FILE_PATH));
			String line = "";
			while ((line = br.readLine()) !=null) {
				for (String query : freebaseIdList) {
					if (line.contains("/" + query + ">") 
							&& line.contains("<http://rdf.freebase.com/key/wikipedia.en_id>")) { 
					    String[] b = line.split(splitBy);
						System.out.println("query: " + query + ", wikipedia ID: " + b[DESCRIPTION_POS] + ", line: " + line);
					}
				}
			}
		    br.close();
		} catch (FileNotFoundException e1) {
			System.out.println("File not found. " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO error. " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	
	@Test
	public void extractDBPediaDescriptionsFromFreebaseOutput() throws IOException {
	
    	List<Concept> concepts = getSkosUtils().retrieveCompositionConceptsFromCsv(
    			searchAnalysisFodler + ONLY_COMPOSITIONS_CSV_FILE_PATH);    	

    	List<String> freebaseIdList = new ArrayList<String>();
    	Iterator<Concept> itrConcepts = concepts.iterator();
    	while (itrConcepts.hasNext()) {
			Concept concept = itrConcepts.next();
			String freebaseId = concept.getUri();
			System.out.println("freebaseId from CSV file: " + freebaseId);
			String queryFreebaseId = getSkosUtils().freebaseIdToFileName(freebaseId);
			freebaseIdList.add(queryFreebaseId);
		}

		PrintWriter out = new PrintWriter("descriptions_en.txt");

    	int DESCRIPTION_POS = 2;
    	String splitBy = "\t";	    
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(searchAnalysisFodler + FREEBASE_DUMP_OUTPUT_FILE_PATH));
			String line = "";
			while ((line = br.readLine()) !=null) {
				for (String query : freebaseIdList) {
					if (line.contains("/" + query + ">") 
							&& line.contains("<http://rdf.freebase.com/ns/common.topic.description>") 
							&& line.contains("@en")) { 
					    String[] b = line.split(splitBy);
						out.println("query: " + query + ", wikipedia description: " + b[DESCRIPTION_POS]);
					}
				}
			}
		    br.close();
		    out.close();
		} catch (FileNotFoundException e1) {
			System.out.println("File not found. " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO error. " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}
