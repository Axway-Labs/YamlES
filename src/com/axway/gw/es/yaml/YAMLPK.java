package com.axway.gw.es.yaml;

import java.net.URL;

public class YAMLPK implements com.vordel.es.ESPK {

	private final String location;
	public YAMLPK(String location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		return location.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YAMLPK other = (YAMLPK) obj;
		if (location.equals(other.location))
			return true;
		return false;
	}

	public String toString() {
		return location.toString();
	}

	public String getPath() {
		return location;
	}
}
