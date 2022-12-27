package com.fhb.fis.rollback.transfer.routes;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.junit.Test;

import com.fhb.fis.camel.processor.EnvelopeWrapperProcessor;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.rollback.transfer.RoutesTestSupport;


public class ReceiveKafkaEventRouteBuilderTest extends RoutesTestSupport {

    
    @Test
    public void testingKafkaSuccess() throws Exception {

        String body = "body";

        EnvelopeWrapperProcessor envelopeWrapperProcessor = new EnvelopeWrapperProcessor();
        
        Map<String,Object> properties = new HashMap<String, Object>();

        properties.put("propiedad1", "propiedad1");
        properties.put("propiedad2", 2);
        properties.put("propiedad3", new String[]{"1","2","3"});

        Exchange exchange = createExchangeWithBody(body);

        MockEndpoint mockRetryEndpoint = getMockEndpoint("mock:"+ KafkaRetryRouteBuilder.KAFKA_RETRY_URI);

        MockEndpoint mockAsyncEndpoint = getMockEndpoint("mock:"+ AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI);

        exchange.setProperty(KafkaConstants.ROLLBACK_OPERATION, ReceiveKafkaEventRouteBuilder.DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_URI);

        envelopeWrapperProcessor.process(exchange);

        mockRetryEndpoint.expectedMessageCount(0);

        mockAsyncEndpoint.expectedMessageCount(1);

        template.send("direct:rollback", exchange);


    }
    
    
    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new ReceiveKafkaEventRouteBuilder(new UseOriginalAggregationStrategy());
    }
    
    @Override
    public String isMockEndpointsAndSkip() {
        return KafkaRetryRouteBuilder.KAFKA_RETRY_URI +"|" +AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI;
    }

}
