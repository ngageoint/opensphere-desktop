package io.opensphere.core.util;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A validator that rolls up the status of other validators.
 */
@ThreadSafe
public class RollupValidator extends DefaultValidatorSupport
{
    /** The child validators. */
    private final Collection<ValidatorSupport> myChildren = new ArrayList<>();

    /** Listener for status changes on my children. */
    private final ValidationStatusChangeListener myListener = new ValidationStatusChangeListener()
    {
        @Override
        public void statusChanged(Object object, ValidationStatus valid, String message)
        {
            synchronized (myChildren)
            {
                for (ValidatorSupport validator : myChildren)
                {
                    if (validator.getValidationStatus() != ValidationStatus.VALID)
                    {
                        setValidationResult(validator);
                        return;
                    }
                }
                setValidationResult(ValidationStatus.VALID, null);
            }
        }
    };

    /**
     * Construct the validator support.
     *
     * @param validationObject The object being validated.
     */
    public RollupValidator(Object validationObject)
    {
        super(validationObject);
        setValidationResult(ValidationStatus.VALID, null);
    }

    /**
     * Add a child validator support whose status should be rolled into mine.
     *
     * @param childValidatorSupport The child.
     */
    public void addChildValidator(ValidatorSupport childValidatorSupport)
    {
        synchronized (myChildren)
        {
            myChildren.add(childValidatorSupport);
            childValidatorSupport.addAndNotifyValidationListener(myListener);
        }
    }

    /**
     * Remove a child validator support.
     *
     * @param childValidatorSupport The child.
     */
    public void removeChildValidator(ValidatorSupport childValidatorSupport)
    {
        synchronized (myChildren)
        {
            childValidatorSupport.removeValidationListener(myListener);
            myChildren.remove(childValidatorSupport);
            myListener.statusChanged(null, ValidationStatus.VALID, null);
        }
    }

    /**
     * Removes all child validator supports.
     */
    public void clear()
    {
        synchronized (myChildren)
        {
            for (ValidatorSupport childValidatorSupport : myChildren)
            {
                childValidatorSupport.removeValidationListener(myListener);
            }
            myChildren.clear();
            myListener.statusChanged(null, ValidationStatus.VALID, null);
        }
    }
}
