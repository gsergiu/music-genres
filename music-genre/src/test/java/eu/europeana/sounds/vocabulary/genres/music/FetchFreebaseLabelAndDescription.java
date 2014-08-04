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

public class FetchFreebaseLabelAndDescription {

	//
	//String decriptionFolder = "./src/test/resources/search/analysis/description";
	//
	
	FreebaseApiClient apiClient = new FreebaseApiClient(
			FreebaseApiClient.DEFAULT_FREEBASE_TOPIC_URI,
			FreebaseApiClient.DEFAULT_API_KEY);
	String MUSIC_GENRE = "/music/genre";

	BufferedWriter outFileWriter = null;

	// BufferedWriter genresFileWriter = null;
	// Buffered

	@Test
	public void fetchLabelsAndDescriptions() throws IOException {

		File input = new File(
				"./src/test/resources/analysis/description/in/voc_ansembled_mids.csv");
		File out = new File(
				"./src/test/resources/analysis/description/out/voc_ansembled_mids_out.csv");
		
//		File input = new File(
//				"./src/test/resources/search/analysis/description/in/genres_0_in.csv");
//		File out = new File(
//				"./src/test/resources/search/analysis/description/out/genres_0_out.csv");

//		File input1 = new File(
//		"./src/test/resources/search/analysis/description/in/genres_2_in.csv");
//		File out1 = new File(
//		"./src/test/resources/search/analysis/description/out/genres_2_out.csv");

		fetchLablesAndDescriptions(input, out);
	}

	private void fetchLablesAndDescriptions(File input1, File out1)
			throws FileNotFoundException, IOException {
		// create parent dirs
		BufferedReader reader = null;
		String line;
		String label;
		String freebaseId;
		String description;

		try {
			reader = new BufferedReader(new FileReader(input1));
			while ((line = reader.readLine()) != null) {
				// clear description
				description = null;
				label = null;
				if(line.isEmpty())
					continue;
				freebaseId = line.split(";", 3)[1];
				
				System.out.println( "id:" + freebaseId);
				if (freebaseId != null && freebaseId.startsWith("/m/")) {
					label = getLabel(freebaseId);
					description = getDescription(freebaseId);
				}
				
				writeToOutput(out1, line, label, description);
			}

		} finally {
			try {
				if (reader != null){
					reader.close();
					reader = null;
				}
				if (outFileWriter != null){
					outFileWriter.close();
					outFileWriter = null;
				}
			} catch (IOException e) {
				System.out.println("cannot close results writer for file: "
						+ input1);
			}
		}
	}

	private String getDescription(String freebaseId) throws IOException {
		String json = apiClient.getJsonDescription(freebaseId);
		return extractDescription(json);
	}

	private String getLabel(String freebaseId) throws IOException {
		String json = apiClient.getJsonLabel(freebaseId);
		return extractLabel(json);
	}

	private void writeToOutput(File outFile, String line, String label, String description)
			throws IOException {

		if (outFileWriter == null) {
			//File outFile = new File(decriptionFolder, "others.csv");
			// create parent dirs
			outFile.getParentFile().mkdirs();
			outFileWriter = new BufferedWriter(new FileWriter(outFile));
		}

		outFileWriter.append(line);
		outFileWriter.append(";");
		outFileWriter.append(label);
		outFileWriter.append(";");
		if(description != null){
			description = description.replace("\n", " <p> ");
			description = description.replace(";", ",");
		}
		
		outFileWriter.append(description);
		outFileWriter.append("\n");

	}
	
	private String extractLabel(String json) {
		String res = null;

		try {
			JsonParser parser = new JsonParser();
			JsonObject json_data = (JsonObject) parser.parse(json);

			JsonObject results = (JsonObject) json_data.get("property");
			JsonObject descriptionObj = (JsonObject) results
					.get("/type/object/name");
			JsonArray values = descriptionObj.getAsJsonArray("values");

			res = ((JsonObject) values.get(0)).get("text").getAsString();
		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			throw e;
		}

		return res;
	}

	private String extractDescription(String json) {
		String res = null;

		try {
			JsonParser parser = new JsonParser();
			JsonObject json_data = (JsonObject) parser.parse(json);

			JsonObject results = (JsonObject) json_data.get("property");
			JsonObject descriptionObj = (JsonObject) results
					.get("/common/topic/description");
			JsonArray values = descriptionObj.getAsJsonArray("values");

			res = ((JsonObject) values.get(0)).get("value").getAsString();
		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			//throw e;
			res="";
		}

		return res;
	}

	

}
