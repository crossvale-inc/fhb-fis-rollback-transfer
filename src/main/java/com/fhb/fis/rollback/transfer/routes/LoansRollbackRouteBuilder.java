package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.exceptions.OrchestratedServiceException;
import com.fhb.fis.rollback.transfer.util.Constants;
import com.fhb.fis.model.CommonInputHeader;
@Component
public class LoansRollbackRouteBuilder extends RouteBuilder{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoansRollbackRouteBuilder.class);
    
    private final AggregationStrategy jsonMergeAggregationStrategy;
    private final AggregationStrategy useOriginalAggregationStrategy;
    
    
    public static final String DO_ROLLBACK_DEBIT_LOAN_URI = "direct:do-rollback-loans-debit";
    public static final String DO_ROLLBACK_DEBIT_LOAN_ID = "RR03_do-rollback-loans-debit";


    protected static final String DO_ROLLBACK_CREDIT_LOAN_URI = "direct:do-rollback-loans-credit";
    protected static final String DO_ROLLBACK_CREDIT_LOAN_ID = "RR03_do-rollback-loans-credit";
    
    @Autowired
    public LoansRollbackRouteBuilder(final AggregationStrategy jsonMergeAggregationStrategy, final AggregationStrategy useOriginalAggregationStrategy) {
        super();
        this.jsonMergeAggregationStrategy = jsonMergeAggregationStrategy;
        this.useOriginalAggregationStrategy = useOriginalAggregationStrategy;
    }

    
    @Override
    public void configure() throws Exception {
        onException()
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the reversal loan note transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
            .to(KafkaRetryRouteBuilder.KAFKA_RETRY_URI)
        .end();

        fromDoRollbackDebitLoans(from(DO_ROLLBACK_DEBIT_LOAN_URI).routeId(DO_ROLLBACK_DEBIT_LOAN_ID));

        fromDoRollbackCreditLoans(from(DO_ROLLBACK_CREDIT_LOAN_URI).routeId(DO_ROLLBACK_CREDIT_LOAN_ID));

    }


    public void fromDoRollbackCreditLoans(RouteDefinition fromEntry){
        fromEntry
        .setProperty(Constants.ACCOUNT_NR_HEADER).jsonpath("$.toAccount", true)
        .setProperty(Constants.NOTE_NR_HEADER).jsonpath("$.toLoanNoteOrDepositId", true)
        //Get loan note transactions to get note transaction sequence number
        //Get fields from request to filter output of note transactions
        .setProperty(Constants.REQUESTED_TRANSFER_AMOUNT).jsonpath("$.amount", true)
        .setProperty(Constants.POSTED_DATE_PROPERTY).jsonpath("$.creditPostedDate", true)
        .setProperty(Constants.LOAN_CURRENT_BALANCE).jsonpath("$.creditLoanCurrentBalance", true)
        .setProperty(Constants.TRANSACTION_CODE_PROPERTY).jsonpath("$.creditTransactionCode", true)
        .setProperty("postType",constant("credit"))
        .enrich(LoansNoteRouteBuilder.GET_LOAN_NOTE_TRANSACTION_URI, jsonMergeAggregationStrategy)
        //Marshal after enrich
        .marshal().json(JsonLibrary.Jackson)
        //Transform request
        .setHeader(Constants.ACCOUNT_NR_HEADER, exchangeProperty(Constants.ACCOUNT_NR_HEADER))
        .setHeader(Constants.NOTE_NR_HEADER, exchangeProperty(Constants.NOTE_NR_HEADER))
        .to("{{jolt.transfer.to.remove.loan.note.transaction.credit.request.spec}}")
        //Execute request
        .to("direct:postAccountsAcctNbrNotesNoteNbrReversalsImmediate")
        //Analyze response not an error
        .choice()
            .when().jsonpath("$.fault", true)
                .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
                .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
                .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
                .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_CREDIT_POST_FAILED))
            .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, LOGGER, "Loan Note Transaction removed")
            .endChoice()
        .end();
    }

    public void fromDoRollbackDebitLoans(RouteDefinition fromEntry){
        fromEntry
        .setProperty(Constants.ACCOUNT_NR_HEADER).jsonpath("$.fromAccount", true)
        .setProperty(Constants.NOTE_NR_HEADER).jsonpath("$.fromLoanNoteOrDepositId", true)
        //Get loan note transactions to get note transaction sequence number
        //Get fields from request to filter output of note transactions
        .setProperty(Constants.REQUESTED_TRANSFER_AMOUNT).jsonpath("$.amount", true)
        .setProperty(Constants.POSTED_DATE_PROPERTY).jsonpath("$.debitPostedDate", true)
        .setProperty(Constants.LOAN_CURRENT_BALANCE).jsonpath("$.debitLoanCurrentBalance", true)
        .setProperty(Constants.TRANSACTION_CODE_PROPERTY).jsonpath("$.debitTransactionCode", true)
        .setProperty("postType",constant("debit"))
        .enrich(LoansNoteRouteBuilder.GET_LOAN_NOTE_TRANSACTION_URI, jsonMergeAggregationStrategy)
        //Marshal after enrich
        .marshal().json(JsonLibrary.Jackson)
        //Transform request
        .setHeader(Constants.ACCOUNT_NR_HEADER, exchangeProperty(Constants.ACCOUNT_NR_HEADER))
        .setHeader(Constants.NOTE_NR_HEADER, exchangeProperty(Constants.NOTE_NR_HEADER))
        .to("{{jolt.transfer.to.remove.loan.note.transaction.debit.request.spec}}")
        //Execute request
        .to("direct:postAccountsAcctNbrNotesNoteNbrReversalsImmediate")
        //Analyze response not an error
        .choice()
            .when().jsonpath("$.fault", true)
                .setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code", true)
                .setProperty(Constants.FAULT_MESSAGE).jsonpath("$.fault.message", true)
                .setProperty(Constants.STATUS_CODE).jsonpath("$.fault.transportErrorCode", true)
                .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception thrown by the reversal loan note transaction service: rollback process failure. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
            .endChoice()
            .otherwise()
                .log(LoggingLevel.INFO, LOGGER, "Loan Note Transaction removed")
            .endChoice()
        .end()
        .log(LoggingLevel.INFO, LOGGER, "Loan Debit rollback executed successfully, posting table record will not be marked as 'FAILED' if rollback process fails")
        .setProperty(Constants.MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY, constant(false));
    }
    
}
