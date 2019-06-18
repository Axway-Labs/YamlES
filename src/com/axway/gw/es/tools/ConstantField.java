package com.axway.gw.es.tools;

import com.vordel.es.ConstantFieldType;

public class ConstantField {
	public String name; 
	public String type; 
	public String value;
	
	void setField(String name, ConstantFieldType ft) {
		this.name = name;
		this.value = ft.getDefaultValues().get(0).getData();
		this.type = ft.getType();
	}	
}
