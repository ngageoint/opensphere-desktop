package io.opensphere.auxiliary.video;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.RawVideoChunk;
import io.opensphere.core.video.VideoChunk;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoTestUtil;

/**
 * Tests the {@link XugglerStreamChunkRunner}.
 */
public class XugglerStreamChunkRunnerTestFunctional
{
    /**
     * The test cache category.
     */
    private static final DataModelCategory ourCacheCategory = new DataModelCategory("source", VideoChunk.class.getName(), "test");

    /**
     * The number of chunks we have received.
     */
    private int myNumberOfChunks;

    /**
     * The actual number of metadata chunks.
     */
    private int myNumberOfMetadatas;

    /**
     * Tests that we can chunkify goodVideoFile.
     *
     * @throws CacheException Bad cache.
     * @throws FileNotFoundException Bad file.
     * @throws ChunkException Bad chunk.
     */
    @Test
    public void test() throws CacheException, FileNotFoundException, ChunkException
    {
        EasyMockSupport support = new EasyMockSupport();

        myNumberOfChunks = 0;

        CacheDepositReceiver receiver = createReceiver(support);
        CancellableInputStream fileStream = VideoTestUtil
                .getFileStream(System.getProperty("videoFile5", VideoTestUtil.VIDEO_FILE_5));
        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.call();

        assertEquals(57, myNumberOfChunks);

        support.verifyAll();
    }

    /**
     * Tests that we can chunkify badVideoFile1.
     *
     * @throws CacheException Bad cache.
     * @throws FileNotFoundException Bad file.
     * @throws ChunkException Bad chunk.
     */
    @Test
    public void test1() throws CacheException, FileNotFoundException, ChunkException
    {
        EasyMockSupport support = new EasyMockSupport();

        myNumberOfChunks = 0;

        CacheDepositReceiver receiver = createReceiver(support);
        CancellableInputStream fileStream = VideoTestUtil
                .getFileStream(System.getProperty("videoFile1", VideoTestUtil.VIDEO_FILE_1));
        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.call();

        assertEquals(60, myNumberOfChunks);

        support.verifyAll();
    }

    /**
     * Tests that we can chunkify badVideoFile2.
     *
     * @throws CacheException Bad cache.
     * @throws FileNotFoundException Bad file.
     * @throws ChunkException Bad chunk.
     */
    @Test
    public void test2() throws CacheException, FileNotFoundException, ChunkException
    {
        EasyMockSupport support = new EasyMockSupport();

        myNumberOfChunks = 0;

        CacheDepositReceiver receiver = createReceiver(support);
        CancellableInputStream fileStream = VideoTestUtil
                .getFileStream(System.getProperty("videoFile2", VideoTestUtil.VIDEO_FILE_2));
        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.call();

        assertEquals(60, myNumberOfChunks);

        support.verifyAll();
    }

    /**
     * Tests that we can chunkify badVideoFile3.
     *
     * @throws CacheException Bad cache.
     * @throws FileNotFoundException Bad file.
     * @throws ChunkException Bad chunk.
     */
    @Test
    public void test3() throws CacheException, FileNotFoundException, ChunkException
    {
        EasyMockSupport support = new EasyMockSupport();

        myNumberOfChunks = 0;

        CacheDepositReceiver receiver = createReceiver(support);
        CancellableInputStream fileStream = VideoTestUtil
                .getFileStream(System.getProperty("videoFile3", VideoTestUtil.VIDEO_FILE_3));
        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.call();

        assertEquals(57, myNumberOfChunks);

        support.verifyAll();
    }

    /**
     * Tests that we can chunkify badVideoFile4.
     *
     * @throws CacheException Bad cache.
     * @throws FileNotFoundException Bad file.
     * @throws ChunkException Bad chunk.
     */
    @Test
    public void test4() throws CacheException, FileNotFoundException, ChunkException
    {
        EasyMockSupport support = new EasyMockSupport();

        myNumberOfChunks = 0;

        CacheDepositReceiver receiver = createReceiver(support);
        CancellableInputStream fileStream = VideoTestUtil
                .getFileStream(System.getProperty("videoFile4", VideoTestUtil.VIDEO_FILE_4));
        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.call();

        assertEquals(60, myNumberOfChunks);

        support.verifyAll();
    }

    /**
     * Tests providing video chunks and metadata from a test klv file.
     *
     * @throws ChunkException Bad chunk.
     * @throws FileNotFoundException Bad file.
     * @throws CacheException Bad cache.
     */
    @Test
    public void testProvideChunksWithMetadata() throws ChunkException, FileNotFoundException, CacheException
    {
        myNumberOfChunks = 0;
        myNumberOfMetadatas = 0;

        CancellableInputStream fileStream = VideoTestUtil.getFileStream(System.getProperty("klvFile1", VideoTestUtil.KLV_FILE_1));

        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));
        CacheDepositReceiver receiver = createReceiver(support);
        VideoContentHandler<ByteBuffer> contentHandler = createContentHandler(support);

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.registerNonVideoContentHandler(contentHandler);
        chunker.call();

        assertEquals(165, myNumberOfChunks);
        assertEquals(299, myNumberOfMetadatas);

        support.verifyAll();
    }

    /**
     * Tests providing video chunks and metadata from a test klv file.
     *
     * @throws ChunkException Bad chunk.
     * @throws FileNotFoundException Bad file.
     * @throws CacheException Bad Cache.
     */
    @Test
    public void testProvideChunksWithMetadata2() throws ChunkException, FileNotFoundException, CacheException
    {
        myNumberOfChunks = 0;
        myNumberOfMetadatas = 0;

        CancellableInputStream fileStream = VideoTestUtil.getFileStream(System.getProperty("klvFile2", VideoTestUtil.KLV_FILE_2));

        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span = TimeSpan.get(System.currentTimeMillis(), new Minutes(5));
        CacheDepositReceiver receiver = createReceiver(support);
        VideoContentHandler<ByteBuffer> contentHandler = createContentHandler(support);

        support.replayAll();

        XugglerStreamChunkRunner chunker = new XugglerStreamChunkRunner();
        chunker.init(fileStream, span, 5000, ourCacheCategory, ourCacheCategory.getCategory(), receiver,
                Constants.MILLIS_PER_HOUR);
        chunker.registerNonVideoContentHandler(contentHandler);
        chunker.call();

        assertEquals(516, myNumberOfChunks);
        assertEquals(901, myNumberOfMetadatas);

        support.verifyAll();
    }

    /**
     * Adds one to myNumberOfMetadata.
     *
     * @return Null.
     */
    private Void contentHandlerAnswer()
    {
        ByteBuffer buffer = (ByteBuffer)EasyMock.getCurrentArguments()[0];
        if (buffer.hasRemaining())
        {
            myNumberOfMetadatas++;
        }

        return null;
    }

    /**
     * Creates an easy mocked {@link VideoContentHandler} that ups
     * myNumberOfMetadatas.
     *
     * @param support Used to create the mock.
     * @return The mocked metadata handler.
     */
    @SuppressWarnings("unchecked")
    private VideoContentHandler<ByteBuffer> createContentHandler(EasyMockSupport support)
    {
        VideoContentHandler<ByteBuffer> handler = support.createMock(VideoContentHandler.class);

        handler.handleContent(EasyMock.isA(ByteBuffer.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(this::contentHandlerAnswer).atLeastOnce();

        return handler;
    }

    /**
     * Creates an easy mocked {@link CacheDepositReceiver}.
     *
     * @param support Used to create the mock.
     * @return The {@link CacheDepositReceiver}.
     * @throws CacheException Bad cache.
     */
    @SuppressWarnings("unchecked")
    private CacheDepositReceiver createReceiver(EasyMockSupport support) throws CacheException
    {
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        IAnswer<long[]> answer = new IAnswer<long[]>()
        {
            @Override
            public long[] answer() throws Throwable
            {
                CacheDeposit<VideoChunk> deposit = (CacheDeposit<VideoChunk>)EasyMock.getCurrentArguments()[0];
                RawVideoChunk videoChunk = (RawVideoChunk)deposit.getInput().iterator().next();
                if (videoChunk.getVideoAsStream().available() > 0)
                {
                    myNumberOfChunks++;
                }

                return new long[] { 1 };
            }
        };

        EasyMock.expect(receiver.receive((CacheDeposit<VideoChunk>)EasyMock.isA(CacheDeposit.class))).andAnswer(answer)
                .atLeastOnce();

        return receiver;
    }
}
