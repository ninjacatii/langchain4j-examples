package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.internal.Json;

class SyncRequestExecutor<Response> {
    private final HttpClient httpClient;
    private final HttpRequest httpRequest;
    private final Class<Response> responseClass;

    SyncRequestExecutor(HttpClient httpClient, HttpRequest httpRequest, Class<Response> responseClass) {
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.responseClass = responseClass;
    }

    Response execute() {
        SuccessfulHttpResponse successfulHttpResponse = this.httpClient.execute(this.httpRequest);
        return (Response) Json.fromJson(successfulHttpResponse.body(), this.responseClass);
    }
}
