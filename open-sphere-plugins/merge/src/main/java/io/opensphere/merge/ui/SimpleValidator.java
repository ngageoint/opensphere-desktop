package io.opensphere.merge.ui;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;

/**
 * Ye olde boiler plate shoppe.
 */
public class SimpleValidator implements ValidatorSupport
{
    /** Attached listeners. */
    private final List<ValidationStatusChangeListener> ears = new LinkedList<>();

    /** Attributed source of events; defaults to itself. */
    protected Object src = this;

    /** Current status. */
    private ValidationStatus status = ValidationStatus.WARNING;

    /** Current message, if any. */
    private String message = "Stuff";

    /** Create. */
    public SimpleValidator()
    {
        /* intentionally blank */
    }

    /**
     * Create with event attribution.
     *
     * @param valObj event source
     */
    public SimpleValidator(Object valObj)
    {
        src = valObj;
    }

    /**
     * Assign a new status and message; notify all listeners.
     *
     * @param st new status code
     * @param msg new message
     */
    public void setStatus(ValidationStatus st, String msg)
    {
        status = st;
        message = msg;
        for (ValidationStatusChangeListener ear : ears)
        {
            ear.statusChanged(src, status, message);
        }
    }

    @Override
    public void addAndNotifyValidationListener(ValidationStatusChangeListener ear)
    {
        if (ear == null || ears.contains(ear))
        {
            return;
        }
        ears.add(ear);
        ear.statusChanged(src, status, message);
    }

    @Override
    public String getValidationMessage()
    {
        return message;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        return status;
    }

    @Override
    public void removeValidationListener(ValidationStatusChangeListener ear)
    {
        ears.remove(ear);
    }
}