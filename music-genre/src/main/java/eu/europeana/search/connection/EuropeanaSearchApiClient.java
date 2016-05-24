package eu.europeana.search.connection;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.exception.EuropeanaApiProblem;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.model.search.EuropeanaApi2Item;
import eu.europeana.api.client.search.query.Api2QueryBuilder;
import eu.europeana.api.client.search.query.Api2QueryInterface;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.definitions.model.concept.impl.MimoMappingView;

public class EuropeanaSearchApiClient {

	private static final Log log = LogFactory.getLog(EuropeanaSearchApiClient.class);

//	public static final String TITLE_KEY = "title";
//	public static final String DESCRIPTION_KEY = "description";
//	public static final String EUROPEANA_ID_KEY = "europeanaId";

	public final static String ONB_INSTRUMENTS_FOLDER = "./src/test/resources/MIMO/onb";
	
	public final static String CONCEPTS_FILE_EXTENDED = "Linked instruments of the ONB-MIMO list.csv";
	
	public final static String CSV_DELIMITER = ";";
	 

    /**
     * @param query
     * @return
     * @throws IOException
     * @throws EuropeanaApiProblem
     */
    public List<Concept> loadConceptFromEuropeanaSearchApi(Concept queryConcept) throws IOException, EuropeanaApiProblem {

    	List<Concept> conceptList = new ArrayList<Concept>();
    	
    	String prefLabel = (String)queryConcept.getPrefLabel().values().toArray()[0];
    	
    	//		.replace("\"", "").replace(" ", "");
     	
		List<Concept> europeanaSearchApiResultsToConceptList = null;
		if (StringUtils.isNotEmpty(prefLabel)){
			europeanaSearchApiResultsToConceptList = parseEuropeanaSearchApiResultsToConcept(queryConcept, prefLabel);
    		conceptList.addAll(europeanaSearchApiResultsToConceptList);
		}
	    Iterator<Map.Entry<String,String>> it = queryConcept.getAltLabel().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String,String> pair = (Map.Entry<String,String>) it.next();
//	        System.out.println(pair.getKey() + " = " + pair.getValue());
			if (StringUtils.isNotEmpty(pair.getValue()))
				europeanaSearchApiResultsToConceptList = parseEuropeanaSearchApiResultsToConcept(queryConcept, prefLabel);
    		
				conceptList.addAll(europeanaSearchApiResultsToConceptList);
	        it.remove(); 
	    }
        return conceptList;
    }

    
	/**
	 * @param queryConcept The original concept object
	 * @param query A pariticular query string for search
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws EuropeanaApiProblem
	 */
	private List<Concept> parseEuropeanaSearchApiResultsToConcept(Concept queryConcept, String query)
			throws UnsupportedEncodingException, MalformedURLException, IOException, EuropeanaApiProblem {
		EuropeanaApi2Results results = searchMimoTermInEuropeanaCollection(query);
		
    	List<Concept> conceptList = new ArrayList<Concept>();

    	// parse results into concept objects
        for (EuropeanaApi2Item item : results.getAllItems()) {
        	MimoMappingView concept = new MimoMappingView();
        	concept.setUri(queryConcept.getUri());
        	concept.setPrefLabel(queryConcept.getPrefLabel());
        	concept.setAltLabel(queryConcept.getAltLabel());
        	
        	if (StringUtils.isNotEmpty(item.getTitle().get(0)))
        		concept.setTitle(item.getTitle());
        	if (StringUtils.isNotEmpty(item.getDcDescription().get(0)))
        		concept.setDcDescription(item.getDcDescription());
        	if (StringUtils.isNotEmpty(item.getId()))
        		concept.setEuropeanaId(item.getId());
            conceptList.add(concept);
		}
        return conceptList;
	}

    
	/**
	 * @param query
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws EuropeanaApiProblem
	 */
	private EuropeanaApi2Results searchMimoTermInEuropeanaCollection(String query)
			throws UnsupportedEncodingException, MalformedURLException, IOException, EuropeanaApiProblem {
		//create the query object
        EuropeanaApi2Client europeanaClient = new EuropeanaApi2Client();
		Api2QueryBuilder queryBuilder = europeanaClient.getQueryBuilder();
		String portalUrl = "http://www.europeana.eu/portal/search?q=";
				
		String toEncode ="(europeana_collectionName:(2059216_Ag_EU_eSOUNDS_1001_ONB) AND (title:\"" 
				+ query+ "\" OR proxy_dc_description:\"" + query+ "\"))";
		
		portalUrl += URLEncoder.encode(toEncode, "UTF-8");
		Api2QueryInterface apiQuery = queryBuilder.buildQuery(portalUrl);
		apiQuery.setProfile("rich");
		
		EuropeanaApi2Results results = europeanaClient.searchApi2(apiQuery, 0, -1);
		return results;
	}
    
    
    /**
     * Headers: Searched label; Skos_resource;title; description;europeanaId;
     * @param conceptList
     * @param sFileName
     */
    public void generateSearchConceptCsvFile(List<Concept> conceptList, String sFileName) {

    	try {
    		FileWriter writer = new FileWriter(ONB_INSTRUMENTS_FOLDER + "/" + sFileName);

    		writer.append("Searched label");
    		writer.append(CSV_DELIMITER);
    		writer.append("Skos_resource");
    		writer.append(CSV_DELIMITER);
    		writer.append("title");
    		writer.append(CSV_DELIMITER);
    		writer.append("description");
    		writer.append(CSV_DELIMITER);
    		writer.append("europeanaId");
    		writer.append('\n');

    		Iterator<Concept> itr = conceptList.iterator();
    		while (itr.hasNext()) {
    			MimoMappingView concept = (MimoMappingView) itr.next();
    			writer.append(concept.getPrefLabel().values().toString());
        		writer.append(CSV_DELIMITER);
    			writer.append(concept.getUri());
        		writer.append(CSV_DELIMITER);
        		writer.append(StringUtils.join(concept.getTitle(), ','));
//    			writer.append(concept.getNote().get(TITLE_KEY));
        		writer.append(CSV_DELIMITER);
        		writer.append(StringUtils.join(concept.getDcDescription(), ','));
//    			writer.append(concept.getNote().get(DESCRIPTION_KEY));
        		writer.append(CSV_DELIMITER);
        		writer.append(concept.getEuropeanaId());
//    			writer.append(concept.getNote().get(EUROPEANA_ID_KEY));
    			writer.append('\n');
    		}
    		writer.flush();
    		writer.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    
	/**
	 * This method maps ONB items to Europeana search API.
	 * @param conceptList
	 * @return Number of mapped concepts
	 * @throws IOException
	 */
	public int mapOnbMimo(List<Concept> conceptList) throws IOException {
		
		int mappedConceptCount = 0;
		
		List<Concept> enrichedConceptList = new ArrayList<Concept>();

		// load Europeana Search API Concept data and store it in CSV file
	    for (Concept concept : conceptList) {
		    try {
		    	List<Concept> enrichedConcept = loadConceptFromEuropeanaSearchApi(concept);
    			enrichedConceptList.addAll(enrichedConcept);
			} catch (Exception e) {
				log.error("Error by mapping ONB - MIMO using Europeana Search API" + e.getMessage());
			}
	    }
	
	    // parse mid and Concept family and store it in CSV comma separated files
	    generateSearchConceptCsvFile(enrichedConceptList, CONCEPTS_FILE_EXTENDED);

    	mappedConceptCount = enrichedConceptList.size();

	    return mappedConceptCount;
	}

}
