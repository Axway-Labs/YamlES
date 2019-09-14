package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import com.vordel.es.Value;

import java.util.Map;

public class EntityFactory {
	
	private static class MyEntity extends Entity {
		public MyEntity(EntityType type) {
			super(type);
		}
		public void setPK(ESPK pk) {
			this.pk = pk;
		}

		public void setParentPK(ESPK pk) {
			this.parentPK = pk;
		}
	}
	
	public static Entity convert(com.axway.gw.es.model.entity.Entity e, ESPK parentPK, YamlEntityStore es) {
		EntityType type = es.getTypeForName(e.meta.type);
		MyEntity entity = new MyEntity(type);
		
		// fields
		for (Map.Entry<String, String> field : e.fields.entrySet()) {
			if (!type.isConstantField(field.getKey())) { // don't set constants
				FieldType ft = type.getFieldType(field.getKey());
				if (ft.isRefType() || ft.isSoftRefType()) 
					entity.setReferenceField(field.getKey(), new YamlPK(field.getValue()));
				else  // set the value
					entity.setField(field.getKey(), new Value[] {new Value(field.getValue())});
			}
		}
		
		// pk
		ESPK pk = new YamlPK(parentPK, e.name);
		entity.setPK(pk);
		// parent pk
		//ESPK parentPK = new YamlPK(e.parent);
		entity.setParentPK(parentPK);
		return entity;
	}
}
