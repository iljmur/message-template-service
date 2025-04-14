package com.vr.radalla;

import com.vr.radalla.entity.MessageTemplateEntity;
import com.vr.radalla.repository.MessageTemplateRepository;
import com.vr.radalla.util.TestResourceUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageTemplateIntegrationTest {

    private final S3Client s3Client;
    private final SqsAsyncClient sqsClient;
    private final MessageTemplateRepository repository;

    private final String bucket = "s3-bucket";
    private final String key = "template.json";
    private final String queueUrl = "http://localhost:4566/000000000000/s3-notifications-queue";

    @Test
    void fullTrainDelayTemplateProcessingFlow() {
        // Step 1: Upload the template to S3
        String templateJson = TestResourceUtils.load("templates/train-delayed-template.json");
        byte[] bytes = templateJson.getBytes(StandardCharsets.UTF_8);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/json")
                        .contentLength((long) bytes.length)
                        .contentEncoding("identity")
                        .build(),
                RequestBody.fromBytes(bytes)
        );

        // Step 2: Send a simulated S3 notification to SQS
        String s3Event = TestResourceUtils.load("static/s3-notification.json");
        sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(s3Event)
                        .build()
        );

        // Step 3: Wait and assert template is saved in DynamoDB
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    MessageTemplateEntity entity = repository.findById("TRAIN_DELAYED");
                    assertThat(entity).isNotNull();
                    assertThat(entity.getTrafficType()).isNotBlank();
                    assertThat(entity.getSubject()).contains("{TRAIN_TYPE}", "{TRAIN_NUMBER}", "delayed");
                    assertThat(entity.getBody()).contains("{TRAIN_TYPE}", "{TRAIN_NUMBER}", "{DELAY_REASON}");
                });
    }

    @Test
    void testFindAllByTrafficType() {
        // Assume one item is already saved by previous test
        List<MessageTemplateEntity> results = repository.findAllByTrafficType("222-222-222");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getPk()).isEqualTo("TEMPLATE#TRAIN_DELAYED");
    }
}
