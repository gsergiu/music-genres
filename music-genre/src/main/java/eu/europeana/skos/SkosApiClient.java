package eu.europeana.skos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.europeana.sounds.definitions.model.concept.Concept;


/**
 * This class implements SKOS API 
 *  
 * @author GrafR
 *
 */
public class SkosApiClient {

	private static final Log log = LogFactory.getLog(SkosApiClient.class);

	final String lineBreak = "\n"; 
	final String tab = "\t"; 
	public final String EN = "en";
	public final String PREF_LABEL = "_prefLabel";
	public final String ALT_LABEL = "_altLabel";
		

	/**
	 * Create a new connection to the SKOS API.
	 */
	public SkosApiClient() {
	}

	
	/**
	 * This method creates SKOS RDF XML file for concepts and returns their number.
	 * @param conceptList
	 * @param recordFile
	 * @return number of concepts
	 * @throws IOException
	 */
	public int generateSkosRdfFile(List<Concept> conceptList, File recordFile) throws IOException {

		log.trace("Call to SKOS API");
		
		int res = 0;
		
		try {
			String header = new StringBuilder()
					.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append(lineBreak)
					.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"").append(lineBreak)
					.append(tab).append("xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"").append(lineBreak)
					.append(tab).append("xml:base=\"").append(conceptList.get(0).getInScheme().get(0))
					.append("\"").append(lineBreak)
					.append(tab).append(">").append(lineBreak)
					.toString();
			FileUtils.writeStringToFile(recordFile, header, "UTF-8", true);
					
			for (Concept concept : conceptList) {
				String row = new StringBuilder()
						.append(tab).append("<skos:Concept rdf:ID=\"").append(concept.getUri())
						.append("\">").append(lineBreak)
						.append(tab).append(tab).append("<skos:prefLabel>")
						.append(concept.getPrefLabel().get(EN + PREF_LABEL))
						.append("</skos:prefLabel>").append(lineBreak)
						.append(tab).append(tab).append("<skos:inScheme rdf:resource=\"").append(concept.getInScheme().get(0))
						.append("\"/>").append(lineBreak)
						.append(tab).append("</skos:Concept>").append(lineBreak)
						.toString();
				FileUtils.writeStringToFile(recordFile, row, "UTF-8", true);
				res = res + 1;	
			}

			String end = new StringBuilder().append("</rdf:RDF>").append(lineBreak).toString();
			FileUtils.writeStringToFile(recordFile, end, "UTF-8", true);
		} catch (Exception e) {
			log.error("Error by generating SKOS RDF XML file. " + e.getMessage());
		}
		return res;
	}
	
	
}
