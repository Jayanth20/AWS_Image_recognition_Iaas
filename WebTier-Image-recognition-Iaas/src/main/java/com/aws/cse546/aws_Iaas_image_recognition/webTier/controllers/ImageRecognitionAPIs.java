package com.aws.cse546.aws_Iaas_image_recognition.webTier.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.cse546.aws_Iaas_image_recognition.webTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.AWSService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.services.ImageRecognitionWebTierService;
import com.aws.cse546.aws_Iaas_image_recognition.webTier.store.OutputResponses;


@Controller
public class ImageRecognitionAPIs {
	
	@Autowired
	private AWSService awsService;
	
	@Autowired
	private ImageRecognitionWebTierService webTierService;
	
	@GetMapping("/home")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			Model model) {
		return "imagerecognization";
	}
	
	@PostMapping("/imagerecognization")
	public String  getImageUrl(@RequestParam(name = "imageurl", required = true) MultipartFile[] files, Map<String, Object> model){
		
		// If you provide the name of an existing queue along with the exact names and values of all the queue's attributes, CreateQueue returns the queue URL for the existing queue.
		awsService.createQueue(ProjectConstants.OUTPUT_QUEUE);
		
		Set<String> imageSet = new HashSet<>(); 
		Map<String,String> recognitionResult = new HashMap<>();
		
		int preSize = OutputResponses.getLength();
		
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
		
//		for(String fileName: imageSet) {
//			String[] result = webTierService.getOutputFromResponseQueue(fileName);
//			recognitionResult.put(result[0],result[1]);
//		}
		
		System.out.println(imageSet.size() + preSize);
		while(true) {
			if(OutputResponses.getLength() == imageSet.size() + preSize) {
				for(String fileName: imageSet) {
					String result = OutputResponses.output.get(fileName);
					recognitionResult.put(fileName, result);
				}
				break;
			}
		}
		
		model.put("recognitionResult", recognitionResult);
		return "result";
	}
	

}
