package com.mindtree.transformer.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mindtree.transformer.TransformerApp;
import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.transformer.service.ITransformer;
import com.mindtree.utils.helper.MigrationUtils;

public class AWSTransformationHandler implements RequestHandler<TransformerRequest, TransformerResponse> {

	static final Logger LOGGER = LoggerFactory.getLogger(AWSTransformationHandler.class);

	@Override
	public TransformerResponse handleRequest(TransformerRequest request, Context arg1) {
		LOGGER.info("AWSTransformationHandler args:{}", request);
		
		TransformerApp transformerApp = new TransformerApp();
		
		if(! transformerApp.init()) {
			LOGGER.error("Couldn't find your transformer. Please check transformer.properties");
			return new TransformerResponse(false);
		}
		

		LOGGER.info("AWSTransformationHandler The type of the transformer:{}", request.getSourceType());
		boolean isSuccess = transformerApp.execute( request.getSourceType(), request.getBrandCode(), 
													request.getInstanceNumber());

		return new TransformerResponse(isSuccess);
	}

}
