package com.fhb.fis.rollback.transfer.routes;


import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fhb.fis.camel.builder.EntryRouteConfigurable;
import com.fhb.fis.camel.builder.MultiOrchestratedServiceRouteBuilder;
import com.fhb.fis.model.CacheConstants;
import com.fhb.fis.model.CommonInputHeader;
import com.fhb.fis.model.MessagingInfrastructureHeader;
import com.fhb.fis.rollback.transfer.util.Constants;

public class IBSServiceRouteBuilder extends RouteBuilder implements EntryRouteConfigurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBSServiceRouteBuilder.class);
    
    private final String mappingOperation;
    private final String version;
    private final String operationId;
    private final String operationUri;
    private final String bridgeEndpoint;

    public IBSServiceRouteBuilder(String mappingOperation, String version, String operationId, String bridgeEndpoint) {
        this.mappingOperation = mappingOperation;
        this.version = version;
        this.operationId = operationId;
        this.operationUri = "direct:" + operationId;
        this.bridgeEndpoint = bridgeEndpoint;
    }

	@Override
	public void configureEntryRoute(RouteDefinition routeDefinition) {

		routeDefinition
			.setProperty(MultiOrchestratedServiceRouteBuilder.OPERATION_ID_PROPERTY,
					constant(Constants.BIC_OPERATION_ID))
			.setProperty(MultiOrchestratedServiceRouteBuilder.ATOMIC_ENDPOINT_PROPERTY,
					simple(bridgeEndpoint))
			.setHeader(Constants.IBS_OPERATION_HEADER,
					constant(operationId))
			.setProperty(Constants.IBS_OPERATION_HEADER,header(Constants.IBS_OPERATION_HEADER))
			.setHeader(Constants.UUID_HEADER,
			        header(CommonInputHeader.AUDIT_TRAIL_ID))
			.setProperty(MessagingInfrastructureHeader.EXTERNAL_MAPPING_URL, 
					simple("{{mapping.url}}"))
			.setProperty(MessagingInfrastructureHeader.EXTERNAL_MAPPING_SOR,
					constant(Constants.IBS_SOR_ID))
			.setProperty(MessagingInfrastructureHeader.EXTERNAL_MAPPING_OPERATION, 
					constant(mappingOperation))
			.setProperty(MessagingInfrastructureHeader.EXTERNAL_MAPPING_VERSION,
					simple(version))
			.choice()
				.when(exchangeProperty(CacheConstants.USE_CACHE_FLAG).isNotNull())
					.setHeader(CacheConstants.USE_CACHE_FLAG,exchangeProperty(CacheConstants.USE_CACHE_FLAG))
					.log(LoggingLevel.DEBUG, LOGGER, "useCache flag set to ${exchangeProperty["+CacheConstants.USE_CACHE_FLAG+"]} for operation " + this.operationId )
				.endChoice()
				.otherwise()
					.setHeader(CacheConstants.USE_CACHE_FLAG,constant(CacheConstants.CACHE_DISABLED))
					.log(LoggingLevel.DEBUG, LOGGER, "useCache disabled for operation " + this.operationId )
				.endChoice()
			.end()
			.to(MultiOrchestratedServiceRouteBuilder.ENTRY_URI);

	}

	@Override
	public void configure() throws Exception {

		this.errorHandler(noErrorHandler());

		this.configureEntryRoute(from(operationUri).routeId("R00_" + operationId));
	}

}
