package io.opensphere.core.util.lang;

/** Extension of {@link AutoCloseable} that doesn't throw checked exceptions. */
public interface QuietCloseable extends AutoCloseable
{
    @Override
    void close();
}
