package com.axway.gw.es.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.es.FieldType;

public class Field {
    @JsonIgnore
    public String name;
    public String type;
    public String defaultValue;
    public String cardinality;
    public boolean isKey;

    public void setField(String name, FieldType ft) {
        this.name = name;
        String type = ft.getType();
        if (ft.isSoftRefType())
            type = type.replace("^", "@");  // no supporting softrefs in yaml es
        this.type = type;
        this.cardinality = ft.getCardinality().toString();
        this.defaultValue = ft.getDefault();
        if ("boolean".equals(type) && ft.getDefault() != null) { // why do types use true/false and entities sometimes 1/0 ?
			switch (ft.getDefault()) {
				case "0" : defaultValue = "false"; break;
				case "1" : defaultValue = "true"; break;
				default: defaultValue = ft.getDefault();
			}
        }
    }
}
