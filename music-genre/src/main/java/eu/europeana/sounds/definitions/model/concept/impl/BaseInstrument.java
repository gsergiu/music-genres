package eu.europeana.sounds.definitions.model.concept.impl;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.sounds.definitions.model.WebAnnotationFields;
import eu.europeana.sounds.definitions.model.concept.Instrument;

/**
 * This is a class for Instrument object.
 */
public class BaseInstrument implements Instrument {

	private String name;
	private String mid;
	private List<String> instrumentFamily = new ArrayList<String>();

	
	public List<String> getInstrumentFamily() {
		return instrumentFamily;
	}

	public void setInstrumentFamily(List<String> instrumentFamily) {
		this.instrumentFamily = instrumentFamily;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof Instrument)) {
	        return false;
	    }

	    Instrument that = (Instrument) other;

	    boolean res = true;
	    
	    /**
	     * equality check for all relevant fields.
	     */
	    if ((this.getName() != null) && (that.getName() != null) &&
	    		(!this.getName().toString().equals(that.getName().toString()))) {
	    	System.out.println("Instrument objects have different 'Name' fields.");
	    	res = false;
	    }
	    
	    return res;
	}

	
	@Override
	public String toString() {
		String res = "\t### Instrument ###\n";
		
		if (getName() != null) 
			res = res + "\t\t" + WebAnnotationFields.NAME + ":" + getName() + "\n";
		return res;
	}	
}
