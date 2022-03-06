package com.aws.cse546.aws_Iaas_image_recognition.webTier.services;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstant;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.repositories.AWSS3Repository;


@Service
public class AWSService implements Runnable{

	@Autowired
	private AWSS3Repository s3Repo;
	
	public static Logger logger = LoggerFactory.getLogger(AWSService.class);
	
	@Autowired
	private AWSConfigurations awsConfigurations;
	
	@Override
	public void run() {
		logger.info("**************** Starting AWSService thread ****************");
		this.scaleOut();
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

	
	public void scaleOut() {
		while (true) {
			// total Messages in queue
			Integer totalNumberOfMsgInQueue = getTotalNumberOfMessagesInQueue(ProjectConstant.INPUT_QUEUE);
			// Current number of running instances
			Integer totalNumberOfAppInstancesRunning = getTotalNumOfInstances();
			logger.info("**************** Current number of instance running: {} ************", totalNumberOfAppInstancesRunning);
			Integer numberOfInstancesToRun = 0;
			if (totalNumberOfAppInstancesRunning < totalNumberOfMsgInQueue) {
				logger.info("**************** Required number instance are: {} ************", totalNumberOfMsgInQueue - totalNumberOfAppInstancesRunning);
				logger.info("**************** Available (limit) number instance that can be triggered: {} ************", ProjectConstant.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning);
				if (ProjectConstant.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning > 0) {
					numberOfInstancesToRun = ProjectConstant.MAX_NUM_OF_APP_INSTANCES - totalNumberOfAppInstancesRunning;
				}
			}
			// number of instances to triggering
			logger.info("**************** Create {} number of new instances ****************", numberOfInstancesToRun);

			if (numberOfInstancesToRun > 0) {
				createAndRunInstance(ProjectConstant.AMI_ID, ProjectConstant.INSTANCE_TYPE, 
						numberOfInstancesToRun);
			}
			
			try {
				logger.info("**************** Timed Waiting AWSService thread: {} milli seconds  ****************", 2000);
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createAndRunInstance(String imageId, String instanceType, Integer requiredInstance) {
		
	}

	private Integer getTotalNumOfInstances() {
		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.setIncludeAllInstances(true);
		DescribeInstanceStatusResult describeInstances = awsConfigurations.getEC2Service()
				.describeInstanceStatus(describeInstanceStatusRequest);
		List<InstanceStatus> instanceStatusList = describeInstances.getInstanceStatuses();
		Integer total = 0;
		for (InstanceStatus is : instanceStatusList)
			if (is.getInstanceState().getName().equals(InstanceStateName.Running.toString())
					|| is.getInstanceState().getName().equals(InstanceStateName.Pending.toString()))
				total++;

		return total - 1;
	}

	private Integer getTotalNumberOfMessagesInQueue(String queueName) {
		logger.info("**************** Getting total Number of Messages in Queue **************** ");
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		
		GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl,
				ProjectConstant.SQS_METRICS);
		
		logger.info("**************** Getting Queue Attributes **************** ");
		Map<String, String> map = awsConfigurations.getSQSService().getQueueAttributes(getQueueAttributesRequest)
				.getAttributes();
		
		logger.info("**************** Total Number of Messages in Queue: {} **************** ", map.get(ProjectConstant.TOTAL_MSG_IN_SQS));
		
		return Integer.parseInt((String) map.get(ProjectConstant.TOTAL_MSG_IN_SQS));
	}


}
