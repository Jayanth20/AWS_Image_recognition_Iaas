# CSE-546-Project-1
This is the CSE546 course Iaas project.

The goal was to create an Image Recognition as a Service using the AWS Iaas Resources. The application triggers the given deep learning model which was given in an Amazon Machine Image. The application will handle multiple requests simultaneously and also will perform the autoscaling using the Amazon SQS based on the demand.

Services used in the project are:
 - **Amazon SQS**
 - **Amazon EC2**
 - **Amazon S3**

## WebTier
It is a RESTful Web Service which will accept the requests from the user, sends to the **SQS Request Queue** and stores the input in the **S3 input bucket**. The WebTier consists of the Scale out function whcih creates app instances when the demand is high. WebTier also takes the output from the **SQS Response Queue** and sends to the UI.

## AppTier
This application runs inside the app instances and listens for messages in the SQS Request Queue. When the message arrives, it takes the message, stores the image in the S3 Input Bucket and executes the deep learning model, and finally, the classification result is sent into a S3 output bucket as well as the SQS Response Queue. When there is no message in the Input queue, the application terminates the instance in which its running, facilitating scale in.

## Execution Steps
- Update the project constants in the ProjectConstants.java file as per your AWS set up in both modules (web tier and app-tier).
- Using Maven clean and install, create a jar file for the AppTier.
Customize the given AMI by creating an EC2 instance and upload the AppTier .jar file using WinSCP, then install the java environment in that instance and finally create an image of this instance.
- Create an instance that runs continuously until the application is closed.
- Transfer the WebTier .jar file using WinSCP to the created EC2 Instance.
- Setup the java environment in the java using appropriate install commands in the same instance.
- Execute the web .jar file in the instance.

- The HTTP requests are sent to the  http://ec2-52-87-139-44.compute-1.amazonaws.com:8080/getfacerecognizationperImage

To test the application run the following command:
Python3 multithread_workload_generator_verify_results_updated.py --num_request 100 --url http://ec2-52-87-139-44.compute-1.amazonaws.com:8080/getfacerecognizationperImage --image_folder face_images_100/

## Team Members
- Sai Charan Papani - 1222323895
- Jayanth Kumar Tumuluri - 1221727325
- Lakshmi Venu Raghu Ram Makkapati - 
- Sarath Chandra Mahamkali - 1222281736

## Useful Links
- https://spring.io/guides/gs/multi-module/
- https://www.codejava.net/ides/eclipse/create-multi-module-maven-project

