package io.opensphere.core.util.javafx.input.view.behavior;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import io.opensphere.core.util.collections.New;

/**
 * An abstract base implementation of a behavior, in which default implementations are provided for all behaviors. Each provided
 * implementation is a no-op, allowing sub-classes to only implement behaviors in which they are interested.
 *
 * @param <C> the type of control supported by the behavior.
 */
public class AbstractBehavior<C extends Control> implements MouseBehavior<C>, KeyBehavior<C>
{
    /**
     * The control to which the behavior is bound.
     */
    private final C myControl;

    /**
     * Listens to any focus events on the Control and calls behavior methods as a result.
     */
    private final InvalidationListener myFocusListener = property -> focusChanged();

    /**
     * Listens to any key events on the Control and responds to them.
     */
    private final EventHandler<KeyEvent> myKeyEventListener = e -> keyEvent(e);

    /**
     * The set of key bindings known to the behavior.
     */
    private final List<KeyActionBinding> myBindings;

    /**
     * Creates a new behavior, bound to the supplied control.
     *
     * @param pControl the control to which the behavior is bound.
     */
    public AbstractBehavior(final C pControl)
    {
        myControl = pControl;
        myControl.focusedProperty().addListener(myFocusListener);
        myControl.addEventHandler(KeyEvent.ANY, myKeyEventListener);
        myBindings = New.list();
    }

    /**
     * {@inheritDoc}
     *
     * @see Behavior#focusChanged()
     */
    @Override
    public void focusChanged()
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see Behavior#dispose()
     */
    @Override
    public void dispose()
    {
        myControl.focusedProperty().removeListener(myFocusListener);
        myControl.removeEventHandler(KeyEvent.ANY, myKeyEventListener);
    }

    /**
     * {@inheritDoc}
     *
     * @see Behavior#getControl()
     */
    @Override
    public C getControl()
    {
        return myControl;
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#mousePressed(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#mouseDragged(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#mouseReleased(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#mouseEntered(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#mouseExited(javafx.scene.input.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see MouseBehavior#contextMenuRequested(javafx.scene.input.ContextMenuEvent)
     */
    @Override
    public void contextMenuRequested(ContextMenuEvent pEvent)
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#addBinding(KeyActionBinding)
     */
    @Override
    public void addBinding(KeyActionBinding pBinding)
    {
        myBindings.add(pBinding);
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#removeBinding(KeyActionBinding)
     */
    @Override
    public void removeBinding(KeyActionBinding pBinding)
    {
        myBindings.remove(pBinding);
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#bind(javafx.scene.input.KeyCode, java.lang.String)
     */
    @Override
    public void bind(KeyCode pCode, String pAction)
    {
        KeyActionBinding binding = new KeyActionBinding(pCode, pAction);
        myBindings.add(binding);
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#bind(javafx.scene.input.KeyCode, java.lang.String, javafx.scene.input.KeyCode[])
     */
    @Override
    public void bind(KeyCode pCode, String pAction, KeyCode... pModifiers)
    {
        KeyActionBinding binding = new KeyActionBinding(pCode, pAction, pModifiers);
        myBindings.add(binding);
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#removeBinding(javafx.scene.input.KeyCode)
     */
    @Override
    public void removeBinding(KeyCode pCode)
    {
        Collection<KeyActionBinding> bindingsToRemove = findBindings(pCode);

        myBindings.removeAll(bindingsToRemove);
    }

    /**
     * Locates the set of bindings that correlate to the supplied parameters.
     *
     * @param pCode the code for which to search.
     * @param pModifiers the optional modifiers to apply to the code during the search.
     * @return the {@link Collection} of bindings correlating to the supplied code and modifiers, or an empty collection if none
     *         are found.
     */
    protected Collection<KeyActionBinding> findBindings(KeyCode pCode, KeyCode... pModifiers)
    {
        List<KeyActionBinding> bindingsToRemove = New.list();

        Set<KeyCode> modifiers = New.set();
        if (pModifiers != null)
        {
            modifiers.addAll(Arrays.asList(pModifiers));
        }
        for (KeyActionBinding binding : myBindings)
        {
            // only remove bindings that have the same modifiers for the supplied binding.
            if (binding.getCode() == pCode && binding.getModifiers().equals(modifiers))
            {
                bindingsToRemove.add(binding);
            }
        }
        return bindingsToRemove;
    }

    /**
     * {@inheritDoc}
     *
     * @see KeyBehavior#keyEvent(javafx.scene.input.KeyEvent)
     */
    @Override
    public void keyEvent(KeyEvent pEvent)
    {
        if (!pEvent.isConsumed())
        {
            Set<KeyCode> modifiers = New.set();
            if (pEvent.isAltDown())
            {
                modifiers.add(KeyCode.ALT);
            }
            if (pEvent.isControlDown())
            {
                modifiers.add(KeyCode.CONTROL);
            }
            if (pEvent.isShiftDown())
            {
                modifiers.add(KeyCode.SHIFT);
            }
            if (pEvent.isMetaDown())
            {
                modifiers.add(KeyCode.META);
            }
            if (pEvent.isShortcutDown())
            {
                modifiers.add(KeyCode.SHORTCUT);
            }

            Collection<KeyActionBinding> bindings = findBindings(pEvent.getCode(),
                    modifiers.toArray(new KeyCode[modifiers.size()]));

            for (KeyActionBinding binding : bindings)
            {
                actionPerformed(binding.getAction());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.behavior.KeyBehavior#actionPerformed(java.lang.String)
     */
    @Override
    public void actionPerformed(String pAction)
    {
        /* intentionally blank */
    }
}
