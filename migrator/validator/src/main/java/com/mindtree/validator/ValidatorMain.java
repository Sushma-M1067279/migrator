package com.mindtree.validator;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.helper.MigrationUtils;
import com.mindtree.validator.service.IValidatorService;
import com.mindtree.validator.service.impl.ValidatorServiceImpl;

/**
 * @author M1044289
 *
 */
public class ValidatorMain {

	private static final Logger LOG = LoggerFactory.getLogger(ValidatorMain.class);

	// private AbstractApplicationContext applicationContext;
	//
	// @Autowired
	// private ValidatorService validatorService;
	//
	// @Autowired
	// private ValidationConfigProperties validationConfig;
	//
	// public void configure(Class<?> configurationClass) {
	// applicationContext = new
	// AnnotationConfigApplicationContext(configurationClass);
	// applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
	// }

	static String sourceAbbreviation = null;

	public static void main(String[] args) {
		LOG.debug("Valiadator app is starting..");
		boolean initSuccess = MigrationUtils.initializeConfig();
		Properties prop = null;
		try {
			prop = MigrationUtils.getPropValues();
		} catch (MigratorServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (args != null && args.length > 0) {
			sourceAbbreviation = args[0];
		} else {
			LOG.debug("arguments are not passed");
		}
		IValidatorService validatorService = null;
		if (initSuccess) {
			validatorService = new ValidatorServiceImpl();
		}

		// Validator validator = new Validator();
		LOG.debug("Valiadator app is configured..");
		if (validatorService != null) {
			if (prop.getProperty("migrator.migration.validation.flag").equals("true")) {
				validatorService.validateUploadsToAEM(sourceAbbreviation);
			} else {
				validatorService.createReportFromAem();
			}
		}
		LOG.debug("Valiadator app is closing..");
	}

}