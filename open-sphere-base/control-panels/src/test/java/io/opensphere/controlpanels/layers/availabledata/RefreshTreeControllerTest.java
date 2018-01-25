package io.opensphere.controlpanels.layers.availabledata;

import java.util.concurrent.CountDownLatch;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Tests the {@link RefreshTreeController}.
 *
 */
public class RefreshTreeControllerTest
{
    /**
     * Tests refresh.
     *
     * @throws InterruptedException Interrupted.
     */
    @Test
    public void testRefresh() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        support.replayAll();

        RefreshTreeController controller = new RefreshTreeController();

        controller.refresh(group);

        CountDownLatch latch = new CountDownLatch(1);

        controller.refresh(new RefreshableGroupMock(latch));

        latch.await();

        support.verifyAll();
    }
}
