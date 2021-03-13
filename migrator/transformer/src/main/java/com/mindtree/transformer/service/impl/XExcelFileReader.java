package com.mindtree.transformer.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IDataFileReader;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationReportUtil;

/**
 * 
 * XSSF and XML Stream Reader
 * 
 * If memory footprint is an issue, then for XSSF, you can get at the underlying
 * XML data, and process it yourself. This is intended for intermediate
 * developers who are willing to learn a little bit of low level structure of
 * .xlsx files, and who are happy processing XML in java. Its relatively simple
 * to use, but requires a basic understanding of the file structure. The
 * advantage provided is that you can read a XLSX file with a relatively small
 * memory footprint.
 * 
 * @author lchen
 * 
 */
public class XExcelFileReader implements IDataFileReader {
	private int rowNum = 0;
	private OPCPackage opcPkg;
	private ReadOnlySharedStringsTable stringsTable;
	private XMLStreamReader xmlReader;

	static final Logger LOGGER = LoggerFactory.getLogger(XExcelFileReader.class);

	/**
	 * Constructor method to initialize the XML stream reader.
	 * 
	 * @param excelPath
	 * @param sheetIndex
	 * @param brandAbbreviation 
	 * @throws MigratorServiceException
	 */
	public XExcelFileReader(String excelPath, int sheetIndex, String brandAbbreviation) throws MigratorServiceException {
		try {

			LOGGER.info("XExcelFileReader : Reading file from S3:{}", excelPath);
			String devMigrationConfigPath = AppContext.getAppVariables().configFolder;
			File localFile = AppContext.getStorage().getFile(
					devMigrationConfigPath+ AppContext.getStorage().fileSeparator() +brandAbbreviation, excelPath);
			opcPkg = OPCPackage.open(localFile.getAbsolutePath(), PackageAccess.READ);

			// opcPkg = OPCPackage.open(excelPath, PackageAccess.READ);
			this.stringsTable = new ReadOnlySharedStringsTable(opcPkg);

			XSSFReader xssfReader = new XSSFReader(opcPkg);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			InputStream inputStream = xssfReader.getSheet("rId" + sheetIndex);
			xmlReader = factory.createXMLStreamReader(inputStream);

			while (xmlReader.hasNext()) {
				xmlReader.next();
				if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("sheetData")) {
					break;
				}
			}
		} catch (InvalidOperationException e) {
			LOGGER.error("XExcelFileReader : InvalidOperationException:{}", e);
			throw new MigratorServiceException("XExcelFileReader : InvalidOperationException ", e);
		} catch (IOException e) {
			LOGGER.error("XExcelFileReader : IOException:{}", e);
			throw new MigratorServiceException("XExcelFileReader : IOException ", e);
		} catch (SAXException e) {
			LOGGER.error("XExcelFileReader : SAXException:{}", e);
			throw new MigratorServiceException("XExcelFileReader : SAXException ", e);
		} catch (OpenXML4JException e) {
			LOGGER.error("XExcelFileReader : OpenXML4JException:{}", e);
			throw new MigratorServiceException("XExcelFileReader : OpenXML4JException ", e);
		} catch (XMLStreamException e) {
			LOGGER.error("XExcelFileReader XExcelFileReader : XMLStreamException:{}", e);
			throw new MigratorServiceException("XExcelFileReader :XMLStreamException ", e);
		} finally {
			/*
			 * if(opcPkg != null){ try { opcPkg.close(); } catch (IOException e)
			 * { LOGGER.error("XExcelFileReader : IOException:{}",e); } }
			 */
		}
	}

	/**
	 * Returns the row number processed till now.
	 * 
	 * @return
	 */
	@Override
	public int rowNum() {
		return rowNum;
	}

	/**
	 * This method returns the row data upto batch size specified.
	 * 
	 * @param batchSize
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String[]> readRows(int batchSize) throws MigratorServiceException {
		String elementName = "row";
		List<String[]> dataRows = new ArrayList<String[]>();
		try {

			while (xmlReader.hasNext()) {
				xmlReader.next();
				if (xmlReader.isStartElement() && xmlReader.getLocalName().equals(elementName)) {
					rowNum++;
					dataRows.add(getDataRow());
					if (batchSize != -1 && dataRows.size() == batchSize)
						break;
				}
			}
		} catch (XMLStreamException e) {
			LOGGER.error("XExcelFileReader : XMLStreamException:{}", e);
			throw new MigratorServiceException("XExcelFileReader readRows : XMLStreamException ", e);
		}
		return dataRows;
	}

	/**
	 * This method is to read the data row by columns into string array.
	 * 
	 * @return
	 * @throws XMLStreamException
	 */
	private String[] getDataRow() throws XMLStreamException {
		List<String> rowValues = new ArrayList<String>();
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.isStartElement()) {
				if (xmlReader.getLocalName().equals("c")) {
					CellReference cellReference = new CellReference(xmlReader.getAttributeValue(null, "r"));
					// Fill in the possible blank cells!
					while (rowValues.size() < cellReference.getCol()) {
						rowValues.add("");
					}
					String cellType = xmlReader.getAttributeValue(null, "t");
					rowValues.add(getCellValue(cellType));
				}
			} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("row")) {
				break;
			}
		}
		return rowValues.toArray(new String[rowValues.size()]);
	}

	/**
	 * This method is to read the cell values of each column.
	 * 
	 * @param cellType
	 * @return
	 * @throws XMLStreamException
	 */
	private String getCellValue(String cellType) throws XMLStreamException {
		String value = ""; // by default
		while (xmlReader.hasNext()) {
			xmlReader.next();
			if (xmlReader.isStartElement()) {
				if (xmlReader.getLocalName().equals("v")) {
					if (cellType != null && cellType.equals("s")) {
						int idx = Integer.parseInt(xmlReader.getElementText());
						return stringsTable.getItemAt(idx).getString();
					} else {
						return xmlReader.getElementText();
					}
				}
			} else if (xmlReader.isEndElement() && xmlReader.getLocalName().equals("c")) {
				break;
			}
		}
		return value;
	}

	@Override
	public List<String[]> readRowsByOffset(int batchNumber, int batchSize) throws MigratorServiceException {
		String elementName = "row";
		List<String[]> dataRows = new ArrayList<String[]>();
		int startIndex = (batchNumber - 1) * batchSize;
		try {

			while (xmlReader.hasNext()) {
				xmlReader.next();
				if (xmlReader.isStartElement() && xmlReader.getLocalName().equals(elementName)) {
					rowNum++;

					if (rowNum < startIndex) {
						continue;
					}

					dataRows.add(getDataRow());
					if (batchSize != -1 && dataRows.size() == batchSize)
						break;
				}
			}
		} catch (XMLStreamException e) {
			LOGGER.error("XExcelFileReader : XMLStreamException:{}", e);
			throw new MigratorServiceException("XExcelFileReader : XMLStreamException ", e);
		}
		return dataRows;
	}

}