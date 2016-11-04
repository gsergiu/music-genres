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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import eu.europeana.sounds.skos.BaseSkosTest;


/**
 * This class implements ONB MIMO mapping testing.
 */
public class MimoJsonLdInstruments extends BaseSkosTest {

	
	int EUROPEANA_ID_COL_POS = 3;
	int EXACT_MATCH_COL_POS = 4;
	int BROAD_MATCH_COL_POS = 5;
	String ID_DELIMITER = "_";
	String CSV_LINE_DELIMITER = ";";
	
	String MIMO_BASE_URI = "http://data.mimo-db.eu/sparql/describe?uri=";

	String mimoFolder = "./src/test/resources/MIMO/onb/mapping";
	String downloadFolder = mimoFolder + "/instruments-jsonld/";
	
	public final String INPUT_ENRICHED_INSTRUMENT_LIST_FILE_V1_PATH = 
			mimoFolder + "/Enrichments_V1.csv";
	public final String INPUT_ENRICHED_INSTRUMENT_LIST_FILE_V2_PATH = 
			mimoFolder + "/Enrichments_V2.csv";
	
	protected Logger log = Logger.getLogger(getClass());

	
	/**
	 * 
	 * Download instruments in JSON-LD format
	 * 
	 * Having a MIMO exact and broad match links for instruments we download data in JSON-LD format. 
	 * @throws IOException
	 */
	@Test
	public void DownloadMimoInstrumentsInJsonLd() throws IOException {
		
		int EXACT_MATCH_COLUMN_POS = 4;
		int BROAD_MATCH_COLUMN_POS = 5;
		
		/**
		 * Read MIMO instrument IDs from exact and broad match URLs
		 */
		File enrichedFile = new File(INPUT_ENRICHED_INSTRUMENT_LIST_FILE_V1_PATH);
		List<String> instrumentLines = FileUtils.readLines(enrichedFile);
		File enrichedFile2 = new File(INPUT_ENRICHED_INSTRUMENT_LIST_FILE_V2_PATH);
		List<String> instrumentLines2 = FileUtils.readLines(enrichedFile2);
		instrumentLines.addAll(instrumentLines2.subList(1, instrumentLines2.size()));
		for (String instrumentLine : instrumentLines.subList(1, instrumentLines.size())) {
			queryMimoApiAndSaveResult(instrumentLine, EXACT_MATCH_COLUMN_POS);
			queryMimoApiAndSaveResult(instrumentLine, BROAD_MATCH_COLUMN_POS);
		}		

	}
	
	
	/**
	 * @param queryInstrumentLine
	 * @param columnPos
	 */
	private void queryMimoApiAndSaveResult(String queryInstrumentLine, int columnPos) {
		
		String[] instrumentsArr = queryInstrumentLine.split(CSV_LINE_DELIMITER);
		if (instrumentsArr != null && instrumentsArr.length > columnPos) {
			String instrumentIdUrl = instrumentsArr[columnPos];
			if (StringUtils.isNotEmpty(instrumentIdUrl)) {
				String[] instrumentIdArr = instrumentIdUrl.split("/");
				String instrumentId = instrumentIdArr[instrumentIdArr.length-1];
				if (!(new File("path/to/file.txt").isFile())) {
					HttpResponse response;
					try {
						response = queryMimoApi(instrumentIdUrl);
						String jsonLdString = readJsonHttpResponse(response);
						File recordFile = FileUtils.getFile(downloadFolder + instrumentId + ".json");
						FileUtils.writeStringToFile(
							recordFile
							, convertJsonStringToPrettyPrintJsonOutput(jsonLdString)
							, "UTF-8"
						);
					} catch (ClientProtocolException e) {
						log.error("Can't read MIMO API response." + e.getMessage());
					} catch (IOException e) {
						log.error("Can't read MIMO API response." + e.getMessage());
					}	
				}
			}
		}
		
	}
	
	
	/**
	 * This method queries MIMO API by ID.
	 * e.g. retrieve data by id e.g. http://data.mimo-db.eu/sparql/describe?
	 *     uri=http%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F2251&type=Resource&default-graph-uri=data
	 * @param id
	 * @return HttpResponse
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private HttpResponse queryMimoApi(String id) {
		
		HttpResponse response = null;
		
		try {
			String searchUrl = MIMO_BASE_URI;
			searchUrl += id.replace(" ", "");
			searchUrl += "&type=Resource&default-graph-uri=data";
			log.trace("Call to MIMO API: " + searchUrl);
		
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(searchUrl);
			httpGet.addHeader("Accept" , "application/ld+json");
	
			response = httpclient.execute(httpGet);
		} catch (ClientProtocolException e) {
			log.error("Can't read MIMO API response." + e.getMessage());
		} catch (IOException e) {
			log.error("Can't read MIMO API response." + e.getMessage());
		}	
		
		return response;
	}
	
	
	/**
	 * This methods reads JSON content from HTTP response.
	 * @param response
	 * @return JSON content string
	 * @throws IOException
	 */
	private String readJsonHttpResponse(HttpResponse response)
			throws IOException {
		
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			while ((line = reader.readLine()) != null)
				builder.append(line);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				log.warn("cannot close results writer for response: "
						+ response.toString());
			}
		}

		return builder.toString();
	}
	
	
	/**
	 * To indent JSON string, bind it as Object and write it output with indentation.
	 * @param input The input JSON string
	 * @return indented JSON string
	 */
	public String convertJsonStringToPrettyPrintJsonOutput(String input) {

		String res = input;
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(input, Object.class);
			res = mapper.defaultPrettyPrintingWriter().writeValueAsString(json);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	
}
