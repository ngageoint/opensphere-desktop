package io.opensphere.core.video;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.io.ListOfBytesOutputStream;
import io.opensphere.core.video.VideoChunk.VideoChunkSpanAccessor;

/**
 * Tests the {@link RawVideoChunk} class.
 */
public class RawVideoChunkTest
{
    /**
     * Tests getting the timespan for a video chunk whose timespan crosses the
     * 20 minute table boundary.
     */
    @Test
    public void testGetPropertyAccessorsForChunkOverBoundary()
    {
        TimeSpan chunkSpan = TimeSpan.get(new Minutes(19).asDate().getTime(), new Minutes(21).asDate().getTime());

        TimeSpan klvSpan = TimeSpan.get(0, new Minutes(30).asDate().getTime());

        RawVideoChunk videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        VideoChunkSpanAccessor accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(TimeSpan.get(0, new Minutes(20)), accessor.getExtent());
    }

    /**
     * Tests getting the timespan for a klv that is five minutes long.
     */
    @Test
    public void testGetPropertyAccessorsForFiveMinuteKlv()
    {
        TimeSpan chunkSpan = TimeSpan.get(1000, 6000);

        TimeSpan klvSpan = TimeSpan.get(1000, new Minutes(5).asDate().getTime() + 1000);

        RawVideoChunk videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        VideoChunkSpanAccessor accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(klvSpan, accessor.getExtent());
    }

    /**
     * Tests getting the timespan for live video chunks.
     */
    @Test
    public void testGetPropertyAccessorsForLive()
    {
        TimeSpan chunkSpan = TimeSpan.get(1000, 6000);
        TimeSpan streamSpan = TimeSpan.newUnboundedEndTimeSpan(1000);

        RawVideoChunk videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        VideoChunkSpanAccessor accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(streamSpan).get(0);
        assertEquals(streamSpan, accessor.getExtent());
    }

    /**
     * Tests getting the timespan for a klv that is one hour long.
     */
    @Test
    public void testGetPropertyAccessorsForOneHourKlv()
    {
        TimeSpan chunkSpan = TimeSpan.get(0, 5000);

        TimeSpan klvSpan = TimeSpan.get(0, new Minutes(60).asDate().getTime());

        RawVideoChunk videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        VideoChunkSpanAccessor accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(TimeSpan.get(0, new Minutes(20)), accessor.getExtent());

        chunkSpan = TimeSpan.get(new Minutes(20).asDate().getTime(), new Minutes(20).asDate().getTime() + 5000);
        videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(TimeSpan.get(new Minutes(20).asDate().getTime(), new Minutes(40).asDate().getTime()), accessor.getExtent());

        chunkSpan = TimeSpan.get(new Minutes(40).asDate().getTime(), new Minutes(40).asDate().getTime() + 5000);
        videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(TimeSpan.get(new Minutes(40).asDate().getTime(), new Minutes(60).asDate().getTime()), accessor.getExtent());
    }

    /**
     * Tests getting the timespan for a video chunk who ends right at the twenty
     * minute boundary.
     */
    @Test
    public void testGetPropertyAccessorsForTwentyMinuteVideo()
    {
        TimeSpan chunkSpan = TimeSpan.get(new Minutes(19).asDate().getTime(), new Minutes(20).asDate().getTime());

        TimeSpan klvSpan = TimeSpan.get(0, new Minutes(30).asDate().getTime());

        RawVideoChunk videoChunk = new RawVideoChunk(new ListOfBytesOutputStream(), new TLongArrayList(), chunkSpan.getStart(),
                chunkSpan.getEnd());
        VideoChunkSpanAccessor accessor = (VideoChunkSpanAccessor)videoChunk.getPropertyAccessors(klvSpan).get(0);
        assertEquals(TimeSpan.get(0, new Minutes(20)), accessor.getExtent());
    }
}
