package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.dto.entity.EntityDTO;
import com.vordel.es.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.axway.gw.es.yaml.YamlEntityStore.YAML_MAPPER;
import static com.axway.gw.es.yaml.utils.ESTestsUtil.getFileFromClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;

public class YamlEntityStoreRefsTest {

    // At this place, also the META-INF/Types.yaml is expected
    private static final String testPackage = "/com/axway/gw/es/yaml/refs/";

    private YamlEntityStore yamlEntityStore;

    @BeforeEach
    void setupEntityStoreToTest() throws SecurityException, IllegalArgumentException, EntityStoreException, IOException {
        yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.setRootLocation(new File(YamlEntityStore.class.getResource(testPackage).getPath()));
        // Entity need to load types to be able to create an Entity
        yamlEntityStore.loadTypes();
        yamlEntityStore.loadEntities();
        assertThat(yamlEntityStore.getRootPK()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "policies/API Manager Protection Policy.yaml,Policies/API Manager Protection Policy,Disable Monitoring,11,SetAttributeFilter",
            "policies/oauth20/Access Token Service.yaml,Policies/Access Token Service,Decide what grant type to use,1,SwitchFilter",
            "policies/oauth20/Client Credentials.yaml,Policies/Client Credentials,Access Token using client credentials,1,ClientCredentialsFilter",
            "policies/oauth20/Refresh.yaml,Policies/Refresh,Refresh Access token,1,RefreshingAccessTokenFilter",
            "policies/oauth20/Resource Owner Password Credentials.yaml,Policies/Resource Owner Password Credentials,Resource Owner Password Credentials,1,ResourceOwnerPasswordCredentialsFilter",
            "policies/oauth20/SAML.yaml,Policies/SAML,Access token using SAML Assertion,1,SAMLBearerAssertionGrantFilter",
            "policies/oauth20/Verify SAML Signature.yaml,Policies/Verify SAML Signature,XML Signature Verification,1,IntegrityVerifySignatureFilter"
    })
    public void ref_should_be_expended_from_DTO(String dtoFile, String entityPK, String startFieldShortRef, int childrenCount, String startNodeTargetType) throws IOException {

        // get entity in ES
        final Entity entity = yamlEntityStore.getEntity(new YamlPK(entityPK));
        assertThat(entity).isNotNull();

        // check the number of children
        assertThat(yamlEntityStore.findChildren(entity.getPK(), null, null)).hasSize(childrenCount);

        // load equivalent DTO, check start has a "short" name ref
        EntityDTO entityDTO = YAML_MAPPER.readValue(getFileFromClasspath(testPackage, dtoFile), EntityDTO.class);
        assertThat(entityDTO.getFields().get("start")).isEqualTo(startFieldShortRef);

        // check that start is a real ref
        final Object startRef = entity.get("start");
        assertThat(startRef).isInstanceOf(YamlPK.class);

        final ESPK startESPK = (ESPK) startRef;
        assertThat(startESPK.toString()).isEqualTo(entityPK + '/' + startFieldShortRef);
        assertThat(yamlEntityStore.findChildren(entity.getPK(), null, null)).contains(startESPK);

        // get object from Ref
        Entity startEntity = yamlEntityStore.getEntity(startESPK);
        assertThat(startEntity).isNotNull();
        assertThat(startEntity.getType().getName()).isEqualTo(startNodeTargetType);
        assertThat(startEntity.getType()).isSameAs(yamlEntityStore.getTypeForName(startNodeTargetType));

        // search for child of type
        final Collection<ESPK> children = yamlEntityStore.findChildren(entity.getPK(), null, yamlEntityStore.getTypeForName(startNodeTargetType));
        assertThat(children).hasSize(1);
        assertThat(children.iterator().next()).isEqualTo(startESPK);

        // search for child of type
        final Collection<ESPK> childList = yamlEntityStore.listChildren(entity.getPK(), yamlEntityStore.getTypeForName(startNodeTargetType));
        assertThat(childList).hasSize(1);
        assertThat(childList.iterator().next()).isEqualTo(startESPK);

        final Collection<ESPK> namedChildren = yamlEntityStore.findChildren(entity.getPK(), new Field[]{entity.getType().createField("name", startFieldShortRef)}, yamlEntityStore.getTypeForName(startNodeTargetType));
        assertThat(namedChildren).hasSize(1);
        assertThat(namedChildren.iterator().next()).isEqualTo(startESPK);

        EntityStoreDelegate.getEntity(yamlEntityStore, "/[FilterCircuit]**/["+startNodeTargetType+"]name="+startFieldShortRef);
        assertThat(namedChildren).hasSize(1);
        assertThat(namedChildren.iterator().next()).isEqualTo(startESPK);

    }

}
