package com.vr.radalla.entity;

import com.vr.radalla.model.MessageTemplate;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTemplateEntity {

    @Getter(onMethod_ = {@DynamoDbPartitionKey, @DynamoDbAttribute("PK")})
    @Setter
    private String pk;

    @Getter(onMethod_ = {@DynamoDbSortKey, @DynamoDbAttribute("SK")})
    @Setter
    private String sk;

    private String name;

    @Getter(onMethod_ = {
            @DynamoDbAttribute("TrafficType"),
            @DynamoDbSecondaryPartitionKey(indexNames = "TrafficTypeIndex")
    })
    private String trafficType;
    private String subject;
    private String body;

    public static MessageTemplateEntity from(MessageTemplate template) {
        return MessageTemplateEntity.builder()
                .pk("TEMPLATE#" + template.getId())
                .sk("META#v1")
                .name(template.getName())
                .trafficType(template.getTrafficType())
                .subject(template.getSubject())
                .body(template.getBody())
                .build();
    }
}