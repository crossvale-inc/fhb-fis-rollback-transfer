package com.fhb.fis.rollback.transfer.util;

public enum TransactionStatus {
    
    LOAN_CREDIT_API_UNAVAILABLE("POSTED", true),
    POSTED("POSTED", true),
    UNFUNDED("UNFUNDED", false),
    REJECTED("REJECTED", false),
    FAILED("REJECTED", false);
    
    private String category;
    private Boolean sucessful;
    
    private TransactionStatus(String category, Boolean successful) {
        this.category = category;
        this.sucessful = successful;
    }

    public String getCategory() {
        return category;
    }
    
    public Boolean isSuccessful() {
        return this.sucessful;
    }

}
