package com.axway.gw.es.yaml;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;
import com.vordel.es.EntityStoreProvider;

import java.util.ServiceLoader;

public class YamlEntityStoreFactory {

	private static final ServiceLoader<EntityStoreProvider> STORE_PROVIDERS = ServiceLoader.load(EntityStoreProvider.class, YamlEntityStoreFactory.class.getClassLoader());

	public static EntityStore createESForURL(String url)  {
		EntityStore es = createESFromServiceLoader(url);
		if (es == null)
			throw new EntityStoreException("No provider registered for URL: "+url);
		return es;
	}

	private static EntityStore createESFromServiceLoader(String url) {
		for (EntityStoreProvider p : STORE_PROVIDERS) {
			EntityStore es = p.createStoreForURL(url);
			if (es != null)
				return es;
		}

		return new YamlEntityStoreProvider().createStoreForURL(url);
	}

	private YamlEntityStoreFactory() {}

	private static YamlEntityStoreFactory instance;

	public static YamlEntityStoreFactory getInstance() {
		if (instance == null) {
			instance = new YamlEntityStoreFactory();
		}
		return instance;
	}

}
