# Context

Enclosed, there is a JSON file (`template.json`) produced by a 3rd party Content Management System (CMS). The file represents a communication message template that is used to generate communication messages to be sent to customers.

These message template files are continuously being backed up in an AWS S3 bucket, which is configured to raise S3 bucket notifications to an SQS queue whenever a new file is uploaded to it.

## JSON structure

On root level, the main attributes of interest are `items` and `includes`. 

The `items` attribute is an array that contains only 1 element which represents the communication message template. The element consists of 5 main attributes (under `fields` attribute; `metadata` and `sys` attributes are to be considered as CMS specific attributes):
* `key` - technical entity key, value is a plain string
* `name` - template name in CMS system, value is a plain string
* `trafficType` - traffic type for which the template is applicable, value is a linked node
* `subject` - parametrized message subject, value is a document node
* `body` - parametrized message body, value is a document node

The content of these attributes is in control of system users.

Document node represents a rich text document structure that can contain multiple paragraphs. Each paragraph further consists of either plain text nodes or linked embedded entry nodes. 

The `includes` attribute is an array that contains all linked elements included in the message template. Structure of these can vary, but all of them will contain a `key` attribute similar to template.

# Functional requirements

Implement a web-service that listens to the SQS queue and processes the incoming S3 notifications by reading the JSON file from the S3 bucket, transforming the message template from CMS system structure to domain structure and ultimately storing the message template in AWS DynamoDB table.

If a message template with the same ID already exists in the DynamoDB table, it should be updated with the new content.

The domain structure should contain the message template in a format that enables simple token find-and-replace type of processing to generate the final communication message. An example of such a structure is:
```
Lorem ipsum dolor sit amet, {PARAM1} adipiscing elit. Vivamus sed {PARAM2} tellus. 
Aliquam a orci laoreet, {PARAM1} risus non, porttitor velit. Mauris tortor felis, convallis vitae ex in, eleifend accumsan urna. 
```

# Non-functional requirements

* The service should be implemented in Java using Spring Boot framework. LTS versions should be preferred.
* Test coverage should be no less than 80%. This can be achieved either through unit tests or integration tests.
* The solution should account for the fact that SQS messages can be delivered more than once.

DynamoDB schema should account for additional access patterns. No need to implement them, but be ready to discuss how these can be addressed through the schema design. The access patterns are: 
  * find message template by ID
  * find all message templates by traffic type

# Evaluation

Your solution will be verified for correctness, and also used to evaluate your approach, attention to detail, and craftsmanship.
This is your opportunity to give us an idea of what we can expect from you on a day-to-day basis. Try to write the code as you would any ticket assigned to you.

We understand this is an effort you do potentially in your free time, and we do not expect a fully polished production-ready solution. It's perfectly fine to take shortcuts and omit code
in case of time constraints. Just drop a comment where you would have implemented code, and explain what approach you would take and what the code would have done.

# AWS infrastructure

The required AWS resources can be created locally using [Localstack](https://www.localstack.cloud/). The required setup is enclosed in `localstack` folder, it requires Docker to be installed and running on the local machine.

## Localstack instructions

To create the Localstack docker container image, run the following command:
```
docker-compose build
```

To start the container, run the following command:
```
docker-compose up -d
```

To shut down the container, run the following command:
```
docker-compose down
```
