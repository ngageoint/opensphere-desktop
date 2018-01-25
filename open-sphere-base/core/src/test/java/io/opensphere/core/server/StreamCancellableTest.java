package io.opensphere.core.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Tests the {@link StreamCancellable} class.
 */
public class StreamCancellableTest
{
    /**
     * Tests the cancel.
     */
    @Test
    public void testCancel()
    {
        EasyMockSupport support = new EasyMockSupport();

        UUID streamId = UUID.randomUUID();

        StreamingServer server = support.createMock(StreamingServer.class);
        server.stop(EasyMock.eq(streamId));

        support.replayAll();

        StreamCancellable cancellable = new StreamCancellable(server, streamId);

        assertFalse(cancellable.isCancelled());

        cancellable.cancel();

        assertTrue(cancellable.isCancelled());

        support.verifyAll();
    }
}
