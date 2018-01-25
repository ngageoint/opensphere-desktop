package io.opensphere.core.common.services;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is a <code>RuntimeException</code> for web services.
 */
public class ServiceException extends RuntimeException
{
    /**
     * The default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The HTTP response.
     */
    private Response response;

    /**
     * Constructs a new service exception with <code>null</code> as its detail
     * message and default HTTP status code of 500.
     */
    public ServiceException()
    {
        response = Response.serverError().build();
    }

    /**
     * Constructs a new service exception with the specified detail message and
     * default HTTP status code of 500. The cause is not initialized, and may
     * subsequently be initialized by a call to
     * {@link Throwable#initCause(java.lang.Throwable)}.
     *
     * @param message the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable#getMessage()} method.
     */
    public ServiceException(String message)
    {
        super(message);
        response = Response.serverError().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    public ServiceException(String message, int statusCode)
    {
        super(message);
        response = Response.status(statusCode).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    public ServiceException(String message, Throwable cause, int statusCode)
    {
        super(message, cause);
        response = Response.status(statusCode).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Constructs a new service exception with the specified cause and a detail
     * message of <code>(cause==null ? null : cause.toString())</code> (which
     * typically contains the class and detail message of <code>cause</code>)
     * and default HTTP status code of 500. This constructor is useful for
     * service exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ServiceException(Throwable cause)
    {
        super(cause);
        response = Response.serverError().build();
    }

    /**
     * Constructs a new service exception with the specified detail message and
     * cause and default HTTP status code of 500.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this service exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by
     *            the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ServiceException(String message, Throwable cause)
    {
        super(message, cause);
        response = Response.serverError().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Constructs a new service exception with <code>null</code> as its detail
     * message.
     */
    public ServiceException(Response response)
    {
        this.response = response;
    }

    /**
     * Constructs a new service exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link Throwable#initCause(java.lang.Throwable)}.
     *
     * @param message the detail message. The detail message is saved for later
     *            retrieval by the {@link Throwable#getMessage()} method.
     */
    public ServiceException(String message, Response response)
    {
        super(message);
        this.response = response;
    }

    /**
     * Constructs a new service exception with the specified cause and a detail
     * message of <code>(cause==null ? null : cause.toString())</code> (which
     * typically contains the class and detail message of <code>cause</code>).
     * This constructor is useful for service exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ServiceException(Throwable cause, Response response)
    {
        super(cause);
        this.response = response;
    }

    /**
     * Constructs a new service exception with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this service exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by
     *            the {@link Throwable#getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *            {@link Throwable#getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ServiceException(String message, Throwable cause, Response response)
    {
        super(message, cause);
        this.response = response;
    }

    /**
     * Get the HTTP response.
     *
     * @return the HTTP response.
     */
    public Response getResponse()
    {
        return response;
    }
}
