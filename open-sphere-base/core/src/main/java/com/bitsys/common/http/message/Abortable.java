package com.bitsys.common.http.message;

/**
 * This interface represents a class that can be aborted.
 * <p>
 * Implementations of this class must be thread-safe.
 */
public interface Abortable
{
   /**
    * Aborts execution.
    */
   void abort();

   /**
    * Indicates if there has been a request to abort the execution.
    *
    * @return <code>true</code> if the {@link #abort()} method has been called.
    */
   boolean isAborted();
}
