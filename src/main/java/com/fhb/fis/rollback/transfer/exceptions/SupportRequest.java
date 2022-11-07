package com.fhb.fis.rollback.transfer.exceptions;

public enum SupportRequest {

    REVIEW_DEBIT("DRev", "Review if the debit was posted and reverse if necessary", "Review if debit was posted"),
    REVIEW_CREDIT("CRev", "Review if the credit was posted and reverse if necessary", "Review if credit was posted"),
    DO_ROLLBACK_DEBIT("DRb", "The debit was posted but we couldn't reverse it: reversal required", "Reverse debit"),
    DO_ROLLBACK_CREDIT("CRb", "The credit was posted but we couldn't reverse it: reversal required", "Reverse credit"),
    SET_BIC_DEBIT_CANCELLED("DBC", "The debit was reversed but we couldn't update the status in the BIC Posting table: update debit's record with status=CANCELLED", "Update BIC debit record"),
    SET_BIC_CREDIT_CANCELLED("CBC", "The credit was reversed but we couldn't update the status in the BIC Posting table: update credit's record with status=CANCELLED", "Update BIC debit record");

    private String reviewCode;
    private String reviewMessage;
    private String reviewSubject;

    private SupportRequest(String reviewCode, String reviewMessage, String reviewSubject) {
        this.reviewCode = reviewCode;
        this.reviewMessage = reviewMessage;
        this.reviewSubject = reviewSubject;
    }

    public String getReviewCode() {
        return reviewCode;
    }

    public void setReviewCode(String reviewCode) {
        this.reviewCode = reviewCode;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public String getReviewSubject() {
        return reviewSubject;
    }

    public void setReviewSubject(String reviewSubject) {
        this.reviewSubject = reviewSubject;
    }
    
}
