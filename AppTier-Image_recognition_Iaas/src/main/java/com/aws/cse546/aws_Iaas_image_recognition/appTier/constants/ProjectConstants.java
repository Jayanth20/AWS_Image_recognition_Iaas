package com.aws.cse546.aws_Iaas_image_recognition.appTier.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.regions.Regions;

public class ProjectConstants {
	
	public static final String INPUT_QUEUE = "inputQueue.fifo";

	public static final String OUTPUT_QUEUE = "outputQueue.fifo";
	
	// accessKey - The AWS access key.
	public static final String ACCESS_KEY_ID = "";		 

	// secretKey - The AWS secret access key.
	public static final String SECRET_ACCESS_KEY = "";
	
	public static final String PRIVATE_KEY = "my_key_pair";

	public static final Regions AWS_REGION = Regions.US_EAST_1;

	public static final String INPUT_BUCKET = "image-input-bucket-cse546-version-1";
	
	public static final String OUTPUT_BUCKET = "image-output-bucket-cce546-version-1";
	
	public static final String TOTAL_MSG_IN_SQS = "ApproximateNumberOfMessages";

	public static final Integer MAX_NUM_OF_APP_INSTANCES = 19;

	public static final List<String> SQS_METRICS = new ArrayList<String>(Arrays.asList(TOTAL_MSG_IN_SQS));
	
	public static final String INPUT_OUTPUT_SEPARATOR = "---";
	
	public static final Integer MAX_NUMBER_OF_THREAD = 250; 
	
	public static final Integer MAX_NUMBER_OF_ACCEPTED_THREAD = 20;
	
	public static final Integer MAX_VISIBILITY_TIMEOUT = 40;

	public static final Integer MAX_WAIT_TIME_OUT = 20;
	
	public static final String PATH_TO_DIRECTORY = "/home/ubuntu/classifier/";
	
	public static final String PYTHON_SCRIPT = "image_classification.py";

}
