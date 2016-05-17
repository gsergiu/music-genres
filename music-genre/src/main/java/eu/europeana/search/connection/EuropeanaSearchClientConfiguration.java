package eu.europeana.search.connection;

import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.client.exception.TechnicalRuntimeException;

public class EuropeanaSearchClientConfiguration{

	private static final String EUROPEANA_SEARCH_PROPERTIES_FILE = "/europeana-search.properties";
	private static final String PROP_EUROPEANA_SEARCH_COLLECTION = "search.collection";

	
	/**
	 * Accessor method for the singleton
	 * 
	 * @return
	 */
	public static synchronized EuropeanaSearchClientConfiguration getInstance() {
		singleton = new EuropeanaSearchClientConfiguration();
		singleton.loadProperties();
		return singleton;
	}
	
	//local attributes
		private static Properties properties = null;
		private static EuropeanaSearchClientConfiguration singleton;

		/**
		 * Hide the default constructor
		 */
		EuropeanaSearchClientConfiguration() {
		}

		/**
		 * Laizy loading of configuration properties
		 */
		public synchronized void loadProperties() {
			try {
				properties = new Properties();
				InputStream resourceAsStream = getClass().getResourceAsStream(
						EUROPEANA_SEARCH_PROPERTIES_FILE);
				if (resourceAsStream != null)
					getProperties().load(resourceAsStream);
				else
					throw new TechnicalRuntimeException(
							"No properties file found in classpath! "
									+ EUROPEANA_SEARCH_PROPERTIES_FILE);

			} catch (Exception e) {
				throw new TechnicalRuntimeException(
						"Cannot read configuration file: "
								+ EUROPEANA_SEARCH_PROPERTIES_FILE, e);
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
			return EUROPEANA_SEARCH_PROPERTIES_FILE;
		}

		/**
		 * This method provides path to the Europeana search collection defined in the configuration
		 * file
		 * @see PROP_EUROPEANA_SEARCH_COLLECTION
		 * 
		 * @return
		 */
		public String getCollectionPath() {
			return getProperties().getProperty(PROP_EUROPEANA_SEARCH_COLLECTION);
		}

}
