/**
 *
 */
package com.bitsys.common.http.message;

import java.net.URI;

import com.bitsys.common.http.entity.HttpEntity;
import com.google.common.collect.Multimap;

/**
 * This class assists in the creation of {@link HttpRequest}s.
 */
public class HttpRequestFactory
{
   private static HttpRequestFactory instance;

   /**
    * Default constructor.
    */
   protected HttpRequestFactory()
   {
   }

   /**
    * Returns the single instance of the factory.
    *
    * @return the single instance of the factory.
    */
   public static HttpRequestFactory getInstance()
   {
      if (instance == null)
      {
         instance = new HttpRequestFactory();
      }
      return instance;
   }

   /**
    * Creates a <code>DELETE</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @return the new request.
    */
   public HttpRequest delete(final URI uri)
   {
      return delete(uri, null);
   }

   /**
    * Creates a <code>DELETE</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest delete(final URI uri, final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.DELETE, headers);
   }

   /**
    * Creates a <code>GET</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @return the new request.
    */
   public HttpRequest get(final URI uri)
   {
      return get(uri, null);
   }

   /**
    * Creates a <code>GET</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest get(final URI uri, final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.GET, headers);
   }

   /**
    * Creates a <code>HEAD</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @return the new request.
    */
   public HttpRequest head(final URI uri)
   {
      return head(uri, null);
   }

   /**
    * Creates a <code>HEAD</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest head(final URI uri, final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.HEAD, headers);
   }

   /**
    * Creates an <code>OPTIONS</code> request on the given URI. No additional
    * HTTP headers will be set.
    *
    * @param uri the URI of the request.
    * @return the new request.
    */
   public HttpRequest options(final URI uri)
   {
      return options(uri, null);
   }

   /**
    * Creates an <code>OPTIONS</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest options(final URI uri, final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.OPTIONS, headers);
   }

   /**
    * Creates a <code>PATCH</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @return the new request.
    */
   public HttpRequest patch(final URI uri, final HttpEntity entity)
   {
      return patch(uri, entity, null);
   }

   /**
    * Creates a <code>PATCH</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest patch(final URI uri,
      final HttpEntity entity,
      final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.PATCH, entity, headers);
   }

   /**
    * Creates a <code>POST</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @return the new request.
    */
   public HttpRequest post(final URI uri, final HttpEntity entity)
   {
      return post(uri, entity, null);
   }

   /**
    * Creates a <code>POST</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest post(final URI uri,
      final HttpEntity entity,
      final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.POST, entity, headers);
   }

   /**
    * Creates a <code>PUT</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @return the new request.
    */
   public HttpRequest put(final URI uri, final HttpEntity entity)
   {
      return put(uri, entity, null);
   }

   /**
    * Creates a <code>PUT</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param entity the HTTP entity.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest put(final URI uri,
      final HttpEntity entity,
      final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.PUT, entity, headers);
   }

   /**
    * Creates a <code>TRACE</code> request on the given URI. No additional HTTP
    * headers will be set.
    *
    * @param uri the URI of the request.
    * @return the new request.
    */
   public HttpRequest trace(final URI uri)
   {
      return trace(uri, null);
   }

   /**
    * Creates a <code>TRACE</code> request on the given URI.
    *
    * @param uri the URI of the request.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the new request.
    */
   public HttpRequest trace(final URI uri, final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, HttpRequest.TRACE, headers);
   }

   /**
    * Creates an HTTP request from the given URI, HTTP method and headers.
    *
    * @param uri the URI of the request.
    * @param method the HTTP method.
    * @param headers the optional HTTP header values to set or <code>null</code>
    *           .
    * @return the {@linkplain HttpRequest}.
    */
   protected HttpRequest createHttpRequest(final URI uri,
      final String method,
      final Multimap<String, String> headers)
   {
      return createHttpRequest(uri, method, null, headers);
   }

   /**
    * Creates an HTTP request from the given URI, HTTP method and headers.
    *
    * @param uri the URI of the request.
    * @param method the HTTP method.
    * @param entity the HTTP entity.
    * @param headers the HTTP header values to set or <code>null</code>.
    * @return the {@linkplain HttpRequest}.
    */
   protected HttpRequest createHttpRequest(final URI uri,
      final String method,
      final HttpEntity entity,
      final Multimap<String, String> headers)
   {
      final HttpRequest request = new BasicHttpRequest(uri, method, entity);
      if (headers != null)
      {
         request.getHeaders().putAll(headers);
      }
      return request;
   }
}
