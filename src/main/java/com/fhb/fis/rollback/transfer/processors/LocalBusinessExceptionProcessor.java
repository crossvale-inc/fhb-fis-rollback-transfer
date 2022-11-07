package com.fhb.fis.rollback.transfer.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.model.BusinessExceptionHeader;
import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.exceptions.OrchestratedServiceException;
import com.fhb.fis.rollback.transfer.exceptions.SupportRequest;

@Component(LocalBusinessExceptionProcessor.BEAN_NAME)
public class LocalBusinessExceptionProcessor implements Processor{

    public static final String BEAN_NAME = "LocalBusinessExceptionProcessor";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Exchange exchange) throws Exception {

        if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT)!=null && exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class) instanceof OrchestratedServiceException){
            //determine the support request, if needed
            SupportRequest supportRequest = null;
            OrchestratedServiceException ex = (OrchestratedServiceException) exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            if (ex.getErrorCode() == ErrorCode.UNEXPECTED_DEBIT_POST_ERROR) {
                supportRequest = SupportRequest.REVIEW_DEBIT;
            } else if (ex.getErrorCode() == ErrorCode.UNEXPECTED_CREDIT_POST_ERROR) {
                supportRequest = SupportRequest.REVIEW_CREDIT;
            } else if (ex.getErrorCode() == ErrorCode.ROLLBACK_DEBIT_POST_FAILED) {
                supportRequest = SupportRequest.DO_ROLLBACK_DEBIT;
            } else if (ex.getErrorCode() == ErrorCode.ROLLBACK_DEBIT_BIC_FAILED) {
                supportRequest = SupportRequest.DO_ROLLBACK_CREDIT;
            } else if (ex.getErrorCode() == ErrorCode.ROLLBACK_CREDIT_POST_FAILED) {
                supportRequest = SupportRequest.SET_BIC_DEBIT_CANCELLED;
            } else if (ex.getErrorCode() == ErrorCode.ROLLBACK_CREDIT_BIC_FAILED) {
                supportRequest = SupportRequest.SET_BIC_CREDIT_CANCELLED;
            }
            if (supportRequest!=null) {
                logger.info("Setting support property");
                exchange.setProperty("supportRequest", supportRequest);
                exchange.getIn().setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_SUBJECT, supportRequest.getReviewSubject());
                exchange.getIn().setHeader(BusinessExceptionHeader.HEADER_BUSINESS_EX_MESSAGE, supportRequest.getReviewMessage());
            }
        }
    }
}
