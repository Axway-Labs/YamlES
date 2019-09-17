package com.axway.gw.es.model.type;

import com.vordel.es.FieldType;

public class Field {
	public String name; 
	public String type; 
	private String defaultValue;
	public String cardinality = "1";
	public boolean isKey = false;

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public void setField(String name, FieldType ft) {
		this.name = name;
		this.defaultValue = ft.getDefault();
		String type = ft.getType();
		if (ft.isSoftRefType())
			type = type.replace("^", "@");  // no supporting softrefs in yaml es
		this.type = type;
		this.cardinality = ft.getCardinality().toString();
	}
}
