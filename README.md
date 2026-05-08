# AI Chat Service

A Spring Boot middleware service that acts as a bridge between users and a Large Language Model (LLM) via OpenRouter.

## Requirements

- Java 25
- Maven
- OpenRouter API key (free tier available at [openrouter.ai](https://openrouter.ai))

## Features

- Multiple AI personalities via system prompts
- Session-based conversation memory
- Automatic retry with exponential backoff
- Global error handling
- OpenAPI/Swagger documentation
- Chat UI with Markdown rendering

## Setup

### 1. Clone the repository

git clone https://github.com/SandraNelj/ai-chat-service.git
cd ai-chat-service

### 2. Configure your API key

Create a .env file in the project root:

OPENROUTER_API_KEY=your-api-key-here
OPENROUTER_API_URL=https://openrouter.ai/api/v1
OPENROUTER_API_MODEL=openrouter/free

### 3. Run the application

./mvnw spring-boot:run

### 4. Access the application

- Chat UI: http://localhost:8080
- Swagger API docs: http://localhost:8080/swagger-ui.html

## API

Endpoint: POST /api/v1/chat

Request body:

{
"personality": "helper",
"message": "Hello!",
"sessionId": "my-session-123"
}

Personalities: helper, coder, pirate

Response:

{
"reply": "Hello! How can I help you?",
"personality": "helper"
}


