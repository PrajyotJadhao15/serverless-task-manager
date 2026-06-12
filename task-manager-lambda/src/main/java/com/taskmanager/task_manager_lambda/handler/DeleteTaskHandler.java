package com.taskmanager.task_manager_lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.taskmanager.task_manager_lambda.util.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.Map;

public class DeleteTaskHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        try {
            String taskId = event.getPathParameters().get("taskId");

            DynamoDBUtil.getClient().deleteItem(DeleteItemRequest.builder()
                    .tableName(DynamoDBUtil.TABLE_NAME)
                    .key(Map.of("taskId", AttributeValue.fromS(taskId)))
                    .build());

            return response(200,
                    "{\"message\":\"Task deleted\",\"taskId\":\"" + taskId + "\"}");

        } catch (Exception e) {
            return response(500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private APIGatewayProxyResponseEvent response(int code, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(code)
                .withHeaders(Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*",
                        "Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS",
                        "Access-Control-Allow-Headers", "Content-Type,Authorization"
                ))
                .withBody(body);
    }
}