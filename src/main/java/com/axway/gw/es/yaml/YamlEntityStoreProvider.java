package com.axway.gw.es.yaml;

import java.util.ArrayList;
import java.util.Collection;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreProvider;

import javax.annotation.Nullable;

public class YamlEntityStoreProvider extends EntityStoreProvider  {

	@Override
	@Nullable
	public EntityStore createStoreForURL(String url) {
		if (url.startsWith("yaml:"))  
			return new YamlEntityStore();           
		return null;
	}

	@Override
	@Nullable
	public Collection<String> getConnectionCredentialsForURL(String url) {
		if (url.startsWith("yaml:"))               
			return new ArrayList<>();
		return null;
	}
}
