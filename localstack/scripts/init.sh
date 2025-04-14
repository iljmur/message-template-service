#!/bin/bash
echo 'Running init script...'

awslocal s3 mb s3://s3-bucket --region eu-west-1

awslocal dynamodb create-table --cli-input-json file://../../../../../data/dynamodb-table-definition.json

awslocal sqs create-queue --queue-name generic-dlq

awslocal sqs create-queue --queue-name s3-notifications-queue --attributes "{\"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"arn:aws:sqs:eu-west-1:000000000000:generic-dlq\\\",\\\"maxReceiveCount\\\":\\\"1\\\"}\"}"

awslocal s3api put-bucket-notification --bucket s3-bucket --notification-configuration file://../../../../../data/s3-bucket-notifications.json
### purge queue to get rid of the test message raised by the bucket notification configuration
awslocal sqs purge-queue --queue-url http://sqs.eu-west-1.localhost:4566/000000000000/s3-notifications-queue

echo 'Resources ready.'
