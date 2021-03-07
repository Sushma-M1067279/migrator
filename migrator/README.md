# Migrator - An AEM asset migration tool
This tool can be used for migrating assets into AEM from S3, Azure storage or local file system. Two types of asset transformations are handled in this tool.
1) DAM based transformation: When assets along with metadata is available.
2) Drive based transformation: When assets are structured into folders, but doesn't have separate metadata.

## Checkout the application

> Repository: https://github.com/prasadgsk-mindtree/migrator.git
> Checkout into eclipse as mvn project

## Building the application

From the root folder, execute
> mvn clean install

This will create
> transformer-1.0-SNAPSHOT-jar-with-dependencies.jar under transformer/target which is used to AWS lamda deployment.

> azure-functions/damtransformer under transformer/target. This will be used for AWS function deployment.

## Deployment

This application can run be deployed as

1) AWS Lambda function: User transformer-1.0-SNAPSHOT-jar-with-dependencies.jar for deployment. This can be directly uploaded or thru s3 bucket.

2) Azure Function: Move into transformer folder and run. This should deploy azure function called 'migrator' and create/update function App 'damtransformer'.
> mvn azure-functions:deploy


Note: you need to login using 'az login' to login to azure account prior to running the deploy which will create local profile and access tokens.
If you want to change Azure resource group, update it in transformer/pom.xml

## Setting up variables

This application looks for following environment variables. Make sure required variables are set with appropriate values.
1) storage_account_name: Name of the storage account. Only used when Azure is used as asset storage.
2) storage_key: Key supplied for s3 bucket or azure blob storage.
3) storage_secrete: Secret for S3 bucket. Only used when S3 is used as asset storage.
4) config_bucket: Blob container name for Azure. S3 bucket name for AWS.
5) Config_folder: name of the folder in S3/container which has application configurations.

## Application folder structure

Following folder structure is expected by the application under config_bucket.
+ Folder for configuration as supplied for config_folder (say config).
++ config/config.properties : master configuration file. Same name to be used.
++ \<config>/MasterMetadataFields.xlsx 
++ \<config>/\<brand>/\<brand>_Configuration.xlsx
++ \<config>/\<brand>/\<brand>_AssetDump.xlsx

+ Folder with assets to be migrated. This will have all assets.

## Run application

This applicaation expects 3 runtime arguments.
1) brandCode : Code/prefix of the brand. For now, either BN or HX
2) sourceType : drive or dam
3) instanceNumber : 1 (for future use)

### Development
From eclipse IDE, run configuration can be setup to run com.mindtree.transformer.TransformerMain. Use appropriate program arguments and environment variables.

### Lambda function

Setup a test event. Sample json is given below. Run test event to run the tool.

    {
      "brandCode": "BN",
      "sourceType": "drive",
      "instanceNumber": "1"
    }

### Azure function

After the deployment, application is available @https://damtransformer.azurewebsites.net/api/migrator
Run it using parameters as

> https://damtransformer.azurewebsites.net/api/migrator?brandcode=BN&transformationtype=drive&instance=1
