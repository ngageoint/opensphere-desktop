package io.opensphere.core.common.connection;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class contains enumerated types for HTTP header fields.
 */
public class HttpHeaders
{
    /**
     * The mapping of field name to {@link HttpRequestHeader}.
     */
    private static final Map<String, HttpRequestHeader> REQUEST_HEADER_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * The mapping of field name to {@link HttpResponseHeader}.
     */
    private static final Map<String, HttpResponseHeader> RESPONSE_HEADER_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * HTTP request header fields.
     */
    public enum HttpRequestHeader
    {
        /**
         * Content-Types that are acceptable.
         */
        ACCEPT("Accept"),

        /**
         * Character sets that are acceptable.
         */
        ACCEPT_CHARSET("Accept-Charset"),

        /**
         * Acceptable encodings.
         */
        ACCEPT_ENCODING("Accept-Encoding"),

        /**
         * Acceptable languages for response.
         */
        ACCEPT_LANGUAGE("Accept-Language"),

        /**
         * Authentication credentials for HTTP authentication.
         */
        AUTHORIZATION("Authorization"),

        /**
         * Used to specify directives that MUST be obeyed by all caching
         * mechanisms along the request/response chain.
         */
        CACHE_CONTROL("Cache-Control"),

        /**
         * What type of connection the user-agent would prefer.
         */
        CONNECTION("Connection"),

        /**
         * An HTTP cookie previously sent by the server with
         * {@link ResponseHeader#SET_COOKIE Set-Cookie}.
         */
        COOKIE("Cookie"),

        /**
         * The length of the request body in octets (8-bit bytes).
         */
        CONTENT_LENGTH("Content-Length"),

        /**
         * A Base64-encoded binary MD5 sum of the content of the request body.
         */
        CONTENT_MD5("Content-MD5"),

        /**
         * The mime type of the body of the request (used with POST and PUT
         * requests).
         */
        CONTENT_TYPE("Content-Type"),

        /**
         * The date and time that the message was sent.
         */
        DATE("Date"),

        /**
         * Indicates that particular server behaviors are requested by the
         * client.
         */
        EXPECT("Expect"),

        /**
         * The e-mail address of the user making the request.
         */
        FROM("From"),

        /**
         * The domain name of the server (for virtual hosting), mandatory since
         * HTTP/1.1.
         */
        HOST("Host"),

        /**
         * Only perform the action if the client supplied entity matches the
         * same entity on the server. This is mainly for methods like PUT to
         * only update a resource if it has not been modified since the user
         * last updated it.
         */
        IF_MATCH("If-Match"),

        /**
         * Allows a <i>304 Not Modified</i> to be returned if content is
         * unchanged.
         */
        IF_MODIFIED_SINCE("If-Modified-Since"),

        /**
         * Allows a <i>304 Not Modified</i> to be returned if content is
         * unchanged, see HTTP {@link ResponseHeader#ETAG ETag}.
         */
        IF_NONE_MATCH("If-None-Match"),

        /**
         * If the entity is unchanged, send me the part(s) that I am missing.
         * Otherwise, send me the entire new entity.
         */
        IF_RANGE("If-Range"),

        /**
         * Only send the response if the entity has not been modified since a
         * specific time.
         */
        IF_UNMODIFIED_SINCE("If-Unmodified-Since"),

        /**
         * Custom request header that holds the flag indicating if the server
         * name handling the HTTP request should be included in the response.
         */
        INCLUDE_SERVER_NAME("Include-Server-Name"),

        /**
         * Limit the number of times the message can be forwarded through
         * proxies or gateways.
         */
        MAX_FORWARDS("Max-Forwards"),

        /**
         * Authorization credentials for connecting to a proxy.
         */
        PROXY_AUTHORIZATION("Proxy-Authorization"),

        /**
         * Request only part of an entity. Bytes are numbered from 0.
         */
        RANGE("Range"),

        /**
         * This is the address of the previous web page from which a link to the
         * currently requested page was followed.
         */
        REFERRER("Referrer"),

        /**
         * The transfer encodings the user agent is willing to accept: the same
         * values as for the response header
         * {@link ResponseHeader#TRANSFER_ENCODING Transfer-Encoding} can be
         * used, plus the "trailers" value (related to the "chunked" transfer
         * method) to notify the server it accepts to receive additional headers
         * (the trailers) after the last, zero-sized, chunk.
         */
        TE("TE"),

        /**
         * Ask the server to upgrade to another protocol.
         */
        UPGRADE("Upgrade"),

        /**
         * The user agent string of the user agent.
         */
        USER_AGENT("User-Agent"),

        /**
         * Informs the server of proxies through which the request was sent.
         */
        VIA("Via"),

        /**
         * A general warning about possible problems with the entity body.
         */
        WARNING("Warning");

        /**
         * The HTTP header field name.
         */
        private final String fieldName;

        /**
         * Constructs a RequestHeader.
         *
         * @param fieldName the HTTP header field name.
         */
        HttpRequestHeader(String fieldName)
        {
            this.fieldName = fieldName;
            REQUEST_HEADER_MAP.put(fieldName, this);
        }

        /**
         * Returns the HTTP header field name.
         *
         * @return the HTTP header field name.
         */
        public String getFieldName()
        {
            return fieldName;
        }

        /**
         * Returns the {@link HttpRequestHeader} for the given HTTP header field
         * name.
         *
         * @param fieldName the HTTP header field name.
         * @return the {@link HttpRequestHeader} for the given HTTP header field
         *         name or <code>null</code> if one was not found.
         */
        public static HttpRequestHeader fromFieldName(String fieldName)
        {
            return REQUEST_HEADER_MAP.get(fieldName);
        }
    }

    /**
     * HTTP response header fields.
     */
    public enum HttpResponseHeader
    {
        /**
         * What partial content range types this server supports.
         */
        ACCEPT_RANGES("Accept-Ranges"),

        /**
         * The age the object has been in a proxy cache in seconds.
         */
        AGE("Age"),

        /**
         * Valid actions for a specified resource. To be used for a <i>405
         * method not allowed</i>.
         */
        ALLOW("Allow"),

        /**
         * Tells all caching mechanisms from server to client whether they may
         * cache this object.
         */
        CACHE_CONTROL("Cache-Control"),

        /**
         * The type of encoding used on the data.
         */
        CONTENT_ENCODING("Content-Encoding"),

        /**
         * The language the content is in.
         */
        CONTENT_LANGUAGE("Content-Language"),

        /**
         * The length of the response body in octets (8-bit bytes).
         */
        CONTENT_LENGTH("Content-Length"),

        /**
         * An alternative location for the returned data.
         */
        CONTENT_LOCATION("Content-Location"),

        /**
         * A Base64-Encoded binary MD5 sum of the content of the response.
         */
        CONTENT_MD5("Content-MD5"),

        /**
         * An opportunity to raise a "File Download" dialogue box for a known
         * MIME type.
         */
        CONTENT_DISPOSITION("Content-Disposition"),

        /**
         * Where in a full body message this partial message belongs.
         */
        CONTENT_RANGE("Content-Range"),

        /**
         * The mime type of this content (e.g. Content-Type:
         * text/html;charset=utf-8).
         */
        CONTENT_TYPE("Content-Type"),

        /**
         * The date and time that the message was sent.
         */
        DATE("Date"),

        /**
         * An identifier for a specific version of a resource, often a Message
         * Digest, see ETag.
         */
        ETAG("ETag"),

        /**
         * The http expires header field.
         */
        EXPIRES("Expires"),

        /**
         * The last modified date for the requested object in RFC 2822 format.
         */
        LAST_MODIFIED("Last-Modified"),

        /**
         * Used to express a typed relationship with another resource, where the
         * relation type is defined by RFC 5988.
         */
        LINK("Link"),

        /**
         * Used in redirection, or when a new resource has been created.
         */
        LOCATION("Location"),

        /**
         * This header is supposed to set P3P policy, in the form of
         * <code>P3P:CP="your_compact_policy"</code>.
         * <p>
         * However, P3P did no take off, most browsers have never fully
         * implemented it, a lot of web sites set this header with fake policy
         * text, that was enough to fool browsers the existence of P3P policy
         * and grant permissions for third party cookies.
         */
        P3P("P3P"),

        /**
         * Request authentication to access the proxy.
         */
        PROXY_AUTHENTICATE("Proxy-Authenticate"),

        /**
         * Used in redirection, or when a new resource has been created. This
         * refresh redirects after 5 seconds. (This is a
         * proprietary/non-standard header extension introduced by Netscape and
         * supported by most web browsers).
         */
        REFRESH("Refresh"),

        /**
         * If an entity is temporarily unavailable, this instructs the client to
         * try again after a specified period of time.
         */
        RETRY_AFTER("Retry-After"),

        /**
         * A name for the server.
         */
        SERVER("Server"),

        /**
         * An HTTP cookie.
         */
        SET_COOKIE("Set-Cookie"),

        /**
         * The trailer general field value indicates that the given set of
         * header fields is present in the trailer of a message encoded with
         * chunked transfer-coding.
         */
        TRAILER("Trailer"),

        /**
         * The form of encoding used to safely transfer the entity to the user.
         * Currently defined methods are: chunked, compress, deflate, gzip and
         * identity.
         */
        TRANSFER_ENCODING("Transfer-Encoding"),

        /**
         * Tells downstream proxies how to match future request headers to
         * decide whether the cached response can be used rather than requesting
         * a fresh one.
         */
        VARY("Vary"),

        /**
         * Informs the client of proxies through which the resopnse was sent.
         */
        VIA("Via"),

        /**
         * A general warning about possible problems with the entity body.
         */
        WARNING("Warning"),

        /**
         * Indicates the authentication scheme that should be used to access the
         * requested entity.
         */
        WWW_AUTHENTICATE("WWW-Authenticate");

        /**
         * The HTTP header field name.
         */
        private final String fieldName;

        /**
         * Constructs a ResponseHeader.
         *
         * @param fieldName the HTTP header field name.
         */
        HttpResponseHeader(String fieldName)
        {
            this.fieldName = fieldName;
            RESPONSE_HEADER_MAP.put(fieldName, this);
        }

        /**
         * Returns the HTTP header field name.
         *
         * @return the HTTP header field name.
         */
        public String getFieldName()
        {
            return fieldName;
        }

        /**
         * Returns the {@link HttpResponseHeader} for the given HTTP header
         * field name.
         *
         * @param fieldName the HTTP header field name.
         * @return the {@link HttpResponseHeader} for the given HTTP header
         *         field name or <code>null</code> if one was not found.
         */
        public static HttpResponseHeader fromFieldName(String fieldName)
        {
            return RESPONSE_HEADER_MAP.get(fieldName);
        }
    }
}
