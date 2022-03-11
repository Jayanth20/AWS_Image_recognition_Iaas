package com.aws.cse546.aws_Iaas_image_recognition.webTier.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.sqs.model.Message;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;

@Service
public class ImageRecognitionWebTierService implements Runnable {
	
	public static Logger logger = LoggerFactory.getLogger(ImageRecognitionWebTierService.class);
	
	@Autowired
	private AWSService awsService;
	
	private Map<String, String> outputMap = new HashMap<>();
	
	public Map<String, String> getOutputMap() {
		return outputMap;
	}

	public void setOutputMap(Map<String, String> outputMap) {
		this.outputMap = outputMap;
	}

	
	public String[] getOutputFromResponseQueue(String imageUrl) {
		System.out.println("ImageURL: " + imageUrl);
		while (true) {
			try {
				if (this.outputMap.containsKey(imageUrl)) {
					String output = this.outputMap.get(imageUrl);
					this.outputMap.remove(imageUrl);
					return new String[] { this.formatImageUrl(imageUrl), output };
				} else {
					try {
						Thread.sleep(8000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.out.println("Some Error while getting outPut from HashMap");
				try {
					Thread.sleep(8000);
				} catch (Exception o) {
					o.printStackTrace();
				}
			}
		}
	}
	
	
	public String formatImageUrl(String imageUrl) {
		int firstIndex = imageUrl.indexOf('-');
		int lastIndex = imageUrl.lastIndexOf('.');
		return imageUrl.substring(firstIndex + 1, lastIndex);
	}
	
	
	@Override
	public void run() {
		logger.info("**************** Listening on the output Queue for messages from App **************** ");
		this.putOutputFromResponseQueueToHashMap();
	}

	private void putOutputFromResponseQueueToHashMap() {
		while (true) {
			List<Message> msgList = null;
			try {
				msgList = awsService.receiveMessage(ProjectConstants.OUTPUT_QUEUE, 20, ProjectConstants.MAX_WAIT_TIME_OUT, 10);
				if (msgList != null) {
					try {
						for (Message msg : msgList) {
							String[] classificationResult = null;
							classificationResult = msg.getBody().split(ProjectConstants.INPUT_OUTPUT_SEPARATOR);
							outputMap.put(classificationResult[0], classificationResult[1]);
							awsService.deleteMessage(msg, ProjectConstants.OUTPUT_QUEUE);
						}
					} catch (Exception w) {
						logger.info("Error in putting message from queue to map");
					}
				}
			} catch (Exception e) {
				logger.info("No Msg Available: " + e.getMessage());
				logger.info("Thread sleeping 10sec");
				try {
					Thread.sleep(10000);
				} catch (Exception p) {
					logger.info("Thread not sleeping some error");
				}
			}
		}
		
	}

	public String createUniqueFileName(MultipartFile file) {
		// time stamp + the original filename in the client's filesystem.
		return System.currentTimeMillis() + "-" + file.getOriginalFilename().replace(" ", "_");
	}

	/*
	 * Converting file type from Spring web multipart to java.io.file
	 */
	public File convertMultiPartToFile(MultipartFile file) {
		try {
			File convFile = new File(file.getOriginalFilename());
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
			return convFile;
		} catch (Exception e) {
			return null;
		}
	}

}
