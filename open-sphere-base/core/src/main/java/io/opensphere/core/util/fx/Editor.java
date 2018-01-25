package io.opensphere.core.util.fx;

import io.opensphere.core.util.Validatable;

/** Editor interface. */
public interface Editor extends Validatable
{
    /**
     * Accepts the changes.
     */
    void accept();
}
