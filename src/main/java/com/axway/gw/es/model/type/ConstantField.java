package com.axway.gw.es.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.es.ConstantFieldType;

public class ConstantField {
	@JsonIgnore
	public String name; 
	public String type; 
	public String value;

	public void setField(String name, ConstantFieldType ft) {
		this.name = name;
		this.value = ft.getDefaultValues().get(0).getData();
		this.type = ft.getType();
	}	
}
