package com.fhb.fis.rollback.transfer.routes;

import com.fhb.fis.camel.builder.BusinessExceptionRouteConfigurable;
import com.fhb.fis.camel.processor.BusinessExceptionProcessor;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.model.BusinessExceptionHeader;
import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.processors.LocalBusinessExceptionProcessor;
import com.fhb.fis.rollback.transfer.util.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BusinessExceptionRouteBuilder extends RouteBuilder implements BusinessExceptionRouteConfigurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessExceptionRouteBuilder.class);
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_CONTENT_TYPE = "application/vnd.kafka.json.v2+json";
    
    @Override
    public void configure() throws Exception {
        
        configureEnrtyBusinessExceptionRoute(
            from(BusinessExceptionRouteConfigurable.ENTRY_BUSINESS_EXCEPTION_URI)
                .routeId(BusinessExceptionRouteConfigurable.ENTRY_BUSINESS_EXCEPTION_ID));
        
        configureInvokeBusinessExceptionRoute(
            from(BusinessExceptionRouteConfigurable.INVOKE_BUSINESS_EXCEPTION_URI)
                .routeId(BusinessExceptionRouteConfigurable.INVOKE_BUSINESS_EXCEPTION_ID));
    }

    @Override
    public void configureEnrtyBusinessExceptionRoute(final RouteDefinition fromEntryBusinessException) {
        
        fromEntryBusinessException
            .log(LoggingLevel.DEBUG, LOGGER, "Setting up Business Exceptions properties")
            .log(LoggingLevel.DEBUG, LOGGER, "Setting up Business Exceptions Headers")
            .setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_MICROSERVICE, header(KafkaConstants.FROM_MICROSERVICE))
            .setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_ASSIGNEE, simple("Funds Transfer Support"))
            .setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_TYPE, simple("Funds Transfer"))
            .process(LocalBusinessExceptionProcessor.BEAN_NAME)
            .setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_CATEGORY, simple("High"))
            .setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_PRIORITY, simple("P1"))
            .filter(exchangeProperty("supportRequest").isNotNull())
                .filter(PredicateBuilder.and(exchangeProperty(Constants.FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY).isNotNull(),exchangeProperty(Constants.FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY).isNotEqualTo("")))
                    .setProperty(Constants.FROM_ACCOUNT_NUMBER,simple("${property["+Constants.FROM_ACCOUNT_NUMBER+"]}-${property["+Constants.FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY+"]}"))
                .end()
                .filter(PredicateBuilder.and(exchangeProperty(Constants.TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY).isNotNull(),exchangeProperty(Constants.TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY).isNotEqualTo("")))
                    .setProperty(Constants.TO_ACCOUNT_NUMBER,simple("${property["+Constants.TO_ACCOUNT_NUMBER+"]}-${property["+Constants.TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY+"]}"))
                .end()
                .process((ex)->{
                    String today = LocalDateTime.now(ZoneId.of("Pacific/Honolulu")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ex.setProperty("date", today);
		        })
                .to("language:simple:classpath:business.exception.message.spec.txt")
                .log(LoggingLevel.DEBUG, LOGGER, "Formatted body: ${body}")
                .process(new BusinessExceptionProcessor())
                .to(INVOKE_BUSINESS_EXCEPTION_URI)
            .end();
    }

    @Override
    public void configureInvokeBusinessExceptionRoute(final RouteDefinition fromInvokeBusinessException) {
        
        fromInvokeBusinessException
            .log(LoggingLevel.DEBUG, LOGGER, "Sending message to Business Exception Queue: ${body}")
            .convertBodyTo(String.class)
            .to(BUSINESS_EXCEPTION_ENDPOINT) 
            .log(LoggingLevel.DEBUG, LOGGER, "Message sent to Business Exception Queue: ${body}")
            .log(LoggingLevel.INFO,LOGGER,"{\"Stage\":\"BusinessException\", \"ExceptionSubject\":\"${header["+BusinessExceptionHeader.HEADER_BUSINESS_EX_SUBJECT+"]}\", \"ExceptionMessage\":\"${header["+BusinessExceptionHeader.HEADER_BUSINESS_EX_MESSAGE+"]}\", \"method\":\"${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${property["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"plaform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\",\"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\"}");
            
    }

    
}
