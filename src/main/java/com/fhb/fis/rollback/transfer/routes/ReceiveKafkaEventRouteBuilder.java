package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.kafka.filter.MessageFilterDate;
import com.fhb.fis.kafka.model.KafkaConstants;

@Component
public class ReceiveKafkaEventRouteBuilder extends OABServiceRouteBuilder{
	
	public static final String KAFKA_ENTRY_URI="{{kafka.rollback.uri}}";
	private static final String KAFKA_ENTRY_ID="R01_kafka";
	

	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveKafkaEventRouteBuilder.class);
	
	public static final String ROLLBACK_OPERATION_ID = "rollback";
	
	public static final String ROLLBACK_ENTRY_URI = "direct:"+ROLLBACK_OPERATION_ID;
	public static final String ROLLBACK_ENTRY_ID = "R02_"+ROLLBACK_OPERATION_ID;
	
	@Override
	public void configure() throws Exception {
		this.errorHandler(noErrorHandler());
		super.configure();
		configureEntryRoute(from(KAFKA_ENTRY_URI)
				.routeId(KAFKA_ENTRY_ID));
		configureExecuteRollbackRoute(from(ROLLBACK_ENTRY_URI)
				.routeId(ROLLBACK_ENTRY_ID));
	}

	@Override
	public void configureEntryRoute(RouteDefinition fromKafka) {
        fromKafka
		.process("envelopeUnWrapper")
		.marshal().string()
		.log(LoggingLevel.INFO,LOGGER,"Initializing Kafka, headers: ${headers}, body:${body}")
		.choice()
			.when(method(MessageFilterDate.class,"isAfterHeaderLimit").isEqualTo(Boolean.FALSE))//Kafka to envelop wrapper
				.log(LoggingLevel.INFO,LOGGER,"Receiving Kafka event headers: ${headers}, body:${body}")
				.to(KAFKA_ENTRY_URI)
			.endChoice()
			.otherwise()
				.to(ROLLBACK_ENTRY_URI)
			.endChoice()
		.end()
		.log(LoggingLevel.INFO,LOGGER,"Kafka event processed headers: ${headers}, body:${body}")
		;
	}

	public void configureExecuteRollbackRoute(RouteDefinition fromEntry){
		fromEntry
		.log(LoggingLevel.INFO,LOGGER,"Receiving Kafka event headers:${headers}, body: ${body}")
		.choice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.DEPOSITS),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.CREDIT)))
				.to(DepositRollbackRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI)
			.endChoice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.DEPOSITS),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.DEBIT)))
				.to(DepositRollbackRouteBuilder.DO_ROLLBACK_DEBIT_DEPOSIT_URI)
			.endChoice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.LOANS),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.CREDIT)))
				.to("direct:LoansNoteRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI")
			.endChoice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.LOANS),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.DEBIT)))
				.to("direct:LoansNoteRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI")
			.endChoice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.LOANS_NOTE),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.CREDIT)))
				.to("direct:LoansNoteRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI")
			.endChoice()
			.when(PredicateBuilder.and(header(KafkaConstants.ACCCOUNT_TYPE).isEqualTo(KafkaConstants.LOANS_NOTE),header(KafkaConstants.TYPE).isEqualTo(KafkaConstants.DEBIT)))
				.to("direct:LoansNoteRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI")
			.endChoice()
		.end();
	}
	
}
