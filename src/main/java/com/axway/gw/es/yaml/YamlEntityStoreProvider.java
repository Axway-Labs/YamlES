package com.axway.gw.es.yaml;

import java.util.ArrayList;
import java.util.Collection;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreProvider;

public class YamlEntityStoreProvider extends EntityStoreProvider  {

	private static final Collection<String> connectionCredentials = new ArrayList<String>();  

	@Override
	public EntityStore createStoreForURL(String url) {
		if (url.startsWith("yaml:"))  
			return new YamlEntityStore();           
		return null;
	}

	@Override
	public Collection<String> getConnectionCredentialsForURL(String url) {
		if (url.startsWith("yaml:"))               
			return connectionCredentials;  
		return null;
	}
}
