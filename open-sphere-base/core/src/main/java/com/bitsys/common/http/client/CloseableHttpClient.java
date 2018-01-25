package com.bitsys.common.http.client;

import java.io.Closeable;

/**
 * This interface is an {@link HttpClient} that is also {@link Closeable}. Closing an HTTP Client
 * cleans up resources and therefore means that any references to this instance should no longer be
 * used.
 */
public interface CloseableHttpClient extends HttpClient, Closeable {
}
