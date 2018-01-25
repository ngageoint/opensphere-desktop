package io.opensphere.wfs.consumer;

import java.util.List;
import java.util.Map;

import io.opensphere.core.TimeManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * The Class FeatureConsumerManager.
 */
public class FeatureConsumerManager
{
    /** Map of allocated and available consumers based on data type. */
    private final Map<Pair<String, Boolean>, FeatureConsumer> myConsumerList = New.map();

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /**
     * The system time manager.
     */
    private final TimeManager myTimeManager;

    /**
     * Instantiates a new feature consumer manager.
     *
     * @param mantleToolbox the core toolbox
     * @param timeManager The system time manager.
     */
    public FeatureConsumerManager(MantleToolbox mantleToolbox, TimeManager timeManager)
    {
        myMantleToolbox = mantleToolbox;
        myTimeManager = timeManager;
    }

    /**
     * Removes the type.
     *
     * @param type the type
     */
    public void removeType(WFSDataType type)
    {
        Utilities.checkNull(type, "type");
        Utilities.checkNull(type.getTypeKey(), "type.getTypeKey()");

        List<FeatureConsumer> consumers = New.list(myConsumerList.remove(new Pair<>(type.getTypeKey(), Boolean.TRUE)),
                myConsumerList.remove(new Pair<>(type.getTypeKey(), Boolean.FALSE)));
        for (FeatureConsumer consumer : consumers)
        {
            if (consumer != null)
            {
                consumer.cleanup();
            }
        }
    }

    /**
     * Request a consumer for a specific {@link DataTypeInfo}.
     *
     * @param type the type (layer) associated with the features
     * @param isLoadTimeAware isLoadTimeAware True if this consumer shouldn't
     *            add any {@link DataElement} that aren't within the load spans,
     *            false if an element should always be added no matter the time.
     * @return the feature consumer for the specified type
     */
    public FeatureConsumer requestConsumer(DataTypeInfo type, boolean isLoadTimeAware)
    {
        Utilities.checkNull(type, "type");
        Utilities.checkNull(type.getTypeKey(), "type.getTypeKey()");

        FeatureConsumer consumer = myConsumerList.get(new Pair<>(type.getTypeKey(), Boolean.valueOf(isLoadTimeAware)));
        if (consumer == null)
        {
            int flushSize = myMantleToolbox.getDataElementCache().getPreferredInsertBlockSize();
            if (flushSize <= 0)
            {
                flushSize = 5000;
            }
            consumer = new DataTypeControllerFeatureConsumer(myMantleToolbox.getDataTypeController(),
                    isLoadTimeAware ? myTimeManager : null, type, flushSize);
            myConsumerList.put(new Pair<>(type.getTypeKey(), Boolean.valueOf(isLoadTimeAware)), consumer);
        }
        return consumer;
    }
}
