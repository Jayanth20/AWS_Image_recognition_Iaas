package com.aws.cse546.aws_Iaas_image_recognition.appTier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.cse546.aws_Iaas_image_recognition.appTier.configurations.AppTierConfig;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.constants.ProjectConstants;
import com.aws.cse546.aws_Iaas_image_recognition.appTier.services.AWSService;


@SpringBootApplication
@EnableAutoConfiguration
public class AppTierApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppTierApplication.class, args);

		Integer NUMBER_OF_THREAD = 1;
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppTierConfig.class)) {
			AWSService awsService = context.getBean(AWSService.class);

			Integer TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE = awsService
					.getTotalNumberOfMessagesInQueue(ProjectConstants.INPUT_QUEUE);
			
			if (ProjectConstants.MAX_NUM_OF_APP_INSTANCES < TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE) 
				NUMBER_OF_THREAD = TOTAL_NUMBER_OF_MSG_IN_INPUT_QUEUE / ProjectConstants.MAX_NUM_OF_APP_INSTANCES;
			
			if(ProjectConstants.MAX_NUMBER_OF_THREAD < NUMBER_OF_THREAD)
				NUMBER_OF_THREAD = ProjectConstants.MAX_NUMBER_OF_THREAD;
			
			try {
				for (int t = 0; t < NUMBER_OF_THREAD; t++) {
					Thread thread = new Thread((Runnable) awsService);
					thread.start();
					thread.join();
				}
			}
			catch(Exception a){
				try {
					for (int t = 0; t < ProjectConstants.MAX_NUMBER_OF_ACCEPTED_THREAD; t++) {
						Thread thread = new Thread((Runnable) awsService);
						thread.start();
						thread.join();
					}
				}
				catch(Exception p){
					awsService.scaleIn();
				}
			}
			

			awsService.terminateInstance();
			context.close();
		} catch (Exception e) {
			System.out.println("Problem in logic");
			e.printStackTrace();
		}
	}

}
