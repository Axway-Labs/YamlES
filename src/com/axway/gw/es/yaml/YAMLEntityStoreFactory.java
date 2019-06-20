package com.axway.gw.es.yaml;

import java.util.Collection;
import java.util.ServiceLoader;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;
import com.vordel.es.EntityStoreProvider;
import com.vordel.es.EntityTypeFactory;
import com.vordel.es.EntityTypeFactoryImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YAMLEntityStoreFactory {
	final static Logger logger = LoggerFactory.getLogger(YAMLEntityStoreFactory.class);

	public static final String DBG_ES_TYPE_FACTORY = "entityStore.typeFactory";

	private EntityTypeFactory etFactory;

	private static ServiceLoader<EntityStoreProvider> esLoader = ServiceLoader.load(EntityStoreProvider.class, YAMLEntityStoreFactory.class.getClassLoader());

	public static EntityStore createESForURL(String url) throws EntityStoreException {
		EntityStore es = createESFromServiceLoader(url);
		if (es == null)
			throw new EntityStoreException("No provider registered for URL: "+url);
		return es;
	}

	private static EntityStore createESFromServiceLoader(String url) {
		for (EntityStoreProvider p : esLoader) {
			EntityStore es = p.createStoreForURL(url);
			if (es != null)
				return es;
		}

		EntityStore es = new YAMLEntityStoreProvider().createStoreForURL(url);

		if (es != null)
			return es;

		return null;
	}

	private YAMLEntityStoreFactory() {}

	private static YAMLEntityStoreFactory instance;

	public static final YAMLEntityStoreFactory getInstance() {
		if (instance == null) {
			instance = new YAMLEntityStoreFactory();
		}
		return instance;
	}

	/**
	 * Retrieve an instance of an EntityStore which is compatible with
	 * the url specified
	 * @param url The connection string for a particular flavor of ES
	 * @return an EntityStore object
	 * @throws EntityStoreException if the factory was unable to find a suitable
	 * provider for the URL specified.
	 */
	public EntityStore getEntityStoreForURL(String url)
			throws EntityStoreException
	{
		if (url == null || "".equals(url))
			throw new IllegalArgumentException("URL must be non-null");

		EntityStore es = createESFromServiceLoader(url);
		if (es != null)
			return es;

		throw new EntityStoreException("No provider configured for URL \"" + url + "\"");
	}

	public String[] getPropsForURL(String url) {
		if (url == null)
			return null;
		for (EntityStoreProvider p : esLoader) {
			Collection<String> props = p.getConnectionCredentialsForURL(url);
			if (props != null)
				return props.toArray(new String[props.size()]);
		}
		return null;
	}

	public EntityTypeFactory getEntityTypeFactory() {
		if (etFactory == null) {
			String fc = System.getProperty(DBG_ES_TYPE_FACTORY);
			if (fc != null) {
				try {
					Class<?> c = Class.forName(fc);
					registerEntityTypeFactory((EntityTypeFactory)c.newInstance());
				} catch (Exception e) {
					logger.error("Couldn't create EntityType Factory '"+fc+"' - using default...",e);
				}
			}
			if (etFactory == null)
				registerEntityTypeFactory(new EntityTypeFactoryImpl());
		}
		return etFactory;
	}

	public void registerEntityTypeFactory(EntityTypeFactory f) {
		etFactory = f;
		logger.debug("Registered EntityTypeFactory: "+f.getClass().getName());
	}
}