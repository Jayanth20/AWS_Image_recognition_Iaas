package com.aws.cse546.aws_Iaas_image_recognition.webTier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
public class WebTierApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebTierApplication.class, args);
	}

}
