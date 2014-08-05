package eu.europeana.sounds.vocabulary.genres.music;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.freebase.connection.FreebaseApiClient;

public class FetchWikipediaTitle {

	//
	// String decriptionFolder =
	// "./src/test/resources/search/analysis/description";

	FreebaseApiClient apiClient = new FreebaseApiClient(
			FreebaseApiClient.DEFAULT_FREEBASE_MQLREAD_URI,
			null);
	
	BufferedWriter outFileWriter = null;

	// BufferedWriter genresFileWriter = null;
	// Buffered

	@Test
	public void fetchParentAndSubgenres() throws IOException {
		
		String[] mids = new String[]{"/m/0c0yr", "/m/0577x", "/m/08srqn",
				"/m/02ytbz", "/m/03rmvt", "/m/02yb7", "/m/06c87y","/m/07_mdk",
				"/m/05c4kxb","/m/020twq","/m/04jm4w","/m/01x2nx",
				"/m/0406kty","/m/053fhg","/m/059kh","/m/02vwm_","/m/03bldk","/m/0334f1","/m/030cx7",
				"/m/06zp5r","/m/05pd6","/m/0d6n1","/m/0m0gsw8","/m/080hjb5","/m/0c56g5","/m/0g965",
				"/m/027z2gp","/m/03clh74","/m/026bxb6","/m/03gsfyw","/m/070k2b","/m/02n15r",
				"/m/03cqtsq","/m/0710fh","/m/0197_n","/m/04y9r2s","/m/0199v5","/m/02r5pjq","/m/02v1zs","/m/0m0fmr9"};


				fetchWikipediaTitles(mids);
	}

	private void fetchWikipediaTitles(String[] mids)
			throws FileNotFoundException, IOException {
		// create parent dirs
		String json;
		String result;

			for (int i = 0; i < mids.length; i++) {
				result = null;
				json = apiClient.getWikipediaTitle(mids[i]);
				result = parseJsonResponse(json);
				System.out.println(result);
			}

	}

	

//	private void writeToOutput(File outFile, String line, String text)
//			throws IOException {
//
//		if (outFileWriter == null) {
//			// File outFile = new File(decriptionFolder, "others.csv");
//			// create parent dirs
//			outFile.getParentFile().mkdirs();
//			outFileWriter = new BufferedWriter(new FileWriter(outFile));
//		}
//
//		outFileWriter.append(line);
//		outFileWriter.append(";");
//		if (text != null)
//			outFileWriter.append(text.replace("\n", " <p> "));
//		outFileWriter.append("\n");
//
//	}

	private String parseJsonResponse(String json) throws UnsupportedEncodingException {
		String res = null;
		String mid = "";
		String name = "";
		String id = "";
		String wikipediaTitles = "";
		String encodedValue;
		String decodedValue;

		try {
			JsonParser parser = new JsonParser();
			JsonObject json_data = (JsonObject) parser.parse(json);

			JsonArray results = json_data.getAsJsonArray("result");
			if (results.size() > 0) {
				JsonObject firstResult = results.get(0).getAsJsonObject();
				mid = firstResult.get("mid").getAsString();
				name = firstResult.get("name").getAsString();
				id = firstResult.get("id").getAsString();
				JsonArray keys = firstResult
						.getAsJsonArray("key");
				if (keys != null) {
					JsonObject key;
					key = keys.get(0).getAsJsonObject();
						encodedValue = key.get("value").getAsString();
						decodedValue = decodeFreebaseEscape(encodedValue);
						wikipediaTitles += decodedValue;
						wikipediaTitles += ";";
						wikipediaTitles += "http://dbpedia.org/resource/"+decodedValue;
					
				}

				
			} else {
				System.err.println("wikipedia title not found: " + json);
			}

			res = mid + ";" + id + ";" + name + ";"
					+ wikipediaTitles;

		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			throw e;
		}

		return res;
	}

	private String decodeFreebaseEscape(String encodedValue) {
		String res = encodedValue.replace("$0028", "(");
		res=res.replace("$0027", "'");
		res=res.replace("$00E9", "é");
		res=res.replace("$00F3", "ó");
		res=res.replace("$014D", "ō");
		res=res.replace("$00F2", "ò");
		
		return res.replace("$0029", ")");
		
	}

	
}
