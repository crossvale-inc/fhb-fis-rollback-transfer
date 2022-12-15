package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.junit.Test;

import com.fhb.fis.rollback.transfer.RoutesTestSupport;


public class DepositRollbackRouteBuilderTest extends RoutesTestSupport{


    @Test
    public void testingSuccess(){

        Exchange exchange = createExchangeWithBody("");

    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new DepositRollbackRouteBuilder();
    }
    
}
