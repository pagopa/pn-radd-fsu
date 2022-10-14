package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.middleware.db.entities.RaddOperationIun;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.MappedTableResource;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

@Component
public class TransactWriterInitializer {

    private TransactWriteItemsEnhancedRequest.Builder builder;


    public void init(){
        this.builder = TransactWriteItemsEnhancedRequest.builder();
    }

    public void addRequestTransaction(
            MappedTableResource<RaddTransactionEntity> mappedTableResource, TransactPutItemEnhancedRequest<RaddTransactionEntity> request) {
        if (builder == null) return;
        this.builder.addPutItem(mappedTableResource, request);
    }

    public void addRequestOperationAndIun(
            MappedTableResource<RaddOperationIun> mappedTableResource, TransactPutItemEnhancedRequest<RaddOperationIun> request) {
        if (builder == null) return;
        this.builder.addPutItem(mappedTableResource, request);
    }

    public TransactWriteItemsEnhancedRequest build(){
        if (builder == null) return null;
        return this.builder.build();
    }


}
