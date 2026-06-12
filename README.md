# Serverless Task Manager

## Overview

Serverless Task Manager is a cloud-native task management application built using AWS serverless services. Users can create, view, update, and delete tasks while tracking task statistics through a responsive React frontend.

## Architecture

```text
React Frontend
      |
      v
API Gateway
      |
      v
AWS Lambda Functions
      |
      v
Amazon DynamoDB
```

## Tech Stack

### Frontend

* React.js
* JavaScript
* CSS

### Backend

* AWS Lambda
* Java 17
* AWS SDK v2

### Database

* Amazon DynamoDB

### Cloud Services

* Amazon API Gateway
* AWS Lambda
* Amazon DynamoDB

## Features

* Create Tasks
* View All Tasks
* Update Existing Tasks
* Delete Tasks
* Task Statistics Dashboard
* Priority Management (HIGH, MEDIUM, LOW)
* Status Tracking (PENDING, COMPLETED)
* Serverless Architecture

## API Endpoints

### Get All Tasks

```http
GET /tasks
```

### Create Task

```http
POST /tasks
```

Request Body:

```json
{
  "title": "Learn AWS",
  "description": "Lambda Project",
  "priority": "HIGH"
}
```

### Update Task

```http
PUT /tasks/{taskId}
```

### Delete Task

```http
DELETE /tasks/{taskId}
```

### Get Task Statistics

```http
GET /tasks/stats
```

## Project Structure

```text
serverless-task-manager
│
├── task-manager-frontend
│   ├── public
│   ├── src
│   ├── package.json
│   └── ...
│
├── task-manager-lambda
│   ├── src
│   ├── pom.xml
│   └── ...
│
└── README.md
```

## DynamoDB Schema

| Attribute   | Type                   |
| ----------- | ---------------------- |
| taskId      | String (Partition Key) |
| title       | String                 |
| description | String                 |
| priority    | String                 |
| status      | String                 |
| dueDate     | String                 |
| createdAt   | String                 |

## Local Setup

### Frontend

```bash
cd task-manager-frontend
npm install
npm start
```

### Backend

```bash
cd task-manager-lambda
mvn clean package
```

## AWS Deployment

* Frontend hosted on AWS
* Backend deployed using AWS Lambda
* API exposed through API Gateway
* Data stored in DynamoDB

## Learning Outcomes

This project demonstrates:

* Serverless Application Development
* REST API Design
* AWS Lambda Integration
* API Gateway Configuration
* DynamoDB Operations
* Cloud Deployment
* Frontend-Backend Integration

## Author

Prajyot Jadhao
