package eu.europeana.skos;

import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.client.exception.TechnicalRuntimeException;

public class SkosClientConfiguration{

	private static final String SKOS_PROPERTIES_FILE = "/skos.properties";

	/**
	 * Accessor method for the singleton
	 * 
	 * @return
	 */
	public static synchronized SkosClientConfiguration getInstance() {
		singleton = new SkosClientConfiguration();
		singleton.loadProperties();
		return singleton;
	}
	
	//local attributes
	private static Properties properties = null;
	private static SkosClientConfiguration singleton;

	/**
	 * Hide the default constructor
	 */
	SkosClientConfiguration() {
	}

	/**
	 * Laizy loading of configuration properties
	 */
	public synchronized void loadProperties() {
		try {
			properties = new Properties();
			InputStream resourceAsStream = getClass().getResourceAsStream(
					SKOS_PROPERTIES_FILE);
			if (resourceAsStream != null)
				getProperties().load(resourceAsStream);
			else
				throw new TechnicalRuntimeException(
						"No properties file found in classpath! "
								+ SKOS_PROPERTIES_FILE);

		} catch (Exception e) {
			throw new TechnicalRuntimeException(
					"Cannot read configuration file: "
							+ SKOS_PROPERTIES_FILE, e);
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
		return SKOS_PROPERTIES_FILE;
	}


}
