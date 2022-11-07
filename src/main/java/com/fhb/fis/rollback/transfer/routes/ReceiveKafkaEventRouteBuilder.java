package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.kafka.filter.MessageFilterDate;
import com.fhb.fis.rollback.transfer.util.Constants;

@Component
public class ReceiveKafkaEventRouteBuilder extends OABServiceRouteBuilder{
	
	public static final String KAFKA_ENTRY_URI="{{kafka.rollback.uri}}";
	private static final String KAFKA_ENTRY_ID="R01_kafka";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveKafkaEventRouteBuilder.class);
	
	@Override
	public void configure() throws Exception {
		this.errorHandler(noErrorHandler());

		configureEntryRoute(from(KAFKA_ENTRY_URI)
				.routeId(KAFKA_ENTRY_ID));
	}

	@Override
	public void configureEntryRoute(RouteDefinition fromKafka) {
        fromKafka
			.choice()
				.when(method(MessageFilterDate.class,"isAfterHeaderLimit").isEqualTo(Boolean.FALSE))
					.to(KAFKA_ENTRY_URI)
				.endChoice()
				.otherwise()
					.log(LoggingLevel.INFO,LOGGER,"Receiving Kafka event")
					.to(Constants.PUT_BIC_OPERATION)
					.setProperty(Constants.FAULT_CODE).jsonpath("$.fault.code",true)
						.filter(exchangeProperty(Constants.FAULT_CODE).isNotNull())
							.to(KafkaRetryRouteBuilder.KAFKA_RETRY_URI)
						.end()
					.log(LoggingLevel.INFO,LOGGER,"Kafka event processed")
				.endChoice()
			.end()
			;
	}
	
}
