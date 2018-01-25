package io.opensphere.core.video;

import java.util.List;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.ref.VolatileReference;

/** Test for {@link ScheduledVideoContentHandlerWrapper}. */
public class ScheduledVideoContentHandlerWrapperTest
{
    /**
     * Test for
     * {@link ScheduledVideoContentHandlerWrapper#handleContent(Object, long)}.
     */
    @Test
    public void testHandleContent()
    {
        EasyMockSupport support = new EasyMockSupport();

        int[] order = new int[] { 2, 1, 3, 4, 0 };
        long[] times = new long[order.length];
        List<Object> objs = New.list(times.length);
        for (int index = 0; index < order.length; ++index)
        {
            times[index] = 100L * index + 100L;
            objs.add(new Object());
        }

        final VolatileReference<AutoCloseable> ref = new VolatileReference<>();
        @SuppressWarnings("unchecked")
        VideoContentHandler<Object> wrapped = support.createStrictMock(VideoContentHandler.class);
        for (int index = 0; index < order.length; ++index)
        {
            wrapped.handleContent(objs.get(index), times[index]);

            if (index == order.length - 1)
            {
                EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
                {
                    @Override
                    public Object answer()
                    {
                        Utilities.close(ref.get());
                        return null;
                    }
                });
            }
        }

        support.replayAll();

        TimeInstant startTime = TimeInstant.get();
        ReadOnlyProperty<TimeInstant> handleTime = new SimpleObjectProperty<TimeInstant>();
        try (ScheduledVideoContentHandlerWrapper<Object> wrapper = new ScheduledVideoContentHandlerWrapper<>(wrapped, 0L,
                startTime, handleTime))
        {
            for (int index = 0; index < order.length; ++index)
            {
                wrapper.handleContent(objs.get(order[index]), times[order[index]]);
            }

            ref.set(wrapper);
            wrapper.run();
        }

        support.verifyAll();
    }
}
