package io.opensphere.core.util.swing.misc;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.util.swing.SplitButton;

/**
 * The Class ControlMenuOptionContextSplitButton, a split button that uses a
 * context menu key to provide dynamic menu options in addition to any added
 * components to the menu.
 *
 * @param <T> the generic type
 */
public abstract class ControlMenuOptionContextSplitButton<T> extends SplitButton
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Context identifier. */
    private final String myContextIdentifier;

    /** The Context menu key type. */
    private final Class<T> myContextMenuKeyType;

    /** The Control action manager. */
    private final ContextActionManager myControlActionManager;

    /** The Menu option context. */
    private final ActionContext<T> myMenuOptionContext;

    /**
     * Instantiates a new control menu option context split button.
     *
     * @param contextActionManager The context action manager.
     * @param text the text
     * @param icon the icon
     * @param contextIdentifier the context identifier
     * @param keyType the key type
     */
    public ControlMenuOptionContextSplitButton(ContextActionManager contextActionManager, String text, Icon icon,
            String contextIdentifier, Class<T> keyType)
    {
        super(text, icon);
        myContextIdentifier = contextIdentifier;
        myContextMenuKeyType = keyType;
        myControlActionManager = contextActionManager;
        myMenuOptionContext = myControlActionManager.getActionContext(myContextIdentifier, myContextMenuKeyType);
    }

    @Override
    protected List<Component> getDynamicMenuItems()
    {
        List<Component> resultList = null;

        if (myMenuOptionContext != null)
        {
            resultList = myMenuOptionContext.getMenuItems((T)null);
        }

        return resultList == null ? Collections.<Component>emptyList() : resultList;
    }
}
