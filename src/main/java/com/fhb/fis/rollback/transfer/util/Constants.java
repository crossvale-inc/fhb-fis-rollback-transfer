package com.fhb.fis.rollback.transfer.util;

public class Constants {
    private Constants(){ }
    
    public static final String IBS_BRIDGE = "{{ibs.bridge.endpoint}}";
    public static final String MAPPING_SERVICE_URL = "{{mapping.url}}";

    public static final String UUID_HEADER = "uuid";
    public static final String JOB_ID = "jobId";
    public static final String HEADER_JOB_STATUS = "jobStatus";
    public static final String HEADER_JOB_CURSOR = "jobCursor";

    public static final String IBS_SOR_ID = "IBS";
    public static final String IBS_OPERATION_HEADER = "IBSOperationId";
    public static final String IBS_UUID_HEADER = "uuid";
    public static final String BIC_OPERATION_ID = "postTransfers";

    //
    public static final String PUT_BIC_OPERATION = "direct:putInternalTransferPostingIxpid";
    public static final String REQUEST_BODY = "requestBody";
    //
    public static final String DEBIT_HARD_POSTING_INTERNAL_ID = "debitHardPostingInternalId";
    public static final String CREDIT_HARD_POSTING_INTERNAL_ID = "creditHardPostingInternalId";
    
    
    public static final String INTERNAL_POSTING_ID_HEADER = "internalPostId";
    
    public static final String TRANSACTION_STATUS = "transactionStatus";

    public static final String DEPOSIT_TRANSACTION_CONTROL_NUMBER = "transactionControlNumber";
    public static final String DEBIT_TRANSACTION_CONTROL_NUMBER = "debitTransactionControlNumber";
    public static final String CREDIT_TRANSACTION_CONTROL_NUMBER = "creditTransactionControlNumber";
    public static final String DEPOSIT_TRANSACTION_SOURCE_CODE = "transactionSourceCode";
    public static final String DEBIT_TRANSACTION_SOURCE_CODE = "debitTransactionSourceCode";
    public static final String CREDIT_TRANSACTION_SOURCE_CODE = "creditTransactionSourceCode";


    public static final String POSTED_DATE_PROPERTY = "postedDateProperty";
    public static final String DEBIT_POSTED_DATE_PROPERTY = "debitPostedDateProperty";
    public static final String CREDIT_POSTED_DATE_PROPERTY = "creditPostedDateProperty";
    public static final String LIMIT_TIME_HEADER = "limitTime";
    public static final String RETRIES_HEADER = "retries";


    public static final String FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "fromLoanNoteOrDepositIdProperty";
    public static final String TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "toLoanNoteOrDepositIdProperty";

    public static final String FROM_ACCOUNT_NUMBER = "fromAccountNr";
    public static final String TO_ACCOUNT_NUMBER = "toAccountNr";
    public static final String STATUS_CODE = "statusCode";
    public static final String CARD_PLATFORM_PROPERTY = "cardPlatform";
    public static final String APP_PATH="appPath";

    public static final String FAULT_ERROR_MESSAGE = "faultErrorMessage";
    public static final String FAULT_ERROR_CODE = "faultErrorCode";
    public static final String FAULT_CODE = "faultCode";
    public static final String FAULT_MESSAGE = "faultMessage";


    public static final String APP_CODE = "applicationCode";
    public static final String HEADER_MICROSERVICE = "microservice";

}
