package com.bitsys.common.http.message;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.bitsys.common.http.entity.HttpEntity;
import com.google.common.base.Preconditions;

/**
 * This class provides the default implementation for an HTTP request message.
 */
public class BasicHttpRequest extends AbstractHttpMessage implements HttpRequest
{
   /** The requested URI. */
   private final URI uri;

   /** The HTTP method. */
   private final String method;

   /** The HTTP entity. */
   private final HttpEntity entity;

   /** The handler for abort requests. */
   private final AtomicReference<Abortable> abortable = new AtomicReference<>();

   /**
    * Constructs a <code>BasicHttpRequest</code>. The default HTTP method will
    * be <code>GET</code>. The HTTP entity will be <code>null</code>.
    *
    * @param uri
    *           the requested URI.
    */
   public BasicHttpRequest(final URI uri)
   {
      this(uri, GET);
   }

   /**
    * Constructs a <code>BasicHttpRequest</code>. The HTTP entity will be
    * <code>null</code>.
    *
    * @param uri
    *           the requested URI.
    * @param method
    *           the HTTP method (e.g. {@link #GET}).
    */
   public BasicHttpRequest(final URI uri, final String method)
   {
      this(uri, method, null);
   }

   /**
    * Constructs a <code>BasicHttpRequest</code>.
    *
    * @param uri
    *           the requested URI.
    * @param method
    *           the HTTP method (e.g. {@link #GET}).
    * @param entity
    *           the HTTP entity or <code>null</code>.
    */
   public BasicHttpRequest(final URI uri, final String method, final HttpEntity entity)
   {
      if (uri == null)
      {
         throw new IllegalArgumentException("The URI is null");
      }
      if (method == null)
      {
         throw new IllegalArgumentException("The HTTP method is null");
      }
      this.uri = uri;
      this.method = method;
      this.entity = entity;
      abortable.set(new Abortable() {
         private final AtomicBoolean aborted = new AtomicBoolean();

         @Override
         public boolean isAborted() {
            return aborted.get();
         }

         @Override
         public void abort() {
            aborted.set(true);
         }
      });
   }

   @Override
   public HttpEntity getEntity()
   {
      return entity;
   }

   @Override
   public URI getURI()
   {
      return uri;
   }

   @Override
   public String getMethod()
   {
      return method;
   }

   /**
    * Aborts the request. Using this method is thread-safe as opposed to invoking
    * <code>getAbortable().abort()</code> directly.
    */
   @Override
   public synchronized void abort() {
      abortable.get().abort();
   }

   @Override
   public boolean isAborted() {
      return abortable.get().isAborted();
   }

   @Override
   public synchronized void setAbortable(final Abortable abortable)
   {
      Preconditions.checkArgument(abortable != null, "The Abortable cannot be null");
      this.abortable.set(abortable);
   }

   @Override
   public Abortable getAbortable()
   {
      return abortable.get();
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (entity == null ? 0 : entity.hashCode());
      result = prime * result + method.hashCode();
      result = prime * result + uri.hashCode();
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      final BasicHttpRequest other = (BasicHttpRequest)obj;
      if (entity == null)
      {
         if (other.entity != null)
         {
            return false;
         }
      }
      else if (!entity.equals(other.entity))
      {
         return false;
      }
      if (!method.equals(other.method))
      {
         return false;
      }
      if (!uri.equals(other.uri))
      {
         return false;
      }
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder builder = new StringBuilder();
      builder.append(getMethod());
      builder.append(' ');
      builder.append(getURI());
      for (final Map.Entry<String, String> entry : getHeaders().entries())
      {
         builder.append('\n');
         builder.append(entry.getKey());
         builder.append(": ");
         builder.append(entry.getValue());
      }
      if (getEntity() != null)
      {
         builder.append('\n');
         builder.append("entity=");
         builder.append(getEntity());
      }
      if (getAbortable() != null)
      {
         builder.append('\n');
         builder.append("abortable=");
         builder.append(getAbortable());
      }
      return builder.toString();
   }
}
