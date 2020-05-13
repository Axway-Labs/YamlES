package com.axway.gw.es.yaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YamlPKTest {

	@Test
	void testSimplePk() {
		String location = "API Manager Protection Policy";
		YamlPK yamlPk = new YamlPK(location);
		YamlPK yamlPk2 = new YamlPK(location);
		assertEquals(yamlPk, yamlPk2);
		assertEquals(yamlPk.hashCode(), yamlPk2.hashCode());
		assertEquals(yamlPk.getLocation(), location);
	}
	
	@Test
	void testWithParentPK() {
		String parentKeyLocation = "Parent PK Location";
		YamlPK parentPk = new YamlPK(parentKeyLocation);
		String childLocation = "Child Location";
		YamlPK yamlPk = new YamlPK(parentPk, childLocation);
		assertNotEquals(parentPk, yamlPk);
		assertTrue(yamlPk.getLocation().startsWith(parentKeyLocation));
		assertTrue(yamlPk.getLocation().endsWith(childLocation));
	}
	
	@Test
	void testPKNotEquals() {
		String locationA = "Location A";
		String locationB = "Location B";
		YamlPK yamlPkA = new YamlPK(locationA);
		YamlPK yamlPkB = new YamlPK(locationB);
		assertNotEquals(yamlPkA, yamlPkB);
	}

}
