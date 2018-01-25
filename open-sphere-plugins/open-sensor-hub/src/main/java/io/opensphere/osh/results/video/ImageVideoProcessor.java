package io.opensphere.osh.results.video;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.util.OSHQuerier;

/** Video processor. */
public class ImageVideoProcessor extends AbstractVideoProcessor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageVideoProcessor.class);

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param querier The data registry querier
     */
    public ImageVideoProcessor(Toolbox toolbox, OSHQuerier querier)
    {
        super(toolbox, querier);
    }

    @Override
    public void processData(DataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            List<VideoFieldHandler> fieldHandlers)
        throws IOException
    {
        int fieldIndex = 0;
        final VideoData prevVideoData = new VideoData();
        final VideoData curVideoData = new VideoData();

        boolean eof = false;
        while (!eof)
        {
            VideoFieldHandler handler = fieldHandlers.get(fieldIndex);
            if (handler != null)
            {
                eof = handler.readField(stream, curVideoData);
            }

            fieldIndex = fieldIndex == fieldHandlers.size() - 1 ? 0 : fieldIndex + 1;

            if (curVideoData.hasData())
            {
                if (curVideoData.getTime() > 0)
                {
                    // write previous frame
                    if (prevVideoData.hasData())
                    {
                        writeVideoData(dataType, prevVideoData, curVideoData.getTime());
                    }

                    prevVideoData.setEqual(curVideoData);
                }
                else
                {
                    LOGGER.warn("Skipping frame with invalid time of " + curVideoData.getTime());
                }

                curVideoData.reset();
            }

            if (ta.isCancelled())
            {
                stream.cancel();
                break;
            }
        }

        // write final frame
        if (prevVideoData.hasData())
        {
            writeVideoData(dataType, prevVideoData, prevVideoData.getTime() + Constants.MILLI_PER_UNIT);
        }
    }
}
