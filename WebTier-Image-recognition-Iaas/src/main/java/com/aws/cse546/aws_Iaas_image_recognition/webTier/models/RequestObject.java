package com.aws.cse546.aws_Iaas_image_recognition.webTier.models;

import org.springframework.web.multipart.MultipartFile;

public class RequestObject {
	
	private MultipartFile myfile;

	public MultipartFile getMyfile() {
		return myfile;
	}

	public void setMyfile(MultipartFile myfile) {
		this.myfile = myfile;
	}

	public RequestObject(MultipartFile myfile) {
		super();
		this.myfile = myfile;
	}
	
	

}
