package com.fhb.fis.rollback.transfer.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.util.Constants;

@Component(value = Unexpected50XErrorPredicateFilter.BEAN_ID)
public class Unexpected50XErrorPredicateFilter implements Predicate {
	
	public static final String BEAN_ID = "unexpected50XErrorPredicateFilter";
    private static final Logger LOGGER = LoggerFactory.getLogger(Unexpected50XErrorPredicateFilter.class);

	@Override
	public boolean matches(Exchange exchange) {
		
		String httpError = exchange.getProperty(Constants.STATUS_CODE, String.class);
		Boolean error50x = false;
		
        if (httpError!=null) {
            try {
                error50x = (Integer.parseInt(httpError) >= 500);
            } catch (Exception ex) {
                String uid = exchange.getIn().getHeader(CommonInputHeader.AUDIT_TRAIL_ID,String.class);
                LOGGER.warn("uid={} - Unsable to determine http error code: {}", uid, httpError);
            }
        }
		return error50x;
	}
}
