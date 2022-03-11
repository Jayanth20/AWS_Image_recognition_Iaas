package com.aws.cse546.aws_Iaas_image_recognition.appTier.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.repositories.AWSS3RepositoryImpl;

@Service
public class AWSService implements Runnable {
	
	@Autowired
	private AWSConfigurations awsConfigurations;
	
	@Autowired
	private AWSS3RepositoryImpl awsS3Repo;
	
	@Override
	public void run() {
		this.scaleIn();
	}

	public void scaleIn() {
		
		
	}

	public Integer getTotalNumberOfMessagesInQueue(String queueName) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl,
				ProjectConstants.SQS_METRICS);
		Map<String, String> map = awsConfigurations.getSQSService().getQueueAttributes(getQueueAttributesRequest)
				.getAttributes();

		return Integer.parseInt((String) map.get(ProjectConstants.TOTAL_MSG_IN_SQS));
	}

	public void createQueue(String queueName) {
		CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName(queueName)
				.addAttributesEntry(QueueAttributeName.FifoQueue.toString(), Boolean.TRUE.toString())
				.addAttributesEntry(QueueAttributeName.ContentBasedDeduplication.toString(), Boolean.TRUE.toString());
		awsConfigurations.getSQSService().createQueue(createQueueRequest);
	}

	public void terminateInstance() {
		
	}

}
