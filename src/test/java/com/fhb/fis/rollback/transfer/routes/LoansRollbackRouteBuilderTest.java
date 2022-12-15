package com.fhb.fis.rollback.transfer.routes;

import org.apache.camel.RoutesBuilder;

import com.fhb.fis.camel.aggregate.MergeExchangeBodyAggregationStrategy;
import com.fhb.fis.camel.aggregate.PreservePropertiesAggregationStrategy;
import com.fhb.fis.rollback.transfer.RoutesTestSupport;
import com.fhb.fis.rollback.transfer.util.Constants;

public class LoansRollbackRouteBuilderTest extends RoutesTestSupport{
    
    String INTRADAY_RES = "{ \"payload\" : { \"intradayTransactionDataList\" : [ { \"depositTransactionDate\" : \"24/12/2021\", \"depositTransactionControl\" : 11111111111 }, { \"depositTransactionDate\" : \"08/14/2020\", \"depositTransactionControl\" : \"001000000000002\" } ] } }";
    
    public void testingLoanRollback(){
        
    }


    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new LoansRollbackRouteBuilder(new PreservePropertiesAggregationStrategy(new MergeExchangeBodyAggregationStrategy(), 
        Constants.TRANSACTION_STATUS));
    }
    
}
