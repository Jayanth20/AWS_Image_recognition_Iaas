package com.aws.cse546.aws_Iaas_image_recognition.appTier.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.EC2MetadataUtils;
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

	/*
	 * This method will scale in, terminate app instance when demand is low. 
	 */
	public void scaleIn() {
		while (true) {
			Message msg = receiveMessage(ProjectConstants.INPUT_QUEUE, ProjectConstants.MAX_VISIBILITY_TIMEOUT,
					ProjectConstants.MAX_WAIT_TIME_OUT);
			if (msg != null) {
				try {
					// get file from s3
					S3Object object = awsConfigurations.getS3()
							.getObject(new GetObjectRequest(ProjectConstants.INPUT_BUCKET, msg.getBody()));
					// input stream for reading
					InputStream is = object.getObjectContent();
					
					// writing the file to a location where Python script can look and process
					File imageFile = new File(ProjectConstants.PATH_TO_DIRECTORY + msg.getBody());
					FileOutputStream fos = new FileOutputStream(imageFile);
					byte[] buf = new byte[8192];
					int length;
					while ((length = is.read(buf)) > 0) {
						fos.write(buf, 0, length);
					}
					fos.close();
					// running the script on it.
					String predicted_value = runPythonScript(msg.getBody());
					// sending the result through the queue
					this.queueResponse(msg.getBody() + ProjectConstants.INPUT_OUTPUT_SEPARATOR + predicted_value,
							ProjectConstants.OUTPUT_QUEUE, 0);
					// storing the result into the s3
					awsS3Repo.uploadFile(formatInputRequest(msg.getBody()), predicted_value);
					// deleting the file we created on the instance.
					imageFile.delete();
					// deleting the message on the input queue
					deleteMessage(msg, ProjectConstants.INPUT_QUEUE);
				}catch(Exception w) {
					w.printStackTrace();
				}
				
			}else {
				break;
			}
			
		}
		
	}
	
	
	public void deleteMessage(Message message, String queueName) {
		String queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		String messageReceiptHandle = message.getReceiptHandle();
		DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, messageReceiptHandle);
		awsConfigurations.getSQSService().deleteMessage(deleteMessageRequest);
	}
	
	public String formatInputRequest(String fileName) {
		int firstIndex = fileName.indexOf("-");
		int lastIndex = fileName.lastIndexOf(".");
		return fileName.substring(firstIndex + 1, lastIndex);
	}
	
	
	/*
	 * Run python script present on app instance to classify image using process builder.
	 */
	public String runPythonScript(String fileName) {
		try {
			// create OS process by taking the program and respective arguments
			ProcessBuilder processBuilder = new ProcessBuilder("python3",
					resolvePythonScriptPath(ProjectConstants.PYTHON_SCRIPT), resolvePythonScriptPath(fileName));
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();
			List<String> results = readProcessOutput(process.getInputStream());

			if (!results.isEmpty() && results != null)
				return results.get(0);
			return "Sorry!!! Algo not able to process your request";
		} catch (Exception e) {
			return "Sorry!!! Algo not able to process your request";
		}
	}
	
	private static List<String> readProcessOutput(InputStream inputStream) {
		try {
			BufferedReader output = new BufferedReader(new InputStreamReader(inputStream));
			return output.lines().collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>(Arrays.asList("No Prediction"));
		}
	}
	
	private static String resolvePythonScriptPath(String filename) {
		File file = new File(ProjectConstants.PATH_TO_DIRECTORY + filename);
		return file.getAbsolutePath();
	}
	
	/*
	 * For writing the message on the output queue
	 */
	public void queueResponse(String message, String queueName, int delay) {
		String queueUrl = null;

		try {
			queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		} catch (Exception e) {
			createQueue(queueName);
		}
		queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		awsConfigurations.getSQSService().sendMessage(new SendMessageRequest().withQueueUrl(queueUrl)
				.withMessageGroupId(UUID.randomUUID().toString()).withMessageBody(message).withDelaySeconds(0));
	}

	private Message receiveMessage(String queueName, Integer maxVisibilityTimeout, Integer maxWaitTimeOut) {
		String queueUrl = awsConfigurations.getSQSService().getQueueUrl(queueName).getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		receiveMessageRequest.setVisibilityTimeout(maxVisibilityTimeout);
		receiveMessageRequest.setWaitTimeSeconds(maxWaitTimeOut);
		ReceiveMessageResult receiveMessageResult = awsConfigurations.getSQSService()
				.receiveMessage(receiveMessageRequest);
		List<Message> messageList = receiveMessageResult.getMessages();
		if (messageList.isEmpty()) {
			return null;
		}
		return messageList.get(0);
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
		String myId = EC2MetadataUtils.getInstanceId();
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(myId);
		awsConfigurations.getEC2Service().terminateInstances(request);
	}

}
