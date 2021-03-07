package com.mindtree.utils.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;

/**
 * Reads master metadata mapping sheet
 * 
 * @author M1032046
 *
 */
public class MasterMetadataMapReader {

	private MasterMetadataMapReader() {
		throw new IllegalStateException("MasterMetadataMapReader class");
	}

	static final Logger LOGGER = LoggerFactory.getLogger(MasterMetadataMapReader.class);

	/**
	 * Reads master metadata mapping sheet
	 * 
	 * @param fileName
	 * @param sheetName
	 * @param brand
	 * @return
	 */
	public static Map<String, BrandMasterMappingDto> getBrandMasterMapping(String fileName, String sheetName,
			String brand) {

		Map<String, Integer> headers = new HashMap<String, Integer>();

		XSSFSheet assetSheet = null;
		try {
			String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
			// Read master metadata mapping from S3
			assetSheet = ReadExcel.getExcelSheet(fileName, sheetName, devMigrationConfigPath);
			LOGGER.info("sheet: "+ assetSheet.getSheetName());

			// get headers only
			XSSFRow row = assetSheet.getRow(0);
			Iterator<Cell> cells = row.iterator();

			// populate headers map
			while (cells.hasNext()) {
				Cell cell = cells.next();
				LOGGER.info("header: "+ cell.getStringCellValue());
				if (MigratorConstants.COLUMN_MASTER_METADATA.equalsIgnoreCase(cell.getStringCellValue())
						|| (MigratorConstants.PREFIX_METADATA + brand).equalsIgnoreCase(cell.getStringCellValue())
						|| MigratorConstants.COLUMN_FIELD_TYPE.equalsIgnoreCase(cell.getStringCellValue())
						|| MigratorConstants.COLUMN_AEM_PROPERTY.equalsIgnoreCase(cell.getStringCellValue())) {
					headers.put(cell.getStringCellValue(), cell.getColumnIndex());
				}
			}

			LOGGER.info("headers added: "+ headers.size());

			Map<String, BrandMasterMappingDto> brandMasterMappingMap = new HashMap<String, BrandMasterMappingDto>();

			Iterator<Row> rowIterator = assetSheet.iterator();

			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
				// ignor headers row
				if (currentRow.getRowNum() == 0)
					continue;
				else {
					processRow(brand, headers, brandMasterMappingMap, currentRow);
				}

			}

			LOGGER.info("brandMasterMappingList:"+ brandMasterMappingMap.size());
			return brandMasterMappingMap;
		} catch (MigratorServiceException e) {
			LOGGER.error("BrandMTReader getBrandMasterMapping : ElcServiceException ", e);
		}
		return null;

	}

	private static void processRow(String brand, Map<String, Integer> headers,
			Map<String, BrandMasterMappingDto> brandMasterMappingMap, Row currentRow) {
		BrandMasterMappingDto brandMasterMappingDto = new BrandMasterMappingDto();
		// populate data from sheet to brandMasterMappingDto
		for (String header : headers.keySet()) {
			prepareBrandMasterMappingDto(brand, headers, currentRow, brandMasterMappingDto, header);
		}
		if (brandMasterMappingDto.getBrandMetadata() != null
				&& !MigratorConstants.NOT_AVAILABLE.equalsIgnoreCase(brandMasterMappingDto.getBrandMetadata())
				&& !MigratorConstants.NOT_AVAILABLE.equalsIgnoreCase(brandMasterMappingDto.getAemPropertyName())) {
			brandMasterMappingMap.put(brandMasterMappingDto.getBrandMetadata(), brandMasterMappingDto);
		}
	}

	private static void prepareBrandMasterMappingDto(String brand, Map<String, Integer> headers, Row currentRow,
			BrandMasterMappingDto brandMasterMappingDto, String header) {
		Cell cell;
		cell = currentRow.getCell(headers.get(header));

		if (cell != null && cell.getStringCellValue() != "" && cell.getStringCellValue() != null
				&& !cell.getStringCellValue().isEmpty()) {
			if (header.equalsIgnoreCase("Metadata_" + brand)) {
				brandMasterMappingDto.setBrandMetadata(cell.getStringCellValue());
			} else if (header.equalsIgnoreCase("Metadata_Master")) {
				brandMasterMappingDto.setMasterMetadata(cell.getStringCellValue());
			} else if (header.equalsIgnoreCase("Type")) {
				brandMasterMappingDto.setFieldType(cell.getStringCellValue());
			} else if (header.equalsIgnoreCase("AEM Properties")) {
				brandMasterMappingDto.setAemPropertyName(cell.getStringCellValue());
			}
		}
	}

}
