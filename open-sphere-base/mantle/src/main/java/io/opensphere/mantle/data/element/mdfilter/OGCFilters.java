package io.opensphere.mantle.data.element.mdfilter;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import io.opensphere.core.scenegraph.SceneGraph.Node;
import io.opensphere.core.util.collections.New;

/**
 * The Class Filters.
 */
@XmlRootElement(name = "filters")
@XmlSeeAlso({ CustomBinaryLogicOpType.class, CustomFilterType.class })
public class OGCFilters
{
    /** The Filters. */
    // This must be an object in order to get jaxb to work, also XmlElementRef
    // uses the jaxb.index file in resources to find the class on read.
    @XmlElement(name = "filter")
    private List<Object> myFilters;

    /**
     * Instantiates a new filters.
     */
    public OGCFilters()
    {
    }

    /**
     * Adds the filter.
     *
     * @param aFilter the a filter
     */
    public void addFilter(CustomFilter aFilter)
    {
        if (myFilters == null)
        {
            myFilters = New.list();
        }
        if (!myFilters.contains(aFilter))
        {
            myFilters.add(aFilter);
        }
    }

    /**
     * Gets the filter by name.
     *
     * @param filterName the filter name
     * @return the filter by name
     */
    public CustomFilter getFilterByName(String filterName)
    {
        return myFilters.stream().filter(f -> f instanceof CustomFilter).map(f -> (CustomFilter)f)
                .filter(cf -> cf.getTitle().equals(filterName)).findFirst().orElse(null);
    }

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public List<CustomFilter> getFilters()
    {
        if (myFilters == null)
        {
            myFilters = New.list();
        }
        return myFilters.stream().filter(f -> f instanceof CustomFilter).map(f -> (CustomFilter)f).collect(Collectors.toList());
    }

    /**
     * Gets the list of objects that may be {@link CustomFilter}, or
     * {@link Node}.
     *
     * @return The list of filters.
     */
    public List<Object> getRawFilters()
    {
        return myFilters;
    }
}
