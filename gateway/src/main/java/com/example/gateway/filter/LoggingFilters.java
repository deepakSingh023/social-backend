package com.example.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

public final class LoggingFilters {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingFilters.class);

    private LoggingFilters(){}

    public static HandlerFilterFunction<ServerResponse, ServerResponse> log() {

        return (request, next) -> {

            log.info(
                    "Incoming {} {}",
                    request.method(),
                    request.path()
            );

            ServerResponse response = next.handle(request);

            log.info(
                    "Completed {} {} -> {}",
                    request.method(),
                    request.path(),
                    response.statusCode()
            );

            return response;
        };
    }
}