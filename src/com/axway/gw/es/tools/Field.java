package com.axway.gw.es.tools;

import com.vordel.es.FieldType;

public class Field {
	public String name; 
	public String type; 
	private String defaultValue;
	public String cardinality = "1";
	public boolean isKey = false;
	
	String getDefault() {
		return defaultValue;
	}

	void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	void setField(String name, FieldType ft) {
		this.name = name;
		this.defaultValue = ft.getDefault();
		String type = ft.getType();
		if (ft.isSoftRefType())
			type = type.replace("^", "@");  // no supporting softrefs in yaml es
		this.type = type;
		this.cardinality = ft.getCardinality().toString();
	}
}
