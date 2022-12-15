package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.junit.Test;

import com.fhb.fis.camel.processor.EnvelopeWrapperProcessor;
import com.fhb.fis.rollback.transfer.RoutesTestSupport;


public class ReceiveKafkaEventRouteBuilderTest extends RoutesTestSupport {

    
    @Test
    public void testingKafkaSuccess() throws Exception {

        String body = "body";

        EnvelopeWrapperProcessor envelopeWrapperProcessor = new EnvelopeWrapperProcessor();
        

        Exchange exchange = createExchangeWithBody(body);

        envelopeWrapperProcessor.process(exchange);

        

        template.sendBody("direct:rollback", exchange);


    }
    
    
    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new ReceiveKafkaEventRouteBuilder(new UseOriginalAggregationStrategy());
    }
    
}
