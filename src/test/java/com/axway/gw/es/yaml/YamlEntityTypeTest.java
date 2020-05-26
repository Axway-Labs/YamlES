package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.vordel.es.*;
import com.vordel.es.impl.ConstantField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.vordel.es.EntityType.ZERO_OR_ONE;
import static com.vordel.es.FieldType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class YamlEntityTypeTest {

    private static final String testPackage = "/com/axway/gw/es/yaml/minimal";

    private File baseDir;
    private YamlEntityTypeImpEx yamlEntityTypeImpEx;

    @BeforeEach
    void  prepare() {
        baseDir = new File(YamlEntityStore.class.getResource(testPackage).getPath());
        yamlEntityTypeImpEx = new YamlEntityTypeImpEx();
    }

    @Test
    public void should_load_all_DTO() throws Exception {
        final TypeDTO typeDTO = yamlEntityTypeImpEx.readTypes(baseDir);
        assertThat(typeDTO).isNotNull();
        assertThat(typeDTO.getName()).isEqualTo("Entity");
        assertThat(typeDTO.getChildren()).isNotEmpty();
    }

    @Test
    public void should_load_all_type_through_ES() throws Exception {
        final YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.setRootLocation(baseDir);
        yamlEntityStore.loadTypes();
        EntityType entity = yamlEntityStore.getTypeForName("Entity");
        assertThat(entity).isNotNull();
        assertThat(entity).isInstanceOf(YamlEntityType.class);
        assertThat(entity.getSuperType()).isNull();
        EntityType entity2 = yamlEntityStore.retrieveType("Entity");
        assertThat(entity).isSameAs(entity2);
        assertThat(entity).isSameAs(yamlEntityStore.getBaseType());

        final EntityType filterCircuit = yamlEntityStore.getTypeForName("FilterCircuit");
        assertThat(filterCircuit).isNotNull();
        assertThat(filterCircuit.getFieldType(EntityType.NAME)).isNotNull();
        assertThat(filterCircuit.getFieldType(EntityType.NAME).getType()).isEqualTo(STRING);
        assertThat(filterCircuit.getConstantField(EntityType.VSTAMP)).isNotNull();
        assertThat(filterCircuit.getConstantField(EntityType.VSTAMP).getType()).isNotNull();
        assertThat(filterCircuit.getConstantField(EntityType.VSTAMP).getType().getType()).isEqualTo(INTEGER);
        assertThat(filterCircuit.getKeyFieldNames()).containsOnly(EntityType.NAME);
        assertThat(filterCircuit.getDeclaredComponentTypes()).isNotEmpty();
        assertThat(filterCircuit.getDeclaredComponentTypes().get("Filter")).isNotNull();
        assertThat(filterCircuit.getSuperType()).isNotNull();
        assertThat(filterCircuit.getSuperType().getSuperType()).isNotNull();
        assertThat(filterCircuit.getSuperType().getSuperType()).isSameAs(entity);

        assertThat(yamlEntityStore.getSubtypes("Entity")).isNotEmpty();
        assertThat(yamlEntityStore.getSubtypes("RootChild")).contains("FilterCircuit");
        assertThat(yamlEntityStore.hasType("FilterCircuit")).isTrue();


        final EntityType filterCircuit2 = yamlEntityStore.retrieveType("FilterCircuit");
        assertThat(filterCircuit).isSameAs(filterCircuit2);

        final YamlEntityType newType = new YamlEntityType();
        newType.setSuperType((YamlEntityType) yamlEntityStore.retrieveType("RendezvousDaemon"));
        newType.setName("MyType");
        newType.addFieldType("index", new YamlEntityTypeConverter.YamlFieldType(INTEGER, ONE, null));
        newType.addKeyFieldName("index");
        newType.addFieldType("description", new YamlEntityTypeConverter.YamlFieldType(STRING, ZERO_OR_ONE, null));
        newType.addConstantField(new ConstantField(new YamlEntityTypeConverter.YamlConstantFieldType(BOOLEAN, new Value("true")), "happy"));

        assertThat(yamlEntityStore.retrieveType("MyType")).isNull();
        yamlEntityStore.persistType(newType);
        EntityType myType = yamlEntityStore.retrieveType("MyType");
        assertThat(myType).isNotNull();
        assertThat(myType).isSameAs(newType);

        assertThat(myType.getAllDeclaredFieldNames()).hasSize(2);
        assertThat(myType.getKeyFieldNames()).containsExactlyInAnyOrder(EntityType.NAME, "index");
        assertThat(myType.getAllOptionalFieldNames()).containsOnly("description");
        assertThat(myType.getAllConstantFieldNames()).contains("happy");
        assertThat(myType.getAllFieldNames()).hasSize(6);
        assertThat(myType.getAllDefaultedFieldNames()).hasSize(4);

        yamlEntityStore.deleteType("MyType");
        assertThat(yamlEntityStore.retrieveType("MyType")).isNull();

    }

}
