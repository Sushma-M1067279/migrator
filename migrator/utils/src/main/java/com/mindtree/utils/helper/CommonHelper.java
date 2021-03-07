package com.mindtree.utils.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mindtree.models.AssetMetadataMapper;
import com.mindtree.models.MetadataDto;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;

public class CommonHelper {

	private CommonHelper() {

	}

	/**
	 * @param assetMetadataMapper
	 * @return
	 */
	public static List<MetadataDto> createMetaDataDtos(List<AssetMetadataMapper> assetMetadataList) {
		List<MetadataDto> metadataDtos = new ArrayList<MetadataDto>();

		for (AssetMetadataMapper assetMetadata : assetMetadataList) {
			metadataDtos.add(CommonHelper.createMetaDataDto(assetMetadata));
		}

		return metadataDtos;
	}

	public static MetadataDto createMetaDataDto(AssetMetadataMapper assetMetadata) {

		MetadataDto metadataDto = new MetadataDto();
		metadataDto.setAssetMetdata(assetMetadata.getAssetMetdata());
		metadataDto.setAssetType(assetMetadata.getAssetType());
		metadataDto.setBrand(assetMetadata.getBrand());
		metadataDto.setCreatedDate(assetMetadata.getCreatedDate());
		metadataDto.setId(assetMetadata.getId());
		metadataDto.setAssetId(assetMetadata.getAssetId());
		metadataDto.setModifiedDate(assetMetadata.getModifiedDate());
		metadataDto.setRetryCount(assetMetadata.getRetryCount());
		metadataDto.setStatus(assetMetadata.getStatus());
		metadataDto.setCreatedBy(assetMetadata.getCreatedBy());
		metadataDto.setModifiedBy(assetMetadata.getModifiedBy());

		return metadataDto;
	}

	/**
	 * @param assetMetadataMapper
	 * @return
	 */
	public static AssetMetadataMapper createAssetMetaDataMapper(MetadataDto metadataDto) {

		AssetMetadataMapper assetMetadata = new AssetMetadataMapper();
		assetMetadata.setAssetMetdata(metadataDto.getAssetMetdata());
		assetMetadata.setAssetType(metadataDto.getAssetType());
		assetMetadata.setBrand(metadataDto.getBrand());
		assetMetadata.setCreatedDate(metadataDto.getCreatedDate());
		assetMetadata.setId(metadataDto.getId());
		assetMetadata.setAssetId(metadataDto.getAssetId());
		assetMetadata.setModifiedDate(metadataDto.getModifiedDate());
		assetMetadata.setRetryCount(metadataDto.getRetryCount());
		assetMetadata.setStatus(metadataDto.getStatus());
		assetMetadata.setModifiedBy(metadataDto.getModifiedBy());
		assetMetadata.setCreatedBy(metadataDto.getCreatedBy());

		return assetMetadata;
	}

	

	

}
