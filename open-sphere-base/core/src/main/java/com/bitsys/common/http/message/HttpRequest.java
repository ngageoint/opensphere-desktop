package com.bitsys.common.http.message;

import java.net.URI;

/**
 * This interface defines the contract for HTTP request messages.
 */
public interface HttpRequest extends HttpMessage, Abortable
{
    /**
     * This enumeration defines the standard HTTP methods.
     */
    @SuppressWarnings("hiding")
    enum HttpMethod
    {
        /** The HTTP <code>DELETE</code> method. */
        DELETE("DELETE"),

        /** The HTTP <code>GET</code> method. */
        GET("GET"),

        /** The HTTP <code>HEAD</code> method. */
        HEAD("HEAD"),

        /** The HTTP <code>OPTIONS</code> method. */
        OPTIONS("OPTIONS"),

        /** The HTTP <code>PATCH</code> method. */
        PATCH("PATCH"),

        /** The HTTP <code>POST</code> method. */
        POST("POST"),

        /** The HTTP <code>PUT</code> method. */
        PUT("PUT"),

        /** The HTTP <code>TRACE</code> method. */
        TRACE("TRACE");

        /** The HTTP method. */
        private final String method;

        /**
         * Constructs a new {@linkplain HttpMethod} with the given HTTP method.
         *
         * @param method the HTTP method.
         */
        HttpMethod(final String method)
        {
            this.method = method;
        }

        /**
         * Returns the HTTP method.
         *
         * @return the HTTP method.
         */
        public String getMethod()
        {
            return method;
        }
    }

    /** The HTTP <code>DELETE</code> method. */
    String DELETE = HttpMethod.DELETE.getMethod();

    /** The HTTP <code>GET</code> method. */
    String GET = HttpMethod.GET.getMethod();

    /** The HTTP <code>HEAD</code> method. */
    String HEAD = HttpMethod.HEAD.getMethod();

    /** The HTTP <code>OPTIONS</code> method. */
    String OPTIONS = HttpMethod.OPTIONS.getMethod();

    /** The HTTP <code>PATCH</code> method. */
    String PATCH = HttpMethod.PATCH.getMethod();

    /** The HTTP <code>POST</code> method. */
    String POST = HttpMethod.POST.getMethod();

    /** The HTTP <code>PUT</code> method. */
    String PUT = HttpMethod.PUT.getMethod();

    /** The HTTP <code>TRACE</code> method. */
    String TRACE = HttpMethod.TRACE.getMethod();

    /**
     * Returns the HTTP method this request uses. For example, <code>GET</code>,
     * <code>POST</code>, <code>PUT</code>, <code>DELETE</code>, etc.
     *
     * @return the HTTP method of this request.
     */
    String getMethod();

    /**
     * Returns the URI of this request.
     *
     * @return the URI of this request.
     */
    URI getURI();

    /**
     * Sets the {@link Abortable} instance that will handle aborting requests.
     *
     * @param abortable the abortable.
     */
    void setAbortable(Abortable abortable);

    /**
     * Returns the {@link Abortable} instance.
     *
     * @return the abortable.
     * @deprecated Use the {@link #abort()} and {@link #isAborted()} methods on
     *             this class as they can be made thread-safe.
     */
    @Deprecated
    Abortable getAbortable();
}
