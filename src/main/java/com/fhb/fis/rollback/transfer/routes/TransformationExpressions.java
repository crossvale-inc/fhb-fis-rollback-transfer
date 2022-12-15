package com.fhb.fis.rollback.transfer.routes;


import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.util.Constants;

public interface TransformationExpressions {

    String HARD_POSTING_UUID = "{" + 
            "\"transferUuid\": \"${header[" + CommonInputHeader.AUDIT_TRAIL_ID + "]}\"" + 
            "}";
    
    String TRANSFER_CHANNEL = "{" + 
            "\"transferChannel\": \"${header[" + CommonInputHeader.APP_CODE + "]}\"" + 
            "}";
    
    String DEBIT_POSTED_DATE = "{" + 
            "\"debitPostedDate\": \"${exchangeProperty[" + Constants.DEBIT_POSTED_DATE_PROPERTY + "]}\"" + 
            "}";
    
    String CREDIT_POSTED_DATE = "{" + 
            "\"creditPostedDate\": \"${exchangeProperty[" + Constants.CREDIT_POSTED_DATE_PROPERTY + "]}\"" + 
            "}";
    
    String DEBIT_TRANSACTION_SOURCE_CODE = "{" + 
            "\"debitTransactionSourceCode\": \"${exchangeProperty[" + Constants.DEBIT_TRANSACTION_SOURCE_CODE + "]}\"" + 
            "}";
    
    String CREDIT_TRANSACTION_SOURCE_CODE = "{" + 
            "\"creditTransactionSourceCode\": \"${exchangeProperty[" + Constants.CREDIT_TRANSACTION_SOURCE_CODE + "]}\"" + 
            "}";
    
    String DEBIT_TRANSACTION_CONTROL_NUMBER = "{" + 
            "\"debitTransactionControlNumber\": \"${exchangeProperty[" + Constants.DEBIT_TRANSACTION_CONTROL_NUMBER + "]}\"" + 
            "}";
    
    String CREDIT_TRANSACTION_CONTROL_NUMBER = "{" + 
            "\"creditTransactionControlNumber\": \"${exchangeProperty[" + Constants.CREDIT_TRANSACTION_CONTROL_NUMBER + "]}\"" + 
            "}";
            
    String DEBIT_HARD_POSTING_ID = "{" + 
            "\"debitInternalPostId\": \"${exchangeProperty[" + Constants.DEBIT_HARD_POSTING_INTERNAL_ID + "]}\"" + 
            "}";
    
    String CREDIT_HARD_POSTING_ID = "{" + 
            "\"creditInternalPostId\": \"${exchangeProperty[" + Constants.CREDIT_HARD_POSTING_INTERNAL_ID + "]}\"" + 
            "}";
    
    String FROM_DATE = "{" + 
            "\"from\": \"${exchangeProperty[" + Constants.FROM_DATE_PROPERTY + "]}\"" + 
            "}";
    
    String TO_DATE = "{" + 
            "\"to\": \"${exchangeProperty[" + Constants.TO_DATE_PROPERTY + "]}\"" + 
            "}";
}
