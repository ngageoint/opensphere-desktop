package io.opensphere.auxiliary.video;

import java.nio.ByteBuffer;
import java.util.Date;

import net.jcip.annotations.NotThreadSafe;

import org.apache.log4j.Logger;

import gnu.trove.list.TLongList;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.ListOfBytesOutputStream;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.RawVideoChunk;
import io.opensphere.core.video.StreamChunkRunner;
import io.opensphere.core.video.VideoChunk;
import io.opensphere.core.video.VideoContentHandler;

/**
 * Reads a stream and produces video chunks which are provided to a receiver.
 */
@NotThreadSafe
public class XugglerStreamChunkRunner implements StreamChunkRunner, VideoChunkConsumer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(XugglerStreamChunkRunner.class);

    /**
     * The approximate size of individual chunks, usually set to 5 seconds.
     */
    private long myApproxSizeMS;

    /** The category which is associated with the stream. */
    private DataModelCategory myCategory;

    /**
     * Chunks the video stream into 5 seconds chunks and keeps the same video
     * format.
     */
    private VideoChunkProvider myChunkProvider;

    /** The grouped expiration time to use for chunks. */
    private long myCurrentExpiration = -1;

    /**
     * Use an additional buffer for grouping the expiration times so as to
     * reduce the number of tables created.
     */
    private long myExpirationBuffer;

    /** The amount of time before the video chunks expire. */
    private long myExpirationDuration;

    /**
     * The name of the feed.
     */
    private String myFeedName;

    /** When false this runner is not prepared to operate. */
    private boolean myInitialized;

    /**
     * The object interested in some sort of metadata contained in the video, or
     * null if nothing is interested.
     */
    private VideoContentHandler<ByteBuffer> myNonVideoContentHandler;

    /** The query receiver to which deposits will be provided. */
    private CacheDepositReceiver myQueryReceiver;

    /** The stream which will be read to produce the video chunks. */
    private CancellableInputStream myStream;

    /**
     * The time span of the stream.
     */
    private TimeSpan myStreamSpan;

    @Override
    public Void call() throws ChunkException
    {
        if (!myInitialized)
        {
            throw new IllegalStateException("Not initialized.");
        }

        myChunkProvider = VideoChunkProviderFactory.getInstance().createProvider(myStream, myStreamSpan.getStart(),
                myApproxSizeMS, myFeedName);

        boolean success = myChunkProvider.provideChunks(this, myNonVideoContentHandler);

        if (!success)
        {
            LOGGER.warn("Failed to re-encode the stream " + myFeedName + ", attempting to transcode the stream.");
            myChunkProvider = VideoChunkProviderFactory.getInstance().createFailOverProvider(myChunkProvider, myFeedName);
            if (myChunkProvider != null)
            {
                myChunkProvider.provideChunks(this, myNonVideoContentHandler);
            }
            else
            {
                LOGGER.error("Failed to transcode stream " + myFeedName);
            }
        }

        return null;
    }

    /**
     * Deposit everything that remains.
     *
     * @param startTimeEpoch The start time of the stream.
     * @param endTimeEpoch The time in the current chunk.
     * @param previousChunkLastEpoch The last epoch of the previous chunk.
     */
    @Override
    public void consumeLastChunk(long startTimeEpoch, long endTimeEpoch, long previousChunkLastEpoch,
            ListOfBytesOutputStream chunkStream, TLongList keyFrames)
    {
        long endTime = !myStream.isCancelled() && myStreamSpan.isBounded() ? myStreamSpan.getEnd() : endTimeEpoch;

        long chunkStart = previousChunkLastEpoch == -1 ? startTimeEpoch : previousChunkLastEpoch;
        if (endTime > chunkStart)
        {
            consumeVideoChunk(chunkStart, endTime, chunkStream, keyFrames);
        }
    }

    /**
     * Deposit the video into the registry.
     *
     * @param startTimeMS The start of the stream in milliseconds since epoch.
     * @param endTimeMS The end of the stream in milliseconds since epoch.
     */
    @Override
    public void consumeVideoChunk(long startTimeMS, long endTimeMS, ListOfBytesOutputStream chunkStream, TLongList keyFrames)
    {
        VideoChunk chunk = new RawVideoChunk(chunkStream, keyFrames, startTimeMS, endTimeMS);

        if (myExpirationDuration == Long.MAX_VALUE)
        {
            myCurrentExpiration = myExpirationDuration;
        }
        else if (myCurrentExpiration == -1 || myCurrentExpiration - System.currentTimeMillis() < myExpirationBuffer)
        {
            myCurrentExpiration = System.currentTimeMillis() + myExpirationDuration + myExpirationBuffer;
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Depositing chunk " + chunk);
        }
        DefaultCacheDeposit<VideoChunk> deposit = new DefaultCacheDeposit<VideoChunk>(myCategory,
                chunk.getPropertyAccessors(myStreamSpan), New.list(chunk), true, new Date(myCurrentExpiration), true);

        try
        {
            myQueryReceiver.receive(deposit);
        }
        catch (CacheException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void init(CancellableInputStream inStream, TimeSpan streamSpan, long approxSizeMS, DataModelCategory category,
            String feedName, CacheDepositReceiver queryReceiver, long expirationTime)
    {
        myStreamSpan = streamSpan;
        myApproxSizeMS = approxSizeMS;
        myCategory = category;
        myFeedName = feedName;
        myStream = inStream;
        myQueryReceiver = queryReceiver;
        myExpirationDuration = expirationTime;
        myInitialized = true;
        myExpirationBuffer = Math.min(5 * Constants.MILLIS_PER_MINUTE, 2 * myExpirationDuration);
    }

    @Override
    public void registerNonVideoContentHandler(VideoContentHandler<ByteBuffer> contentHandler)
    {
        myNonVideoContentHandler = contentHandler;
    }
}
