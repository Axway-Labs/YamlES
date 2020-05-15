package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStoreException;
import com.vordel.es.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.axway.gw.es.yaml.utils.ESTestsUtil.getFileFromClasspath;
import static org.assertj.core.api.Assertions.assertThat;

public class YamlEntityDTOStoreTest {

    // At this place, also the META-INF/Types.yaml is expected
    private static final String testPackage = "/com/axway/gw/es/yaml/noroot/";

    private YamlEntityStore es;

    @BeforeEach
    void setupEntityStoreToTest() throws SecurityException, IllegalArgumentException, EntityStoreException, IOException {
        es = new YamlEntityStore();
        es.setRootLocation(new File(YamlEntityStore.class.getResource(testPackage).getPath()));
        // Entity need to load types to be able to create an Entity
        es.loadTypes();
    }

    @ParameterizedTest
    @CsvSource({
            "policies/APIManagerProtectionPolicy.yaml,Disable Monitoring,11,SetAttributeFilter",
            "policies/oauth20/Access Code.yaml,Exchange AuthZ Code for Access Token,2,AccessCodeGrantFilter",
            "policies/oauth20/AccessTokenService.yaml,Decide what grant type to use,1,SwitchFilter",
            "policies/oauth20/Client Credentials.yaml,Access Token using client credentials,1,ClientCredentialsFilter",
            "policies/oauth20/JWT.yaml,Access token using JWT,1,JWTBearerTokenProfileFilter",
            "policies/oauth20/Refresh.yaml,Refresh Access token,1,RefreshingAccessTokenFilter",
            "policies/oauth20/Resource Owner Password Credentials.yaml,Resource Owner Password Credentials,1,ResourceOwnerPasswordCredentialsFilter",
            "policies/oauth20/SAML.yaml,Access token using SAML Assertion,1,SAMLBearerAssertionGrantFilter",
            "policies/oauth20/Verify SAML Signature.yaml,XML Signature Verification,1,IntegrityVerifySignatureFilter"
    })

    public void loadSinglePolicy(String file, String startFieldValue, int children, String startNodeType) throws IOException {

        Entity e = es.createEntity(getFileFromClasspath(testPackage, file), null);
        assertThat(e.getField("start").getValueList().get(0).toString()).isEqualTo(startFieldValue);


        // check the number of children
        assertThat(es.findChildren(e.getPK(), null, null)).hasSize(children);
        // check that one of the children has a given name and type, exists

        // lookup the field referenced in "start" field
        final Collection<ESPK> namedChildren = es.findChildren(e.getPK(), new Field[]{e.getType().createField("name", startFieldValue)}, es.getTypeForName(startNodeType));
        assertThat(namedChildren).hasSize(1);

        final ESPK namedPk = namedChildren.iterator().next();
        // check that search did get the right one
        assertThat(es.getEntity(namedPk).getField("name").getValueList().get(0).getData()).isEqualTo(startFieldValue);
        assertThat(es.getEntity(namedPk).getType()).isSameAs(es.getTypeForName(startNodeType));


    }


}
