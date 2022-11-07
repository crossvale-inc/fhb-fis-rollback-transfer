package com.fhb.fis.rollback.transfer.exceptions;

public enum ErrorCode {
    
    INVALID_USER_ACCOUNT("12", "invalid user account", "FHB400", "Invalid User Account"),
    CANNOT_DETERMINE_CARD_PLATFORM("12", "Cannot determine card platform", "FHB400", "Cannot determine card platform"),
    LOAN_TO_LOAN_NOT_ALLOWED("15", "Loan to Loan transfers not supported by Core", "FHB400", "Loan to Loan transfers not supported by Core"),
    TRANSFER_NOT_ALLOWED("13", "system unavailable", "FHB400", "Transfer not allowed"),
    SYSTEM_UNAVAILABLE("13", "system unavailable", "FHB500", "Internal Error"),
    UNEXPECTED_DEBIT_POST_ERROR("13", "system unavailable", "FHB501", "Internal error doing doing the debit post"),
    UNEXPECTED_CREDIT_POST_ERROR("13", "system unavailable", "FHB502", "Internal error doing doing the credit post"),
    INTRADAY_UNAVAILABLE("13", "system unavailable", "FHB503", "Intraday API not available"),
    ROLLBACK_DEBIT_POST_FAILED("13", "system unavailable", "FHB504", "Internal error doing doing the debit rollback"),
    ROLLBACK_DEBIT_BIC_FAILED("13", "system unavailable", "FHB505", "Internal error doing doing the update of the debit on the BIC table"),
    ROLLBACK_CREDIT_POST_FAILED("13", "system unavailable", "FHB506", "Internal error doing doing the credit rollback"),
    ROLLBACK_CREDIT_BIC_FAILED("13", "system unavailable", "FHB507", "Internal error doing doing the update of the debit on the BIC table");
    

    
    private String olbErrorCode;
    private String olbErrorDescription;
    private String code;
    private String description;
    
    ErrorCode(String olbErrorCode, String olbErrorDescription, String code, String description) {
        this.olbErrorCode = olbErrorCode;
        this.olbErrorDescription = olbErrorDescription;
        this.code = code;
        this.description = description;
    }

    public String getOLBErrorCode() {
        return olbErrorCode;
    }
    
    public String getOLBErrorDescription() {
        return olbErrorDescription;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    
}
