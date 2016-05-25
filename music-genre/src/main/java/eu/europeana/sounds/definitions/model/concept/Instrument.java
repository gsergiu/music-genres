package eu.europeana.sounds.definitions.model.concept;

import java.util.List;

/**
 * This interface defines method for Instrument object.
 */
public interface Instrument {

	public String getName();
	
	public void setName(String name);

	public String getMid();

	public void setMid(String mid);

	public List<String> getInstrumentFamily();

	public void setInstrumentFamily(List<String> instrumentFamily);
	
}
