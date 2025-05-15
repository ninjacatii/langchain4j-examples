package dev.langchain4j.example.entity.agent.message_manager._service;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.openai.internal.ErrorHandling;
import dev.langchain4j.model.openai.internal.ResponseHandle;
import dev.langchain4j.model.openai.internal.StreamingCompletionHandling;
import dev.langchain4j.model.openai.internal.StreamingResponseHandling;

import java.util.function.Consumer;

class StreamingRequestExecutor<Response> {
    private final HttpClient httpClient;
    private final HttpRequest streamingHttpRequest;
    private final Class<Response> responseClass;

    StreamingRequestExecutor(HttpClient httpClient, HttpRequest streamingHttpRequest, Class<Response> responseClass) {
        this.httpClient = httpClient;
        this.streamingHttpRequest = streamingHttpRequest;
        this.responseClass = responseClass;
    }

    StreamingResponseHandling onPartialResponse(final Consumer<Response> partialResponseHandler) {
        return new StreamingResponseHandling() {
            public StreamingCompletionHandling onComplete(final Runnable streamingCompletionCallback) {
                return new StreamingCompletionHandling() {
                    public ErrorHandling onError(final Consumer<Throwable> errorHandler) {
                        return new ErrorHandling() {
                            public ResponseHandle execute() {
                                return StreamingRequestExecutor.this.stream(partialResponseHandler, streamingCompletionCallback, errorHandler);
                            }
                        };
                    }

                    public ErrorHandling ignoreErrors() {
                        return new ErrorHandling() {
                            public ResponseHandle execute() {
                                return StreamingRequestExecutor.this.stream(partialResponseHandler, streamingCompletionCallback, (e) -> {
                                });
                            }
                        };
                    }
                };
            }

            public ErrorHandling onError(final Consumer<Throwable> errorHandler) {
                return new ErrorHandling() {
                    public ResponseHandle execute() {
                        return StreamingRequestExecutor.this.stream(partialResponseHandler, () -> {
                        }, errorHandler);
                    }
                };
            }

            public ErrorHandling ignoreErrors() {
                return new ErrorHandling() {
                    public ResponseHandle execute() {
                        return StreamingRequestExecutor.this.stream(partialResponseHandler, () -> {
                        }, (e) -> {
                        });
                    }
                };
            }
        };
    }

    private ResponseHandle stream(final Consumer<Response> partialResponseHandler, final Runnable streamingCompletionCallback, final Consumer<Throwable> errorHandler) {
        ServerSentEventListener listener = new ServerSentEventListener() {
            public void onEvent(ServerSentEvent event) {
                if (!"[DONE]".equals(event.data())) {
                    try {
                        if ("error".equals(event.event())) {
                            errorHandler.accept(new RuntimeException(event.data()));
                            return;
                        }

                        Response response = (Response) Json.fromJson(event.data(), StreamingRequestExecutor.this.responseClass);
                        if (response != null) {
                            partialResponseHandler.accept(response);
                        }
                    } catch (Exception e) {
                        errorHandler.accept(e);
                    }

                }
            }

            public void onClose() {
                streamingCompletionCallback.run();
            }

            public void onError(Throwable t) {
                errorHandler.accept(t);
            }
        };
        this.httpClient.execute(this.streamingHttpRequest, listener);
        return new ResponseHandle();
    }
}
