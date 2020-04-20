package com.axway.gw.es.yaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class YamlPKTest {

	@Test
	void testSimplePk() {
		String location = "API Manager Protection Policy";
		YamlPK yamlPk = new YamlPK(location);
		Assertions.assertEquals(yamlPk.hashCode(), location.hashCode());
		Assertions.assertEquals(yamlPk.getPath(), location);
	}
	
	@Test
	void testWithParentPK() {
		String parentKeyLocation = "Parent PK Location";
		YamlPK parentPk = new YamlPK(parentKeyLocation);
		String chidlLocation = "Child Location";
		YamlPK yamlPk = new YamlPK(parentPk, chidlLocation);
		Assertions.assertEquals(new String(parentKeyLocation + "/" + chidlLocation).hashCode(), yamlPk.hashCode());
		Assertions.assertFalse(parentPk.equals(yamlPk));
		Assertions.assertEquals(new String(parentKeyLocation + "/" + chidlLocation), yamlPk.getPath());
	}
	
	@Test
	void testPKEquals() {
		String locationA = "Location A";
		String locationB = "Location B";
		
		YamlPK yamlPkA = new YamlPK(locationA);
		YamlPK yamlPkB = new YamlPK(locationB);
		YamlPK yamlPkC = new YamlPK(locationA);
		
		Assertions.assertFalse(yamlPkA.equals(yamlPkB));
		Assertions.assertTrue(yamlPkA.equals(yamlPkC));
	}

}
