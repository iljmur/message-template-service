package com.vr.radalla.listener;

import com.vr.radalla.entity.MessageTemplateEntity;
import com.vr.radalla.model.MessageTemplate;
import com.vr.radalla.repository.MessageTemplateRepository;
import com.vr.radalla.transform.CmsTemplateTransformer;
import com.vr.radalla.util.S3NotificationParser;
import com.vr.radalla.util.S3NotificationParser.S3ObjectInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPollingListener {

    private final S3Client s3Client;
    private final SqsAsyncClient sqsAsyncClient;
    private final MessageTemplateRepository repository;

    private static final String QUEUE_URL = "http://localhost:4566/000000000000/s3-notifications-queue";

    @Scheduled(fixedDelay = 3000)
    public void pollMessages() {
        try {
            sqsAsyncClient.receiveMessage(ReceiveMessageRequest.builder()
                            .queueUrl(QUEUE_URL)
                            .maxNumberOfMessages(1)
                            .waitTimeSeconds(1)
                            .build()
                    ).get(3, TimeUnit.SECONDS)
                    .messages()
                    .forEach(message -> {
                        log.info("📥 Received SQS message:\n{}", message.body());

                        try {
                            S3ObjectInfo info = S3NotificationParser.parseS3Event(message.body());

                            log.info("📦 S3 Event — Bucket: {}, Key: {}", info.bucket(), info.key());

                            // Download file
                            GetObjectRequest request = GetObjectRequest.builder()
                                    .bucket(info.bucket())
                                    .key(info.key())
                                    .build();

                            ResponseBytes<?> objectBytes = s3Client.getObject(request, ResponseTransformer.toBytes());
                            String fileContent = objectBytes.asUtf8String();

                            MessageTemplate template = CmsTemplateTransformer.transform(fileContent);
                            log.info("✅ Transformed Template:\n{}", template);

                            MessageTemplateEntity entity = MessageTemplateEntity.from(template);

                            log.info("💾 About to save entity: pk={}, sk={}, name={}, trafficType={}, subject={}, body={}",
                                    entity.getPk(), entity.getSk(), entity.getName(), entity.getTrafficType(),
                                    entity.getSubject(), entity.getBody());

                            System.out.println("➡️ Saving to DynamoDB:");
                            System.out.printf("    PK: [%s]%n", entity.getPk());
                            System.out.printf("    SK: [%s]%n", entity.getSk());
                            System.out.printf("    NAME: [%s]%n", entity.getName());
                            System.out.printf("    SUBJECT: [%s]%n", entity.getSubject());
                            System.out.printf("    BODY: [%s]%n", entity.getBody());
                            repository.save(entity);

                            repository.save(entity);
                            log.info("💾 Template saved to DynamoDB with PK={}", entity.getPk());

                        } catch (Exception e) {
                            log.error("❌ Error processing SQS message", e);
                        }
                    });

        } catch (Exception e) {
            log.error("❌ Error polling SQS", e);
        }
    }
}
