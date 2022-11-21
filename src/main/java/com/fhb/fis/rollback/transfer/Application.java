package com.fhb.fis.rollback.transfer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.spi.DataFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.camel.formatter.KafkaCryptoFormatter;
import com.fhb.fis.camel.impl.DynamicWhiteListHeaderFilterStrategy;
import com.fhb.fis.camel.processor.EnvelopeWrapperProcessor;
import com.fhb.fis.camel.processor.SensitiveDataMaskingFormatter;
import com.fhb.fis.crypto.DataAtRestCryptoConfig;
import com.fhb.fis.crypto.DataAtRestCryptoDataFormat;
import com.fhb.fis.kafka.processor.KafkaRetriesProcessor;
import com.fhb.fis.kafka.serialization.KafkaHeaderDeserializerImpl;
import com.fhb.fis.model.BusinessExceptionHeader;
import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.model.MessagingInfrastructureHeader;
import com.fhb.fis.rollback.transfer.routes.IBSServiceRouteBuilder;
import com.fhb.fis.rollback.transfer.util.Constants;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean(name = OABServiceRouteBuilder.DATA_AT_REST_ID)
    DataFormat dataAtRestCryptoDataFormat(final DataAtRestCryptoConfig config)
            throws GeneralSecurityException, IOException {
        return new DataAtRestCryptoDataFormat(config);
    }

    @Bean
    DataAtRestCryptoConfig dataAtRestCryptoConfig() {
        return new DataAtRestCryptoConfig();
    }
    

    @Bean(name = "CamelCustomLogMask")
    SensitiveDataMaskingFormatter sensitiveLogMask(
            final Environment environment) {
        // sensible defaults
        SensitiveDataMaskingFormatter formatter = new SensitiveDataMaskingFormatter(
                CommonInputHeader.USER_ID, CommonInputHeader.SECURITY_TOKEN);

        // extension keywords
        String[] additionalKeywords = environment
                .getProperty("sensitive.masking.ext", String[].class);
        if (additionalKeywords != null) {
            formatter = formatter.withAdditionalKeywords(additionalKeywords);
        }

        return formatter;
    }


    @Bean
    public RoutesBuilder hardPostingTableUpdateRouteBuilder() {
        return new IBSServiceRouteBuilder("putInternalTransferPostingIxpid", 
                "{{hard-posting-update.version}}", 
                Constants.PUT_BIC_OPERATION_URI, 
                "{{bic.bridge.endpoint}}");
    }

    @Bean
    public RoutesBuilder onlineDollarTransactionsRouteBuilder() {
        return new IBSServiceRouteBuilder("postOnlineDollarTransactions", 
                "{{online-dollar-transactions.version}}", 
                "postOnlineDollarTransactions", 
                "{{deposits.bridge.endpoint}}");
    }
    
    @Bean
    DynamicWhiteListHeaderFilterStrategy headersWhiteList(
            final Environment environment, final CamelContext camelContext)
            throws IOException {
        // sensible defaults
        DynamicWhiteListHeaderFilterStrategy filter = new DynamicWhiteListHeaderFilterStrategy(
                BusinessExceptionHeader.HEADER_BUSINESS_EX_MICROSERVICE,
                CommonInputHeader.APP_CODE, CommonInputHeader.USER_ID,
                CommonInputHeader.TERMINAL, CommonInputHeader.SECURITY_TOKEN,
                CommonInputHeader.AUDIT_TRAIL_ID, CommonInputHeader.VERSION,
                MessagingInfrastructureHeader.OPERATION_ID, Constants.UUID_HEADER,
                Exchange.HTTP_PATH, Exchange.HTTP_METHOD, Constants.JOB_ID, Constants.HEADER_JOB_CURSOR);

        // static extension headers
        String[] additionalHeaders = environment
                .getProperty("headers.whitelist.ext", String[].class);
        if (additionalHeaders != null) {
            filter.getOutFilter().addAll(Arrays.asList(additionalHeaders));
        }

        return filter;
    }

    @Bean
    EnvelopeWrapperProcessor envelopeWrapper() {
        return new EnvelopeWrapperProcessor()
                .withAdditionalRetainedHeaders(Constants.UUID_HEADER,BusinessExceptionHeader.HEADER_BUSINESS_EX_MICROSERVICE);//Add headers before sending to 
    }

    @Bean
    Supplier<String> auditTrailIdSupplier() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean(KafkaRetriesProcessor.BEAN_NAME)
    public KafkaRetriesProcessor kafkaRetriesProcessor(){
        return new KafkaRetriesProcessor();
    }

    @Bean
    public KafkaHeaderDeserializerImpl kafkaHeaderDeserializerImpl(){
        return new KafkaHeaderDeserializerImpl(DataAtRestCryptoDataFormat.DATA_AT_REST_CIPHER_IV_LENGTH,"amount","debitBicId","Service-Execution-End","Service-Execution-Overhead","Service-Execution-Start","Service-Execution-Time");
    }

    @Bean
    public KafkaCryptoFormatter kafkaCryptoFormatter(){
        return new KafkaCryptoFormatter();
    }

}
