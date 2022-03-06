package com.aws.cse546.aws_Iaas_image_recognition.repositories;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.aws.cse546.aws_Iaas_image_recognition.configurations.AWSConfigurations;
import com.aws.cse546.aws_Iaas_image_recognition.constants.ProjectConstant;

public class AWSS3RepositoryImpl implements AWSS3Repository {
	
	@Autowired
	private AWSConfigurations awsConfigurations;

	@Override
	public void uploadFile(String fileName, File file) {
		try {
            if (!awsConfigurations.getS3().doesBucketExistV2(ProjectConstant.INPUT_BUCKET))
                awsConfigurations.getS3().createBucket(ProjectConstant.INPUT_BUCKET);
            awsConfigurations.getS3().putObject(new PutObjectRequest(ProjectConstant.INPUT_BUCKET, fileName, file));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
