package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.exceptions.OrchestratedServiceException;
import com.fhb.fis.rollback.transfer.util.Constants;

public class CardsRollbackRouteBuilder extends RouteBuilder{

    protected static final String DO_ROLLBACK_DEBIT_CARD_URI = "direct:do-rollback-card-debit";
    protected static final String DO_ROLLBACK_DEBIT_CARD_ID = "RR03_do-rollback-card-debit";

    protected static final String GET_CARD_SIGNATURE_AUTHORIZATION_RETRY_URI = "direct:get-card-signature-authorization-retry";
    protected static final String GET_CARD_SIGNATURE_AUTHORIZATION_RETRY_ID = "RR04_get-card-signature-authorization-retry";

    protected static final String GET_CARD_SIGNATURE_AUTHORIZATION_URI = "direct:get-card-signature-authorization";
    protected static final String GET_CARD_SIGNATURE_AUTHORIZATION_ID = "RR04_get-card-signature-authorization";
    
    protected static final String ENRICH_WITH_FROM_DATE_AS_YESTERDAY_HAWAII_URI = "direct:enrich-with-from-date-as-yesterday-in-hawaii";
    protected static final String ENRICH_WITH_FROM_DATE_AS_YESTERDAY_HAWAII_ID = "RR05_enrich-with-from-date-as-yesterday-in-hawaii";
    private static final String AUTHORIZATION_ID_BODY_TEMPLATE = "{"
            + "\"authorizationId\": \"${exchangeProperty[" + Constants.DEBIT_CARD_AUTHORIZATION_ID + "]}\""
            + "}";
    
    protected static final String ENRICH_WITH_TO_DATE_AS_TODAY_URI = "direct:enrich-with-to-date-as-today";
    protected static final String ENRICH_WITH_TO_DATE_AS_TODAY_ID = "RR05_enrich-with-to-date-as-today";

    private static final Logger LOGGER = LoggerFactory.getLogger(CardsRollbackRouteBuilder.class);
    
    private final AggregationStrategy jsonMergeAggregationStrategy;

    @Autowired
    public CardsRollbackRouteBuilder(AggregationStrategy jsonMergeAggregationStrategy){
        this.jsonMergeAggregationStrategy = jsonMergeAggregationStrategy;
    }

    @Override
    public void configure() throws Exception {

        fromDoRollbackDebitCard(from(DO_ROLLBACK_DEBIT_CARD_URI).routeId(DO_ROLLBACK_DEBIT_CARD_ID));
        
        fromGetSignatureCard(from(GET_CARD_SIGNATURE_AUTHORIZATION_RETRY_URI).routeId(GET_CARD_SIGNATURE_AUTHORIZATION_RETRY_ID));
        

    }
    private void fromGetSignatureCard(RouteDefinition fromEntry) {
        fromEntry
        .onException(OrchestratedServiceException.class)
            .maximumRedeliveries("{{fundstransfer.get.card.signature.api.maximumRedeliveries:3}}")
            .delayPattern("{{fundstransfer.get.card.signature.api.delayPattern:1:100000}}")
            .retryAttemptedLogLevel(LoggingLevel.INFO)
            .retriesExhaustedLogLevel(LoggingLevel.WARN)
        .end()
        .to(GET_CARD_SIGNATURE_AUTHORIZATION_URI);
    }

    public void fromDoRollbackDebitCard(RouteDefinition fromEntry){
            //Rollback debit card operation
            from(DO_ROLLBACK_DEBIT_CARD_URI)
            .routeId(DO_ROLLBACK_DEBIT_CARD_ID)
            .log(LoggingLevel.INFO, LOGGER, "Executing Card Debit Rollback")
            //Get card transactions to get authorizationId
            .setBody(exchangeProperty(Constants.REQUEST_BODY))
            //Get authorization id add enrich the body with its value
            .enrich(GET_CARD_SIGNATURE_AUTHORIZATION_RETRY_URI, jsonMergeAggregationStrategy)
            //Marshal after enrich
            .marshal().json(JsonLibrary.Jackson)
            //Transform request
            //Different transformation for cardNr and accountId
            .filter(PredicateBuilder.or(
                    exchangeProperty(Constants.FROM_ACCOUNT_NUMBER).isNull(),
                    exchangeProperty(Constants.FROM_ACCOUNT_NUMBER).isEqualTo("")))
                .setProperty(Constants.FROM_ACCOUNT_NUMBER).jsonpath("$.fromAccount", true, String.class)
            .end()
            .choice()
                .when(ex -> Constants.CARD_NUMBER_LENGTH == ex.getProperty(Constants.FROM_ACCOUNT_NUMBER, "", String.class).length())
                    .log(LoggingLevel.INFO, LOGGER, "Executing Delete Card Signature Authorization By Card Number")
                    .to("{{jolt.transfer.to.remove.card.signature.authorization.request.spec}}")
                .endChoice()
                .otherwise()
                    .log(LoggingLevel.INFO, LOGGER, "Executing Delete Card Signature Authorization By AccountId")
                    .to("{{jolt.transfer.to.remove.card.signature.authorization.by.accountid.request.spec}}")
                .endChoice()
            .end()
            //Execute request
            .to("direct:deleteAuthorizationByCard")
            //Analyze response not an error
            .choice()
                .when().jsonpath("$.fault", true)
                    .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
                    .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
                    .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
	                .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the delete card authorization by card service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                    .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
                .endChoice()
                .otherwise()
                    .log(LoggingLevel.INFO, LOGGER, "Card authorization successfully removed")
                .endChoice()
            .end()
            //If debit rollback executed, do not mark debit posting table as FAILED, rollback was successful
            .log(LoggingLevel.INFO, LOGGER, "Card Debit rollback executed successfully, posting table record will not be marked as 'FAILED' if rollback process fails")
            .setProperty(Constants.MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY, constant(false));
            from(GET_CARD_SIGNATURE_AUTHORIZATION_URI)
            .routeId(GET_CARD_SIGNATURE_AUTHORIZATION_ID)
            //Get fields from request to filter output
            .setProperty(Constants.REQUESTED_TRANSFER_AMOUNT).jsonpath("$.amount", true, String.class)
            //TSYS amount is a string with two decimals
            .process(ex -> {
                
                String amountBody = ex.getProperty(Constants.REQUESTED_TRANSFER_AMOUNT, String.class);
                String formattedAmount = String.format("%.2f", new BigDecimal(amountBody));
                
                ex.setProperty(Constants.REQUESTED_TRANSFER_AMOUNT, formattedAmount);
            })
            //Different transformation for cardNr and accountId
            .filter(PredicateBuilder.or(
                    exchangeProperty(Constants.FROM_ACCOUNT_NUMBER).isNull(),
                    exchangeProperty(Constants.FROM_ACCOUNT_NUMBER).isEqualTo("")))
                .setProperty(Constants.FROM_ACCOUNT_NUMBER).jsonpath("$.fromAccount", true, String.class)
            .end()
            .choice()
                .when(ex -> Constants.CARD_NUMBER_LENGTH == ex.getProperty(Constants.FROM_ACCOUNT_NUMBER, "", String.class).length())
                    .log(LoggingLevel.INFO, LOGGER, "Executing GET Card Signature Authorization By Card Number")
                    .to("{{jolt.transfer.to.get.card.signature.authorization.request.spec}}")
                .endChoice()
                .otherwise()
                    .log(LoggingLevel.INFO, LOGGER, "Executing GET Card Signature Authorization By AccountId")
                    .to("{{jolt.transfer.to.get.card.signature.authorization.by.accountid.request.spec}}")
                .endChoice()
            .end()
            .enrich(ENRICH_WITH_FROM_DATE_AS_YESTERDAY_HAWAII_URI, jsonMergeAggregationStrategy)
            .enrich(ENRICH_WITH_TO_DATE_AS_TODAY_URI, jsonMergeAggregationStrategy)
            //Marshal after enrich
            .marshal().json(JsonLibrary.Jackson)
            .to("direct:getCardSignatureAuthorization")
            .setProperty(Constants.DEBIT_CARD_AUTHORIZATION_ID)
                .jsonpath("$.payload.authorizationList[?("
                        + "@.authorizationAmount.value == '${exchangeProperty[" + Constants.REQUESTED_TRANSFER_AMOUNT + "]}' "
                        + "&& @.response == '" + Constants.SUCCESSFUL_CARD_AUTHORIZATION_RESPONSE + "' "
                        + "&& @.status != '" + Constants.SUCCESSFUL_CARD_AUTHORIZATION_REVERSED + "')].authorizationId", true)
            .choice()
                .when(PredicateBuilder.not(simple("${exchangeProperty[" + Constants.DEBIT_CARD_AUTHORIZATION_ID + "].isEmpty()}")))
                    .setProperty(Constants.DEBIT_CARD_AUTHORIZATION_ID, simple("${exchangeProperty[" + Constants.DEBIT_CARD_AUTHORIZATION_ID + "].get(0)}"))
                    .log(LoggingLevel.DEBUG, LOGGER, "Obtained Authorization ID '${exchangeProperty[" + Constants.DEBIT_CARD_AUTHORIZATION_ID + "]}'")
                .endChoice()
                .otherwise()
	                .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Could not obtain signature authorization ID. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                    .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
                .endChoice()
            .end()
            .transform(simple(AUTHORIZATION_ID_BODY_TEMPLATE));

                    //ENRICH BODY WITH FROM DATE
        from(ENRICH_WITH_FROM_DATE_AS_YESTERDAY_HAWAII_URI)
        .routeId(ENRICH_WITH_FROM_DATE_AS_YESTERDAY_HAWAII_ID)
        .process(ex -> {
            ex.setProperty(Constants.FROM_DATE_PROPERTY, 
                    LocalDate.now(ZoneId.of(Constants.HAWAII_TIME_ZONE)).minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        })
        .transform(simple(TransformationExpressions.FROM_DATE));
    
    //ENRICH BODY WITH TO DATE
    from(ENRICH_WITH_TO_DATE_AS_TODAY_URI)
        .routeId(ENRICH_WITH_TO_DATE_AS_TODAY_ID)
        .process(ex -> {
            ex.setProperty(Constants.TO_DATE_PROPERTY, 
                    LocalDate.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_LOCAL_DATE));
        })
        .transform(simple(TransformationExpressions.TO_DATE));
    }

            
        
        
}
