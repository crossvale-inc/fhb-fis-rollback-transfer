package com.fhb.fis.rollback.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.mock.env.MockEnvironment;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.crypto.DataAtRestCryptoConfig;
import com.fhb.fis.kafka.processor.KafkaRetriesProcessor;

public abstract class RoutesTestSupport extends CamelTestSupport {
    /*
     * Java environment variables are not writable from a running JVM process; this
     * JUnit rule allows us to work around the restriction when running tests
     */
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    protected MockEnvironment mockEnvironment  = new MockEnvironment();
    
    /*
     * we need a test-specific configuration (even though "amqp:*" endpoint URIs
     * _should_ be mocked for unit tests, the Camel context still expects to find a
     * "data-at-rest" data format in the registry and will fail if it is not found)
     */
    protected DataAtRestCryptoConfig dataAtRestCryptoConfig;

    /*
     * discover the absolute file system path of any resource on the classpath
     */
    protected String getResourcePath(final String resourceName) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource(resourceName).toURI()).toFile().getAbsolutePath();
    }

    /* read the content of a resource as a string (assumes UTF-8) */
    protected String getResourceContent(final String resourceName) throws URISyntaxException, IOException {
        return getResourceContent(resourceName, "UTF-8");
    }

    /*
     * read the content of a resource with the given encoding as a string
     */
    protected String getResourceContent(final String resourceName, final String charsetName)
            throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        return new String(Files.readAllBytes(path), charsetName);
    }

    /*
     * avoid excessive mocking by just mocking Camel endpoint URIs, etc.
     */
    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("unit-test.properties")) {
            properties.load(is);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return properties;
    }

    @Override
    protected abstract RoutesBuilder createRouteBuilder();

    /*
     * we need a test-specific encryption data format (we could of course provide a
     * no-op or something silly like a rot13 impl, but we'd still need to override
     * this method anyway, so may as well take advantage of
     * DataAtRestCryptoDataFormat's configuration flexibility and use the
     * "real thing")
     */
    /*
     * this method is called in the following sequence for each test: @Before
     * setUp() -> doPreSetup() -> doSetUp() -> createCamelContext() ->
     * createRegistry()
     */
    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();

        Application app = new Application();

        jndi.bind(OABServiceRouteBuilder.DATA_AT_REST_ID, app.dataAtRestCryptoDataFormat(dataAtRestCryptoConfig));
        jndi.bind("headersWhiteList", app.headersWhiteList(mockEnvironment, new DefaultCamelContext()));
        jndi.bind("envelopeWrapper", app.envelopeWrapper());
        jndi.bind("auditTrailIdSupplier", (Supplier<String>) () -> "3fbfa1a7-0533-26d8-a568-b8cb97edc7f3");
        jndi.bind(KafkaRetriesProcessor.BEAN_NAME, new KafkaRetriesProcessor());
        return jndi;
    }

    /*
     * here's where we customize the data-at-rest encryption configuration; it
     * allows for the possibility of changing the configuration (if necessary) on a
     * per-test basis
     */
    /*
     * this method is called in the following sequence for each test: @Before
     * setUp() -> doPreSetup()
     */
    @Override
    protected void doPreSetup() throws Exception {
        environmentVariables.set("DATA_AT_REST_KEYSTORE_PW", "passw0rd");
        dataAtRestCryptoConfig = new DataAtRestCryptoConfig();
        dataAtRestCryptoConfig.setKeyStorePath(getResourcePath("data-at-rest-testing.ks"));
    }
}
