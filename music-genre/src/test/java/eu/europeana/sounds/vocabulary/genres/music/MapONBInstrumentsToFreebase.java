package eu.europeana.sounds.vocabulary.genres.music;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import eu.europeana.freebase.connection.FreebaseApiClient;


public class MapONBInstrumentsToFreebase {

	FreebaseApiClient apiClient = new FreebaseApiClient();
	
	
	/**
	 * Having an ONB instrument list in CSV format, we query Freebase 
	 * for additional instrument data, store this data as JSON files
	 * and create instrument list in CSV format, enriched by MID and
	 * instument family. 
	 * @throws IOException
	 */
	@Test
	public void mapONBMusicInstrumentsToFreebase() throws IOException {
		
		int numberOfMappedInstruments = apiClient.mapONBInstruments();
		
		assertTrue(numberOfMappedInstruments > 0);
	}

}
