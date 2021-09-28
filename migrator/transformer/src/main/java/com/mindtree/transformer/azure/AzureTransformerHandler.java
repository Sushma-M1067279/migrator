package com.mindtree.transformer.azure;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.mindtree.transformer.TransformerApp;
import com.mindtree.transformer.TransformerMain;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureTransformerHandler {
	
	static final Logger LOGGER = LoggerFactory.getLogger(AzureTransformerHandler.class);

    @FunctionName("migrator")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        Map<String, String> params = request.getQueryParameters();
        
		LOGGER.info("AzureTransformerHandler : Params :{}", params.toString());

		TransformerApp transformerApp = new TransformerApp();
		boolean isSuccess = false;
		String brandCode=null;
		String transformationType=null;
		String instanceNumber=null;
		
			if (params != null && params.size() > 0) {

			brandCode = params.get("brandcode");
			transformationType = params.get("transformationtype");
			instanceNumber = params.get("instance");
				
		}

		if(transformerApp.init(new String[] {brandCode, transformationType, instanceNumber})) {
			LOGGER.error("AzureTransformerHandler : Errrors while initiating the application.");
			
			isSuccess = transformerApp.execute(transformationType, brandCode, instanceNumber);			
		}


        if (!isSuccess) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("XXXXXXXXX Errors during the execution. XXXXXXXXX").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(transformerApp.getSummary()).build();
        }
    }

}


