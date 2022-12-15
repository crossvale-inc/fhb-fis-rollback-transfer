package com.fhb.fis.rollback.transfer.routes;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.rollback.transfer.util.Constants;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AsyncBicUpdateRouteBuilder extends OABServiceRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBicUpdateRouteBuilder.class);
    
    private static final String ASYNC_BIC_UPDATE_OPERATION = "AsyncUpdateBic";

    public static final String ASYNC_BIC_UPDATE_URI = "direct:"+ASYNC_BIC_UPDATE_OPERATION;
    public static final String ASYNC_BIC_UPDATE_ID = "RUB1_"+ASYNC_BIC_UPDATE_OPERATION;

    private static final String KAFKA_UPDATE_BIC_URI = "{{kafka.update.bic.uri}}";


    @Override
    public void configure() throws Exception {
        super.configure();
        this.configureEntryRoute(from(ASYNC_BIC_UPDATE_URI)
                .routeId(ASYNC_BIC_UPDATE_ID));
    }

    @Override
    public void configureEntryRoute(RouteDefinition fromEntry) {
        fromEntry
            .onException(Exception.class)
                .maximumRedeliveries("{{kafka.maximumRedeliveries:2}}")
                .delayPattern("{{kafka.delayPattern:1:500}}")
                .retryAttemptedLogLevel(LoggingLevel.INFO)
                .retriesExhaustedLogLevel(LoggingLevel.WARN)
                //TODO: what should we do with this when the retries to kafka exhausted??
                .to(BusinessExceptionRouteBuilder.ENTRY_BUSINESS_EXCEPTION_URI)
            .end()
            .log(LoggingLevel.INFO, LOGGER, "Sending operation to bic")
            .setHeader(KafkaConstants.FROM_MICROSERVICE, simple("o-fhb-funds-transfer"))
            .setHeader(KafkaConstants.TO_MICROSERVICE, simple("fhb-fis-o-bic-status-manager-status-manager"))
            .setHeader(KafkaConstants.START_TIMESTAMP, simple("${date:now}"))
            .setHeader(KafkaConstants.BIC_ID, header(Constants.INTERNAL_POSTING_ID_HEADER))
            .setHeader(KafkaConstants.BIC_STATUS).jsonpath("$.processingStatus", true)
            .setHeader(KafkaConstants.TYPE).jsonpath("$.postingTypeData.type",true)
            .convertBodyTo(String.class)
            .log(LoggingLevel.DEBUG, LOGGER, "Sending message to Update the BIC table: ${body}")
            .to(KAFKA_UPDATE_BIC_URI) 
            .log(LoggingLevel.DEBUG, LOGGER, "Sent message to Update the BIC table: ${body}");
    }
}
