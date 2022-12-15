package com.fhb.fis.rollback.transfer.processors;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spi.annotations.Component;

import com.fhb.fis.kafka.model.KafkaConstants;
@Component(SetPropertiesProcessor.BEAN_NAME)
public class SetPropertiesProcessor implements Processor{

    public static final String BEAN_NAME = "setPropertiesProcessor";

    @Override
    public void process(Exchange exchange) throws Exception {
        if(exchange.getIn().getHeader(KafkaConstants.PROPERTIES)!=null){
            Map<String, Object> properties = (Map<String, Object>) exchange.getIn().getHeader(KafkaConstants.PROPERTIES);
           properties.keySet().stream().forEach(key->exchange.setProperty(key, properties.get(key)));
        }
    }

}
