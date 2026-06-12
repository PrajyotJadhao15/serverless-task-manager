package com.taskmanager.task_manager_lambda.util;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBUtil {


        private static DynamoDbClient client;

        public static DynamoDbClient getClient() {
            if (client == null) {
                client = DynamoDbClient.builder()
                        .region(Region.US_EAST_1)
                        .build();
            }
            return client;
        }

        public static final String TABLE_NAME = "Tasks";
    }

