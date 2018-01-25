package io.opensphere.filterbuilder.impl;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.mdfilter.CustomBinaryLogicOpType;
import io.opensphere.mantle.data.element.mdfilter.CustomFilter;
import io.opensphere.mantle.data.element.mdfilter.CustomFilterType;
import io.opensphere.mantle.data.element.mdfilter.OGCFilters;

/**
 * Reads the filters out of a state file.
 */
public class FilterReader
{
    /**
     * Reads the filters contained in the file.
     *
     * @param file The filter file.
     * @return The list of {@link CustomFilter} contained in the node.
     * @throws JAXBException If there was an exception reading the node.
     */
    public List<CustomFilter> readFilters(File file) throws JAXBException
    {
        OGCFilters filters = XMLUtilities.readXMLObject(file, OGCFilters.class);
        return keepReadingEm(filters);
    }

    /**
     * Reads the filters contained in the filter node.
     *
     * @param filtersNode The filter node.
     * @return The list of {@link CustomFilter} contained in the node.
     * @throws JAXBException If there was an exception reading the node.
     */
    public List<CustomFilter> readFilters(Node filtersNode) throws JAXBException
    {
        OGCFilters filters = XMLUtilities.readXMLObject(filtersNode, OGCFilters.class);
        return keepReadingEm(filters);
    }

    /**
     * Because the individual filter can be different types we need to read them
     * again if they are xml nodes.
     *
     * @param filters The filters to read.
     * @return The list of {@link CustomFilter} contained in the node.
     * @throws JAXBException If there was an exception reading the node.
     */
    private List<CustomFilter> keepReadingEm(OGCFilters filters) throws JAXBException
    {
        List<CustomFilter> filterList = New.list();
        if (filters.getRawFilters() != null)
        {
            for (Object aFilter : filters.getRawFilters())
            {
                CustomFilter customFilter = null;
                if (aFilter instanceof Node)
                {
                    CustomFilterType type = XMLUtilities.readXMLObject((Node)aFilter, CustomFilterType.class);
                    if (!"spatial".equals(type.getFilterType()))
                    {
                        CustomBinaryLogicOpType metadata = XMLUtilities.readXMLObject((Node)aFilter,
                                CustomBinaryLogicOpType.class);
                        customFilter = metadata;
                    }
                    else
                    {
                        customFilter = type;
                    }
                }
                else if (aFilter instanceof CustomFilter)
                {
                    customFilter = (CustomFilter)aFilter;
                }

                if (customFilter != null)
                {
                    filterList.add(customFilter);
                }
            }
        }
        return filterList;
    }
}
