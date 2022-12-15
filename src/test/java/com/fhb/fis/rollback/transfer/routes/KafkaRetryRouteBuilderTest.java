package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.junit.Test;

import com.fhb.fis.rollback.transfer.RoutesTestSupport;

public class KafkaRetryRouteBuilderTest extends RoutesTestSupport{


    @Test
    public void testRetry(){
        Exchange exchange = createExchangeWithBody("exchange");
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new KafkaRetryRouteBuilder();
    }
    
}
