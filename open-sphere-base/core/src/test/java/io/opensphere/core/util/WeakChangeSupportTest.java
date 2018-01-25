package io.opensphere.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link WeakChangeSupport}.
 */
public class WeakChangeSupportTest
{
    /** Test adding, notifying, and removing a listener. */
    @Test
    public void testAddChangeListener()
    {
        Listener listener = new Listener();
        ChangeSupport<Listener> changeSupport = new WeakChangeSupport<>();
        changeSupport.addListener(listener);

        Assert.assertFalse(listener.isCalled());

        notifyListener(changeSupport);

        Assert.assertTrue(listener.isCalled());
        listener.setCalled(false);

        changeSupport.removeListener(listener);

        listener.setCalled(false);
        notifyListener(changeSupport);
        Assert.assertFalse(listener.isCalled());
    }

    /**
     * Helper method that notifies the listeners.
     *
     * @param changeSupport The change support.
     */
    private void notifyListener(ChangeSupport<Listener> changeSupport)
    {
        changeSupport.notifyListeners(new ChangeSupport.Callback<Listener>()
        {
            @Override
            public void notify(Listener listener)
            {
                listener.setCalled(true);
            }
        }, null);
    }

    /**
     * Test listener class.
     */
    private static class Listener
    {
        /** Flag indicating if this listener was called. */
        private boolean myCalled;

        /**
         * Get if this listener has been called.
         *
         * @return If the listener has been called.
         */
        public boolean isCalled()
        {
            return myCalled;
        }

        /**
         * Set this listener called.
         *
         * @param called The called flag.
         */
        public void setCalled(boolean called)
        {
            myCalled = called;
        }
    }
}
