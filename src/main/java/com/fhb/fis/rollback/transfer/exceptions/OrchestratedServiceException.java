package com.fhb.fis.rollback.transfer.exceptions;

import java.util.Objects;


public class OrchestratedServiceException extends Exception {
    
	private static final long serialVersionUID = -2333567220821649985L;
	
	private final ErrorCode errorCode;

    public OrchestratedServiceException(ErrorCode errorCode) {
        
        super();
        
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode must not be null");
    }

    public OrchestratedServiceException(ErrorCode errorCode, String message) {
        
        super(message);
        
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode must not be null");
    }

    public OrchestratedServiceException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode must not be null");
    }

    
    public OrchestratedServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        
        this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode must not be null");
    }
    

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
