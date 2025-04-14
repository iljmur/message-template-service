package com.vr.radalla.repository;

import com.vr.radalla.entity.MessageTemplateEntity;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

@Repository
public class MessageTemplateRepository {

    private final DynamoDbTable<MessageTemplateEntity> table;

    public MessageTemplateRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("dynamodb-table", TableSchema.fromBean(MessageTemplateEntity.class));
    }

    public void save(MessageTemplateEntity entity) {
        table.putItem(entity);
    }

    public MessageTemplateEntity findById(String id) {
        return table.getItem(Key.builder()
                .partitionValue("TEMPLATE#" + id)
                .sortValue("META#v1")
                .build());
    }

    public List<MessageTemplateEntity> findAllByTrafficType(String trafficType) {
        DynamoDbIndex<MessageTemplateEntity> index = table.index("TrafficTypeIndex");

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(trafficType).build()))
                .build();

        return index.query(request)
                .stream()
                .flatMap(page -> page.items().stream())  // <--- this is the fix
                .toList();
    }
}
