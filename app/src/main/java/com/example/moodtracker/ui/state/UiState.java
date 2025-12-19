package com.example.moodtracker.ui.state;

import androidx.annotation.Nullable;

public class UiState<T> {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String error;

    private UiState(Status status, @Nullable T data, @Nullable String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> UiState<T> idle(@Nullable T data) {
        return new UiState<>(Status.IDLE, data, null);
    }

    public static <T> UiState<T> loading(@Nullable T data) {
        return new UiState<>(Status.LOADING, data, null);
    }

    public static <T> UiState<T> success(@Nullable T data) {
        return new UiState<>(Status.SUCCESS, data, null);
    }

    public static <T> UiState<T> error(@Nullable T data, String error) {
        return new UiState<>(Status.ERROR, data, error);
    }

    public Status getStatus() { return status; }
    @Nullable public T getData() { return data; }
    @Nullable public String getError() { return error; }
}
