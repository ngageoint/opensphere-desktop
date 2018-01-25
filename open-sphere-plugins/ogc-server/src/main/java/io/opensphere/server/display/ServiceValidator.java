package io.opensphere.server.display;

import java.awt.Component;

import io.opensphere.core.util.DefaultValidatorSupport;

/**
 * Validates an server service.
 *
 * @param <T> The type of server source.
 */
public abstract class ServiceValidator<T> extends DefaultValidatorSupport
{
    /**
     * Constructor.
     */
    public ServiceValidator()
    {
        super(null);
    }

    /**
     * Sets the parent component.
     *
     * @param parent the new parent component
     */
    public abstract void setParent(Component parent);

    /**
     * Sets the service.
     *
     * @param service the new service
     */
    public abstract void setService(String service);

    /**
     * Sets the source.
     *
     * @param source the new source
     */
    public abstract void setSource(T source);
}
