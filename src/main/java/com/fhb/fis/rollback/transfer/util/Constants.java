package com.fhb.fis.rollback.transfer.util;

public interface Constants {
    
    String IBS_BRIDGE = "{{ibs.bridge.endpoint}}";
    String MAPPING_SERVICE_URL = "{{mapping.url}}";

    String UUID_HEADER = "uuid";
    String JOB_ID = "jobId";
    String HEADER_JOB_STATUS = "jobStatus";
    String HEADER_JOB_CURSOR = "jobCursor";

    String IBS_SOR_ID = "IBS";
    String IBS_OPERATION_HEADER = "IBSOperationId";
    String IBS_UUID_HEADER = "uuid";
    String BIC_OPERATION_ID = "postTransfers";

    //
    String PUT_BIC_OPERATION_URI = "direct:putInternalTransferPostingIxpid";
    String REQUEST_BODY = "requestBody";
    //
    String DEBIT_HARD_POSTING_INTERNAL_ID = "debitHardPostingInternalId";
    String CREDIT_HARD_POSTING_INTERNAL_ID = "creditHardPostingInternalId";
    
    
    String INTERNAL_POSTING_ID_HEADER = "internalPostId";
    
    String TRANSACTION_STATUS = "transactionStatus";

    String DEPOSIT_TRANSACTION_CONTROL_NUMBER = "transactionControlNumber";
    String DEBIT_TRANSACTION_CONTROL_NUMBER = "debitTransactionControlNumber";
    String CREDIT_TRANSACTION_CONTROL_NUMBER = "creditTransactionControlNumber";
    String DEPOSIT_TRANSACTION_SOURCE_CODE = "transactionSourceCode";
    String DEBIT_TRANSACTION_SOURCE_CODE = "debitTransactionSourceCode";
    String CREDIT_TRANSACTION_SOURCE_CODE = "creditTransactionSourceCode";


    String POSTED_DATE_PROPERTY = "postedDateProperty";
    String DEBIT_POSTED_DATE_PROPERTY = "debitPostedDateProperty";
    String CREDIT_POSTED_DATE_PROPERTY = "creditPostedDateProperty";
    String LIMIT_TIME_HEADER = "limitTime";
    String RETRIES_HEADER = "retries";


    String FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "fromLoanNoteOrDepositIdProperty";
    String TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "toLoanNoteOrDepositIdProperty";

    String FROM_ACCOUNT_NUMBER = "fromAccountNr";
    String TO_ACCOUNT_NUMBER = "toAccountNr";
    String STATUS_CODE = "statusCode";
    String CARD_PLATFORM_PROPERTY = "cardPlatform";
    String APP_PATH="appPath";

    String FAULT_ERROR_MESSAGE = "faultErrorMessage";
    String FAULT_ERROR_CODE = "faultErrorCode";
    String FAULT_CODE = "faultCode";
    String FAULT_MESSAGE = "faultMessage";
    String FAULT_STAGE = "faultStage";

    String APP_CODE = "applicationCode";
    String HEADER_MICROSERVICE = "microservice";
    String MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY = "shouldMarkPostingTableDebitAsFailed";
    String MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY = "shouldMarkPostingTableCreditFailed";
    String ACCOUNT_NR_HEADER = "accountNr";
    String NOTE_NR_HEADER = "noteNr";
    String CUSTOMER_NR_HEADER = "customerNr";
    String MISCELLANEOUS_ACCOUNT_APPLICATION_CODE_HEADER = "applicationIdCode";
    String NOTE_TRANSACTION_REPO_EFF_DATE_HEADER = "repositionEffectiveDate";
    String AUXILIAR_BODY = "auxBodyProperty";
    String REQUESTED_TRANSFER_AMOUNT = "requestedTransferAmount";
    String LOAN_CURRENT_BALANCE = "loanCurrentBalanceProperty";
    String TRANSACTION_CODE_PROPERTY = "transactionCodeProperty";
    String LOAN_NOTE_TRANSACTION_SEQ_NUMBER = "loanNoteTransactionSequenceNr";
    String LOAN_TRANSACTION_REVERSAL_IND = "R";

}
