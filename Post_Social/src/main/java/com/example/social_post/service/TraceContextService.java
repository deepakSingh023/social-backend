package com.example.social_post.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TraceContextService {

    private final OpenTelemetry openTelemetry;

    private static final TextMapSetter<Map<String, String>> SETTER =
            Map::put;

    public String currentTraceParent() {

        Map<String,String> carrier = new HashMap<>();

        openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), carrier, SETTER);

        return carrier.get("traceparent");
    }

    public Context restore(String traceParent) {

        if (traceParent == null || traceParent.isBlank()) {
            return Context.current();
        }

        Map<String, String> carrier = Map.of(
                "traceparent",
                traceParent
        );

        return openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(
                        Context.current(),
                        carrier,
                        GETTER
                );
    }

    private static final TextMapGetter<Map<String, String>> GETTER =
            new TextMapGetter<>() {

                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    if (carrier == null) {
                        return null;
                    }
                    return carrier.get(key);
                }
            };
}