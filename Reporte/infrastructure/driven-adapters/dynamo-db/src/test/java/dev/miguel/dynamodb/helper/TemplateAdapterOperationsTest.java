package dev.miguel.dynamodb.helper;

import dev.miguel.dynamodb.DynamoDBTemplateAdapter;
import dev.miguel.dynamodb.MetricaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TemplateAdapterOperationsTest {

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DynamoDbAsyncTable<MetricaEntity> customerTable;

    private MetricaEntity metricaEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(dynamoDbEnhancedAsyncClient.table("table_name", TableSchema.fromBean(MetricaEntity.class)))
                .thenReturn(customerTable);

        metricaEntity = new MetricaEntity();
        metricaEntity.setId("id");
        metricaEntity.setAtr1("atr1");
    }

    @Test
    void modelEntityPropertiesMustNotBeNull() {
        MetricaEntity metricaEntityUnderTest = new MetricaEntity("id", "atr1");

        assertNotNull(metricaEntityUnderTest.getId());
        assertNotNull(metricaEntityUnderTest.getAtr1());
    }

    @Test
    void testSave() {
        when(customerTable.putItem(metricaEntity)).thenReturn(CompletableFuture.runAsync(()->{}));
        when(mapper.map(metricaEntity, MetricaEntity.class)).thenReturn(metricaEntity);

        DynamoDBTemplateAdapter dynamoDBTemplateAdapter =
                new DynamoDBTemplateAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoDBTemplateAdapter.save(metricaEntity))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetById() {
        String id = "id";

        when(customerTable.getItem(
                Key.builder().partitionValue(AttributeValue.builder().s(id).build()).build()))
                .thenReturn(CompletableFuture.completedFuture(metricaEntity));
        when(mapper.map(metricaEntity, Object.class)).thenReturn("value");

        DynamoDBTemplateAdapter dynamoDBTemplateAdapter =
                new DynamoDBTemplateAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoDBTemplateAdapter.getById("id"))
                .expectNext("value")
                .verifyComplete();
    }

    @Test
    void testDelete() {
        when(mapper.map(metricaEntity, MetricaEntity.class)).thenReturn(metricaEntity);
        when(mapper.map(metricaEntity, Object.class)).thenReturn("value");

        when(customerTable.deleteItem(metricaEntity))
                .thenReturn(CompletableFuture.completedFuture(metricaEntity));

        DynamoDBTemplateAdapter dynamoDBTemplateAdapter =
                new DynamoDBTemplateAdapter(dynamoDbEnhancedAsyncClient, mapper);

        StepVerifier.create(dynamoDBTemplateAdapter.delete(metricaEntity))
                .expectNext("value")
                .verifyComplete();
    }
}