package io.opensphere.wfs.state.save;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * The Class StyleStateSaver.
 */
public abstract class StyleStateSaver implements StyleParameterSaver
{
    /** The Style keys. */
    private final List<String> myStyleKeys = New.list();

    /**
     * Gets the style keys.
     *
     * @return the style keys
     */
    public List<String> getStyleKeys()
    {
        return myStyleKeys;
    }
}
