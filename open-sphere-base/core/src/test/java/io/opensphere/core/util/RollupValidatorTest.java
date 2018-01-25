package io.opensphere.core.util;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;

/** Test for {@link RollupValidator}. */
public class RollupValidatorTest
{
    /** Test for {@link RollupValidator}. */
    @Test
    public void test()
    {
        EasyMockSupport em = new EasyMockSupport();

        Object obj = new Object();
        RollupValidator val = new RollupValidator(obj);

        ValidatorSupport child1 = em.createMock(ValidatorSupport.class);
        ValidatorSupport child2 = em.createMock(ValidatorSupport.class);
        ValidatorSupport child3 = em.createMock(ValidatorSupport.class);

        Capture<ValidationStatusChangeListener> listenerCapture = EasyMock.newCapture();
        child1.addAndNotifyValidationListener(EasyMock.capture(listenerCapture));
        child2.addAndNotifyValidationListener(EasyMockHelper.eq(listenerCapture));
        child3.addAndNotifyValidationListener(EasyMockHelper.eq(listenerCapture));

        ValidationStatusChangeListener listener = em.createMock(ValidationStatusChangeListener.class);
        /* when the listener is added */
        listener.statusChanged(obj, ValidationStatus.VALID, null);

        em.replayAll();

        val.addChildValidator(child1);
        val.addChildValidator(child2);
        val.addChildValidator(child3);

        val.addAndNotifyValidationListener(listener);

        em.verifyAll();
        em.resetAll();

        Object obj1 = new Object();
        String message1 = "message1";

        EasyMock.expect(child1.getValidationStatus()).andReturn(ValidationStatus.ERROR).times(2);
        EasyMock.expect(child1.getValidationMessage()).andReturn(message1);
        listener.statusChanged(obj, ValidationStatus.ERROR, message1);

        em.replayAll();

        listenerCapture.getValue().statusChanged(obj1, ValidationStatus.ERROR, message1);

        Assert.assertEquals(ValidationStatus.ERROR, val.getValidationStatus());
        Assert.assertEquals(message1, val.getValidationMessage());

        em.verifyAll();
        em.resetAll();

        String message2 = "message2";

        EasyMock.expect(child1.getValidationStatus()).andReturn(ValidationStatus.VALID);
        EasyMock.expect(child2.getValidationStatus()).andReturn(ValidationStatus.ERROR).times(2);
        EasyMock.expect(child2.getValidationMessage()).andReturn(message2);
        listener.statusChanged(obj, ValidationStatus.ERROR, message2);

        em.replayAll();

        listenerCapture.getValue().statusChanged(null, ValidationStatus.ERROR, message2);

        Assert.assertEquals(ValidationStatus.ERROR, val.getValidationStatus());
        Assert.assertEquals(message2, val.getValidationMessage());

        em.verifyAll();
        em.resetAll();

        EasyMock.expect(child1.getValidationStatus()).andReturn(ValidationStatus.VALID);
        EasyMock.expect(child2.getValidationStatus()).andReturn(ValidationStatus.VALID);
        EasyMock.expect(child3.getValidationStatus()).andReturn(ValidationStatus.VALID);
        listener.statusChanged(obj, ValidationStatus.VALID, null);

        em.replayAll();

        listenerCapture.getValue().statusChanged(null, ValidationStatus.VALID, null);

        Assert.assertEquals(ValidationStatus.VALID, val.getValidationStatus());
        Assert.assertNull(val.getValidationMessage());

        em.verifyAll();
    }
}
