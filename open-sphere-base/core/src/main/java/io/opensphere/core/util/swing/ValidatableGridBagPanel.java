package io.opensphere.core.util.swing;

import io.opensphere.core.util.RollupValidator;
import io.opensphere.core.util.Validatable;

/**
 * A {@link GridBagPanel} that is also {@link Validatable}.
 */
public class ValidatableGridBagPanel extends GridBagPanel implements Validatable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The validator support. */
    private final RollupValidator myValidatorSupport = new RollupValidator(this);

    @Override
    public RollupValidator getValidatorSupport()
    {
        return myValidatorSupport;
    }
}
