package eu.europeana.sounds.utils.concept;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import eu.europeana.sounds.definitions.model.WebAnnotationFields;
import eu.europeana.sounds.definitions.model.concept.Concept;
import eu.europeana.sounds.definitions.model.concept.impl.BaseConcept;
import eu.europeana.sounds.definitions.model.vocabulary.ConceptTypes;


/**
 * This class implements methods for working with SKOS. 
 *
 */
public class SkosUtils {
	
	private static Logger log = Logger.getRootLogger();

	String REGULAR_FREEBASE_PREFIX = "/m/";
	String FILE_FORM_FREEBASE_PREFIX = "m.";
	String WIKIDATA_ID_CLOSE_MATCH_KEY = "wikidataId_closeMatch";
	String WIKIDATA_BASE_URL = "https://www.wikidata.org/wiki/Q";
	String CLOSE_MATCH_PREDICATE_URL = "http://www.w3.org/2004/02/skos/core#closeMatch";
	String RDF_RES_FILE_NAME = "model.rdf";
	
	
    /**
     * This method performs parsing of the SKOS RDF in XML format to Europeana Annotation Concept object using Jena library.
     * @param inputFileName
     * @return Concept object
     */
    public Concept parseSkosRdfXmlToConcept(String inputFileName) {
    	
    	Model model = createModelFromRdfFile(inputFileName);

    	// write it to standard out
    	model.write(System.out);    
    	
    	// retrieve the statements from the model
    	return retrieveStatements(model);
    }
	
    
    /**
     * This method performs parsing of the SKOS RDF in XML format to Europeana Annotation Concept collection using Jena library.
     * @param inputFileName
     * @return A collection of the Concept objects
     */
    public List<Concept> parseSkosRdfXmlToConceptCollection(String inputFileName) {
    	
    	Model model = createModelFromRdfFile(inputFileName);

    	// write it to standard out
    	model.write(System.out);    
    	
    	// retrieve the statements from the model
    	return retrieveConcepts(model);
    }
	
    
    /**
     * This method performs parsing of the SKOS flat file in CSV format to 
     * Europeana Annotation Concept collection.
     * @param inputFileName
     * @return A collection of the Concept objects
     */
    public List<Concept> retrieveConceptsFromCsv(String inputFileName) {
	        
		List<Concept> res = new ArrayList<Concept>();
		
		int ID_POS = 1; 
		int LABEL_POS = 0; 
    	String splitBy = ";";
	    
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(inputFileName));
			String line = "";
			while ((line = br.readLine()) !=null) {
		    	BaseConcept concept = new BaseConcept();
		    	concept.addType(ConceptTypes.SKOS_CONCEPT.name());
			    String[] b = line.split(splitBy);
			    if (b.length >= 2 && StringUtils.isNotEmpty(b[ID_POS]) && StringUtils.isNotEmpty(b[LABEL_POS])) {
			    	String uri = b[ID_POS];
			    	concept.setUri(uri);
			    	String[] chunks = uri.split("/");
			    	String id = chunks[chunks.length-1];
				    concept.addPrefLabelInMapping(id, b[LABEL_POS]);
				    res.add(concept);
			    }
			}
		    br.close();
		} catch (FileNotFoundException e1) {
			log.error("File not found. " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			log.error("IO error. " + e.getMessage());
			e.printStackTrace();
		}

		return res;
	}    


	/**
	 * This method returns concept object if already in the list
	 * or creates new object if not exists.
	 * @param uri
	 * @param conceptList
	 * @return concept object
	 */
	Concept getConcept(String uri, List<Concept> conceptList) {
		
		BaseConcept concept = new BaseConcept();
		
		for (Concept curConcept : conceptList) {
			if (curConcept.getUri().equals(uri))
				return curConcept;
		}
		
    	concept.addType(ConceptTypes.SKOS_CONCEPT.name());
		return concept;
	}

	
	/**
	 * This method checks whether a given concept already exists in a passed list.
	 * @param concept
	 * @param conceptList
	 * @return true if exists, false if not
	 */
	boolean conceptExists(Concept concept, List<Concept> conceptList) {
		
		for (Concept curConcept : conceptList) {
			if (curConcept.getUri().equals(concept.getUri()))
				return true;
		}
		return false;
	}

	
    /**
     * This method performs parsing of the MIMO matches file in CSV format to 
     * Europeana Annotation Concept collection.
     * @param inputFileName
     * @return A collection of the Concept objects
     */
    public List<Concept> retrieveMatchesFromCsv(String inputFileName) {
	        
		List<Concept> res = new ArrayList<Concept>();
		
		int URI_POS = 0; 
		int EXACT_POS = 1; 
		int CLOSE_POS = 2; 
		int BROAD_POS = 3; 
		int NARROW_POS = 4; 
    	String splitBy = ";";
	    
	    BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(inputFileName));
			String line = br.readLine();			
			while ((line = br.readLine()) !=null) {
			    String[] b = line.split(splitBy);
			    if (b.length >= 1 && StringUtils.isNotEmpty(b[URI_POS])) {
			    	String uri = b[URI_POS];
			    	uri = uri.replace("#", "").replace(" ", "+");
			    	Concept concept = getConcept(uri, res);
			    	concept.setUri(uri);
//			    	String[] chunks = uri.split("/");
//			    	String id = chunks[chunks.length-1];
			    	String id = uri;
				    if (b.length > EXACT_POS && StringUtils.isNotEmpty(b[EXACT_POS]))
				    	concept.addExactMatchInMapping(id, b[EXACT_POS]);
				    if (b.length > CLOSE_POS && StringUtils.isNotEmpty(b[CLOSE_POS]))
				    	concept.addCloseMatchInMapping(id, b[CLOSE_POS]);
				    if (b.length > BROAD_POS && StringUtils.isNotEmpty(b[BROAD_POS]))
				    	concept.addBroadMatchInMapping(id, b[BROAD_POS]);
				    if (b.length > NARROW_POS && StringUtils.isNotEmpty(b[NARROW_POS]))
				    	concept.addNarrowMatchInMapping(id, b[NARROW_POS]);
				    if (!conceptExists(concept, res))
				    		res.add(concept);
			    }
			}
		    br.close();
		} catch (FileNotFoundException e1) {
			log.error("File not found. " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			log.error("IO error. " + e.getMessage());
			e.printStackTrace();
		}

		return res;
	}    

    
	/**
	 * @param orig
	 * @return
	 */
	public String setPredicate(String orig) {
		String res = "";
		res = matchPredicate(orig);
		return res;
	}
	
	/**
	 * The predicate value is obtained from RDF statement. This method retrieves the field value of the Concept object. E.g. 'prefLabel'.
	 * Sample: http://www.w3.org/2004/02/skos/core#prefLabel
	 * @param parsed predicate value
	 * @return field name of the Concept object
	 */
	private String matchPredicate(String value) {
		String res = "";
		String regex = "#(.*)$"; // we find any character sequence at the end of the line, after '#'
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
		    res = matcher.group(0).substring(1); // cut first element
		}
		return res;
	}
	
	
	/**
	 * @param concept
	 * @param predicate
	 * @param conceptFieldName
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void fillConcept(
			BaseConcept concept, String predicate, String conceptFieldName, String value) {

    	String methodName = WebAnnotationFields.ADD + conceptFieldName.substring(0, 1).toUpperCase() + conceptFieldName.substring(1);
    	String getMethodName = WebAnnotationFields.GET + conceptFieldName.substring(0, 1).toUpperCase() + conceptFieldName.substring(1);

    	Method method = null;
		Method getMethod = null;

	    try {
		    getMethod = concept.getClass().getMethod(getMethodName);
		    Class<?> returnType = getMethod.getReturnType();
		    if (returnType.equals(Map.class)) {
				method = concept.getClass().getMethod(methodName + WebAnnotationFields.IN_MAPPING, String.class, String.class);
			} else {
				method = concept.getClass().getMethod(methodName, String.class);
			}

		    if (method != null) {
			    if (returnType.equals(Map.class)) {
		    		Object valueMap = getMethod.invoke(concept);
		    		// counter is necessary to have different IDs in map for the same linguistic key
		    		int counter = 0;
		    		if (valueMap != null) {
		    			counter = ((Map<String,String>) valueMap).size();
		    		}
				    method.invoke(concept, predicate + counter, value);
		    	} else {
		  		    method.invoke(concept, value);            		
		    	}
		    }
		} catch (IllegalArgumentException e) {
        	log.error("IllegalArgumentException error by filling Concept object from SKOS RDF/XML model for Concept field: " + conceptFieldName);
		} catch (IllegalAccessException e) {
        	log.error("IllegalAccessException error by filling Concept object from SKOS RDF/XML model for Concept field: " + conceptFieldName);
		} catch (InvocationTargetException e) {
        	log.error("InvocationTargetException error by filling Concept object from SKOS RDF/XML model for Concept field: " + conceptFieldName);
		} catch (SecurityException e) {
        	log.error("SecurityException error by filling Concept object from SKOS RDF/XML model for Concept field: " + conceptFieldName);
		} catch (NoSuchMethodException e) {
        	log.error("NoSuchMethodException error by filling Concept object from SKOS RDF/XML model for Concept field: " + conceptFieldName);
        }
		
	}

	
	/**
	 * @param model
	 * @return
	 */
	public BaseConcept retrieveStatements(Model model) {
		StmtIterator itr = model.listStatements();
    	BaseConcept concept = new BaseConcept();
    	concept.addType(ConceptTypes.SKOS_CONCEPT.name());
    	    	    	
    	while (itr.hasNext()) {
    		Statement statement = itr.next();
        	Triple triple = statement.asTriple();
        	if (StringUtils.isEmpty(concept.getUri())) {
        		concept.setUri(triple.getSubject().toString());
        	}

        	String predicate = triple.getPredicate().toString();
        	String conceptFieldName = setPredicate(predicate);
        	
        	String value = triple.getObject().toString();
        	log.info("Statement: " + predicate + " = " + value);
        	
    		fillConcept(concept, predicate, conceptFieldName, value);
    	}
		return concept;
	}

	
	/**
	 * This method extracts FreebaseId from a Concept field 'closeMatch'.
	 * It takes first valid value from the closeMatch map.
	 * @param concept
	 * @return FreebaseId
	 */
	public String extractFreebaseIdFromConceptCloseMatch(Concept concept) {	
		String closeMatch = "";
		if (concept != null && concept.getCloseMatch() != null && concept.getCloseMatch().size() > 0) {
			for (Object value : concept.getCloseMatch().values()) {
				closeMatch = (String) value;
				break;
			}
		}
		String freebaseId = "";
		if (closeMatch != null && closeMatch.contains("/")) {
			String[] chunks = closeMatch.split("/");
			freebaseId = chunks[chunks.length-1];
		}
		return freebaseId;
	}
		
	
	/**
	 * This method extracts DBPedia ID from a Concept field 'exactMatch'.
	 * It takes first valid value from the closeMatch map.
	 * @param concept
	 * @return dbpediaId
	 */
	public String extractDBPediaIdFromConceptExactMatch(Concept concept) {	
		String exactMatch = "";
		if (concept != null && concept.getExactMatch() != null && concept.getExactMatch().size() > 0) {
			for (Object value : concept.getExactMatch().values()) {
				exactMatch = (String) value;
				break;
			}
		}
		String dbpediaId = "";
//		if (exactMatch != null && exactMatch.contains("/")) {
//			String[] chunks = exactMatch.split("/");
//			dbpediaId = chunks[chunks.length-1];
//		}
		if (exactMatch != null && exactMatch.contains("@")) {
			String[] chunks = exactMatch.split("@");
			dbpediaId = chunks[0].replace("\"", "");
		}
		return dbpediaId;
//		return exactMatch;
	}
	
	
	/**
	 * Text is separated by given splitter string.
	 * @param text
	 * @param splitter
	 * @return last chunk
	 */
	public String getLastChunk(String text, String splitter) {
		String lastChunk = "";
		if (text != null && text.contains(splitter)) {
			String[] chunks = text.split(splitter);
			lastChunk = chunks[chunks.length-1];
		}
        return lastChunk;
	}
		
	
	/**
	 * This method converts Freebase ID from the file name form e.g. 'm.xyz'
	 * into a regular Freebase ID form e.g. '/m/xyz'.
	 * @param freebaseId
	 * @return normalized Freebase ID
	 */
	public String normalizeFreebaseId(String freebaseId) {
		return freebaseId.replace(FILE_FORM_FREEBASE_PREFIX, REGULAR_FREEBASE_PREFIX);
	}
	

	/**
	 * This method converts Freebase ID from a regular Freebase ID form e.g. '/m/xyz' 
	 * into the file name form e.g. 'm.xyz'.
	 * @param freebaseId
	 * @return file name Freebase ID
	 */
	public String freebaseIdToFileName(String freebaseId) {
		return freebaseId.replace(REGULAR_FREEBASE_PREFIX, FILE_FORM_FREEBASE_PREFIX);
	}
	
	
	/**
	 * This method queries Wikidata by given FreebaseId and returns Wikidata ID.
	 *     e.g. https://wdq.wmflabs.org/api?q=string[646:%27/m/0557q%27]
	 *     where 646 is a number of Freebase ID property in Wikidata namespace.
	 * @param freebaseId
	 * @return Wikidata ID
	 */
	public String queryWikidataIdByFreebaseId(String freebaseId) {
		String wikidataId = "";
		return wikidataId;
	}

	
	/**
	 * This method retrieves concepts from a model.
	 * @param model
	 * @return
	 */
	public List<Concept> retrieveConcepts(Model model) {
		List<Concept> res = new ArrayList<Concept>();
		
		ResIterator itrConcepts = model.listSubjects();
		while (itrConcepts.hasNext()) {
			Resource rdfNode = itrConcepts.next();
        	System.out.println("rdfNode: " + rdfNode.toString());
        	Property p = null;
        	String object = null;
			StmtIterator itr = model.listStatements(rdfNode, p, object);

	    	BaseConcept concept = new BaseConcept();
	    	concept.addType(ConceptTypes.SKOS_CONCEPT.name());

	    	while (itr.hasNext()) {
	    		Statement statement = itr.next();
	        	Triple triple = statement.asTriple();
	        	if (StringUtils.isEmpty(concept.getUri())) {
	        		concept.setUri(triple.getSubject().toString());
	        	}
	
	        	String predicate = triple.getPredicate().toString();
	        	String conceptFieldName = setPredicate(predicate);
	        	
	        	String value; 
	        	
	        	if(triple.getObject().isLiteral())
	        		value = (String) triple.getObject().getLiteralValue();
	        	else 
	        		value = triple.getObject().toString(false);
	        	
	        	log.info("Statement: " + predicate + " = " + value);
	        	System.out.println("Statement: " + predicate + " = " + value);
	        	
	    		fillConcept(concept, predicate, conceptFieldName, value);
	    	}
	    	res.add(concept);
		}
		return res;
	}

	
	/**
	 * This method finds a concept by it's URL.
	 * @param concepts The list of concepts
	 * @param url The query URL
	 * @return concept object
	 */
	private Concept getConceptByUrl(List<Concept> concepts, String url) {
		Concept res = null;
		Iterator<Concept> itr = concepts.iterator();
		while (itr.hasNext()) {
			Concept concept = itr.next();
			if (concept.getUri().equals(url)) {
				res = concept;
			    break;
			}
		}
		return res;
	}
	
	
	/**
	 * This method adds concepts to a model retrieved from original RDF file 
	 * and stores it in enriched RDF file.
	 * @param concepts The list of concepts
	 * @param inputFileName The original RDF file
	 * @param pathToAnalysisFolder The location for enriched RDF file
	 */
	public void writeConceptsToRdf(List<Concept> concepts, String inputFileName, String pathToAnalysisFolder) {

    	Model model = createModelFromRdfFile(inputFileName);
		
		ResIterator itrConcepts = model.listSubjects();
		while (itrConcepts.hasNext()) {
			Resource rdfNode = itrConcepts.next();
        	System.out.println("rdfNode: " + rdfNode.toString());
        	Property p = null;
        	String object = null;
			StmtIterator itr = model.listStatements(rdfNode, p, object);
	    	
	    	while (itr.hasNext()) {
	    		Statement statement = itr.next();
	        	Triple triple = statement.asTriple();
	        	Concept curConcept = getConceptByUrl(concepts, triple.getSubject().toString());
		        if (curConcept.getCloseMatch() != null 
		        		&& StringUtils.isNotEmpty(curConcept.getCloseMatch().get(WIKIDATA_ID_CLOSE_MATCH_KEY))) {
    				// set subject, predicate, object
	        		Resource subject = statement.getSubject();
	        		Property predicate = ResourceFactory.createProperty(CLOSE_MATCH_PREDICATE_URL);
	        		Resource rdfObject = ResourceFactory.createResource(
	        				WIKIDATA_BASE_URL +
	        				curConcept.getCloseMatch().get(WIKIDATA_ID_CLOSE_MATCH_KEY));
	        		Statement newStatement = ResourceFactory.createStatement(subject, predicate, rdfObject);
       				model.add(newStatement);
       				break;
	        	}	
	    	}
		}
		
    	// write model to standard out
//    	model.write(System.out);
		writeModelToFile(model, pathToAnalysisFolder + RDF_RES_FILE_NAME);
	}


	/**
	 * This method saves updated Jena Model to a file
	 * @param model
	 * @param fileName
	 */
	public void writeModelToFile(Model model, String fileName) {
		FileWriter out = null;
		try {
			out = new FileWriter( fileName );
		    model.write( out, "RDF/XML-ABBREV" );
		} catch (IOException closeException) {
		       // ignore
		} finally {
		    try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	
	/**
	 * This method loads data from given RDF file in a model.
	 * @param inputFileName The path to the RDF file
	 * @return model
	 */
	private Model createModelFromRdfFile(String inputFileName) {
		// create an empty model
   	    Model model = ModelFactory.createDefaultModel();

    	// use the FileManager to find the input file
    	InputStream in = FileManager.get().open(inputFileName);
    	
    	if (in == null) {
    	    throw new IllegalArgumentException(
    	                                 "File: " + inputFileName + " not found");
    	}

    	// read the RDF/XML file
    	model.read(in, null);
		return model;
	}

	
}
