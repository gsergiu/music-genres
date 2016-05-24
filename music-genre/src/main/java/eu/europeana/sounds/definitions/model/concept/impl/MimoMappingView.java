package eu.europeana.sounds.definitions.model.concept.impl;

import java.util.List;

/**
 * This is a class for MIMO mapping view that extends base concept by
 * title and description lists and europeana ID fields.
 */
public class MimoMappingView extends BaseConcept {

    private List<String> title;
	private List<String> dcDescription;
	private String europeanaId;
			
	public List<String> getTitle() {
		return title;
	}

	public void setTitle(List<String> title) {
		this.title = title;
	}

	public List<String> getDcDescription() {
		return dcDescription;
	}

	public void setDcDescription(List<String> dcDescription) {
		this.dcDescription = dcDescription;
	}

	public String getEuropeanaId() {
		return europeanaId;
	}

	public void setEuropeanaId(String europeanaId) {
		this.europeanaId = europeanaId;
	}

	@Override
	public String toString() {
		String res = "\t### MIMO mapping view ###\n";
		
		if (getTitle() != null) 
			res = res + "\t\t" + "title:" + getTitle().get(0) + "\n";
		if (getEuropeanaId() != null) 
			res = res + "\t\t" + "europeana ID:" + getEuropeanaId() + "\n";
		return res;
	}	
}
