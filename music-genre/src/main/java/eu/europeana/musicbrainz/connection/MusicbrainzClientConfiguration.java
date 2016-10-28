package eu.europeana.musicbrainz.connection;

import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.client.exception.TechnicalRuntimeException;

public class MusicbrainzClientConfiguration{

	private static final String MUSIC_GENRES_PROPERTIES_FILE = "/music-genre.properties";
	private static final String PROP_WIKIDATA_API_KEY = "wikidata.apiKey";

	/**
	 * Accessor method for the singleton
	 * 
	 * @return
	 */
	public static synchronized MusicbrainzClientConfiguration getInstance() {
		singleton = new MusicbrainzClientConfiguration();
		singleton.loadProperties();
		return singleton;
	}
	
	//local attributes
		private static Properties properties = null;
		private static MusicbrainzClientConfiguration singleton;

		/**
		 * Hide the default constructor
		 */
		MusicbrainzClientConfiguration() {
		}

		/**
		 * Laizy loading of configuration properties
		 */
		public synchronized void loadProperties() {
			try {
				properties = new Properties();
				InputStream resourceAsStream = getClass().getResourceAsStream(
						MUSIC_GENRES_PROPERTIES_FILE);
				if (resourceAsStream != null)
					getProperties().load(resourceAsStream);
				else
					throw new TechnicalRuntimeException(
							"No properties file found in classpath! "
									+ MUSIC_GENRES_PROPERTIES_FILE);

			} catch (Exception e) {
				throw new TechnicalRuntimeException(
						"Cannot read configuration file: "
								+ MUSIC_GENRES_PROPERTIES_FILE, e);
			}

		}

		/**
		 * provides access to the configuration properties. It is not recommended to
		 * use the properties directly, but the
		 * 
		 * @return
		 */
		Properties getProperties() {
			return properties;
		}

		/**
		 * 
		 * @return the name of the file storing the client configuration
		 */
		String getConfigurationFile() {
			return MUSIC_GENRES_PROPERTIES_FILE;
		}

		/**
		 * This method provides access to the API key defined in the configuration
		 * file
		 * @see PROP_FREEBASE_API_KEY
		 * 
		 * @return
		 */
		public String getApiKey() {
			return getProperties().getProperty(PROP_WIKIDATA_API_KEY);
		}

}
