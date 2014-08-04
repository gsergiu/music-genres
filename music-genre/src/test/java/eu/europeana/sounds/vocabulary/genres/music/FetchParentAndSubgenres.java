package eu.europeana.sounds.vocabulary.genres.music;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.freebase.connection.FreebaseApiClient;

public class FetchParentAndSubgenres {

	//
	// String decriptionFolder =
	// "./src/test/resources/search/analysis/description";

	FreebaseApiClient apiClient = new FreebaseApiClient(
			FreebaseApiClient.DEFAULT_FREEBASE_MQLREAD_URI,
			FreebaseApiClient.DEFAULT_API_KEY);
	String MUSIC_GENRE = "/music/genre";

	BufferedWriter outFileWriter = null;

	// BufferedWriter genresFileWriter = null;
	// Buffered

	@Test
	public void fetchParentAndSubgenres() throws IOException {

		File input = new File(
				"./src/test/resources/analysis/description/in/genres_top_in.csv");
		File out = new File(
				"./src/test/resources/analysis/description/out/structure_top_out.csv");

		
		
		//		 File input = new File(
//		 "./src/test/resources/search/analysis/description/in/genres_0_in.csv");
//		 File out = new File(
//		 "./src/test/resources/search/analysis/description/out/structure_0_out.csv");

//		File input = new File(
//				"./src/test/resources/search/analysis/description/in/genres_1_in.csv");
//		File out = new File(
//				"./src/test/resources/search/analysis/description/out/structure_1_out.csv");

//		File input = new File(
//				"./src/test/resources/search/analysis/description/in/genres_2_in.csv");
//		File out = new File(
//				"./src/test/resources/search/analysis/description/out/structure_2_out.csv");

		fetchParentAndSubgenres(input, out);
	}

	private void fetchParentAndSubgenres(File input1, File out1)
			throws FileNotFoundException, IOException {
		// create parent dirs
		BufferedReader reader = null;
		String line;
		String label;
		String freebaseId;
		String json;
		String parentAndSubgenres;

		try {
			reader = new BufferedReader(new FileReader(input1));
			while ((line = reader.readLine()) != null) {
				// clear description
				parentAndSubgenres = null;

				label = line.split(";", 5)[0];
				freebaseId = extractMid(line);
				System.out.println(label + ":" + freebaseId);
				if (freebaseId != null && freebaseId.startsWith("/m/")) {
					json = apiClient.getJsonParentGenreAndSubgenres(label,
							freebaseId);
					parentAndSubgenres = extractParentAndSubgenres(json);
				}
				writeToOutput(out1, line, parentAndSubgenres);
			}

		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (outFileWriter != null) {
					outFileWriter.close();
					outFileWriter = null;
				}
			} catch (IOException e) {
				System.out.println("cannot close results writer for file: "
						+ input1);
			}
		}
	}

	private String extractMid(String line) {
		int startPos = 0;
		int endPos = 0;
		int lastCommaPos = -1;
		String mid = null;
		for (int i = 0; i < 5; i++) {
			lastCommaPos = line.indexOf(';', lastCommaPos + 1);
			if (i == 3)
				startPos = lastCommaPos;
			if (i == 4)
				endPos = lastCommaPos;
		}

		mid = line.substring(startPos + 1, endPos).trim();
		return mid;
	}

	private void writeToOutput(File outFile, String line, String text)
			throws IOException {

		if (outFileWriter == null) {
			// File outFile = new File(decriptionFolder, "others.csv");
			// create parent dirs
			outFile.getParentFile().mkdirs();
			outFileWriter = new BufferedWriter(new FileWriter(outFile));
		}

		outFileWriter.append(line);
		outFileWriter.append(";");
		if (text != null)
			outFileWriter.append(text.replace("\n", " <p> "));
		outFileWriter.append("\n");

	}

	private String extractParentAndSubgenres(String json) {
		String res = null;
		String parentGenres = "";
		String parentGenresIds = "";
		String subgenres = "";
		String subgenreIds = "";

		try {
			JsonParser parser = new JsonParser();
			JsonObject json_data = (JsonObject) parser.parse(json);

			JsonArray results = json_data.getAsJsonArray("result");
			if (results.size() > 0) {
				JsonObject firstResult = results.get(0).getAsJsonObject();
				JsonArray parentGenresArray = firstResult
						.getAsJsonArray("parent_genre");
				if (parentGenresArray != null) {
					JsonObject parentGenre;
					for (int i = 0; i < parentGenresArray.size(); i++) {
						parentGenre = (JsonObject) parentGenresArray.get(i);
						parentGenres += parentGenre.get("name").getAsString();
						parentGenres += ", ";
						parentGenresIds += parentGenre.get("id").getAsString();
						parentGenresIds += ", ";
					}
				}

				JsonArray subgenresArray = firstResult
						.getAsJsonArray("subgenre");
				if (subgenresArray != null) {
					JsonObject subGenre;
					for (int i = 0; i < subgenresArray.size(); i++) {
						subGenre = (JsonObject) subgenresArray.get(i);
						subgenres += subGenre.get("name").getAsString();
						subgenres += ", ";
						subgenreIds += subGenre.get("id").getAsString();
						subgenreIds += ", ";
					}
				}
			} else {
				System.err.println("not marked as music genre: " + json);
			}

			res = parentGenres + ";" + parentGenresIds + ";" + subgenres + ";"
					+ subgenreIds;

		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			throw e;
		}

		return res;
	}

	// private String missingGenres = "Rautalanka";

	// @Test
	// public void saveSearchResults() {
	// String[] queries = missingGenres.split(",");
	// String query = null;
	// for (int i = 0; i < queries.length; i++) {
	// query = queries[i];
	// try {
	// apiClient.saveSearchResults(query);
	// } catch (IOException e) {
	// System.out.println(query);
	// System.out.println(e.getMessage());
	// }
	// }
	// }
	//
	// // parse search results
	// @Test
	// public void parseSearchResults() {
	// String[] queries = missingGenres.split(",");
	// String query = null;
	// String json = null;
	// Map<String, String> results;
	// try {
	//
	// for (int i = 0; i < queries.length; i++) {
	// query = queries[i];
	//
	// json = apiClient.getSearchResultFromFile(query);
	// System.out.println(query);
	// results = parseResult(json);
	// if(results != null && !results.isEmpty())
	// writeToFiles(query, results);
	// else{
	// System.out.println("NOT FOUND: " + query);
	// }
	//
	// }
	// } catch (IOException e) {
	// System.out.println(query);
	// System.out.println(e.getMessage());
	// } finally{
	// closeWriters();
	// }
	// }
	//
	// private void writeToFiles(String query, Map<String, String> results)
	// throws IOException {
	// if (results.containsKey(MUSIC_GENRE))
	// writeToGenresFile(query, results);
	// else
	// writeToOthersFile(query, results);
	//
	// }
	//
	// private void writeToOthersFile(String query, Map<String, String> results)
	// throws IOException {
	//
	// if (othersFileWriter == null) {
	// File othersFile = new File(decriptionFolder, "others.csv");
	// // create parent dirs
	// othersFile.getParentFile().mkdirs();
	// othersFileWriter = new BufferedWriter(new FileWriter(othersFile));
	// }
	//
	// othersFileWriter.append(query);
	// othersFileWriter.append(";");
	// for (Map.Entry<String, String> entry : results.entrySet()) {
	// othersFileWriter.append(entry.getKey());
	// othersFileWriter.append(";");
	// othersFileWriter.append(entry.getValue());
	// }
	// othersFileWriter.append("\n");
	//
	// }
	//
	// private void writeToGenresFile(String query, Map<String, String> results)
	// throws IOException {
	// if (genresFileWriter == null) {
	// File genresFile = new File(decriptionFolder, "genres.csv");
	// // create parent dirs
	// genresFile.getParentFile().mkdirs();
	// genresFileWriter = new BufferedWriter(new FileWriter(genresFile));
	// }
	//
	// genresFileWriter.append(query);
	// genresFileWriter.append(";");
	//
	// String genres = results.get(MUSIC_GENRE);
	// genresFileWriter.append(genres);
	// genresFileWriter.append(";");
	// genresFileWriter.append(";");
	//
	// for (Map.Entry<String, String> entry : results.entrySet()) {
	// genresFileWriter.append(entry.getKey());
	// genresFileWriter.append(";");
	// genresFileWriter.append(entry.getValue());
	// }
	// genresFileWriter.append("\n");
	// }
	//
	// private void closeWriters() {
	// try {
	// genresFileWriter.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// try {
	// othersFileWriter.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// private Map<String, String> parseResult(String json) {
	// JsonParser parser = new JsonParser();
	// JsonObject json_data = (JsonObject) parser.parse(json);
	//
	// JsonArray results = (JsonArray) json_data.getAsJsonArray("result");
	// String mid;
	// String id;
	// String name;
	// String type;
	// JsonObject notable;
	//
	// Map<String, String> res = new HashMap<String, String>();
	// for (JsonElement jsonElement : results) {
	// if ((((JsonObject) jsonElement).get("mid")) != null)
	// mid = (((JsonObject) jsonElement).get("mid")).getAsString();
	// else
	// mid = null;
	//
	// if ((((JsonObject) jsonElement).get("id")) != null)
	// id = (((JsonObject) jsonElement).get("id")).getAsString();
	// else
	// id = null;
	//
	// if ((((JsonObject) jsonElement).get("name")) != null)
	// name = (((JsonObject) jsonElement).get("name")).getAsString();
	// else
	// name = null;
	//
	// if ((((JsonObject) jsonElement).get("notable")) != null) {
	// notable = (((JsonObject) jsonElement).get("notable"))
	// .getAsJsonObject();
	// type = notable.get("id").getAsString();
	// } else{
	// type = null;
	// }
	//
	// if (!res.containsKey(type))
	// res.put(type, name + "," + id + "," + mid);
	// else
	// res.put(type, res.get(type)+ "," + name + ", " + id + "," + mid);
	//
	// if (MUSIC_GENRE.equals(type))
	// System.out.println(id + "-" + mid);
	// }
	//
	// return res;
	// }

}
