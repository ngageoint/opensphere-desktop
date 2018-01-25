package io.opensphere.core.search;

/**
 * An abstract search provider for place name search providers to inherit from.
 */
public abstract class PlaceNameSearchProvider implements ResultsSearchProvider
{
    @Override
    public String getType()
    {
        return "Place Names";
    }
}
