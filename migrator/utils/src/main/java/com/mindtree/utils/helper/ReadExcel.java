package com.mindtree.utils.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.utils.exception.MigratorServiceException;

public class ReadExcel {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadExcel.class);

	/**
	 * 
	 */
	private ReadExcel() {
		throw new IllegalStateException("ReadExcel class");
	}

	/**
	 * This method is to get excel sheet by file name and sheet name.
	 * 
	 * @param fileName
	 * @param sheetname
	 * @return
	 * @throws MigratorServiceException
	 */
	public static XSSFSheet getExcelSheet(AmazonS3 S3Client, String fileName, String sheetname, String s3Folder)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		XSSFWorkbook workbook = null;
		OPCPackage opcPackage =null;
		try {
			LOGGER.info("fileName : "+fileName);
			LOGGER.info("sheetname : "+sheetname);
			LOGGER.info("s3Folder : "+s3Folder);
			File file = MigrationReportUtil.getFileFromS3(S3Client, fileName, s3Folder);
			WorkbookFactory.create(file);
			opcPackage = OPCPackage.open(file);
			workbook = new XSSFWorkbook(opcPackage);
			assetSheet = workbook.getSheet(sheetname);
		} catch (InvalidFormatException e) {
			LOGGER.error("getExcelSheet:  InvalidFormatException ", e);
			throw new MigratorServiceException("getExcelSheet:  InvalidFormatException ", e);
		} catch (IOException e) {
			LOGGER.error("getExcelSheet:  IOException", e);
			throw new MigratorServiceException("getExcelSheet:  IOException", e);
		} finally {
			try {
				if (workbook != null) {
					workbook.close();
				}
			//	opcPackage.close();
			} catch (IOException e) {
				LOGGER.error("getExcelSheet:  IOException - unable to close workbook ", e);
			}
		}
		return assetSheet;
	}

	/**
	 * This method is to get excel sheet by file name and sheet name.
	 * 
	 * @param fileName
	 * @param sheetname
	 * @return
	 * @throws MigratorServiceException
	 */
	public static XSSFWorkbook getExcelWorkbook(AmazonS3 s3Client, String fileName, String s3Folder) throws MigratorServiceException {
		XSSFWorkbook workbook = null;
//		OPCPackage opcPackage = null;
		try {
			LOGGER.info("filename : "+fileName+" S3folder: "+s3Folder);
			File file = MigrationReportUtil.getFileFromS3(s3Client, fileName, s3Folder);
			WorkbookFactory.create(file);
			FileInputStream inputStream = new FileInputStream(file);

			//opcPackage = OPCPackage.open(file);
//			workbook = new XSSFWorkbook(inputStream);
//		} catch (InvalidFormatException e) {
//			LOGGER.error("getExcelSheet:  InvalidFormatException ", e);
//			throw new MigratorServiceException("getExcelSheet:  InvalidFormatException ", e);
		} catch (Exception e) {
			LOGGER.error("getExcelSheet:  IOException", e);
			throw new MigratorServiceException("getExcelSheet:  IOException", e);
		} finally {
			try {
				if (workbook != null) {
					workbook.close();
				}
//				opcPackage.close();
			} catch (IOException e) {
				LOGGER.error("getExcelSheet:  IOException - unable to close workbook ", e);
			}
		}

		return workbook;
	}

}
