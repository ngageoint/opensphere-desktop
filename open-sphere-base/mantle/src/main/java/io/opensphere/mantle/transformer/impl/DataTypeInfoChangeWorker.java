package io.opensphere.mantle.transformer.impl;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportGeometryFactory;
import io.opensphere.mantle.transformer.impl.worker.AllVisibilityRenderPropertyUpdator;
import io.opensphere.mantle.transformer.impl.worker.ColorAllRenderPropertyUpdator;
import io.opensphere.mantle.transformer.impl.worker.DefaultUpdateGeometriesWorker;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DataTypeInfoChangeWorker.
 */
class DataTypeInfoChangeWorker implements Runnable
{
    /**
     * The transformer on which the worker will operate.
     */
    private final DefaultMapDataElementTransformer myDefaultMapDataElementTransformer;

    /** The event. */
    private final AbstractDataTypeInfoChangeEvent myEvent;

    /** The Factory. */
    private final MapGeometrySupportGeometryFactory myFactory;

    /**
     * Instantiates a new data type info change worker.
     *
     * @param event the event
     * @param factory the factory on which the worker will operate.
     * @param defaultMapDataElementTransformer The transformer on which the
     *            worker will operate.
     */
    public DataTypeInfoChangeWorker(DefaultMapDataElementTransformer defaultMapDataElementTransformer,
            AbstractDataTypeInfoChangeEvent event, MapGeometrySupportGeometryFactory factory)
    {
        myDefaultMapDataElementTransformer = defaultMapDataElementTransformer;
        myEvent = event;
        myFactory = factory;
    }

    @Override
    public void run()
    {
        switch (myEvent.getType())
        {
            case LIFT_CHANGED:
            case Z_ORDER_CHANGED:
            case LOADS_TO_CHANGED:
            {
                // Rebuild all geometries.
                DefaultUpdateGeometriesWorker worker = new DefaultUpdateGeometriesWorker(myDefaultMapDataElementTransformer,
                        myFactory, myDefaultMapDataElementTransformer.getIdsAsList());
                myDefaultMapDataElementTransformer.executeIfNotShutdown(worker);
            }
                break;
            case VISIBILITY_CHANGED:
            {
                // Update all render properties to not visible.
                DataTypeVisibilityChangeEvent visEvt = (DataTypeVisibilityChangeEvent)myEvent;
                AllVisibilityRenderPropertyUpdator worker = new AllVisibilityRenderPropertyUpdator(
                        myDefaultMapDataElementTransformer, visEvt.isVisible());
                myDefaultMapDataElementTransformer.executeIfNotShutdown(worker);
            }
                break;
            case METADATA_SPECIAL_KEY_CHANGED:
                // TODO: This needs work. Need to use those special
                // metadata properties
                // to alter our MapGeometrySupport and then rebuild.
                // all geometries.
                DefaultUpdateGeometriesWorker worker = new DefaultUpdateGeometriesWorker(myDefaultMapDataElementTransformer,
                        myFactory, myDefaultMapDataElementTransformer.getIdsAsList());
                myDefaultMapDataElementTransformer.executeIfNotShutdown(worker);
                break;
            case TYPE_COLOR_CHANGED:
                // Update all render properties colors to match the new
                // color.
                DataTypeInfoColorChangeEvent colorEvent = (DataTypeInfoColorChangeEvent)myEvent;
                if (colorEvent.getUpdateNumber() > myDefaultMapDataElementTransformer.getLastColorChangeUpdateNumber())
                {
                    myDefaultMapDataElementTransformer.setLastColorChangeUpdateNumber(colorEvent.getUpdateNumber());
                    ColorAllRenderPropertyUpdator updator = new ColorAllRenderPropertyUpdator(myDefaultMapDataElementTransformer,
                            colorEvent.getColor(), colorEvent.isOpacityChangeOnly());
                    myDefaultMapDataElementTransformer.executeIfNotShutdown(updator);
                    if (colorEvent.isOpacityChangeOnly())
                    {
                        MantleToolboxUtils.getDataElementUpdateUtils(myDefaultMapDataElementTransformer.getToolbox())
                                .setDataElementsOpacity(colorEvent.getColor().getAlpha(),
                                        myDefaultMapDataElementTransformer.getIdsAsList(),
                                        myDefaultMapDataElementTransformer.getDataType().getTypeKey(),
                                        myDefaultMapDataElementTransformer);
                    }
                    else
                    {
                        MantleToolboxUtils.getDataElementUpdateUtils(myDefaultMapDataElementTransformer.getToolbox())
                                .setDataElementsColor(colorEvent.getColor(),
                                        myDefaultMapDataElementTransformer.getIdsAsList(),
                                        myDefaultMapDataElementTransformer.getDataType().getTypeKey(),
                                        myDefaultMapDataElementTransformer);
                    }
                }
                break;
            case REBUILD_GEOMETRY_REQUEST:
                DefaultUpdateGeometriesWorker aWorker = new DefaultUpdateGeometriesWorker(myDefaultMapDataElementTransformer,
                        myFactory, myDefaultMapDataElementTransformer.getIdsAsList());
                myDefaultMapDataElementTransformer.executeIfNotShutdown(aWorker);
                break;
            default:
                break;
        }
    }
}
