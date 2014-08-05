package eu.europeana.sounds.vocabulary.genres.music;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.freebase.connection.FreebaseApiClient;

public class FetchFreebaseAliases {

	//
	// String baseFolder = "./src/test/resources/search/analysis/description";
	//

	FreebaseApiClient apiClient = new FreebaseApiClient(
			FreebaseApiClient.DEFAULT_FREEBASE_TOPIC_URI,
			null);

	BufferedWriter outFileWriter = null;

	// BufferedWriter genresFileWriter = null;
	// Buffered

	@Test
	public void fetchAliases() throws IOException {

		File input = new File(
				"./src/test/resources/analysis/description/in/voc_ansembled_mulitlingual_in.csv");
		File out = new File(
				"./src/test/resources/analysis/description/out/voc_ansembled_aliases_out.csv");

		// File input = new File(
		// "./src/test/resources/search/analysis/description/in/genres_0_in.csv");
		// File out = new File(
		// "./src/test/resources/search/analysis/description/out/genres_0_out.csv");

		// File input1 = new File(
		// "./src/test/resources/search/analysis/description/in/genres_2_in.csv");
		// File out1 = new File(
		// "./src/test/resources/search/analysis/description/out/genres_2_out.csv");

		fetchAliases(input, out);
	}

	private void fetchAliases(File input1, File out1)
			throws FileNotFoundException, IOException {
		// create parent dirs
		BufferedReader reader = null;
		String line;
		String label;
		String freebaseLabel;
		String[] aliases;
		String freebaseId;
		String[] input = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(input1), "UTF-8"));
			while ((line = reader.readLine()) != null) {
				// clear description
				aliases = null;
				if (line.isEmpty())
					continue;
				input = line.split(";", 3);
				label = input[0];
				freebaseLabel = input[1];
				freebaseId = input[2];

				System.out.println("id:" + freebaseId);
				if (freebaseId != null && freebaseId.startsWith("/m/")) {
					aliases = getAliases(label, freebaseLabel, freebaseId);
				}

				writeToOutput(out1, line, aliases);
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

	private String[] getAliases(String label, String freebaseLabel,
			String freebaseId) throws IOException {
		String json = apiClient.getJsonAliases(freebaseId);
		return extractAliases(label, freebaseLabel, json);
	}

	private void writeToOutput(File outFile, String line, String[] aliases)
			throws IOException {

		if (outFileWriter == null) {
			// File outFile = new File(decriptionFolder, "others.csv");
			// create parent dirs
			outFile.getParentFile().mkdirs();
			//outFileWriter = new BufferedWriter(new FileWriter(outFile));
			outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
		}

		outFileWriter.append(line);
		outFileWriter.append(";");
		if (aliases != null) {
			for (int i = 0; i < aliases.length; i++) {
				outFileWriter.append(aliases[i]);
				outFileWriter.append(";");
			}
		}

		outFileWriter.append("\n");

	}

	private String[] extractAliases(String label, String freebaseLabel,
			String json) {
		List<String> res = new ArrayList<String>();
		boolean isFreebaseLabelAnAlias = !label.equalsIgnoreCase(freebaseLabel);
		if (isFreebaseLabelAnAlias)
			res.add(freebaseLabel);

		try {
			JsonParser parser = new JsonParser();
			JsonObject json_data = (JsonObject) parser.parse(json);

			if (!json_data.get("result").isJsonNull()) {

				JsonObject results = json_data.getAsJsonObject("result");

				JsonArray values;

				values = results.getAsJsonArray("/common/topic/alias");

					String value;
					for (int i = 0; i < values.size(); i++) {
						value = ((JsonObject) values.get(i)).get("value")
								.getAsString();
						value = value.replaceAll("&amp;", "&");
						if (!label.equalsIgnoreCase(value))
							res.add(value);
					}
			}

		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			throw e;
		}

		return (String[]) res.toArray(new String[res.size()]);
	}

}
