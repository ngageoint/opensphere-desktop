package io.opensphere.core.control.action;

import java.util.List;

/**
 * The Interface MenuOptionProvider.
 */
@FunctionalInterface
public interface MenuOptionProvider
{
    /**
     * Gets the list of MenuOption from this provider.
     *
     * @return the {@link List} of {@link MenuOption}
     */
    List<MenuOption> getMenuOptions();
}
