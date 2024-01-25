package it.pagopa.pn.radd.middleware.db;

import it.pagopa.pn.radd.config.BaseTest;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.OperationsIunsEntity;
import it.pagopa.pn.radd.middleware.db.entities.RaddTransactionEntity;
import it.pagopa.pn.radd.pojo.RaddTransactionStatusEnum;
import it.pagopa.pn.radd.utils.Const;
import it.pagopa.pn.radd.utils.OperationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static it.pagopa.pn.radd.exception.ExceptionTypeEnum.DATE_VALIDATION_ERROR;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RaddTransactionDAOImplTest extends BaseTest.WithLocalStack {

    private final Duration d = Duration.ofMillis(3000);
    @Autowired
    @SpyBean
    private RaddTransactionDAO raddTransactionDAO;
    private RaddTransactionEntity baseEntity;
    private final List<OperationsIunsEntity> iunsEntities = new ArrayList<>();

    @Mock
    DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    DynamoDbAsyncTable<RaddTransactionEntity> raddTable;

    @BeforeEach
    public void setUp() {
        baseEntity = new RaddTransactionEntity();
        baseEntity.setIun("iun");
        baseEntity.setUid("uid");
        baseEntity.setOperationId("operationId");
        baseEntity.setOperationType(OperationTypeEnum.ACT.toString());
        baseEntity.setStatus(Const.COMPLETED);
        baseEntity.setQrCode("qrcode12345");
        baseEntity.setRecipientId("recipientId");
        baseEntity.setFileKey("filekey1");
    }

    @Test
    void testCreateRaddTransaction() {

        RaddTransactionEntity response = raddTransactionDAO.createRaddTransaction(baseEntity, iunsEntities).block();
        assertNotNull(response);
        assertEquals(baseEntity.getOperationId(), response.getOperationId());
        assertEquals(baseEntity.getIun(), response.getIun());
        assertEquals(baseEntity.getUid(), response.getUid());
        assertEquals(Const.STARTED, response.getStatus());
        assertEquals(baseEntity.getRecipientType(), response.getRecipientType());
    }

    @Test
    void testUpdateStatus() {

        RaddTransactionEntity response = raddTransactionDAO.updateStatus(baseEntity, RaddTransactionStatusEnum.COMPLETED).block(d);
        assertNotNull(response);
        assertEquals(response.getOperationId(), baseEntity.getOperationId());
        assertEquals(response.getStatus(), baseEntity.getStatus());
    }

    @Test
    void testWhenGetActTransactionReturnEntity() {
        RaddTransactionEntity response = raddTransactionDAO.getTransaction("", "", "operationId", OperationTypeEnum.ACT).block();
        assertNotNull(response);
        assertEquals(response.getOperationType(), baseEntity.getOperationType());
    }

    @Test
    void testWhenGetActTransactionOnThrow() {

        StepVerifier.create(
                raddTransactionDAO.getTransaction("", "", "oper", OperationTypeEnum.ACT)
                ).expectError(RaddGenericException.class).verify();
    }

    @Test
    void testCountFromIunAndOperationIdAndStatus() {
        baseEntity.setIun("iun");
        baseEntity.setOperationId("operationId");
        raddTransactionDAO.updateStatus(baseEntity, RaddTransactionStatusEnum.COMPLETED).block();
        StepVerifier.create( raddTransactionDAO.countFromIunAndOperationIdAndStatus(baseEntity.getOperationId(), baseEntity.getIun()))
                    .expectNext(1)
                    .verifyComplete();
    }

    @Test
    void testGetTransactionsFromIun() {
        String iun = "iun";
        this.raddTransactionDAO.getTransactionsFromIun(iun)
                .map(transaction -> {
                    assertNotNull(transaction);
                    return Mono.empty();
                })
                .blockFirst();
    }

    @Test
    void testGetTransactionsFromFiscalCode() {
        String fiscalCode = "ABCDEF12G34H567I";
        this.raddTransactionDAO.getTransactionsFromFiscalCode(fiscalCode, new Date(), new Date())
                .map(transaction -> {
                    assertNotNull(transaction);
                    return Mono.empty();
                })
                .blockFirst();

        this.raddTransactionDAO.getTransactionsFromFiscalCode(fiscalCode, null, null)
                .map(transaction -> {
                    assertNotNull(transaction);
                    return Mono.empty();
                })
                .blockFirst();

        this.raddTransactionDAO.getTransactionsFromFiscalCode(fiscalCode, null, new Date())
                .map(transaction -> {
                    assertNotNull(transaction);
                    return Mono.empty();
                })
                .blockFirst();

        this.raddTransactionDAO.getTransactionsFromFiscalCode(fiscalCode, new Date(), null)
                .map(transaction -> {
                    assertNotNull(transaction);
                    return Mono.empty();
                })
                .blockFirst();
    }

    @Test
    void testGetTransactionsFromFiscalCodeError() throws ParseException {
        String fiscalCode = "ABCDEF12G34H567I";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
        String from = "03/11/2022";
        String to = "02/11/2022";
        Date dateFrom = formatter.parse(from);
        Date dateTo = formatter.parse(to);
        Flux<RaddTransactionEntity> response = raddTransactionDAO.getTransactionsFromFiscalCode(fiscalCode, dateFrom, dateTo);
        response.onErrorResume(exception -> {
            if (exception instanceof RaddGenericException){
                assertEquals(DATE_VALIDATION_ERROR.getMessage(), ((RaddGenericException) exception).getExceptionType().getMessage());
                return Mono.empty();
            }
            fail("Badly type exception");
            return Mono.empty();
        }).blockFirst();
    }

    @Test
    void testCountFromQrCodeCompleted() {
        CompletableFuture<QueryResponse> queryResponseCompletableFuture = CompletableFuture.completedFuture(QueryResponse.builder().count(1).build());
        Mockito.when(dynamoDbAsyncClient.query((QueryRequest) Mockito.any())).thenReturn(queryResponseCompletableFuture);
        Mono<Integer> entityMono = raddTransactionDAO.countFromQrCodeCompleted(Mockito.any());
        assertNotNull(entityMono);
        entityMono.map(entity -> {
            assertEquals(1, entity);
            return Mono.empty();
        });
    }

    @Test
    void testCreateTransactionWithOperationIunsNotEmpty() {
        String uid = "uid";
        RaddTransactionEntity raddTransactionEntity = new RaddTransactionEntity();
        raddTransactionEntity.setUid(uid);
        raddTransactionEntity.setOperationType(OperationTypeEnum.AOR.name());
        List<OperationsIunsEntity> entityIuns = new ArrayList<>();
        OperationsIunsEntity operationsIunsEntity = new OperationsIunsEntity();
        operationsIunsEntity.setTransactionId(baseEntity.getOperationId());
        operationsIunsEntity.setIun(baseEntity.getIun());
        entityIuns.add(operationsIunsEntity);
        Mono<RaddTransactionEntity> entityMono = raddTransactionDAO.createRaddTransaction(raddTransactionEntity, entityIuns);
        entityMono.map(entity -> {
            assertEquals(uid, entity.getUid());
            return Mono.empty();
        });
    }

    @Test
    void testCreateTransactionWithOperationIunsEmptyAndNull() {
        List<OperationsIunsEntity> entityIuns = null;
        Mono<RaddTransactionEntity> entityMono = raddTransactionDAO.createRaddTransaction(baseEntity, entityIuns);
        entityMono.map(entity -> {
            assertEquals(baseEntity.getUid(), entity.getUid());
            return Mono.empty();
        });

        entityIuns = new ArrayList<>();
        entityMono = raddTransactionDAO.createRaddTransaction(baseEntity, entityIuns);
        entityMono.map(entity -> {
            assertEquals(baseEntity.getUid(), entity.getUid());
            return Mono.empty();
        });
    }
}