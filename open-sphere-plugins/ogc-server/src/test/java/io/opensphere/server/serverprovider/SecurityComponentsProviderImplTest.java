package io.opensphere.server.serverprovider;

import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.authentication.UserInteractionX509KeyManager;
import io.opensphere.core.authentication.UserInteractionX509TrustManager;

/**
 * Tests the SecurityComponentsProviderImpl class.
 *
 */
public class SecurityComponentsProviderImplTest
{
    /**
     * Verifies the SecurityComponentsProviderImpl class.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        SecurityManager manager = support.createNiceMock(SecurityManager.class);

        support.replayAll();

        SecurityComponentsProviderImpl provider = new SecurityComponentsProviderImpl();

        Assert.assertTrue(provider.getKeyManager(null, null, null, null, manager) instanceof UserInteractionX509KeyManager);
        Assert.assertTrue(provider.getTrustManager(null, null, null, manager) instanceof UserInteractionX509TrustManager);
        Assert.assertTrue(
                provider.getUserCredentials(null, null, null, manager) instanceof UserInteractionUsernamePasswordCredentials);

        support.verifyAll();
    }
}
