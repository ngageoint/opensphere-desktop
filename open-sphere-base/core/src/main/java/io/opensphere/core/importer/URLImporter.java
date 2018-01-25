package io.opensphere.core.importer;

/** Imports URLs through the importer framework. */
@FunctionalInterface
public interface URLImporter
{
    /**
     * Import a URL.
     *
     * @param urlString The url string.
     */
    void importURL(String urlString);
}
