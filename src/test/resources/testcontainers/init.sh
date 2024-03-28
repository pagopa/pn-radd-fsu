echo "### CREATE RADD ALT TRANSACTION TABLE ###"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-radd-transaction-alt \
    --attribute-definitions \
        AttributeName=transactionId,AttributeType=S \
        AttributeName=operationType,AttributeType=S \
        AttributeName=iun,AttributeType=S \
        AttributeName=recipientId,AttributeType=S \
        AttributeName=delegateId,AttributeType=S \
    --key-schema \
        AttributeName=transactionId,KeyType=HASH \
        AttributeName=operationType,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        '[
            {
                "IndexName": "iun-transaction-index",
                "KeySchema": [{"AttributeName":"iun","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 10, "WriteCapacityUnits": 5}
            },
            {
                "IndexName": "recipient-transaction-index",
                "KeySchema": [{"AttributeName":"recipientId","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 10, "WriteCapacityUnits": 5}
            },
            {
                "IndexName": "delegate-transaction-index",
                "KeySchema": [{"AttributeName":"delegateId","KeyType":"HASH"}],
                "Projection": {"ProjectionType":"ALL"},
                "ProvisionedThroughput": {"ReadCapacityUnits": 10, "WriteCapacityUnits": 5}
            }
        ]'

echo "### CREATE OPERATIONS AND IUNS TABLE ###"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-operations-iuns-alt \
    --attribute-definitions \
        AttributeName=transactionId,AttributeType=S \
        AttributeName=iun,AttributeType=S \
    --key-schema \
        AttributeName=transactionId,KeyType=HASH \
        AttributeName=iun,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"iun-transaction-index\",
            \"KeySchema\": [
                {\"AttributeName\":\"iun\",\"KeyType\":\"HASH\"},
                {\"AttributeName\":\"transactionId\",\"KeyType\":\"RANGE\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"

echo "### CREATE PN RADD REGISTRY IMPORT TABLE ###"

#status-index: pk su "status", projection ALL
#filekey-index: pk su "fileKey", projection ALL

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-RaddRegistryImport \
    --attribute-definitions \
    AttributeName=fileKey,AttributeType=S \
    AttributeName=cxId,AttributeType=S \
    AttributeName=status,AttributeType=S \
    AttributeName=requestId,AttributeType=S \
    --key-schema \
    AttributeName=cxId,KeyType=HASH \
    AttributeName=requestId,KeyType=RANGE \
    --provisioned-throughput \
    ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\":\"status-index\",
            \"KeySchema\":[
                {\"AttributeName\":\"status\",\"KeyType\":\"HASH\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\":{
                \"ReadCapacityUnits\":10,
                \"WriteCapacityUnits\":5
            }
        },{
            \"IndexName\":\"fileKey-index\",
            \"KeySchema\":[
                {\"AttributeName\":\"fileKey\",\"KeyType\":\"HASH\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\":{
                \"ReadCapacityUnits\":10,
                \"WriteCapacityUnits\":5
            }
        }
    ]"

echo "### CREATE PN RADD REGISTRY REQUEST TABLE ###"

#cxId-requestId-index: pk su “cxId" e sk su "requestId" , projection ALL
#correlationId-index: pk su “correlationId", projection ALL
#cxId-registryId-index: pk su “cxId" e sk su "registryId" , projection ALL

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-RaddRegistryRequest \
    --attribute-definitions \
    AttributeName=cxId,AttributeType=S \
    AttributeName=requestId,AttributeType=S \
    AttributeName=correlationId,AttributeType=S \
    AttributeName=registryId,AttributeType=S \
    AttributeName=pk,AttributeType=S \
    --key-schema \
    AttributeName=pk,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\":\"cxId-requestId-index\",
            \"KeySchema\":[
                {\"AttributeName\":\"cxId\",\"KeyType\":\"HASH\"},
                {\"AttributeName\":\"requestId\",\"KeyType\":\"RANGE\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\":{
                \"ReadCapacityUnits\":10,
                \"WriteCapacityUnits\":5
            }
        },{
            \"IndexName\":\"correlationId-index\",
            \"KeySchema\":[
                {\"AttributeName\":\"correlationId\",\"KeyType\":\"HASH\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\":{
                \"ReadCapacityUnits\":10,
                \"WriteCapacityUnits\":5
            }
        },{
            \"IndexName\":\"cxId-registryId-index\",
            \"KeySchema\":[
                {\"AttributeName\":\"cxId\",\"KeyType\":\"HASH\"},
                {\"AttributeName\":\"registryId\",\"KeyType\":\"RANGE\"}
            ],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\":{
                \"ReadCapacityUnits\":10,
                \"WriteCapacityUnits\":5
            }
        }
    ]"

echo "### CREATE PN RADD REGISTRY TABLE ###"

#zipCode-index: pk su "zipCode", projection ALL
#cxId-requestId-index: pk su “cxId" e sk su "requestId" , projection ALL

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-RaddRegistry \
    --attribute-definitions \
    AttributeName=registryId,AttributeType=S \
    AttributeName=cxId,AttributeType=S \
    AttributeName=zipCode,AttributeType=S \
    AttributeName=requestId,AttributeType=S \
    --key-schema \
    AttributeName=registryId,KeyType=HASH \
    AttributeName=cxId,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
            {
                \"IndexName\":\"cxId-requestId-index\",
                \"KeySchema\":[
                    {\"AttributeName\":\"cxId\",\"KeyType\":\"HASH\"},
                    {\"AttributeName\":\"requestId\",\"KeyType\":\"RANGE\"}
                ],
                \"Projection\":{
                    \"ProjectionType\":\"ALL\"
                },
                \"ProvisionedThroughput\":{
                    \"ReadCapacityUnits\":10,
                    \"WriteCapacityUnits\":5
                }
            },{
                \"IndexName\":\"zipCode-index\",
                \"KeySchema\":[
                    {\"AttributeName\":\"zipCode\",\"KeyType\":\"HASH\"}
                ],
                \"Projection\":{
                    \"ProjectionType\":\"ALL\"
                },
                \"ProvisionedThroughput\":{
                    \"ReadCapacityUnits\":10,
                    \"WriteCapacityUnits\":5
                }
            }
        ]"

echo "Initialization terminated"