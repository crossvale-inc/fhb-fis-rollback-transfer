package com.fhb.fis.rollback.transfer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import com.fhb.fis.crypto.DataAtRestCryptoConfig;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cloud.kubernetes.enabled=false")
@RunWith(CamelSpringBootRunner.class)
public abstract class ApplicationTestSupport {
    
    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(DataAtRestCryptoConfig.DATA_AT_REST_KEYSTORE_PW_ENV,
                    "passw0rd");

    @MockBean
    private Supplier<String> auditTrailIdSupplier;

    @Autowired
    private CamelContext context;

    @Autowired
    protected ProducerTemplate template;

    public final CamelContext context() {
        return context;
    }

    public Exchange createExchangeWithBody(final Object body) {
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(body);
        return exchange;
    }

    /*
     * read the content of a resource with the given encoding as a
     * string
     */
    protected String getResourceContent(final String resourceName,
            String charsetName) throws URISyntaxException, IOException {
        Path path = Paths.get(
                getClass().getClassLoader().getResource(resourceName).toURI());
        return new String(Files.readAllBytes(path), charsetName);
    }
}