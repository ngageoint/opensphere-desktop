package io.opensphere.wfs.consumer;

import java.util.List;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.rangeset.DefaultRangedLongSet;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * Consumer that receive Feature data and supplies it to the type controller.
 */
public class DataTypeControllerFeatureConsumer extends FeatureConsumer
{
    /** The mantle type controller. */
    private final DataTypeController ctrl;

    /** Id Set. */
    private final DefaultRangedLongSet idSet = new DefaultRangedLongSet();

    /** Killed. */
    private boolean killed;

    /**
     * Used to prevent {@link DataElement} being added that are not within the
     * load times. Null if all {@link DataElement} should be added no matter
     * their times.
     */
    private final TimeManager myTimeManager;

    /** The layer type that's associated with the features to consume. */
    private final DataTypeInfo myType;

    /**
     * Constructor.
     *
     * @param dataTypeController the controller which receives the consumed data
     *            elements.
     * @param timeManager Used to prevent {@link DataElement} being added that
     *            are not within the load times. Null if all {@link DataElement}
     *            should be added no matter their times.
     * @param info the {@link DataTypeInfo} associated with the features
     * @param flushSize The number of features to accumulate before flushing to
     *            the data element cache.
     */
    public DataTypeControllerFeatureConsumer(DataTypeController dataTypeController, TimeManager timeManager, DataTypeInfo info,
            int flushSize)
    {
        super(flushSize);
        myType = info;
        ctrl = dataTypeController;
        myTimeManager = timeManager;
    }

    @Override
    public void cleanup()
    {
        super.cleanup();
        synchronized (idSet)
        {
            killed = true;
            ctrl.removeDataElements(myType, idSet.getValues());
        }
    }

    @Override
    public void flush()
    {
        List<MapDataElement> features = consumeFeatures();
        if (features == null || features.isEmpty())
        {
            return;
        }
        synchronized (idSet)
        {
            if (killed)
            {
                return;
            }
            if (!ctrl.hasDataTypeInfoForTypeKey(myType.getTypeKey()))
            {
                ctrl.addDataType(getClass().getSimpleName(), myType.getTypeKey(), myType, this);
            }

            features = dontAddElementOutsideLoadTime(features);
            if (!features.isEmpty())
            {
                idSet.addAll(ctrl.addMapDataElements(myType, null, null, features, this));
            }
        }
    }

    /**
     * Gets the system {@link TimeManager}.
     *
     * @return The {@link TimeManager} or null if this consumer is not time
     *         aware.
     */
    protected TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    /**
     * If this consumer is time aware, this function only returns the features
     * that are within a load span.
     *
     * @param features The features to check for load span containment.
     * @return The features that are within load span.
     */
    private List<MapDataElement> dontAddElementOutsideLoadTime(List<MapDataElement> features)
    {
        List<MapDataElement> filtered = features;
        if (myTimeManager != null)
        {
            List<TimeSpan> loadSpans = New.list(myTimeManager.getLoadTimeSpans());
            List<MapDataElement> inTimeFeatures = New.list();
            for (MapDataElement feature : features)
            {
                TimeSpan featureSpan = feature.getTimeSpan();
                for (TimeSpan loadSpan : loadSpans)
                {
                    if (featureSpan.isTimeless() || loadSpan.contains(featureSpan))
                    {
                        inTimeFeatures.add(feature);
                        break;
                    }
                }
            }

            filtered = inTimeFeatures;
        }

        return filtered;
    }
}
