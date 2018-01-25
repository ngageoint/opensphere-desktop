package io.opensphere.core.video;

import java.util.List;
import java.util.function.Consumer;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.collections.New;

/**
 * Test for {@link TimeCalculatingVideoContentHandlerWrapper}.
 */
public class TimeCalculatingVideoContentHandlerWrapperTest
{
    /**
     * Test for
     * {@link TimeCalculatingVideoContentHandlerWrapper#handleContent(Object, long)}
     * .
     */
    @Test
    public void testHandleContent()
    {
        long[] times = new long[] { 1000L, 1100L, 1200L };
        List<Object> objs = New.list(times.length);
        for (int index = 0; index < times.length; ++index)
        {
            objs.add(new Object());
        }

        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        VideoContentHandler<Object> wrapped = support.createStrictMock(VideoContentHandler.class);

        for (int index = 0; index < times.length; ++index)
        {
            wrapped.handleContent(objs.get(index), times[index]);
        }

        TimeInstant startTime = TimeInstant.get();

        @SuppressWarnings("unchecked")
        Consumer<TimeInstant> timeConsumer = support.createStrictMock(Consumer.class);
        for (int index = 0; index < times.length; ++index)
        {
            timeConsumer.accept(startTime.plus(new Milliseconds(times[index] - times[0])));
        }
        support.replayAll();

        try (TimeCalculatingVideoContentHandlerWrapper<Object> handler = new TimeCalculatingVideoContentHandlerWrapper<Object>(
                wrapped, startTime, timeConsumer))
        {
            for (int index = 0; index < times.length; ++index)
            {
                handler.handleContent(objs.get(index), times[index]);
            }
        }
        support.verifyAll();
    }
}
