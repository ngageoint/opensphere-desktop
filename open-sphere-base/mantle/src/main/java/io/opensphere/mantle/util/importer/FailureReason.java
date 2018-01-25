package io.opensphere.mantle.util.importer;

/** The reason this data source failed to load. */
public enum FailureReason
{
    /** Invalid basic auth. */
    INVALID_BASIC_AUTH,

    /** Invalid certificate. */
    INVALID_CERTIFICATE,

    /** Invalid - either basic auth or certificate. */
    INVALID_EITHER,

    /** Malformed URL. */
    MALFORMED_URL,

    /** Other reason. */
    OTHER;
}
