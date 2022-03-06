package com.aws.cse546.aws_Iaas_image_recognition.repositories;

import java.io.File;

public interface AWSS3Repository {
	
	 public void uploadFile(String fileName, File file);

}
