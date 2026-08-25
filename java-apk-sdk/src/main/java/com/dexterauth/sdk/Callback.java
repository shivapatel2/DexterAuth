package com.dexterauth.sdk;

/**
 * Asynchronous callback interface for DexterAuth operations.
 *
 * @param <T> The result type.
 */
public interface Callback<T> {
    void onSuccess(T result);
    void onError(String error);
}
