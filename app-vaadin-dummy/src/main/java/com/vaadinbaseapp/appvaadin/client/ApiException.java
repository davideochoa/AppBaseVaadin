package com.vaadinbaseapp.appvaadin.client;

import com.vaadinbaseapp.appvaadin.dto.ApiError;

public class ApiException extends RuntimeException {

    private final ApiError apiError;

    public ApiException(ApiError apiError) {
        super(apiError != null ? apiError.message() : "Unknown API error");
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
