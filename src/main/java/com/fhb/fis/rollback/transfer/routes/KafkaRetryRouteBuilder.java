package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.camel.processor.EnvelopeWrapperProcessor;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.kafka.processor.KafkaRetriesProcessor;
import com.fhb.fis.rollback.transfer.util.Constants;


@Component(KafkaRetryRouteBuilder.BEAN_NAME)
public class KafkaRetryRouteBuilder extends OABServiceRouteBuilder{
    public static final String BEAN_NAME = "kafkaRetryRouteBuilder";

    public static final String KAFKA_RETRY_URI = "direct:KafkaRetry";

    public static final String KAFKA_RETRY_ID = "R02_Kafka_Retry_ID";

    public static final String SET_FAULT_INFO_ROUTE_URI = "direct:set-fault-info";
    public static final String SET_FAULT_INFO_ROUTE_ID = "R07_set-fault-info";

    public static final Logger LOGGER = LoggerFactory.getLogger(KafkaRetryRouteBuilder.class);

    @Override
    public void configure(){
        configureEntryRoute(from(KAFKA_RETRY_URI).routeId(KAFKA_RETRY_ID));
        configureFault(from(SET_FAULT_INFO_ROUTE_URI).routeId(SET_FAULT_INFO_ROUTE_ID));
    }
    //TODO 
    @Override
    public void configureEntryRoute(RouteDefinition fromEntry) {
        fromEntry
        .filter(header(KafkaConstants.RETRIES_HEADER).isNull())
            .setHeader(KafkaConstants.RETRIES_HEADER, constant(0))
        .end()
        .choice()
            //.process(EnvelopeWrapperProcessor)
            .when().simple("${header["+KafkaConstants.RETRIES_HEADER+"]} < {{kafka.retries}}")
                .process(KafkaRetriesProcessor.BEAN_NAME)
                .setBody(exchangeProperty(Constants.REQUEST_BODY))
                .to(ReceiveKafkaEventRouteBuilder.KAFKA_ENTRY_URI)
            .endChoice()
            .otherwise()
                .to(SET_FAULT_INFO_ROUTE_URI)
                .to(BusinessExceptionRouteBuilder.ENTRY_BUSINESS_EXCEPTION_URI)
            .endChoice()
        .end();
    }

    private void configureFault(RouteDefinition fromFault){
            //Set fault info
            fromFault
            .routeId(SET_FAULT_INFO_ROUTE_ID)
            //Set error message property if present
            .filter().jsonpath("$.fault.message", true)
                .setProperty(Constants.FAULT_ERROR_MESSAGE).jsonpath("$.fault.message")
            .end()
            //Set error code property if present
            .filter().jsonpath("$.fault.code", true)
                .setProperty(Constants.FAULT_ERROR_CODE).jsonpath("$.fault.code")
            .end()
            //Set the http code replied by the SOR
            .filter().jsonpath("$.fault.transportErrorCode", true)
	            .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
            .end()
	        .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+Constants.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}");
    }

}
