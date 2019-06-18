package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import com.vordel.es.Value;

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
	
	public static Entity convert(com.axway.gw.es.tools.Entity e, ESPK parentPK, YamlEntityStore es) {
		EntityType type = es.getTypeForName(e.type);
		MyEntity entity = new MyEntity(type);
		
		// fields
		for (com.axway.gw.es.tools.EntityField field : e.fields) {
			if (!type.isConstantField(field.name)) { // don't set constants 
				FieldType ft = type.getFieldType(field.name);
				if (ft.isRefType() || ft.isSoftRefType()) 
					entity.setReferenceField(field.name, new YAMLPK(field.value));
				else  // set the value
					entity.setField(field.name, new Value[] {new Value(field.value)});
			}
		}
		
		// pk
		ESPK pk = new YAMLPK(e.key);
		entity.setPK(pk);
		// parent pk
		//ESPK parentPK = new YAMLPK(e.parent);
		entity.setParentPK(parentPK);
		return entity;
	}
}
