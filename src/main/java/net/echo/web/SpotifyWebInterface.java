package net.echo.web;

import net.echo.registry.EndpointRegistry;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SpotifyWebInterface {

    public static final OkHttpClient CLIENT = new OkHttpClient();

    public static CompletableFuture<String> get(String accessToken, String endpoint) {
        CompletableFuture<String> future = new CompletableFuture<>();

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        CLIENT.newCall(request).enqueue(new ResponseCallback(future));

        return future;
    }

    public static CompletableFuture<String> get(String accessToken, EndpointRegistry endpoint) {
        return get(accessToken, endpoint.getUrl());
    }

    private record ResponseCallback(CompletableFuture<String> future) implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            future.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (!response.isSuccessful() || response.body() == null) {
                future.completeExceptionally(new IOException("Unexpected code " + response));
                return;
            }

            String responseBody = response.body().string();

            response.body().close();

            future.complete(responseBody);
        }
    }
}
