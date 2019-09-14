package com.axway.gw.es.model.type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.EntityType;

public class Type {
	private final static Logger LOGGER = Logger.getLogger(Type.class.getName());


	private String name;
	public String apiVersion = "http://www.vordel.com/2005/06/24/entityStore";
	public String parent; 
	public boolean isAbstract = false;
	public List<ConstantField> constants = new ArrayList<ConstantField>();
	public List<Field> fields = new ArrayList<Field>();
	public List<Child> component = new ArrayList<Child>();
	public List<String> pathToRoot = new ArrayList<String>();
	private List<Type> children = new ArrayList<Type>();
	
	public Type() {
	}
	
	public void addChild(Type t) {
		children.add(t);
	}
	@JsonIgnore
	public boolean hasChild() {
		return children.size() > 0;
	}
	@JsonIgnore
	public List<Type> getChildren() {
		return children;
	}
	
	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
	public Type(File f) throws JsonParseException, JsonMappingException, IOException {
		mapper.readerForUpdating(this).readValue(f);
	}
	
	public Type(String name) {
		this.name = name; 
	}
	
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}
	@JsonIgnore	
	public void setPathToRoot(EntityType et) {
		if (et == null)
			return;
		if (et.getSuperType() != null)
			pathToRoot.add(0, et.getSuperType().getName());
		setPathToRoot(et.getSuperType());
	}
}
