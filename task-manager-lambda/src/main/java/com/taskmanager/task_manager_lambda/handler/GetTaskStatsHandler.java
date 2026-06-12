package com.taskmanager.task_manager_lambda.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.taskmanager.task_manager_lambda.util.DynamoDBUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.Map;

public class GetTaskStatsHandler implements
        RequestHandler<APIGatewayProxyRequestEvent,
                APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        try {
            ScanResponse result = DynamoDBUtil.getClient()
                    .scan(ScanRequest.builder()
                            .tableName(DynamoDBUtil.TABLE_NAME)
                            .build());

            int total = result.items().size();
            int completed = 0, high = 0, medium = 0, low = 0;

            for (Map<String, AttributeValue> item : result.items()) {
                if ("COMPLETED".equals(item.get("status").s())) completed++;
                switch (item.get("priority").s()) {
                    case "HIGH"   -> high++;
                    case "MEDIUM" -> medium++;
                    case "LOW"    -> low++;
                }
            }

            int pending = total - completed;
            double rate = total > 0 ?
                    Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0;

            String body = String.format(
                    "{\"total\":%d,\"completed\":%d,\"pending\":%d," +
                            "\"completionRate\":%.1f," +
                            "\"byPriority\":{\"HIGH\":%d,\"MEDIUM\":%d,\"LOW\":%d}}",
                    total, completed, pending, rate, high, medium, low
            );

            return response(200, body);

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