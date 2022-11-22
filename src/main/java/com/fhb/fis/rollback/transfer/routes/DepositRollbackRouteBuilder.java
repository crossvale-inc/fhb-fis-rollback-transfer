package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.util.Constants;
import com.fhb.fis.rollback.transfer.util.Constants;
@Component
public class DepositRollbackRouteBuilder extends RouteBuilder {

    
    public static Logger LOGGER = LoggerFactory.getLogger(DepositRollbackRouteBuilder.class);


    protected static final String DO_ROLLBACK_CREDIT_DEPOSIT_URI = "direct:do-rollback-deposits-credit";
    protected static final String DO_ROLLBACK_CREDIT_DEPOSIT_ID = "RR03_do-rollback-deposits-credit";

    protected static final String DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_URI = "direct:do-rollback-employee-deposits-credit";
    protected static final String DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_ID = "RR04_do-rollback-employee-deposits-credit";
    
    protected static final String DO_ROLLBACK_DEBIT_DEPOSIT_URI = "direct:do-rollback-deposits-debit";
    protected static final String DO_ROLLBACK_DEBIT_DEPOSIT_ID = "RR03_do-rollback-deposits-debit";

    protected static final String DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_URI = "direct:do-rollback-employee-deposits-debit";
    protected static final String DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_ID = "RR04_do-rollback-employee-deposits-debit";



    @Override
    public void configure() throws Exception {
        
        fromDoRollbackCreditDeposit(from(DO_ROLLBACK_CREDIT_DEPOSIT_URI).routeId(DO_ROLLBACK_CREDIT_DEPOSIT_ID));
        fromDoRollbackCreditEmployee(from(DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_URI).routeId(DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_ID));
        
        fromDoRollbackDebitDeposit(from(DO_ROLLBACK_DEBIT_DEPOSIT_URI).routeId(DO_ROLLBACK_DEBIT_DEPOSIT_ID));
        fromDoRollbackDebitEmployee(from(DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_URI).routeId(DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_ID));
    }

    //Rollback credit
    public void fromDoRollbackCreditDeposit(RouteDefinition fromEntry){
        fromEntry
        .log(LoggingLevel.INFO,LOGGER,"{\"Stage\":\"Initializing\",\"method\":\"rollbackCreditDeposit\"}")
        .setHeader(Constants.REQUEST_BODY,body())
        .to("{{jolt.transfer.to.dollar.transactions.credit.rollback.request.spec}}")
        .to("direct:postOnlineDollarTransactions")//enrich keep headers and change body
        //In case of error, get error code
        .filter().jsonpath("$.fault", true)
        .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
        .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
            .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Received error response from online dollar transaction service. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${header["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${header["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${header["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${header["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${header["+Constants.FAULT_MESSAGE+"]}\"}")
            .setProperty("creditDepositRollbackFailedError").jsonpath("$.fault.code", true, String.class)
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the online dollar transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${header["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${header["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${header["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${header["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${header["+Constants.FAULT_MESSAGE+"]}\"}")
            .end()
            //Analyze response not an error
            .choice()
            .when().simple("'{{ibs.employee.controlled.error}}' contains ${property.creditDepositRollbackFailedError}")
            .log(LoggingLevel.INFO, LOGGER, "Online dollar transaction failed, retry due to controlled FHB employee error")
            .to(DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_URI)
            .endChoice()
            .when().jsonpath("$.fault", true)
            .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
            .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
            .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
            .setProperty(Constants.FAULT_STAGE,constant(ErrorCode.ROLLBACK_CREDIT_POST_FAILED))
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the online dollar transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${header["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${header["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${header["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${header["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${header["+Constants.FAULT_MESSAGE+"]}\"}")
            .endChoice()
            .otherwise()
            .log(LoggingLevel.INFO, LOGGER, "Deposit rollback executed")
            .endChoice()
            .end()
            //If credit rollback executed, do not mark credit posting table as FAILED, rollback was successful
            .log(LoggingLevel.INFO, LOGGER, "Deposit Credit rollback executed successfully, posting table record will not be marked as 'FAILED' if rollback process fails")
            .setProperty(Constants.MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY, constant(false));
        }
        
        public void fromDoRollbackCreditEmployee(RouteDefinition fromEntry){
            //Do deposit debit rollback for employee
            fromEntry
            .routeId(DO_ROLLBACK_EMPLOYEE_CREDIT_DEPOSIT_ID)
            .log(LoggingLevel.INFO, LOGGER, "Executing Deposit Credit Employee Rollback")
            .setBody(header(Constants.REQUEST_BODY))
            .to("{{jolt.transfer.to.dollar.transactions.credit.employee.rollback.request.spec}}")
            .to("direct:postOnlineDollarTransactions")
            //Analyze response not an error
            .choice()
            .when().jsonpath("$.fault", true)
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the online dollar transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${header["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${header["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${header["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${header["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${header["+Constants.FAULT_MESSAGE+"]}\"}")
            .endChoice()
            .otherwise()
            .log(LoggingLevel.INFO, LOGGER, "Deposit rollback executed")
            .endChoice()
            .end();
        }
        
        //Rollback debit
        public void fromDoRollbackDebitDeposit(RouteDefinition fromEntry){
            fromEntry
            .log(LoggingLevel.INFO,LOGGER,"{\"Stage\":\"Initializing\",\"method\":\"rollbackDebitDeposit\"}")
        .to("{{jolt.transfer.to.dollar.transactions.debit.rollback.request.spec}}")
        .to("direct:postOnlineDollarTransactions")
        //In case of error, get error code
        .filter().jsonpath("$.fault", true)
            .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
            .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
            .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Received error response from online dollar transaction service. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
            .setProperty("debitDepositRollbackFailedError").jsonpath("$.fault.code", true, String.class)
        .end()
        //Analyze response not an error
        .choice()
            .when().simple("'{{ibs.employee.controlled.error}}' contains ${property.debitDepositRollbackFailedError}")
                .log(LoggingLevel.INFO, LOGGER, "Online dollar transaction failed, retry due to controlled FHB employee error")
                .to(DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_URI)
            .endChoice()
            .when().jsonpath("$.fault", true)
                .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
                .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
                .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
                .setProperty(Constants.FAULT_STAGE,constant(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
                .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the online dollar transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                .to(KafkaRetryRouteBuilder.KAFKA_RETRY_URI)
            .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, LOGGER, "Deposit rollback executed")
            .endChoice()
        .end();
    }

    public void fromDoRollbackDebitEmployee(RouteDefinition fromEntry){
        fromEntry
        .routeId(DO_ROLLBACK_EMPLOYEE_DEBIT_DEPOSIT_ID)
        .log(LoggingLevel.INFO, LOGGER, "Executing Deposit Debit Employee Rollback")
        .setBody(exchangeProperty(Constants.REQUEST_BODY))
        .to("{{jolt.transfer.to.dollar.transactions.debit.employee.rollback.request.spec}}")
        .to("direct:postOnlineDollarTransactions")
        //Analyze response not an error
        .choice()
            .when().jsonpath("$.fault", true)
                .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
                .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
                .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
                .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the online dollar transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${header[appPath]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                .setProperty(Constants.FAULT_STAGE,constant(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
                .to(KafkaRetryRouteBuilder.KAFKA_RETRY_URI)
            .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, LOGGER, "Deposit rollback executed")
            .endChoice()
        .end();
    }

}
