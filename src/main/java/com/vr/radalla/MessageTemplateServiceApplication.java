package com.vr.radalla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
		io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration.class,
		io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class
})
@EnableScheduling
public class MessageTemplateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageTemplateServiceApplication.class, args);
	}
}
