package io.opensphere.core.search;

/** Interface for search services. */
public interface SearchProvider
{
    /**
     * Return the name of the search service.
     *
     * @return The name.
     */
    String getName();

    /**
     * The "type" of search is provider is. Such as Place Names or Layer search.
     * This search type is up to the provider to decide and will allow the user
     * to organize search results by this type.
     *
     * @return The search type of this provider.
     */
    String getType();
}
