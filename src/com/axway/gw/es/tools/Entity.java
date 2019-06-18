package com.axway.gw.es.tools;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Entity {
	public String key;
	public String parent; 
	public String type;
	public List<EntityField> fields = new ArrayList<EntityField>();
	
	@JsonIgnore
	public boolean allowsChildren = false;
	@JsonIgnore 
	public String keyFieldValues;
}
