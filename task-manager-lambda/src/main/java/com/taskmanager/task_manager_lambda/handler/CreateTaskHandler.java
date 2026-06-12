package com.taskmanager.task_manager_lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;


import com.taskmanager.task_manager_lambda.model.Task;
import com.taskmanager.task_manager_lambda.util.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateTaskHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        try {
            Task task = gson.fromJson(event.getBody(), Task.class);

            // Validate
            if (task.getTitle() == null || task.getTitle().isEmpty()) {
                return response(400, "{\"error\":\"Title is required\"}");
            }

            // Set defaults
            task.setTaskId(UUID.randomUUID().toString());
            task.setStatus("PENDING");
            task.setCreatedAt(LocalDateTime.now().toString());
            if (task.getPriority() == null) task.setPriority("MEDIUM");

            // Save to DynamoDB
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("taskId",      av(task.getTaskId()));
            item.put("title",       av(task.getTitle()));
            item.put("description", av(task.getDescription() != null ? task.getDescription() : ""));
            item.put("priority",    av(task.getPriority()));
            item.put("status",      av(task.getStatus()));
            item.put("dueDate",     av(task.getDueDate() != null ? task.getDueDate() : ""));
            item.put("createdAt",   av(task.getCreatedAt()));

            DynamoDBUtil.getClient().putItem(PutItemRequest.builder()
                    .tableName(DynamoDBUtil.TABLE_NAME)
                    .item(item)
                    .build());

            return response(201, gson.toJson(task));

        } catch (Exception e) {
            return response(500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private AttributeValue av(String val) {
        return AttributeValue.fromS(val);
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