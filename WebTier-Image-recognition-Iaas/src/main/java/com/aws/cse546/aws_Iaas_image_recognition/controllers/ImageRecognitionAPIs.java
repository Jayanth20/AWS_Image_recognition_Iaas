package com.aws.cse546.aws_Iaas_image_recognition.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.cse546.aws_Iaas_image_recognition.constants.ProjectConstant;
import com.aws.cse546.aws_Iaas_image_recognition.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.services.ImageRecognitionWebTierService;


@RestController
public class ImageRecognitionAPIs {
	
	@Autowired
	private AWSService awsService;
	
	@Autowired
	private ImageRecognitionWebTierService webTierService;
	
	@PostMapping("/imagesRecognition/Upload")
	public Map<String, String> getNameFromImage(@RequestParam(name = "imageurl", required = true) MultipartFile[] files){
		
		awsService.createQueue(ProjectConstant.OUTPUT_QUEUE);
		
		Set<String> imageSet = new HashSet<>(); 
		Map<String,String> recognitionResult = new HashMap<>();
		
		for(MultipartFile file: files) {
			String fileName = webTierService.createUniqueFileName(file);
			awsService.uploadFileToS3(webTierService.convertMultiPartToFile(file), fileName);
			awsService.queueInputRequest(fileName, ProjectConstant.INPUT_QUEUE, 0);
			imageSet.add(fileName);
		}
		
		for(String fileName: imageSet) {
			String[] result = awsService.getOutputFromResponseQueue(fileName);
			recognitionResult.put(result[0], result[1]);
		}
		return recognitionResult;
	}
	

}
