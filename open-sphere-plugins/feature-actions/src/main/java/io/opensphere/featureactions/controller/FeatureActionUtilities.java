package io.opensphere.featureactions.controller;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.util.DataElementLookupException;

/** Feature action utilities. */
public final class FeatureActionUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionUtilities.class);

    /**
     * Gets the data elements from mantle.
     *
     * @param mantleToolbox the mantle toolbox
     * @param ids the data element IDs
     * @param dataType the data type
     * @return the data elements
     */
    public static Collection<DataElement> getDataElements(MantleToolbox mantleToolbox, Collection<Long> ids,
            DataTypeInfo dataType)
    {
        Collection<DataElement> elements;
        try
        {
            elements = mantleToolbox.getDataElementLookupUtils().getDataElements(CollectionUtilities.getList(ids), dataType, null,
                    true);
        }
        catch (DataElementLookupException e)
        {
            elements = Collections.emptyList();
            LOGGER.error("Failed to look up data elements for " + ids, e);
        }
        return elements;
    }

    /**
     * Gets the element IDs from the elements.
     *
     * @param elements the data elements
     * @return the element IDs
     */
    public static List<Long> getIds(Collection<? extends MapDataElement> elements)
    {
        return elements.stream().map(e -> Long.valueOf(e.getIdInCache())).collect(Collectors.toList());
    }

    /**
     * Gets the color from the actions.
     *
     * @param actions the actions
     * @return the color, or null
     */
    public static Color getColor(Collection<? extends Action> actions)
    {
        Color color = null;
        Collection<StyleAction> styleActions = CollectionUtilities.filterDowncast(actions, StyleAction.class);
        if (!styleActions.isEmpty())
        {
            color = styleActions.iterator().next().getStyleOptions().getColor();
        }
        return color;
    }

    /** Disallow instantiation. */
    private FeatureActionUtilities()
    {
    }
}
