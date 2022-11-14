package com.fhb.fis.rollback.transfer.util;

public interface FundsTransferConstants {
    
    String MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY = "shouldMarkPostingTableDebitAsFailed";
    String MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY = "shouldMarkPostingTableCreditFailed";

	String CREDIT_API_UNAVAILABLE_FLAG= "creditApiUnavailable";
	
	String LOAN_ILN = "ILN";
	String LOAN_ODP = "ODP";
	String LOAN_LOC = "LOC";
	String LOAN_LOC_NTE = "LOC NTE";
	
	String DEPOSIT_DDA = "DDA";
	String DEPOSIT_MMA = "MMA";
	String DEPOSIT_SAV = "SAV";
	
	String CREDIT_CARD_CCD = "CCD";

	String MORTGAGE = "MTG";
	
    Integer CARD_NUMBER_LENGTH = 16;
    
    String OUTPUTHEADER_RESULT = "outputHeaderResultProperty";
    
	String ONLINE_BANKING_APPLICATION_CODE = "OLB";
    String FUNDS_TRANSFERS_OPERATION_ID = "postTransfers";
    String OLB_FUNDS_TRANSFERS_OPERATION_ID = "olbFundsTransfers";
    
    String Q2_CHANNEL_APPLICATION_CODE = "Q2";
    String IBS_SOR_ID = "IBS";
    String IBS_OPERATION_HEADER = "IBSOperationId";
    String IBS_UUID_HEADER = "uuid";

    String TSYS_SOR_ID = "TSYS";

    String ROLLBACK_REQUEST_BODY = "rollbackRequestBody";
    String REQUEST_BODY = "requestBody";
    String SOURCE_FROM_APPLICATION_CODE = "sourceFromApplicationCode";
    String SOURCE_FROM_APPLICATION_CODE_FIRST_3 = "sourceFromApplicationCodeFirst3";
    String SOURCE_TO_APPLICATION_CODE = "sourceToApplicationCode";
    String SOURCE_TO_APPLICATION_CODE_FIRST_3 = "sourceToApplicationCodeFirst3";
    String FROM_APPLICATION_CODE = "fromApplicationCode";
    String TO_APPLICATION_CODE = "toApplicationCode";
    String CUSTOMER_NUMBER = "customerNr";
    String FROM_ACCOUNT_NUMBER = "fromAccountNr";
    String FROM_ACCOUNT_NUMBER_LAST_4 = "fromAccountNrLast4";
    String TO_ACCOUNT_NUMBER = "toAccountNr";
    String TO_ACCOUNT_NUMBER_LAST_4 = "toAccountNrLast4";
    String FROM_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "fromLoanNoteOrDepositIdProperty";
    String TO_LOAN_NOTE_OR_DEPOSIT_ID_PROPERTY = "toLoanNoteOrDepositIdProperty";
    String FROM_CARD_AUTHORIZATION_TYPE_PROPERTY = "fromCardAuthorizationTypeProperty";
    String TRANSACTION_POSTED_TYPE_PROPERTY = "transactionPostedTypeProperty";
    String TRANSACTION_HAWAII_TIMESTAMP = "transactionHawaiiTimestamp";
    String TRANSACTION_CODE_PROPERTY = "transactionCodeProperty";
    String DEBIT_TRANSACTION_CODE_PROPERTY = "debitTransactionCodeProperty";
    String CREDIT_TRANSACTION_CODE_PROPERTY = "crebitTransactionCodeProperty";
    String DEBIT_TRANSACTION_DESCRIPTION_PROPERTY = "debitTransactionDescriptionProperty";
    String CREDIT_TRANSACTION_DESCRIPTION_PROPERTY = "crebitTransactionDescriptionProperty";
    String POSTED_DATE_PROPERTY = "postedDateProperty";
    String DEBIT_POSTED_DATE_PROPERTY = "debitPostedDateProperty";
    String CREDIT_POSTED_DATE_PROPERTY = "creditPostedDateProperty";
    String REQUESTED_TRANSFER_AMOUNT = "requestedTransferAmount";
    String REQUESTED_TRANSFER_AUTHORIZATION_TYPE = "requestedTransferAuthorizationType";
    String DEBIT_ACCOUNT_BALANCE = "debitAccountBalance";
    String DEBIT_CARD_AUTHORIZATION_ID = "debitCardAuthorizationId";
    String LOAN_NOTE_TRANSACTION_SEQ_NUMBER = "loanNoteTransactionSequenceNr";
    
    String LOAN_CURRENT_BALANCE = "loanCurrentBalanceProperty";
    String DEBIT_LOAN_CURRENT_BALANCE = "debitLoanCurrentBalanceProperty";
    String CREDIT_LOAN_CURRENT_BALANCE = "creditLoanCurrentBalanceProperty";
    
    String DEBIT_HARD_POSTING_INTERNAL_ID = "debitHardPostingInternalId";
    String CREDIT_HARD_POSTING_INTERNAL_ID = "creditHardPostingInternalId";
    
    String ACCOUNT_NUMBER_TO_VALIDATE = "accountNrToValidate";
    
    String TRANSACTION_STATUS = "transactionStatus";
    String TRANSACTION_ERROR_CODE = "transactionErrorCode";
    
    String FROM_CARD_CUSTOMER_TYPE_PROPERTY = "fromCardCustomerType";
    String TO_CARD_CUSTOMER_TYPE_PROPERTY = "toCardCustomerType";
    
    String CARD_CUSTOMER_TYPE_PROPERTY = "cardCustomerType";
    String TSYS_OPERATION_TYPE_PROPERTY = "tsysOperationType";
    
    String FROM_CARD_EXPIRATION_DATE_PROPERTY = "fromCardExpirationDateProperty";   
    String CALL_WITH_ROLLBACK_ROLLBACK_URI= "rollbackUriProperty";
    String CALL_WITH_ROLLBACK_OPERATION_URI= "operationUriProperty";
    
    String INTERNAL_POSTING_ID_HEADER = "internalPostId";
    String CUSTOMER_NR_HEADER = "customerNr";
    String ACCOUNT_NR_HEADER = "accountNr";
    String NOTE_NR_HEADER = "noteNr";
    String NOTE_TRANSACTION_REPO_EFF_DATE_HEADER = "repositionEffectiveDate";
    String MISCELLANEOUS_ACCOUNT_APPLICATION_CODE_HEADER = "applicationIdCode";

    String BILl_DUE_DATE_PROPERTY = "billDueDateProperty";   
    String BILL_DUE_DATE_FORMAT = "yyyy-MM-dd";
    
    String POSTING_TABLE_TIMESTAMP_FORMAT = "MMddyyyy HH:mm:ss";
    
    String HAWAII_TIME_ZONE = "US/Hawaii";
    
    String CARD_AUTHORIZATION_RESPONSE_PROPERTY = "cardAuthorizationResponse";
    String SUCCESSFUL_CARD_AUTHORIZATION_RESPONSE = "Approved";
    String SUCCESSFUL_CARD_AUTHORIZATION_REVERSED = "REVERSED";
    
    String OLB_PROCESS_DATE_PROPERTY = "olbProcessDateProperty";
    String OLB_FROM_ACCOUNT_TYPE_PROPERTY = "olbFromAccountTypeProperty";
    String OLB_TO_ACCOUNT_TYPE_PROPERTY = "olbToAccountTypeProperty";
    String OLB_FROM_ACCOUNT_CODE_PROPERTY = "olbFromAccountCodeProperty";
    String OLB_TO_ACCOUNT_CODE_PROPERTY = "olbToAccountCodeProperty"; 
    String OLB_FROM_ACCOUNT_NR = "olbFromAccountNr";
    String OLB_FROM_NOTE_NR = "olbFromNoteNr";
    String OLB_TO_ACCOUNT_NR = "olbToAccountNr";
    String OLB_TO_NOTE_NR = "olbToNoteNr";
    
    String CARD_PLATFORM_PROPERTY = "cardPlatformProperty";
    String CARD_PLATFORM_COMMERCIAL = "Commercial";
    String CARD_PLATFORM_CONSUMER = "Consumer";
    
    String DEPOSIT_TRANSACTION_CONTROL_NUMBER = "transactionControlNumber";
    String DEBIT_TRANSACTION_CONTROL_NUMBER = "debitTransactionControlNumber";
    String CREDIT_TRANSACTION_CONTROL_NUMBER = "creditTransactionControlNumber";
    
    String DEPOSIT_TRANSACTION_SOURCE_CODE = "transactionSourceCode";
    String DEBIT_TRANSACTION_SOURCE_CODE = "debitTransactionSourceCode";
    String CREDIT_TRANSACTION_SOURCE_CODE = "creditTransactionSourceCode";
    
    String INTRADAY_ACCOUNT_PAGE_NUMBER_HEADER = "repoIndFl";
    String INTRADAY_SEARCH_AMOUNT_FILTER = "searchAmount";
    String INTRADAY_MORE_TRANSACTIONS_IND = "moreTransactionsInd";
    
    String FAULT_ERROR_MESSAGE = "faultErrorMessage";
    String FAULT_ERROR_CODE = "faultErrorCode";
    String FAULT_CODE = "faultCode";
    String FAULT_MESSAGE = "faultMessage";
    
    //Indicates if a loan note transaction has been reversed
    String LOAN_TRANSACTION_REVERSAL_IND = "R";
    
    String FROM_DATE_PROPERTY = "fromProperty";
    String TO_DATE_PROPERTY = "toProperty";

    String INTRADAY_UNAVAILABLE = "intradyNotAvailable";

    String FROM_BANK = "fromBank";
    String TO_BANK = "toBank";

    String GUAM_BRANCHES = "guamBranches";

    String HAWAII = "HI";
    String GUAM = "GU";

    String FROM_ACCOUNT = "fromAccount";
    String TO_ACCOUNT = "toAccount";
    
    //Indicates if the exception has to be sent to businessmanager
    String BUSINESS_EXCEPTION_NOTIFIER = "notifyException";
    String STATUS_CODE = "statusCode";

}
