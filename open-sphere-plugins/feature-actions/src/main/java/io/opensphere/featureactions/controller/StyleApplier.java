package io.opensphere.featureactions.controller;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementColorChangeEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleMapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleTransformerGeometryProcessor;
import io.opensphere.mantle.transformer.impl.worker.StyleBasedUpdateGeometriesWorker;
import io.opensphere.mantle.transformer.impl.worker.StyleDataElementTransformerWorkerDataProvider;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Applies style actions. */
public class StyleApplier implements ActionApplier
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(StyleApplier.class);

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The style factory. */
    private final StyleFactory myStyleFactory;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public StyleApplier(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myStyleFactory = new StyleFactory(toolbox);
    }

    @Override
    public void applyActions(Collection<? extends Action> actions, List<? extends MapDataElement> elements, DataTypeInfo dataType)
    {
        StyleTransformerGeometryProcessor provider = getGeometryProcessor(dataType);
        if (provider != null)
        {
            MapGeometrySupport mapGeometrySupport = elements.stream().filter(element -> element.getMapGeometrySupport() != null)
                    .findFirst().get().getMapGeometrySupport();

            if (mapGeometrySupport != null)
            {
                FeatureVisualizationStyle defaultStyle = provider.getStyle(mapGeometrySupport, -1);
                FeatureVisualizationStyle style = myStyleFactory.newStyle(actions, dataType, defaultStyle);
                if (style != null)
                {
                    waitForGeometries(provider, elements);

                    // Run it on the right executor because the mantle style code is
                    // not thread-safe
                    provider.getExecutor().execute(() -> applyStyle(style, elements, provider, actions));
                }
            }
            else
            {
                LOGGER.error("Could not find a map geometry support");
            }
        }
    }

    @Override
    public void clearActions(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        StyleTransformerGeometryProcessor provider = getGeometryProcessor(dataType);
        if (provider != null)
        {
            provider.getExecutor().execute(() -> clearStyle(elementIds, dataType, provider));
        }
    }

    @Override
    public void removeElements(Collection<Long> elementIds, DataTypeInfo dataType)
    {
        StyleTransformerGeometryProcessor provider = getGeometryProcessor(dataType);
        if (provider != null)
        {
            provider.removeOverrideStyle(elementIds);
        }
    }

    /**
     * Applies the style to the elements.
     *
     * @param style the style
     * @param elements the data elements
     * @param provider the data provider
     * @param actions the actions
     */
    private void applyStyle(FeatureVisualizationStyle style, List<? extends MapDataElement> elements,
            StyleTransformerGeometryProcessor provider, Collection<? extends Action> actions)
    {
        // Enable the ability to set the geometry color
        boolean isStyleChange = actions.stream().anyMatch(a -> a instanceof StyleAction);
        if (isStyleChange)
        {
            for (DataElement element : elements)
            {
                element.getVisualizationState().setColor(Color.BLACK);
            }
        }

        // Actually update the geometries
        List<Long> ids = FeatureActionUtilities.getIds(elements);
        provider.setOverrideStyle(ids, style);
        new StyleUpdater(provider, ids, style).run();

        Color color = style.getColor();

        // Update the color
        if (isStyleChange)
        {
            for (DataElement element : elements)
            {
                element.getVisualizationState().setColor(color);
            }
        }

        // Send color change event to behave like mantle
        sendColorChangeEvent(color, provider.getDataType(), ids);
    }

    /**
     * Clears any styles from the elements.
     *
     * @param elementIds the data element IDs
     * @param dataType the data type
     * @param provider the data provider
     */
    private void clearStyle(Collection<Long> elementIds, DataTypeInfo dataType, StyleTransformerGeometryProcessor provider)
    {
        Set<Long> overriddenIds = New.set(provider.getOverriddenIds());
        List<Long> ids = elementIds.stream().filter(overriddenIds::contains).collect(Collectors.toList());
        Color color = dataType.getBasicVisualizationInfo().getTypeColor();

        List<VisualizationState> visualizationStates = myMantleToolbox.getDataElementLookupUtils().getVisualizationStates(ids);
        for (VisualizationState visualizationState : visualizationStates)
        {
            visualizationState.setColor(color);
        }

        provider.removeOverrideStyle(ids);
        new StyleBasedUpdateGeometriesWorker(provider, ids).run();

        // Send color change event to behave like mantle
        sendColorChangeEvent(color, provider.getDataType(), ids);
    }

    /**
     * Sends a mantle color change event.
     *
     * @param color the color
     * @param dataType the data type
     * @param elementIds the data element IDs
     */
    private void sendColorChangeEvent(Color color, DataTypeInfo dataType, List<Long> elementIds)
    {
        TLongObjectHashMap<Color> colors = new TLongObjectHashMap<Color>();
        for (Long id : elementIds)
        {
            colors.put(id.longValue(), color);
        }
        ConsolidatedDataElementColorChangeEvent event = new ConsolidatedDataElementColorChangeEvent(elementIds,
                Collections.singleton(dataType.getTypeKey()), colors, false, this);
        event.setExternalOnly(true);
        myToolbox.getEventManager().publishEvent(event);
    }

    /**
     * Gets the geometry processor for the data type.
     *
     * @param dataType the data type
     * @return the geometry processor, or null
     */
    private StyleTransformerGeometryProcessor getGeometryProcessor(DataTypeInfo dataType)
    {
        StyleTransformerGeometryProcessor processor = null;
        MapDataElementTransformer transformer = myMantleToolbox.getDataTypeController()
                .getTransformerForType(dataType.getTypeKey());
        if (transformer instanceof StyleMapDataElementTransformer)
        {
            StyleMapDataElementTransformer styleTransformer = (StyleMapDataElementTransformer)transformer;
            processor = styleTransformer.getGeometryProcessor();
        }
        return processor;
    }

    /**
     * Waits for geometries to show up in the provider. We need to do this to ensure we can remove the old geometries.
     *
     * @param provider the data provider
     * @param elements the data elements
     */
    private void waitForGeometries(StyleDataElementTransformerWorkerDataProvider provider, List<? extends DataElement> elements)
    {
        DataElement lastElement = elements.get(elements.size() - 1);

        for (int i = 0; i < 40; i++)
        {
            Geometry geom = GeometrySetUtil.findGeometryWithId(provider.getGeometrySet(), provider.getGeometrySetLock(),
                    lastElement.getIdInCache(), provider.getDataModelIdFromGeometryIdBitMask());
            if (geom != null)
            {
                break;
            }
            ThreadUtilities.sleep(100);
        }
    }

    /**
     * Worker that updates the data element's geometry's styles. This uses a single style for all geometries passed to it.
     */
    private static class StyleUpdater extends StyleBasedUpdateGeometriesWorker
    {
        /** The style. */
        private final FeatureVisualizationStyle myStyle;

        /**
         * Instantiates a new update geometries.
         *
         * @param provider the provider
         * @param idSet the id set
         * @param style the style
         */
        public StyleUpdater(StyleDataElementTransformerWorkerDataProvider provider, List<Long> idSet,
                FeatureVisualizationStyle style)
        {
            super(provider, idSet);
            myStyle = style;
        }

        @Override
        protected boolean requiresMetaData()
        {
            return myStyle.requiresMetaData();
        }
    }
}
