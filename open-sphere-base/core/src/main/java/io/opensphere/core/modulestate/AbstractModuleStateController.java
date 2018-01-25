package io.opensphere.core.modulestate;

import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation that handles some common behavior.
 */
public abstract class AbstractModuleStateController implements ModuleStateController
{
    @Override
    public List<? extends String> getRequiredStateDependencies()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isAlwaysActivateState()
    {
        return false;
    }

    @Override
    public boolean isAlwaysSaveState()
    {
        return false;
    }
}
