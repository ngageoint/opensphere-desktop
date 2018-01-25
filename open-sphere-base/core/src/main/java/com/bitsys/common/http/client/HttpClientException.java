package com.bitsys.common.http.client;

/**
 * This exception indicates a problem in the configuration or execution of the
 * {@link HttpClient} instance.
 */
public class HttpClientException extends RuntimeException
{
   /** serialVersionUID */
   private static final long serialVersionUID = -1666885171314339741L;

   /**
    * Constructs an <code>HttpClientException</code>.
    */
   public HttpClientException()
   {
   }

   /**
    * Constructs an <code>HttpClientException</code>.
    *
    * @param message
    *           the detail message. The detail message is saved for later
    *           retrieval by the {@link #getMessage()} method.
    */
   public HttpClientException(final String message)
   {
      super(message);
   }

   /**
    * Constructs an <code>HttpClientException</code>.
    *
    * @param cause
    *           the cause (which is saved for later retrieval by the
    *           {@link #getCause()} method). (A <code>null</code> value is
    *           permitted, and indicates that the cause is nonexistent or
    *           unknown.)
    */
   public HttpClientException(final Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructs an <code>HttpClientException</code>.
    * <p/>
    * Note that the detail message associated with cause is <i>not</i>
    * automatically incorporated in this runtime exception's detail message.
    *
    * @param message
    *           the detail message. The detail message is saved for later
    *           retrieval by the {@link #getMessage()} method.
    * @param cause
    *           the cause (which is saved for later retrieval by the
    *           {@link #getCause()} method). (A <code>null</code> value is
    *           permitted, and indicates that the cause is nonexistent or
    *           unknown.)
    */
   public HttpClientException(final String message, final Throwable cause)
   {
      super(message, cause);
   }
}
