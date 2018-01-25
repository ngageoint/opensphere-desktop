package io.opensphere.osh.results.video;

import java.io.IOException;
import java.util.List;

import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.data.DataTypeInfo;

/** Interface for a video processor. */
public interface VideoProcessor
{
    /**
     * Processes image/video data.
     *
     * @param dataType the data type
     * @param stream the result stream
     * @param ta the task activity
     * @param fieldHandlers the field handlers
     * @throws IOException if an problem occurs reading the stream
     */
    void processData(DataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            List<VideoFieldHandler> fieldHandlers)
        throws IOException;
}
