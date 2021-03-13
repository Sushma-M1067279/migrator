/**
 * 
 */
package com.mindtree.transformer.service.impl;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IStorage;
import com.mindtree.core.service.AppContext.AppVariables;

/**
 * @author AdobeDay2
 *
 */
public abstract class AbstractStorage implements IStorage {

	protected AppVariables appVars = AppContext.getAppVariables();
	
	protected String getName(String path, char sep) {
		if(sep == 0) {
			sep = this.fileSeparator();
		}
		return path.substring(path.lastIndexOf(sep) + 1, path.length()).trim();
	}

	protected String getReportFileName(String reportName, boolean addBucket) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
		String dateString = formatter.format(new Date());

		String devMigrationBucketName = appVars.bucketName; 
		String devMigrationReportPath = AppContext.getAppConfig().getProperty(
				"migrator.asset.migration.report.path");

		String fileName = reportName.split("\\.")[0];
		String extn = reportName.split("\\.")[1];
		String absFilePath;
		if (addBucket) {
			absFilePath = devMigrationBucketName+ fileSeparator() + devMigrationReportPath
				+ fileSeparator() + fileName.concat("." + dateString).concat("." + extn);
		} else {
			absFilePath = devMigrationReportPath
					+ fileSeparator() + fileName.concat("." + dateString).concat("." + extn);
		}
		return absFilePath;

	}
}
