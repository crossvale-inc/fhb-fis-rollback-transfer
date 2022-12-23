package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fhb.fis.camel.builder.OABServiceRouteBuilder;
import com.fhb.fis.kafka.filter.MessageFilterDate;
import com.fhb.fis.kafka.model.KafkaConstants;
import com.fhb.fis.kafka.processor.KafkaPropertiesFromHeaderProcessor;
import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.rollback.transfer.exceptions.ErrorCode;
import com.fhb.fis.rollback.transfer.exceptions.OrchestratedServiceException;
import com.fhb.fis.rollback.transfer.processors.Unexpected50XErrorPredicateFilter;
import com.fhb.fis.rollback.transfer.util.ApplicationCodes;
import com.fhb.fis.rollback.transfer.util.Constants;
import com.fhb.fis.rollback.transfer.util.TransactionStatus;

@Component
public class ReceiveKafkaEventRouteBuilder extends OABServiceRouteBuilder{
	
	//public static final String KAFKA_ENTRY_URI="{{kafka.rollback.uri}}";
    public static final String KAFKA_ENTRY_URI ="kafka:reversal.topic?brokers=localhost:9092&kafkaHeaderDeserializer=#kafkaHeaderDeserializerImpl";
	private static final String KAFKA_ENTRY_ID="R01_kafka";
	

	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveKafkaEventRouteBuilder.class);
	
	public static final String ROLLBACK_OPERATION_ID = "rollback";
	
	public static final String ROLLBACK_ENTRY_URI = "direct:"+ROLLBACK_OPERATION_ID;
	public static final String ROLLBACK_ENTRY_ID = "R02_"+ROLLBACK_OPERATION_ID;

    public static final String ROLLBACK_POSTING_TABLE_DEBIT_URI = "direct:rollback-posting-table-debit";
    public static final String ROLLBACK_POSTING_TABLE_DEBIT_ID = "RR02_rollback-posting-table-debit";

    public static final String ROLLBACK_POSTING_TABLE_CREDIT_URI = "direct:rollback-posting-table-credit";
    public static final String ROLLBACK_POSTING_TABLE_CREDIT_ID = "RR02_rollback-posting-table-credit";
    
    public static final String ROLLBACK_POSTING_TABLE_UPDATE_DEBIT_AS_POSTED_URI = "direct:rollback-update-posting-table-debit-posted";
    public static final String ROLLBACK_POSTING_TABLE_UPDATE_DEBIT_AS_POSTED_ID = "RR02_rollback-update-posting-table-debit-posted";
    
    public static final String ROLLBACK_POSTING_TABLE_UPDATE_CREDIT_AS_POSTED_URI = "direct:rollback-update-posting-table-credit-posted";
    public static final String ROLLBACK_POSTING_TABLE_UPDATE_CREDIT_AS_POSTED_ID = "RR02_rollback-update-posting-table-credit-posted";

    protected static final String DO_ROLLBACK_POSTING_TABLE_DEBIT_URI = "direct:do-rollback-posting-table-debit";
    protected static final String DO_ROLLBACK_POSTING_TABLE_DEBIT_ID = "RR03_do-rollback-posting-table-debit";

    protected static final String DO_ROLLBACK_DEBIT_OPERATION_URI = "direct:do-rollback-debit-operation";
    protected static final String DO_ROLLBACK_DEBIT_OPERATION_ID = "RR05_do-rollback-debit-operation";
    
    protected static final String DO_ROLLBACK_POSTING_TABLE_CREDIT_URI = "direct:do-rollback-posting-table-credit";
    protected static final String DO_ROLLBACK_POSTING_TABLE_CREDIT_ID = "RR03_do-rollback-posting-table-credit";

    protected static final String DO_ROLLBACK_CREDIT_OPERATION_URI = "direct:do-rollback-credit-operation";
    protected static final String DO_ROLLBACK_CREDIT_OPERATION_ID = "RR05_do-rollback-credit-operation";

    protected static final String DO_FAILED_ROLLBACK_POSTING_TABLE_DEBIT_URI = "direct:do-failed-rollback-posting-table-debit";
    protected static final String DO_FAILED_ROLLBACK_POSTING_TABLE_DEBIT_ID = "RR03_do-failed-rollback-posting-table-debit";
	
	public static final String ROLLBACK_DEBIT_URI = "direct:rollback-ibs-to-ibs-transfer";
    public static final String ROLLBACK_DEBIT_ID = "RR01_rollback-ibs-to-ibs-transfer";
    
    public static final String ROLLBACK_CREDIT_URI = "direct:rollback-credit";
    public static final String ROLLBACK_CREDIT_ID = "RR01_rollback-credit";

    protected static final String UNEXPECTED_CREDIT_POST_ERROR_URI = "direct:throw-50X-debit-business-exception";
    protected static final String UNEXPECTED_CREDIT_POST_ERROR_ID = "RR06_throw-50X-debit-business-exception";

    protected static final String UNEXPECTED_DEBIT_POST_ERROR_URI = "direct:throw-50X-credit-business-exception";
    protected static final String UNEXPECTED_DEBIT_POST_ERROR_ID = "RR06_throw-50X-credit-business-exception";
    
    protected static final String DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_URI = "direct:do-failed-rollback-posting-table-credit";
    protected static final String DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_ID = "RR03_do-failed-rollback-posting-table-credit";
    

	public final AggregationStrategy useOriginalAggregationStrategy;

	@Autowired
	public ReceiveKafkaEventRouteBuilder(AggregationStrategy useOriginalAggregationStrategy){
		this.useOriginalAggregationStrategy = useOriginalAggregationStrategy;
	}

	@Override
	public void configure() throws Exception {
        onException()
            .log(LoggingLevel.ERROR, LOGGER, "{\"Stage\":\"Error\", \"logMessage\": \"Exception controlled. HTTP=${exchangeProperty[statusCode]}, Error=${exchangeProperty[faultCode]}-${exchangeProperty[faultMessage]}\", \"method\":\"/${exchangeProperty["+Constants.APP_PATH+"]}.\", \"channel\":\"${header["+CommonInputHeader.APP_CODE+"]}\", \"customerNr\":\"${property[customerNr]}\", \"amount\":\"${property[amount]}\", \"fromAccount\":\"${property[fromAccount]}\", \"fromAccountType\":\"${property[fromAccountType]}\", \"toAccount\":\"${property[toAccount]}\", \"ToAccountType\":\"${property[toAccountType]}\", \"platform\":\"${header["+Constants.CARD_PLATFORM_PROPERTY+"]}\", \"debitBicId\":\"${property[debitHardPostingInternalId]}\", \"creditBicId\":\"${property[creditHardPostingInternalId]}\", \"statusCode\":\"${header["+Constants.STATUS_CODE+"]}\",\"ibsOperationId\":\"${property["+Constants.IBS_OPERATION_HEADER+"]}\", \"errorCode\":\"${header["+Constants.FAULT_CODE+"]}\", \"errorMessage\":\"${header["+Constants.FAULT_MESSAGE+"]}\"}")
            .to(KafkaRetryRouteBuilder.KAFKA_RETRY_URI)
        .end();
		super.configure();
		configureEntryRoute(from(KAFKA_ENTRY_URI)
				.routeId(KAFKA_ENTRY_ID));
		configureRollbackEntryRoute();
		configureDoRollback();
	}

	@Override
	public void configureEntryRoute(RouteDefinition fromKafka) {
		fromKafka
		.process(ex->{
			String a = "";
		})
		.setProperty(KafkaConstants.LIMIT_TIME_HEADER).header(KafkaConstants.LIMIT_TIME_HEADER)
		.setProperty(KafkaConstants.RETRIES_HEADER).header(KafkaConstants.RETRIES_HEADER)
		.log(LoggingLevel.INFO,LOGGER,"Initializing Kafka, headers: ${headers}, body:${body}")
		.choice()
			.when(method(MessageFilterDate.class,"isAfterHeaderLimit").isEqualTo(Boolean.FALSE))//Kafka to envelop wrapper
				.log(LoggingLevel.INFO,LOGGER,"Limit header false")
				.to(KAFKA_ENTRY_URI)
			.endChoice()
			.otherwise()
				.setProperty(Constants.KAFKA_BODY,body())
				.process("envelopeUnWrapper")
				.process(KafkaPropertiesFromHeaderProcessor.BEAN_NAME)
				.filter(header(KafkaConstants.ROLLBACK_OPERATION).isNotNull())
					.marshal().string()
					.log(LoggingLevel.INFO,LOGGER,"Receiving Kafka event headers: ${headers}, body:${body}")
					.to("${header["+KafkaConstants.ROLLBACK_OPERATION+"]}")
				.end()
			.endChoice()
		.end()
		.log(LoggingLevel.INFO,LOGGER,"Kafka event processed headers: ${headers}, body:${body}")
		;
	}

	public void configureRollbackEntryRoute(){
		from(ROLLBACK_DEBIT_URI)
		.id(ROLLBACK_DEBIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Starting debit operation rollback")
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		//set the debit bic table as cancelled or failed
		.choice()
			.when().method("bean:" + Unexpected50XErrorPredicateFilter.BEAN_ID)
				//error 50x: set the debit bic table as failed
				.enrich(DO_FAILED_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
				//unexpected 500 error: sending a b.ex
				.enrich(UNEXPECTED_DEBIT_POST_ERROR_URI, useOriginalAggregationStrategy)
			.endChoice()
			.otherwise()
				//set the debit bic table as cancelled
				.enrich(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
				//if failed, do the rollback to deposit (ibs)
				.filter(exchangeProperty(Constants.TRANSACTION_STATUS).isEqualTo(TransactionStatus.FAILED))
					.enrich(DO_ROLLBACK_DEBIT_OPERATION_URI, useOriginalAggregationStrategy)
				.end()
			.endChoice()
		.end()
		.log(LoggingLevel.INFO, LOGGER, "Finished debit operation rollback");

		from(ROLLBACK_CREDIT_URI)
		.id(ROLLBACK_CREDIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Starting credit operation rollback")
		.filter().method("bean:" + Unexpected50XErrorPredicateFilter.BEAN_ID)
			//variable to set the credit bic table as failed
			.setProperty("setCreditAsFailed", constant(true))
			//unexpected 500 error: sending a b.ex
			.enrich(UNEXPECTED_CREDIT_POST_ERROR_URI, useOriginalAggregationStrategy)
		.end()
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		//set the debit bic table as cancelled
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table DEBIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
		//set the debit bic table as cancelled
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table CREDIT")
		.choice()
			.when(exchangeProperty("setCreditAsFailed").isNotNull())
				//error 50x: set the credit bic table as failed
				.enrich(DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_URI, useOriginalAggregationStrategy)
			.endChoice()
			.otherwise()
				.enrich(DO_ROLLBACK_POSTING_TABLE_CREDIT_URI, useOriginalAggregationStrategy)
			.endChoice()
		.end()
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation")
		//always, do the rollback to debit
		.enrich(DO_ROLLBACK_DEBIT_OPERATION_URI, useOriginalAggregationStrategy)
		//if failed, do the rollback to credit
		.filter(exchangeProperty(Constants.TRANSACTION_STATUS).isEqualTo(TransactionStatus.FAILED))
			.enrich(DO_ROLLBACK_CREDIT_OPERATION_URI, useOriginalAggregationStrategy)
		.end()
		.log(LoggingLevel.INFO, LOGGER, "Finished credit operation rollback");

	from(ROLLBACK_POSTING_TABLE_DEBIT_URI)
		.id(ROLLBACK_POSTING_TABLE_DEBIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table DEBIT rollback")
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		.log(LoggingLevel.INFO, LOGGER, "Posting Table DEBIT rollback executed");
	

	from(ROLLBACK_POSTING_TABLE_CREDIT_URI)
		.id(ROLLBACK_POSTING_TABLE_CREDIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table CREDIT rollback")
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table DEBIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
		//Rollback debit operaton
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation")
		.enrich(DO_ROLLBACK_DEBIT_OPERATION_URI, useOriginalAggregationStrategy)
		.log(LoggingLevel.INFO, LOGGER, "Posting Table CREDIT rollback executed");

	from(ROLLBACK_POSTING_TABLE_UPDATE_DEBIT_AS_POSTED_URI)
		.routeId(ROLLBACK_POSTING_TABLE_UPDATE_DEBIT_AS_POSTED_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table DEBIT Update rollback")
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table DEBIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table CREDIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_CREDIT_URI, useOriginalAggregationStrategy)
		//Rollback debit operaton
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation")
		.enrich(DO_ROLLBACK_DEBIT_OPERATION_URI, useOriginalAggregationStrategy)
		//Rollback credit operaton
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback CREDIT Operation")
		.enrich(DO_ROLLBACK_CREDIT_OPERATION_URI, useOriginalAggregationStrategy)
		.log(LoggingLevel.INFO, LOGGER, "Posting Table DEBIT Update rollback executed");

	from(ROLLBACK_POSTING_TABLE_UPDATE_CREDIT_AS_POSTED_URI)
		.routeId(ROLLBACK_POSTING_TABLE_UPDATE_CREDIT_AS_POSTED_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table CREDIT Update rollback")
		.setProperty(Constants.ROLLBACK_REQUEST_BODY, body())
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table DEBIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI, useOriginalAggregationStrategy)
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback Posting table CREDIT")
		.enrich(DO_ROLLBACK_POSTING_TABLE_CREDIT_URI, useOriginalAggregationStrategy)
		//Rollback debit operaton
		.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation")
		.enrich(DO_ROLLBACK_DEBIT_OPERATION_URI, useOriginalAggregationStrategy)
		//Rollback credit operaton
		.enrich(DO_ROLLBACK_CREDIT_OPERATION_URI, useOriginalAggregationStrategy)
		.log(LoggingLevel.INFO, LOGGER, "Posting Table CREDIT Update rollback executed");
	}


	public void configureDoRollback(){
		
		from(DO_ROLLBACK_POSTING_TABLE_DEBIT_URI)
		.routeId(DO_ROLLBACK_POSTING_TABLE_DEBIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table Debit Rollback "
				+ "for record '${exchangeProperty["+ Constants.DEBIT_HARD_POSTING_INTERNAL_ID + "]}'")
		.setHeader(Constants.INTERNAL_POSTING_ID_HEADER,
				exchangeProperty(Constants.DEBIT_HARD_POSTING_INTERNAL_ID))
		.setBody(exchangeProperty(Constants.REQUEST_BODY))
		.to("{{jolt.hard.posting.cancel.debit.spec}}")
		.log(LoggingLevel.DEBUG, LOGGER, "Doing request to the Async BIC Update service: set DEBIT as CANCELLED")
		.to(AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI)
		.log(LoggingLevel.DEBUG, LOGGER, "Completed request to the Async BIC Update service: set DEBIT as CANCELLED");

		from(DO_ROLLBACK_DEBIT_OPERATION_URI)
		.log(LoggingLevel.INFO, LOGGER, "Executing rollback DEBIT Operation")
		//If the rollback of debit fails, record should be marked as failed
		.setProperty(Constants.MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY, constant(true))
		.choice()
			.when(exchangeProperty(Constants.FROM_APPLICATION_CODE).isEqualTo(ApplicationCodes.DEPOSITS))
				.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation - DEPOSITS")
				.enrich(DepositRollbackRouteBuilder.DO_ROLLBACK_DEBIT_DEPOSIT_URI, useOriginalAggregationStrategy)
			.endChoice()
			.when(PredicateBuilder.or(
					exchangeProperty(Constants.FROM_APPLICATION_CODE).isEqualTo(ApplicationCodes.LOANS),
					exchangeProperty(Constants.FROM_APPLICATION_CODE).isEqualTo(ApplicationCodes.COMMERCIAL_LOANS)))
				.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation - LOANS")
				.enrich(LoansRollbackRouteBuilder.DO_ROLLBACK_DEBIT_LOAN_URI, useOriginalAggregationStrategy)
			.endChoice()
			.when(exchangeProperty(Constants.FROM_APPLICATION_CODE).isEqualTo(ApplicationCodes.CARD))
				.log(LoggingLevel.INFO, LOGGER, "Execute rollback DEBIT Operation - CARDS")
				.enrich(CardsRollbackRouteBuilder.DO_ROLLBACK_DEBIT_CARD_URI, useOriginalAggregationStrategy)
			.endChoice()
		.end()
		.log(LoggingLevel.INFO, LOGGER, "Rollback DEBIT Operation executed")
		.log(LoggingLevel.INFO, LOGGER, "Debit rollback executed successfully, posting table record will not be marked as 'FAILED' if rollback process fails")
		.setProperty(Constants.MARK_POSTING_TABLE_DEBIT_AS_FAILED_PROPERTY, constant(false));
		

		from(DO_ROLLBACK_POSTING_TABLE_CREDIT_URI)
		.routeId(DO_ROLLBACK_POSTING_TABLE_CREDIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table Credit Rollback " 
				+ "for record '${exchangeProperty["+ Constants.CREDIT_HARD_POSTING_INTERNAL_ID + "]}'")
		.setHeader(Constants.INTERNAL_POSTING_ID_HEADER,
				exchangeProperty(Constants.CREDIT_HARD_POSTING_INTERNAL_ID))
		.setBody(exchangeProperty(Constants.REQUEST_BODY))
		.to("{{jolt.hard.posting.cancel.credit.spec}}")
		.log(LoggingLevel.DEBUG, LOGGER, "Doing request to the Async BIC Update service: set CREDIT as CANCELLED")
		.to(AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI)
		.log(LoggingLevel.DEBUG, LOGGER, "Completed request to the Async BIC Update service: set CREDIT as CANCELLED");

		//Rollback credit operaton
		from(DO_ROLLBACK_CREDIT_OPERATION_URI)
		.routeId(DO_ROLLBACK_CREDIT_OPERATION_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing rollback CREDIT Operation")
		//If the rollback of credit fails, record should be marked as failed
		.setProperty(Constants.MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY, constant(true))
		.choice()
			.when(exchangeProperty(Constants.TO_APPLICATION_CODE).isEqualTo(ApplicationCodes.DEPOSITS))
				.log(LoggingLevel.INFO, LOGGER, "Execute rollback CREDIT Operation - DEPOSITS")
				.enrich(DepositRollbackRouteBuilder.DO_ROLLBACK_CREDIT_DEPOSIT_URI, useOriginalAggregationStrategy)
			.endChoice()
			.when(PredicateBuilder.or(
					exchangeProperty(Constants.TO_APPLICATION_CODE).isEqualTo(ApplicationCodes.LOANS),
					exchangeProperty(Constants.TO_APPLICATION_CODE).isEqualTo(ApplicationCodes.COMMERCIAL_LOANS)))
				.log(LoggingLevel.INFO, LOGGER, "Execute rollback CREDIT Operation - LOANS")
				.enrich(LoansRollbackRouteBuilder.DO_ROLLBACK_CREDIT_LOAN_URI, useOriginalAggregationStrategy)
			.endChoice()
		.end()
		.log(LoggingLevel.INFO, LOGGER, "Rollback DEBIT Operation executed")
		.log(LoggingLevel.INFO, LOGGER, "Credit rollback executed successfully, posting table record will not be marked as 'FAILED' if rollback process fails")
		.setProperty(Constants.MARK_POSTING_TABLE_CREDIT_AS_FAILED_PROPERTY, constant(false));

		//Fail DEBIT Posting Table Record
		from(DO_FAILED_ROLLBACK_POSTING_TABLE_DEBIT_URI)
		.routeId(DO_FAILED_ROLLBACK_POSTING_TABLE_DEBIT_ID)
		.log(LoggingLevel.INFO, LOGGER, "Executing Posting Table Debit Failed Rollback"
				+ "for record '${exchangeProperty["+ Constants.DEBIT_HARD_POSTING_INTERNAL_ID + "]}'")
		.setHeader(Constants.INTERNAL_POSTING_ID_HEADER,
				exchangeProperty(Constants.DEBIT_HARD_POSTING_INTERNAL_ID))
		.setBody(exchangeProperty(Constants.REQUEST_BODY))
		.to("{{jolt.hard.posting.fail.debit.spec}}")
		.log(LoggingLevel.DEBUG, LOGGER, "Doing request to the Async BIC Update service: set DEBIT as FAILED")
		.to(AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI)
		.log(LoggingLevel.DEBUG, LOGGER, "Completed request to the Async BIC Update service: set DEBIT as FAILED");


        from(UNEXPECTED_DEBIT_POST_ERROR_URI)
            .id(UNEXPECTED_DEBIT_POST_ERROR_ID)
            .setProperty(Exchange.EXCEPTION_CAUGHT,constant(new OrchestratedServiceException(ErrorCode.UNEXPECTED_DEBIT_POST_ERROR)))
            .to(BusinessExceptionRouteBuilder.ENTRY_BUSINESS_EXCEPTION_URI);

        from(UNEXPECTED_CREDIT_POST_ERROR_URI)
            .id(UNEXPECTED_CREDIT_POST_ERROR_ID)
            .setProperty(Exchange.EXCEPTION_CAUGHT,constant(new OrchestratedServiceException(ErrorCode.UNEXPECTED_CREDIT_POST_ERROR)))
            .to(BusinessExceptionRouteBuilder.ENTRY_BUSINESS_EXCEPTION_URI);
		

        //Fail CREDIT Posting Table Record
        from(DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_URI)
            .routeId(DO_FAILED_ROLLBACK_POSTING_TABLE_CREDIT_ID)
            .log(LoggingLevel.INFO, LOGGER, "Executing Posting Table Credit Failed Rollback " 
                    + "for record '${exchangeProperty["+ Constants.CREDIT_HARD_POSTING_INTERNAL_ID + "]}'")
            .setHeader(Constants.INTERNAL_POSTING_ID_HEADER,
                    exchangeProperty(Constants.CREDIT_HARD_POSTING_INTERNAL_ID))
            .setBody(exchangeProperty(Constants.REQUEST_BODY))
            .to("{{jolt.hard.posting.fail.credit.spec}}")
            .log(LoggingLevel.DEBUG, LOGGER, "Doing request to the Async BIC Update service: set CREDIT as FAILED")
            .to(AsyncBicUpdateRouteBuilder.ASYNC_BIC_UPDATE_URI)
            .log(LoggingLevel.DEBUG, LOGGER, "Completed request to the Async BIC Update service: set CREDIT as FAILED");

	}
	

}
