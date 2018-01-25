package io.opensphere.core.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates that access to a field is confined to a
 * single thread. This is an alternative to the
 * {@link javax.annotation.concurrent.GuardedBy} annotation, since a field that
 * is thread confined does not need synchronization.
 */
@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface ThreadConfined
{
    /**
     * Get the type of access that is thread-confined.
     *
     * @return The access type.
     */
    AccessType type() default AccessType.READWRITE;

    /**
     * Get the name of the thread that a field is confined to.
     *
     * @return The name.
     */
    String value();

    /** Enum of the types of access that can be thread-confined. */
    enum AccessType
    {
        /** Read-access is thread-confined. */
        READ,

        /** Both read and write access are thread-confined. */
        READWRITE,

        /** Write-access is thread-confined. */
        WRITE,
    }
}
