package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import eu.europeana.freebase.connection.FreebaseApiClient;


public class MapONBInstrumentsToFreebase {

	public final String VARIATIONS_INSTRUMENT_LIST_FILE_PATH = "Variations instruments of the ONB.csv";
	public final String INSTRUMENTS_FILE_EXTENDED = "Enriched variations instruments of the ONB list.csv";

	FreebaseApiClient apiClient = new FreebaseApiClient();
	
	
	/**
	 * MAIN INSTRUMENTS
	 * 
	 * Having an ONB instrument list in CSV format, we query Freebase 
	 * for additional instrument data, store this data as JSON files
	 * and create instrument list in CSV format, enriched by MID and
	 * instument family. 
	 * @throws IOException
	 */
//	@Test
	public void mapONBMusicInstrumentsToFreebase() throws IOException {
		
		int numberOfMappedInstruments = apiClient.mapONBInstruments();
		
		assertTrue(numberOfMappedInstruments > 0);
	}

	
	/**
	 * VARIATIONS OF INSTRUMENTS
	 * 
	 * Having an ONB instrument list in CSV format, we query Freebase 
	 * for additional instrument data, store this data as JSON files
	 * and create instrument list in CSV format, enriched by MID and
	 * instument family. 
	 * @throws IOException
	 */
	@Test
	public void mapVariationsOfONBMusicInstrumentsToFreebase() throws IOException {
		
		int numberOfMappedInstruments = apiClient.mapONBInstruments(
				VARIATIONS_INSTRUMENT_LIST_FILE_PATH, INSTRUMENTS_FILE_EXTENDED);
		
		assertTrue(numberOfMappedInstruments > 0);
	}

}
