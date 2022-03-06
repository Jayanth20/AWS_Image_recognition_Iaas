package com.aws.cse546.aws_Iaas_image_recognition.configurations;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.aws.cse546.aws_Iaas_image_recognition.constants.ProjectConstant;


public class AWSConfigurations {

	public AmazonSQS getSQSService() {
		return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                .withRegion(ProjectConstant.AWS_REGION).build();
	}

	private AWSCredentials basicAWSCredentials() {
		return new BasicAWSCredentials(ProjectConstant.ACCESS_KEY_ID, ProjectConstant.SECRET_ACCESS_KEY);
	}

	public AmazonS3 getS3() {
		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
                .withRegion(ProjectConstant.AWS_REGION).build();
	}

}
