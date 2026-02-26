package com.example.myapplication.model;

/**
 * API çağrı durumunu temsil eden wrapper sınıf.
 * ViewModel'den UI'a LiveData ile iletilir.
 */
public class ApiResult<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String error;

    private ApiResult(Status status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResult<T> loading() {
        return new ApiResult<>(Status.LOADING, null, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(Status.SUCCESS, data, null);
    }

    public static <T> ApiResult<T> error(String error) {
        return new ApiResult<>(Status.ERROR, null, error);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
