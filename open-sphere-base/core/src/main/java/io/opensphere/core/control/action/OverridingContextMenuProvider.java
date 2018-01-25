package io.opensphere.core.control.action;

import java.util.Collection;

/**
 * A {@link ContextMenuProvider} that can override (remove) other menu options.
 *
 * @param <T> The context menu key type.
 */
public interface OverridingContextMenuProvider<T> extends ContextMenuProvider<T>
{
    /**
     * Gets the text of the items to override (remove).
     *
     * @return the text of the items
     */
    Collection<String> getOverrideItems();
}
