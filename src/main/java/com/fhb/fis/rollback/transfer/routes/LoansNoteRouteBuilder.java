package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.exceptions.OrchestratedServiceException;
import com.fhb.fis.rollback.transfer.util.Constants;
import com.fhb.fis.model.CommonInputHeader;
@Component
public class LoansNoteRouteBuilder extends RouteBuilder{


    protected static final String GET_LOAN_NOTE_TRANSACTION_URI = "direct:get-loan-note-transaction";
    protected static final String GET_LOAN_NOTE_TRANSACTION_ID = "RR04_get-loan-note-transaction";

    public static final Logger LOGGER = LoggerFactory.getLogger(LoansNoteRouteBuilder.class);
    private static final String TRANSACTION_SEQUENCE_NUMBER_TEMPLATE = "{"
    + "\"transactionSequenceNumber\": \"${exchangeProperty[" + Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER + "]}\""
    + "}";

    @Override
    public void configure() throws Exception {
        
        fromGetLoansNoteTransaction(from(GET_LOAN_NOTE_TRANSACTION_URI).routeId(GET_LOAN_NOTE_TRANSACTION_ID));
        
    }
    
    public void fromGetLoansNoteTransaction(RouteDefinition fromEntry){
                //Get loan note transaction
                fromEntry
                //from(GET_LOAN_NOTE_TRANSACTION_URI)
                .routeId(GET_LOAN_NOTE_TRANSACTION_ID)
                //Headers should be passed as properties
                .setHeader(Constants.ACCOUNT_NR_HEADER, exchangeProperty(Constants.ACCOUNT_NR_HEADER))
                .setHeader(Constants.NOTE_NR_HEADER, exchangeProperty(Constants.NOTE_NR_HEADER))
                .setHeader(Constants.NOTE_TRANSACTION_REPO_EFF_DATE_HEADER, exchangeProperty(Constants.POSTED_DATE_PROPERTY))
                .transform(constant(null))
                //GET loan note transactions
                .to("direct:getAccountsLNAcctNbrNotesLNNoteNbrTransactions")
                //Filter response to get sequence number
                .setProperty(Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER)
                    .jsonpath("$.payload.noteTransactionDetailsList[?("
                        //Date should be the same as posted date
                        + "@.transactionDate == '${exchangeProperty[" + Constants.POSTED_DATE_PROPERTY + "]}' "
                        //Transaction amount should be the same as the transfer amount
                        + "&& @.transactionAmount.value == '${exchangeProperty[" + Constants.REQUESTED_TRANSFER_AMOUNT + "]}' "
                        //Transaction current balance should be the same as the operation balance
                        + "&& @.resultingBalance.value == '${exchangeProperty[" + Constants.LOAN_CURRENT_BALANCE + "]}' "
                        //Transaction code should be the same as used in the operation transaction code
                        + "&& @.transactionCode == '${exchangeProperty[" + Constants.TRANSACTION_CODE_PROPERTY + "]}' "
                        //Transaction should not be reversed
                        + "&& @.transactionReversalInd != '" + Constants.LOAN_TRANSACTION_REVERSAL_IND + "'"
                        + ")].transactionSequenceNumber", true)
                .choice()
                    .when(PredicateBuilder.not(simple("${exchangeProperty[" + Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER + "].isEmpty()}")))
                        .log(LoggingLevel.DEBUG, LOGGER, "Loan Note Transaction sequence number '${exchangeProperty[" + Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER + "]}'")
                        .setProperty(Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER, simple("${exchangeProperty[" + Constants.LOAN_NOTE_TRANSACTION_SEQ_NUMBER + "].get(0)}"))         
                    .endChoice()
                    .otherwise()
                        .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Could not obtain loan note transaction sequence number. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${exchangeProperty["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${exchangeProperty["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${exchangeProperty["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${exchangeProperty["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${exchangeProperty["+Constants.FAULT_MESSAGE+"]}\"}")
                        .filter(exchangeProperty("postType").isEqualTo("credit"))
                            .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_CREDIT_POST_FAILED))
                        .end()
                        .throwException(new OrchestratedServiceException(ErrorCode.ROLLBACK_DEBIT_POST_FAILED))
                    .endChoice()
                .end()
                .transform(simple(TRANSACTION_SEQUENCE_NUMBER_TEMPLATE));
            
    }
    
   



}
