package com.aws.cse546.aws_Iaas_image_recognition.webTier.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageRecognitionWebTierService implements Runnable {
	
	private Map<String, String> outputMap = new HashMap<>();
	
	public Map<String, String> getOutputMap() {
		return outputMap;
	}

	public void setOutputMap(Map<String, String> outputMap) {
		this.outputMap = outputMap;
	}

	@Override
	public void run() {
		
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
