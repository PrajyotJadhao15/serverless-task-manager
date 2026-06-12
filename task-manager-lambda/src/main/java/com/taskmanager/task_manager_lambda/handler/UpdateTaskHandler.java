package com.taskmanager.task_manager_lambda.handler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import com.taskmanager.task_manager_lambda.model.Task;
import com.taskmanager.task_manager_lambda.util.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

public class UpdateTaskHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        try {
            String taskId = event.getPathParameters().get("taskId");
            Task updates = gson.fromJson(event.getBody(), Task.class);

            DynamoDBUtil.getClient().updateItem(UpdateItemRequest.builder()
                    .tableName(DynamoDBUtil.TABLE_NAME)
                    .key(Map.of("taskId", AttributeValue.fromS(taskId)))
                    .updateExpression(
                            "SET #s = :status, priority = :priority, " +
                                    "title = :title, description = :desc"
                    )
                    .expressionAttributeNames(Map.of("#s", "status"))
                    .expressionAttributeValues(Map.of(
                            ":status",   AttributeValue.fromS(updates.getStatus() != null ? updates.getStatus() : "PENDING"),
                            ":priority", AttributeValue.fromS(updates.getPriority() != null ? updates.getPriority() : "MEDIUM"),
                            ":title",    AttributeValue.fromS(updates.getTitle() != null ? updates.getTitle() : ""),
                            ":desc",     AttributeValue.fromS(updates.getDescription() != null ? updates.getDescription() : "")
                    ))
                    .build());

            return response(200, "{\"message\":\"Task updated\",\"taskId\":\"" + taskId + "\"}");

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