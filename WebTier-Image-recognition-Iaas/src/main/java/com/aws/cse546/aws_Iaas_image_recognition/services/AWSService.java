package com.aws.cse546.aws_Iaas_image_recognition.services;

import java.io.File;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.aws.cse546.aws_Iaas_image_recognition.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.repositories.AWSS3Repository;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.WebTierApplication;



@Service
public class AWSService implements Runnable{
	
	@Autowired
	private AWSConfigurations awsConfigurations;

	@Autowired
	private AWSS3Repository s3Repo;
	
	public static Logger logger = LoggerFactory.getLogger(AWSService.class);
	
	@Override
	public void run() {
		
	}

	/*
	 * This method generate new queue using Amazon SQS.
	 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-sqs-message-queues.html
	 * https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/sqs/src/main/java/com/example/sqs/SQSExample.java
	 * 
	 */
	public void createQueue(String outputQueue) {
		try {
			CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName(outputQueue)
					.addAttributesEntry(QueueAttributeName.FifoQueue.toString(), Boolean.TRUE.toString())
					.addAttributesEntry(QueueAttributeName.ContentBasedDeduplication.toString(),
							Boolean.TRUE.toString());
			
			logger.info("Creating Queue with Name: {}", outputQueue);
			
			awsConfigurations.getSQSService().createQueue(createQueueRequest);

		} catch (Exception e) {
			System.out.println("Error while creating queue: " + e.getMessage());
		}
	}

	public void uploadFileToS3(File file, String fileName) {
		try {
			s3Repo.uploadFile(fileName, file);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void queueInputRequest(String url, String queueName, int delay) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		
		awsConfigurations.getSQSService().sendMessage(
				new SendMessageRequest().withQueueUrl(queueUrl).withMessageGroupId(UUID.randomUUID().toString()).withMessageBody(url).withDelaySeconds(delay)
				);
	}

	public String[] getOutputFromResponseQueue(String imageUrl) {
//		logger.info("ImageURL: ()", imageUrl);

		return null;
	}



}
