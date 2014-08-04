package eu.europeana.sounds.vocabulary.genres.music;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.europeana.freebase.connection.FreebaseApiClient;

public class FetchFreebaseMultilingualLabels {

	//
	// String baseFolder = "./src/test/resources/search/analysis/description";
	// af>Afrikaans; am>Amharic; ar>Arabic; eu>Basque; bn>Bengali; en-GB>British
	// English; bg>Bulgarian; fr-CA>Canadian French; ca>Catalan; zh-CN>Chinese;
	// zh-HK>Chinese (Hong Kong); zh-TW>Chinese (traditional); hr>Croatian;
	// cs>Czech; da>Danish; nl>Dutch; dz>Dzongkha; en selected=selected>English;
	// et>Estonian; fil>Filipino; fi>Finnish; fr>French; gl>Gallegan; de>German;
	// el>Greek; gu>Gujarati; iw>Hebrew; hi>Hindi; hu>Hungarian; pt-PT>Iberian
	// Portuguese; is>Icelandic; id>Indonesian; it>Italian; ja>Japanese;
	// kn>Kannada; km>Khmer; ko>Korean; lo>Lao; es-419>Latin American Spanish;
	// lv>Latvian; lt>Lithuanian; ms>Malay; ml>Malayalam; mr>Marathi; ne>Nepali;
	// no>Norwegian; fa>Persian; pl>Polish; pt>Portuguese; ro>Romanian;
	// ru>Russian; sr>Serbian; si>Sinhala; sk>Slovak; sl>Slovenian; es>Spanish;
	// sw>Swahili; sv>Swedish; ta>Tamil; te>Telugu; th>Thai; tr>Turkish;
	// en-US>U.S. English; uk>Ukrainian; ur>Urdu; vi>Vietnamese; zu>Zulu
	String[] languages = new String[] { "af", "am", "ar", "eu", "bn", "en-GB",
			"bg", "fr-CA", "ca", "zh-CN", "zh-HK", "zh-TW", "hr", "cs", "da",
			"nl", "dz", "en", "et", "fil", "fi", "fr", "gl", "de", "el", "gu",
			"iw", "hi", "hu", "pt-PT", "is", "id", "it", "ja", "kn", "km",
			"ko", "lo", "es-419", "lv", "lt", "ms", "ml", "mr", "ne", "no",
			"fa", "pl", "pt", "ro", "ru", "sr", "si", "sk", "sl", "es", "sw",
			"sv", "ta", "te", "th", "tr", "en-US", "uk", "ur", "vi", "zu" };

	List<String> languageCodes = Arrays.asList(languages);

	FreebaseApiClient apiClient = new FreebaseApiClient(
			FreebaseApiClient.DEFAULT_FREEBASE_TOPIC_URI,
			FreebaseApiClient.DEFAULT_API_KEY);

	BufferedWriter outFileWriter = null;

	// BufferedWriter genresFileWriter = null;
	// Buffered

	@Test
	public void fetchI18NLabels() throws IOException {

		File input = new File(
				"./src/test/resources/analysis/description/in/voc_ansembled_mulitlingual_in.csv");
		File out = new File(
				"./src/test/resources/analysis/description/out/voc_ansembled_mulitlingual_out.csv");

		// File input = new File(
		// "./src/test/resources/search/analysis/description/in/genres_0_in.csv");
		// File out = new File(
		// "./src/test/resources/search/analysis/description/out/genres_0_out.csv");

		// File input1 = new File(
		// "./src/test/resources/search/analysis/description/in/genres_2_in.csv");
		// File out1 = new File(
		// "./src/test/resources/search/analysis/description/out/genres_2_out.csv");

		fetchI18NLabels(input, out);
	}

	private void fetchI18NLabels(File input1, File out1)
			throws FileNotFoundException, IOException {
		// create parent dirs
		BufferedReader reader = null;
		String line;
		// String label;
		// String freebaseLabel;
		Map<String, String> i18nMap;
		String freebaseId;
		String[] input = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(input1), "UTF-8"));
			
			while ((line = reader.readLine()) != null) {
				// clear description
				i18nMap = null;
				if (line.isEmpty())
					continue;
				input = line.split(";", 3);
				// label = input[0];
				// freebaseLabel = input[1];
				freebaseId = input[2];

				System.out.println("id:" + freebaseId);
				if (freebaseId != null && freebaseId.startsWith("/m/")) {
					i18nMap = getI18NLabels(freebaseId);
				}

				writeToOutput(out1, line, i18nMap);
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

	private Map<String, String> getI18NLabels(String freebaseId)
			throws IOException {
		String json = apiClient.getJsonI18NLabels(freebaseId);
		return extractAliases(json);
	}

	private void writeToOutput(File outFile, String line,
			Map<String, String> i18nMap) throws IOException {

		if (outFileWriter == null) {
			// File outFile = new File(decriptionFolder, "others.csv");
			// create parent dirs
			outFile.getParentFile().mkdirs();
			outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
		}

		outFileWriter.append(line);
		outFileWriter.append(";");
		
		if(line.indexOf(";mid") > 0)
			appendHeader();
		else
			appendLabels(i18nMap);

	}

	private void appendLabels(Map<String, String> i18nMap) throws IOException {
		String value;
		for(String language : languageCodes){
			if (i18nMap != null){
				value = i18nMap.get(language);
				if(value!= null){
					value = value.replaceAll("&amp;", "&");
					outFileWriter.append(value);
				}
			}
			outFileWriter.append(";");
		}
		outFileWriter.append("\n");
	}

	private void appendHeader() throws IOException {
		for(String language : languageCodes){
			outFileWriter.append(language);
			outFileWriter.append(";");
		}
		outFileWriter.append("\n");
	}
	
	private Map<String, String> extractAliases(String json) {
		
		Map<String, String> res = null;
		
		try {
			JsonParser parser = new JsonParser();
			
			JsonObject json_data = (JsonObject) parser.parse(json);

			JsonObject result = json_data.getAsJsonObject("result");

			JsonArray values = result.getAsJsonArray("name");
			
			if(values.size() == 0)
				return null;
		
			res = new HashMap<String, String>();

			JsonObject labelObj;
			String language;
			String value;
			int langStartIndex = "/lang/".length();

			for (int i = 0; i < values.size(); i++) {
				labelObj = (JsonObject) values.get(i);
				language = labelObj.get("lang").getAsString();
				language = language.substring(langStartIndex);
				value = labelObj.get("value").getAsString();
				
				res.put(language, value);
			}

		} catch (RuntimeException e) {
			System.out.println("error in parsing json string: " + json);
			e.printStackTrace();
			throw e;
		}

		return res;
	}

}
