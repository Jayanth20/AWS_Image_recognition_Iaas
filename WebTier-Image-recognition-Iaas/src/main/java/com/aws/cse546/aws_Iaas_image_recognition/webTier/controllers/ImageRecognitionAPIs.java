package com.aws.cse546.aws_Iaas_image_recognition.webTier.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.ImageRecognitionWebTierService;


@RestController
public class ImageRecognitionAPIs {
	
	@Autowired
	private AWSService awsService;
	
	@Autowired
	private ImageRecognitionWebTierService webTierService;
	
	@GetMapping("/test")
	public String getHello() {
		return "Hello";
	}
	
	@PostMapping("/imagesRecognition/Upload")
	public Map<String, String> getNameFromImage(@RequestParam(name = "imageurl", required = true) MultipartFile[] files){
		
		// If you provide the name of an existing queue along with the exact names and values of all the queue's attributes, CreateQueue returns the queue URL for the existing queue.
		awsService.createQueue(ProjectConstants.OUTPUT_QUEUE);
		
		Set<String> imageSet = new HashSet<>(); 
		Map<String,String> recognitionResult = new HashMap<>();
		
		int preSize = webTierService.getOutputMap().size();
		
		for(MultipartFile file: files) {
			String fileName = webTierService.createUniqueFileName(file);
//			awsService.uploadFileToS3(webTierService.convertMultiPartToFile(file), fileName);
			File fileContent = webTierService.convertMultiPartToFile(file);
			String queueInput = webTierService.getBase64OutofImage(fileContent);
			// Web to App tier
			awsService.queueInputRequest(queueInput+ ProjectConstants.SQS_MESSAGE_DELIMITER+fileName, ProjectConstants.INPUT_QUEUE, 0);
			imageSet.add(fileName);
		}
		
		System.out.println(imageSet);
		
		for(String fileName: imageSet) {
			while(!webTierService.getOutputMap().containsKey(fileName)) {
				
			}
			String result = webTierService.getOutputMap().get(fileName);
			System.out.println("Got the file: " + fileName + " with : "+ result);
			recognitionResult.put(fileName, result);
		}
		System.out.println(recognitionResult.size());
		return recognitionResult;
	}
	

}
