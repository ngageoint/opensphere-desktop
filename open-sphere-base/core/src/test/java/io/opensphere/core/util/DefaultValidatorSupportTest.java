package io.opensphere.core.util;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;

/** Test for {@link DefaultValidatorSupport}. */
public class DefaultValidatorSupportTest
{
    /**
     * Test adding a listener, changing the status to different values, and
     * removing the listener.
     */
    @Test
    @SuppressWarnings("PMD.StringInstantiation")
    public void test()
    {
        Object obj = new Object();
        String message1 = "Message1";
        String message1b = new String(message1);
        String message2 = "Message2";

        ValidationStatusChangeListener listener = EasyMock.createMock(ValidationStatusChangeListener.class);
        listener.statusChanged(obj, ValidationStatus.VALID, message1);
        EasyMock.replay(listener);

        DefaultValidatorSupport sup = new DefaultValidatorSupport(obj);
        sup.setValidationResult(ValidationStatus.VALID, message1);
        sup.addAndNotifyValidationListener(listener);

        Assert.assertEquals(ValidationStatus.VALID, sup.getValidationStatus());
        Assert.assertEquals(message1, sup.getValidationMessage());

        EasyMock.verify(listener);

        // Change status to false, still message1.

        EasyMock.reset(listener);
        listener.statusChanged(obj, ValidationStatus.ERROR, message1);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.ERROR, message1);

        Assert.assertEquals(ValidationStatus.ERROR, sup.getValidationStatus());
        Assert.assertEquals(message1, sup.getValidationMessage());

        EasyMock.verify(listener);

        // Change message to message1b (different string instance with same
        // value as message1), still false status.

        EasyMock.reset(listener);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.ERROR, message1b);

        Assert.assertEquals(ValidationStatus.ERROR, sup.getValidationStatus());
        Assert.assertEquals(message1b, sup.getValidationMessage());

        EasyMock.verify(listener);

        // Change message to message2, still false status.

        EasyMock.reset(listener);
        listener.statusChanged(obj, ValidationStatus.ERROR, message2);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.ERROR, message2);

        Assert.assertEquals(ValidationStatus.ERROR, sup.getValidationStatus());
        Assert.assertEquals(message2, sup.getValidationMessage());

        EasyMock.verify(listener);

        // Change message to null, still false status.

        EasyMock.reset(listener);
        listener.statusChanged(obj, ValidationStatus.ERROR, null);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.ERROR, null);

        Assert.assertEquals(ValidationStatus.ERROR, sup.getValidationStatus());
        Assert.assertNull(sup.getValidationMessage());

        EasyMock.verify(listener);

        // Change status to true, still null message.

        EasyMock.reset(listener);
        listener.statusChanged(obj, ValidationStatus.VALID, null);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.VALID, null);

        Assert.assertEquals(ValidationStatus.VALID, sup.getValidationStatus());
        Assert.assertNull(sup.getValidationMessage());

        EasyMock.verify(listener);

        sup.removeValidationListener(listener);

        EasyMock.reset(listener);
        EasyMock.replay(listener);

        sup.setValidationResult(ValidationStatus.ERROR, message2);

        EasyMock.verify(listener);
    }
}
