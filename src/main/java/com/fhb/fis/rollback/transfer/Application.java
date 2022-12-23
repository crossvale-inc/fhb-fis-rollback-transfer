package com.fhb.fis.rollback.transfer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.NoErrorHandlerBuilder;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.spi.DataFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.camel.formatter.KafkaCryptoFormatter;
import com.fhb.fis.camel.impl.DynamicWhiteListHeaderFilterStrategy;
import com.fhb.fis.camel.processor.EnvelopeUnwrapperProcessor;
import com.fhb.fis.camel.processor.EnvelopeWrapperProcessor;
import com.fhb.fis.camel.processor.SensitiveDataMaskingFormatter;
import com.fhb.fis.crypto.DataAtRestCryptoConfig;
import com.fhb.fis.crypto.DataAtRestCryptoDataFormat;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.kafka.processor.KafkaPropertiesFromHeaderProcessor;
import com.fhb.fis.kafka.processor.KafkaRetriesProcessor;
import com.fhb.fis.kafka.serialization.KafkaHeaderDeserializerImpl;
import com.fhb.fis.model.BusinessExceptionHeader;
import com.fhb.fis.model.CacheConstants;
import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.model.MessagingInfrastructureHeader;
import com.fhb.fis.rollback.transfer.routes.IBSServiceRouteBuilder;
import com.fhb.fis.rollback.transfer.util.Constants;
import com.fhb.fis.camel.builder.MultiOrchestratedServiceRouteBuilder;
import com.fhb.fis.camel.aggregate.MergeExchangeBodyAggregationStrategy;
import com.fhb.fis.camel.aggregate.PreservePropertiesAggregationStrategy;

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

    @Bean
    public DynamicWhiteListHeaderFilterStrategy headersWhiteList(
            CamelContext camelContext) throws IOException {
        return new DynamicWhiteListHeaderFilterStrategy(CommonInputHeader.APP_CODE, 
                CommonInputHeader.AUDIT_TRAIL_ID,
                MessagingInfrastructureHeader.OPERATION_ID,
                Constants.IBS_OPERATION_HEADER,
                Constants.IBS_UUID_HEADER,
                Constants.ACCOUNT_NR_HEADER,
                Constants.NOTE_NR_HEADER,
                Constants.CUSTOMER_NR_HEADER,
                Constants.MISCELLANEOUS_ACCOUNT_APPLICATION_CODE_HEADER,
                Constants.INTERNAL_POSTING_ID_HEADER,
                Constants.NOTE_TRANSACTION_REPO_EFF_DATE_HEADER,
                CacheConstants.USE_CACHE_FLAG,
                Exchange.HTTP_QUERY,
                KafkaConstants.RETRIES_HEADER,
                KafkaConstants.RETRY_COUNT,
                KafkaConstants.LIMIT_TIME_HEADER);
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
    public RoutesBuilder loanNoteTransactions() {
        return new IBSServiceRouteBuilder("getAccountsLNAcctNbrNotesLNNoteNbrTransactions", 
                "{{loan-note-transactions.version}}", 
                "getAccountsLNAcctNbrNotesLNNoteNbrTransactions", 
                "{{loans.bridge.endpoint}}");
    }
    
    @Bean
    public RoutesBuilder postAccountsAcctNbrNotesNoteNbrReversalsImmediate() {
        return new IBSServiceRouteBuilder("postAccountsAcctNbrNotesNoteNbrReversalsImmediate", 
                "{{loan-note-reversals-immediate.version}}", 
                "postAccountsAcctNbrNotesNoteNbrReversalsImmediate", 
        		"{{loans.bridge.endpoint}}");
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
    EnvelopeUnwrapperProcessor envelopeUnWrapper() {
        return new EnvelopeUnwrapperProcessor();
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
        return new KafkaHeaderDeserializerImpl(DataAtRestCryptoDataFormat.DATA_AT_REST_CIPHER_IV_LENGTH,KafkaConstants.RETRIES_HEADER);
    }

    @Bean
    public KafkaCryptoFormatter kafkaCryptoFormatter(){
        return new KafkaCryptoFormatter();
    }
    @Bean
    public RoutesBuilder multiOrchestratedServiceRouteBuilder() {
        return new MultiOrchestratedServiceRouteBuilder(new NoErrorHandlerBuilder());
    }
    
    @Bean
    public AggregationStrategy jsonMergeAggregationStrategy() {
    	return new PreservePropertiesAggregationStrategy(new MergeExchangeBodyAggregationStrategy(), 
    	        Constants.TRANSACTION_STATUS);
    }
    
    @Bean
    public AggregationStrategy useOriginalAggregationStrategy() {
        return new PreservePropertiesAggregationStrategy(new UseOriginalAggregationStrategy(), 
            Constants.TRANSACTION_STATUS,
            Constants.MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY,
            Constants.MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY,
            Constants.FAULT_CODE,
            Constants.FAULT_MESSAGE,
            Constants.STATUS_CODE);
    }

    @Bean
    public KafkaPropertiesFromHeaderProcessor kafkaPropertiesFromHeaderProcessor(){
        return new KafkaPropertiesFromHeaderProcessor();
    }

}
