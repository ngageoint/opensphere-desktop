package io.opensphere.osh.results.features;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.Aggregator;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.osh.model.OSHDataTypeInfo;

/** Interface for a feature processor. */
public interface FeatureProcessor
{
    /**
     * Processes image/video data.
     *
     * @param dataType the data type
     * @param stream the result stream
     * @param ta the task activity
     * @param aggregator the result aggregator
     * @throws IOException if an problem occurs reading the stream
     */
    void processData(OSHDataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            Aggregator<List<? extends Serializable>> aggregator)
        throws IOException;
}
