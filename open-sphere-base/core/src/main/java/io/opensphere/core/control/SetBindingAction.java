package io.opensphere.core.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * Action listener that sets a control binding.
 */
@SuppressWarnings("PMD.GodClass")
public final class SetBindingAction implements ActionListener
{
    /** The binding index. */
    private final int myAssignmentIndex;

    /** The binding set by this action. */
    private final BindingsToListener myBindingsToListener;

    /** The popup menu. */
    private JPopupMenu myBtnPopupMenu;

    /** The button associated with this action. */
    private JToggleButton myButton;

    /** The Control context. */
    private final ControlContext myControlContext;

    /** The key listener. */
    private KeyCaptureListener myKeyCaptureListener;

    /** The listener. */
    private final BoundEventListener myListener;

    /** The mouse listener. */
    private MouseCaptureListener myMouseCaptureListener;

    /** The mouse wheel listener. */
    private MouseWheelCaptureListener myMouseWheelCaptureListener;

    /** Flag indicating if input is currently being taken. */
    private boolean myTakingInputMode;

    /**
     * The Key binding support. Provides a mechanism to unbind binding actions
     * that are not necessarily this one.
     */
    private final ChangeSupport<KeyBindingChangeListener> myKeyBindingSupport = new WeakChangeSupport<>();

    /** The Parent component. */
    private final Component myParentComponent;

    /**
     * Construct the action.
     *
     * @param context the control context into which bindings should be modified
     * @param btl The binding.
     * @param assignmentIndex The binding index to be assigned.
     * @param parent the parent component used to orient user messages on the
     *            screen.
     */
    public SetBindingAction(ControlContext context, BindingsToListener btl, int assignmentIndex, Component parent)
    {
        myParentComponent = parent;
        myBindingsToListener = btl;
        myControlContext = context;
        myAssignmentIndex = assignmentIndex;
        myListener = btl.getListener();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myButton = (JToggleButton)e.getSource();
        myButton.setFocusTraversalKeysEnabled(false);
        if (!myTakingInputMode)
        {
            myTakingInputMode = true;
            myButton.setText("Select Key to Map");
            setupKeyListener();
            setupMouseListeners();
        }
        else
        {
            myTakingInputMode = false;
        }
    }

    /**
     * Adds the key binding change listener.
     *
     * @param listener the listener
     */
    public void addKeyBindingChangeListener(KeyBindingChangeListener listener)
    {
        myKeyBindingSupport.addListener(listener);
    }

    /**
     * Clear the binding.
     *
     * @param btn The source button.
     */
    public void clearBinding(JToggleButton btn)
    {
        if (myButton == null)
        {
            myButton = btn;
        }

        myBindingsToListener.setClearBindingPending(myAssignmentIndex);
        myButton.setText("");
    }

    /**
     * Removes the key binding change listener.
     *
     * @param listener the listener
     */
    public void removeKeyBindingChangeListener(KeyBindingChangeListener listener)
    {
        myKeyBindingSupport.removeListener(listener);
    }

    /**
     * Reset the binding to what it was before being changed.
     *
     * @param btn The source button.
     */
    public void resetBinding(JToggleButton btn)
    {
        if (myButton == null)
        {
            myButton = btn;
        }

        myTakingInputMode = false;
        removeKeyAndMouseCaptureListeners();
        myBindingsToListener.setResetBindingPending(myAssignmentIndex);
        Binding stagedBinding = myBindingsToListener.getStagedBinding(myAssignmentIndex);
        myButton.setText(stagedBinding == null ? "" : stagedBinding.toString());
    }

    /**
     * Get the binding type for an event based on what kind of listener I have.
     *
     * @param evt The event.
     * @return The binding.
     */
    Binding getProperBindingType(InputEvent evt)
    {
        Binding incomingBind = null;
        if (myListener instanceof CompoundEventListener)
        {
            if (myControlContext != null)
            {
                incomingBind = new CompoundBindingFactory().makeBinding(evt, (CompoundEventListener)myListener, myControlContext);
            }
        }
        else if (myListener instanceof DiscreteEventListener)
        {
            boolean atLeastOneBinding = false;
            Collection<Binding> bindingList = myBindingsToListener.getBindings();
            DiscreteBindingFactory discreteBindingFactory = new DiscreteBindingFactory();
            for (Binding bind : bindingList)
            {
                if (bind != null)
                {
                    atLeastOneBinding = true;
                }
                if (bind instanceof DiscretePressedReleasedBind || bind instanceof KeyTypedBind
                        || bind instanceof MouseBindingAbs)
                {
                    incomingBind = discreteBindingFactory.makeBinding(evt, (DiscreteEventListener)myListener);
                    break;
                }
            }

            if (!atLeastOneBinding)
            {
                incomingBind = discreteBindingFactory.makeBinding(evt, (DiscreteEventListener)myListener);
            }
        }
        return incomingBind;
    }

    /**
     * Add the mouse listeners to the component.
     *
     * @param component The component.
     */
    private void addMouseListenersToComponent(Component component)
    {
        component.addMouseListener(myMouseCaptureListener);
        component.addMouseMotionListener(myMouseCaptureListener);
        component.addMouseWheelListener(myMouseWheelCaptureListener);
    }

    /**
     * This method will first search the control context to see if a binding is
     * already in use and ask the user to override. If the user chooses to
     * override, the key that was mapped to the incomingBind will be unbound by
     * firing keyBindingChanged and bound to the new key.
     *
     * @param incomingBind The incoming binding.
     */
    private void associateBinding(final Binding incomingBind)
    {
        String previouslyMapped = null;
        Map<String, List<BindingsToListener>> listeners = myControlContext.getEventListenersByCategory();
        for (Entry<String, List<BindingsToListener>> entry : listeners.entrySet())
        {
            for (BindingsToListener btl : entry.getValue())
            {
                Collection<Binding> bindings = btl.getBindings();
                for (Binding b : bindings)
                {
                    if (b != null && b.toString().equals(incomingBind.toString()))
                    {
                        previouslyMapped = btl.getListener().getTitle();
                        break;
                    }
                }
            }
        }

        if (previouslyMapped != null)
        {
            final String previous = previouslyMapped;
            int result = JOptionPane.showConfirmDialog(myParentComponent,
                    "The key binding\n" + "\"" + incomingBind.toString() + "\"\nis already in use by the control key,\n\""
                            + previouslyMapped + "\".\n"
                            + "You may assign this binding but the\nexisting binding will be lost.\n\nDo you want to use the new binding?",
                    "Key Binding Already In Use", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION)
            {
                myKeyBindingSupport.notifyListeners(new Callback<KeyBindingChangeListener>()
                {
                    @Override
                    public void notify(KeyBindingChangeListener listener)
                    {
                        listener.keyBindingChanged(
                                new KeyBindingChangeEvent(this, previous, KeyBindingChangeType.BINDING_REMOVED));
                        doBindingAction(incomingBind);
                    }
                });
            }
            else
            {
                return;
            }
        }
        else
        {
            doBindingAction(incomingBind);
        }
    }

    /**
     * Do binding action.
     *
     * @param incomingBind the incoming bind
     */
    private void doBindingAction(Binding incomingBind)
    {
        myBindingsToListener.associatePendingBinding(incomingBind, myAssignmentIndex);
        myButton.setText(incomingBind.toString());
        myButton.setSelected(false);
        removeKeyAndMouseCaptureListeners();
        myButton.setFocusTraversalKeysEnabled(true);
        myBindingsToListener.commitBindingChanges();
    }

    /**
     * Remove the listeners from my button.
     */
    private void removeKeyAndMouseCaptureListeners()
    {
        myButton.removeKeyListener(myKeyCaptureListener);
        myButton.setComponentPopupMenu(myBtnPopupMenu);

        removeMouseListenersFromComponent(myButton);
        removeMouseListenersFromComponent(myButton.getParent());
        removeMouseListenersFromComponent(SwingUtilities.getWindowAncestor(myButton));
    }

    /**
     * Remove the listeners from the component.
     *
     * @param component The componenet.
     */
    private void removeMouseListenersFromComponent(Component component)
    {
        component.removeMouseListener(myMouseCaptureListener);
        component.removeMouseMotionListener(myMouseCaptureListener);
        component.removeMouseWheelListener(myMouseWheelCaptureListener);
    }

    /**
     * Create the key listener.
     */
    private void setupKeyListener()
    {
        myKeyCaptureListener = new KeyCaptureListener();
        myButton.addKeyListener(myKeyCaptureListener);
    }

    /**
     * Create the mouse listeners.
     */
    private void setupMouseListeners()
    {
        myMouseCaptureListener = new MouseCaptureListener();
        myMouseWheelCaptureListener = new MouseWheelCaptureListener();
        myBtnPopupMenu = myButton.getComponentPopupMenu();

        addMouseListenersToComponent(myButton);
        addMouseListenersToComponent(myButton.getParent());
        addMouseListenersToComponent(SwingUtilities.getWindowAncestor(myButton));

        myButton.setComponentPopupMenu(null);
    }

    /**
     * Factory for compound bindings.
     */
    private static final class CompoundBindingFactory
    {
        /**
         * Create an appropriate binding for an event.
         *
         * @param event The event.
         * @param listener The listener for the event.
         * @param controlContext The control context.
         * @return The binding.
         */
        Binding makeBinding(InputEvent event, CompoundEventListener listener, ControlContext controlContext)
        {
            Binding toReturn;
            if (event instanceof KeyEvent)
            {
                toReturn = new CompoundPressedReleasedBind(((KeyEvent)event).getKeyCode(), ((KeyEvent)event).getModifiersEx(),
                        listener, controlContext);
            }
            else if (event instanceof MouseWheelEvent)
            {
                /* TODO: Impossible to make a compound bind with a mouse wheel
                 * event, since mouse wheel events are discrete. Do nothing
                 * here, but the user should be notified somehow... Or perhaps
                 * we could put a timer on mouse wheel events to make a kludged
                 * compound event? ie: start of roll / end of roll? */
                toReturn = null;
            }
            else if (event instanceof MouseEvent)
            {
                toReturn = new CompoundMouseBind((MouseEvent)event, listener, controlContext);
            }
            else
            {
                toReturn = null;
            }
            return toReturn;
        }
    }

    /**
     * Factory for discrete bindings.
     */
    private static final class DiscreteBindingFactory
    {
        /**
         * Create an appropriate binding for an event.
         *
         * @param event The event.
         * @param listener The listener for the event.
         * @return The binding.
         */
        Binding makeBinding(InputEvent event, DiscreteEventListener listener)
        {
            Binding toReturn = null;
            if (event instanceof KeyEvent)
            {
                toReturn = new DiscretePressedReleasedBind((KeyEvent)event, listener);
            }
            else if (event instanceof MouseWheelEvent)
            {
                toReturn = new MouseWheelBind((MouseWheelEvent)event, listener);
            }
            else if (event instanceof MouseEvent)
            {
                toReturn = new DiscreteMouseBind((MouseEvent)event, listener);
            }

            return toReturn;
        }
    }

    /** The key listener. */
    private final class KeyCaptureListener implements KeyListener
    {
        @Override
        public void keyPressed(KeyEvent evt)
        {
            if (eventIsOnlyAModifier(evt))
            {
                return;
            }
            Binding incomingBind = getProperBindingType(evt);

            if (incomingBind != null)
            {
                associateBinding(incomingBind);
                myTakingInputMode = false;
            }
        }

        @Override
        public void keyReleased(KeyEvent evt)
        {
        }

        @Override
        public void keyTyped(KeyEvent evt)
        {
            if (myListener instanceof DiscreteEventListener)
            {
                Binding incomingBind = null;
                Collection<Binding> bindingList = myBindingsToListener.getBindings();
                for (Binding bind : bindingList)
                {
                    if (bind instanceof KeyTypedBind)
                    {
                        incomingBind = new KeyTypedBind(evt, (DiscreteEventListener)myListener);
                        break;
                    }
                }

                if (incomingBind != null)
                {
                    associateBinding(incomingBind);
                    myTakingInputMode = false;
                }
            }
        }

        /**
         * Determine if an event is for a modifier key.
         *
         * @param evt The event.
         * @return <code>true</code> if the event is only a modifier.
         */
        private boolean eventIsOnlyAModifier(KeyEvent evt)
        {
            final int keyCode = evt.getKeyCode();
            return keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_ALT
                    || keyCode == KeyEvent.VK_ALT_GRAPH || keyCode == KeyEvent.VK_META;
        }
    }

    /**
     * The mouse listener.
     */
    private final class MouseCaptureListener extends MouseInputAdapter
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            Binding incomingBind = getProperBindingType(e);
            if (incomingBind != null)
            {
                associateBinding(incomingBind);
            }

            // Check for button1 here because we will also get the
            // actionPerformed
            // for that button. Let the actionPerformed toggle the
            // myTakingInputMode flag
            // in that case.
            if (e.getButton() != MouseEvent.BUTTON1 && myTakingInputMode)
            {
                myTakingInputMode = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            removeKeyAndMouseCaptureListeners();
            /* If the 3rd mouse button was pressed, the default action for this
             * is to show a context menu, and not deselect the button. The
             * context menu is taken care of elsewhere in this class, but we
             * need the button to 'unclick' with the selection of the third
             * mouse button. So, manually set its selected state to false here. */
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
            {
                myButton.setSelected(false);
            }
            /* If the 2nd mouse button was pressed, this has no default
             * "unclick" action on the button, so deselect the button.
             * Combinations involving button 3 have already been handled above. */
            else if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
            {
                myButton.setSelected(false);
            }
            /* if button 1 is pressed while the mouse is somewhere else on the
             * dialog and not directly over the button, the button won't
             * automatically toggle off. Toggle it off here. */
            else if (e.getSource() != myButton && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
            {
                myButton.setSelected(false);
            }
            myTakingInputMode = false;
        }
    }

    /**
     * The mouse wheel listener.
     */
    private final class MouseWheelCaptureListener implements MouseWheelListener
    {
        @Override
        public void mouseWheelMoved(MouseWheelEvent evt)
        {
            Binding incomingBind = getProperBindingType(evt);
            if (incomingBind != null)
            {
                associateBinding(incomingBind);
                myTakingInputMode = false;
            }
        }
    }
}
