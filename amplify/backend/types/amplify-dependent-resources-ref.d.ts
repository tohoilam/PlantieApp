export type AmplifyDependentResourcesAttributes = {
    "auth": {
        "plantiec0b765d2": {
            "IdentityPoolId": "string",
            "IdentityPoolName": "string",
            "UserPoolId": "string",
            "UserPoolArn": "string",
            "UserPoolName": "string",
            "AppClientIDWeb": "string",
            "AppClientID": "string"
        }
    },
    "storage": {
        "planties3": {
            "BucketName": "string",
            "Region": "string"
        },
        "Plantie": {
            "Name": "string",
            "Arn": "string",
            "StreamArn": "string",
            "PartitionKeyName": "string",
            "PartitionKeyType": "string",
            "Region": "string"
        }
    },
    "function": {
        "plantie2ac44d9f": {
            "Name": "string",
            "Arn": "string",
            "Region": "string",
            "LambdaExecutionRole": "string"
        }
    },
    "api": {
        "Plantie": {
            "RootUrl": "string",
            "ApiName": "string",
            "ApiId": "string"
        }
    }
}