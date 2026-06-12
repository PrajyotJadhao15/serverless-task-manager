package com.taskmanager.task_manager_lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import com.taskmanager.task_manager_lambda.model.Task;
import com.taskmanager.task_manager_lambda.util.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.*;

public class GetAllTasksHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        try {
            ScanResponse result = DynamoDBUtil.getClient()
                    .scan(ScanRequest.builder()
                            .tableName(DynamoDBUtil.TABLE_NAME)
                            .build());

            List<Task> tasks = new ArrayList<>();
            for (Map<String, AttributeValue> item : result.items()) {
                Task task = new Task();
                task.setTaskId(item.get("taskId").s());
                task.setTitle(item.get("title").s());
                task.setDescription(item.get("description").s());
                task.setPriority(item.get("priority").s());
                task.setStatus(item.get("status").s());
                task.setDueDate(item.get("dueDate").s());
                task.setCreatedAt(item.get("createdAt").s());
                tasks.add(task);
            }

            // Filter by status if query param provided
            Map<String, String> params = event.getQueryStringParameters();
            if (params != null && params.containsKey("status")) {
                String filterStatus = params.get("status").toUpperCase();
                tasks.removeIf(t -> !t.getStatus().equals(filterStatus));
            }

            // Search by title if query param provided
            if (params != null && params.containsKey("search")) {
                String keyword = params.get("search").toLowerCase();
                tasks.removeIf(t -> !t.getTitle().toLowerCase().contains(keyword));
            }

            // Sort by priority
            Map<String, Integer> priorityOrder = Map.of(
                    "HIGH", 1, "MEDIUM", 2, "LOW", 3
            );
            tasks.sort(Comparator.comparingInt(t ->
                    priorityOrder.getOrDefault(t.getPriority(), 99)));

            return response(200, gson.toJson(tasks));

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