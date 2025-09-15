package dev.miguel.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
         https://github.com/aws/aws-sdk-java-v2/issues/1932*/
@DynamoDbBean
public class MetricaEntity {

    private String metrica;
    private Integer cantidad;

    public MetricaEntity() {
    }

    public MetricaEntity(String metrica, Integer cantidad) {
        this.metrica = metrica;
        this.cantidad = cantidad;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("metrica")
    public String getMetrica() { return metrica; }
    public void setMetrica(String metrica) { this.metrica = metrica; }

    @DynamoDbAttribute("cantidad")
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
