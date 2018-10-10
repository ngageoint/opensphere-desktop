package io.opensphere.core.control;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.core.util.lang.Pair;

/**
 * Implementation of {@link ControlContext}.
 */
@SuppressWarnings("PMD.GodClass")
class ControlContextImpl implements ControlContext
{
    /**
     * The bindings that are activated during a compound event.
     */
    private final Collection<Binding> myActivatedBindings = Collections.synchronizedCollection(new WeakHashSet<Binding>());

    /**
     * Listeners for control events. Using weak references to the listeners
     * requires the registering class to maintain a reference to the listener.
     */
    private final Map<BoundEventListener, BindingsToListener> myListeners = Collections
            .synchronizedMap(New.<BoundEventListener, BindingsToListener>weakMap());

    /** Bindings for mouse events. */
    private final Collection<MouseBindingAbs> myMouseBindings = Collections
            .synchronizedCollection(new WeakHashSet<MouseBindingAbs>());

    /** The context's name, it must be unique among contexts. */
    private final String myName;

    /**
     * Listeners for pick events. Using weak references to the listeners
     * requires the registering class to maintain a reference to the listener.
     */
    private final Collection<PickListener> myPickListeners = Collections.synchronizedCollection(new WeakHashSet<PickListener>());

    /** Bindings for key press and release events. */
    private final Collection<PressedReleasedKeyBindAbs> myPressedReleasedKeyBindings = Collections
            .synchronizedCollection(new WeakHashSet<PressedReleasedKeyBindAbs>());

    /** Bindings for key typed events. */
    private final Collection<KeyTypedBind> myTypedKeyBindings = Collections
            .synchronizedCollection(new WeakHashSet<KeyTypedBind>());

    /**
     * Construct a ControlContext.
     *
     * @param name Unique name of this context.
     */
    ControlContextImpl(String name)
    {
        myName = name;
    }

    @Override
    public void addListener(CompoundEventListener listener, DefaultBinding... bindings)
    {
        List<Binding> converted = New.list();
        for (DefaultBinding defaultBinding : bindings)
        {
            if (defaultBinding instanceof DefaultKeyPressedBinding)
            {
                CompoundPressedReleasedBind compoundBind = new CompoundPressedReleasedBind(
                        (DefaultKeyPressedBinding)defaultBinding, listener, this);
                converted.add(compoundBind);
                myPressedReleasedKeyBindings.add(compoundBind);
            }
            else if (defaultBinding instanceof DefaultMouseBinding)
            {
                CompoundMouseBind mouseBind = new CompoundMouseBind((DefaultMouseBinding)defaultBinding, listener, this);
                converted.add(mouseBind);
                myMouseBindings.add(mouseBind);
            }
            else
            {
                throw new IllegalArgumentException("Bindings of type " + defaultBinding.getClass().getName()
                        + " are not supported with CompoundEventListeners");
            }
        }
        BindingsToListener bindingToListener = new BindingsToListener(this, listener,
                converted.toArray(new Binding[converted.size()]));
        myListeners.put(listener, bindingToListener);
    }

    @Override
    public void addListener(DiscreteEventListener listener, DefaultBinding... bindings)
    {
        List<Binding> converted = New.list();
        for (DefaultBinding defaultBinding : bindings)
        {
            if (defaultBinding instanceof DefaultKeyPressedBinding)
            {
                DiscretePressedReleasedBind keybinding = new DiscretePressedReleasedBind((DefaultKeyPressedBinding)defaultBinding,
                        listener, this);
                converted.add(keybinding);
                myPressedReleasedKeyBindings.add(keybinding);
            }
            else if (defaultBinding instanceof DefaultKeyTypedBinding)
            {
                KeyTypedBind typedBind = new KeyTypedBind((DefaultKeyTypedBinding)defaultBinding, listener, this);
                converted.add(typedBind);
                myTypedKeyBindings.add(typedBind);
            }
            else if (defaultBinding instanceof DefaultMouseWheelBinding)
            {
                DiscreteMouseBind mouseBind = new MouseWheelBind((DefaultMouseWheelBinding)defaultBinding, listener, this);
                converted.add(mouseBind);
                myMouseBindings.add(mouseBind);
            }
            else if (defaultBinding instanceof DefaultMouseBinding)
            {
                DiscreteMouseBind mouseBind = new DiscreteMouseBind((DefaultMouseBinding)defaultBinding, listener, this);
                converted.add(mouseBind);
                myMouseBindings.add(mouseBind);
            }
        }

        BindingsToListener bindingToListener = new BindingsToListener(this, listener,
                converted.toArray(new Binding[converted.size()]));
        myListeners.put(listener, bindingToListener);
    }

    /**
     * Add a mouse binding to my list of bindings.
     *
     * @param bind Binding to add.
     */
    public void addMouseBinding(MouseBindingAbs bind)
    {
        synchronized (myMouseBindings)
        {
            myMouseBindings.add(bind);
        }
    }

    @Override
    public void addPickListener(PickListener listen)
    {
        myPickListeners.add(listen);
    }

    @Override
    public Map<String, List<BindingsToListener>> getEventListenersByCategory()
    {
        LinkedHashMap<String, List<BindingsToListener>> listenersByCategory = new LinkedHashMap<>();
        for (BindingsToListener btl : getBindingsToListeners())
        {
            String category = btl.getListener().getCategory();
            if (listenersByCategory.containsKey(category))
            {
                List<BindingsToListener> insertedList = listenersByCategory.get(category);
                insertedList.add(btl);
            }
            else
            {
                List<BindingsToListener> toInsert = New.list();
                toInsert.add(btl);
                listenersByCategory.put(btl.getListener().getCategory(), toInsert);
            }
        }
        return listenersByCategory;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        List<PressedReleasedKeyBindAbs> bindings = New.list();
        synchronized (myPressedReleasedKeyBindings)
        {
            bindings.addAll(myPressedReleasedKeyBindings);
        }

        List<PressedReleasedKeyBindAbs> untargetedBinds = New.list();
        for (PressedReleasedKeyBindAbs bind : bindings)
        {
            if (keyBindIsTarget(bind, e, untargetedBinds))
            {
                bind.keyPressed(e);
                myActivatedBindings.add(bind);
            }
            if (e.isConsumed())
            {
                return;
            }
        }

        for (PressedReleasedKeyBindAbs bind : untargetedBinds)
        {
            bind.keyPressed(e);
            myActivatedBindings.add(bind);
            if (e.isConsumed())
            {
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        List<PressedReleasedKeyBindAbs> relBindings = New.list();
        synchronized (myPressedReleasedKeyBindings)
        {
            relBindings.addAll(myPressedReleasedKeyBindings);
        }

        List<PressedReleasedKeyBindAbs> untargetedBinds = New.list();
        for (PressedReleasedKeyBindAbs bind : relBindings)
        {
            if (keyBindIsTarget(bind, e, untargetedBinds))
            {
                bind.keyReleased(e);
                myActivatedBindings.remove(bind);
            }
            if (e.isConsumed())
            {
                return;
            }
        }

        for (PressedReleasedKeyBindAbs prBind : untargetedBinds)
        {
            prBind.keyReleased(e);
            myActivatedBindings.remove(prBind);
            if (e.isConsumed())
            {
                return;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        char keyChar = e.getKeyChar();
        int modifiersEx = e.getModifiersEx();

        List<KeyTypedBind> bindings = New.list();
        synchronized (myTypedKeyBindings)
        {
            bindings.addAll(myTypedKeyBindings);
        }

        List<KeyTypedBind> untargetedBinds = New.list();
        for (KeyTypedBind bind : bindings)
        {
            if (bind.getKeyChar() == keyChar && bind.getModifiersEx() == modifiersEx)
            {
                if (!bind.getListener().mustBeTargeted())
                {
                    untargetedBinds.add(bind);
                }
                else if (bind.getListener().isTargeted())
                {
                    bind.keyTyped(e);
                }
            }
            if (e.isConsumed())
            {
                return;
            }
        }

        for (KeyTypedBind ktBind : untargetedBinds)
        {
            ktBind.keyTyped(e);
            if (e.isConsumed())
            {
                return;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        performMouseBindAction(e, (bind, event) -> ((MouseBindingAbs)bind).mouseClicked((MouseEvent)event), false);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        performMouseBindAction(e, (bind, event) -> ((MouseBindingAbs)bind).mouseDragged((MouseEvent)event), false);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        performMouseBindAction(e, (bind, event) -> ((MouseBindingAbs)bind).mouseEntered((MouseEvent)event), true);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        performMouseBindAction(e, (bind, event) -> ((MouseBindingAbs)bind).mouseExited((MouseEvent)event), true);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        performMouseBindAction(e, (bind, event) -> ((MouseBindingAbs)bind).mouseMoved((MouseEvent)event), false);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        MouseBindActionWorker worker = (bind, event) ->
        {
            ((MouseBindingAbs)bind).mousePressed((MouseEvent)event);
            myActivatedBindings.add(bind);
        };
        performMouseBindAction(e, worker, false);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        MouseBindActionWorker worker = (bind, event) ->
        {
            ((MouseBindingAbs)bind).mouseReleased((MouseEvent)event);
            myActivatedBindings.remove(bind);
        };
        performMouseBindAction(e, worker, true);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        MouseBindActionWorker worker = (bind, event) ->
        {
            ((MouseBindingAbs)bind).mouseReleased((MouseEvent)event);
            myActivatedBindings.remove(bind);
        };
        performMouseBindAction(e, worker, false);
    }

    @Override
    public void notifyPicked(Geometry pickedGeom, Point position)
    {
        PickEvent evt = new PickEvent(pickedGeom, position);
        synchronized (myPickListeners)
        {
            for (PickListener listen : myPickListeners)
            {
                listen.handlePickEvent(evt);
            }
        }
    }

    @Override
    public void removeListener(BoundEventListener listener)
    {
        if (listener == null)
        {
            return;
        }

        BindingsToListener btl = myListeners.remove(listener);
        if (btl != null)
        {
            Collection<Binding> binds = btl.getBindings();
            myActivatedBindings.removeAll(binds);
            myMouseBindings.removeAll(binds);
            myPressedReleasedKeyBindings.removeAll(binds);
            myTypedKeyBindings.removeAll(binds);
        }
    }

    @Override
    public void removeListeners(Collection<? extends BoundEventListener> listeners)
    {
        for (BoundEventListener listener : listeners)
        {
            removeListener(listener);
        }
    }

    /**
     * Remove a mouse binding from my list of bindings.
     *
     * @param bind Binding to remove.
     */
    public void removeMouseBinding(Binding bind)
    {
        synchronized (myMouseBindings)
        {
            myMouseBindings.remove(bind);
        }
    }

    @Override
    public void removePickListener(PickListener listen)
    {
        myPickListeners.remove(listen);
    }

    /**
     * Get the Listener to bindings associations.
     *
     * @return Listener to bindings associations.
     */
    Collection<BindingsToListener> getBindingsToListeners()
    {
        synchronized (myListeners)
        {
            return New.list(myListeners.values());
        }
    }

    /**
     * Get all key press and release binding for the context.
     *
     * @return key press and release binding for the context.
     */
    Collection<PressedReleasedKeyBindAbs> getPressedReleasedKeyBindings()
    {
        return myPressedReleasedKeyBindings;
    }

    /**
     * Get all key typed binding for the context.
     *
     * @return key typed binding for the context.
     */
    Collection<KeyTypedBind> getTypedKeyBindings()
    {
        return myTypedKeyBindings;
    }

    /**
     * Get the modifier bits that are not already in use by other activated
     * bindings.
     *
     * @param incomingModifiers The modifiers for the incoming event.
     * @return The new modifiers.
     */
    private int getApplicableModifiers(int incomingModifiers)
    {
        int usedModifiers = 0;
        for (Binding binding : myActivatedBindings)
        {
            if (binding instanceof MouseBindingAbs)
            {
                usedModifiers |= ((MouseBindingAbs)binding).getModifiersEx();
            }
        }
        // Non-carrying binary mask subtract. For example: 100101 - 000111 =
        // 100000. This removes modifiers that are currently being used by an
        // action from the modifiers used to evaluate incoming events.
        return usedModifiers & incomingModifiers ^ incomingModifiers;
    }

    /**
     * Get a copy of the mouse bindings.
     *
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    private Pair<Collection<MouseBindingAbs>, Collection<MouseBindingAbs>> getMouseBindings()
    {
        List<MouseBindingAbs> untargetedBindings = New.list(myMouseBindings.size());
        List<MouseBindingAbs> targetedBindings = New.list(myMouseBindings.size());
        synchronized (myMouseBindings)
        {
            for (MouseBindingAbs bind : myMouseBindings)
            {
                if (bind.getListener().mustBeTargeted())
                {
                    targetedBindings.add(bind);
                }
                else
                {
                    untargetedBindings.add(bind);
                }
            }
        }

        Collections.sort(targetedBindings, ControlContextImpl::compare);
        return new Pair<>(targetedBindings, untargetedBindings);
    }

    /**
     * Compares mouse bindings based on listener's target priority.
     *
     * @param o1 first mouse binding
     * @param o2 second mouse binding
     * @return int
     */
    private static int compare(MouseBindingAbs o1, MouseBindingAbs o2)
    {
        int pri1 = o1.getListener().getTargetPriority();
        int pri2 = o2.getListener().getTargetPriority();
        return pri1 > pri2 ? -1 : pri1 == pri2 ? 0 : 1;
    }

    /**
     * Determine whether the binding is the target for the event. Bindings which
     * have no pick geometries will be added to the un-targeted binds list.
     * Un-targeted binds expect to receive the event whenever it is not consumed
     * by a targeted bind.
     *
     * @param bind The binding to check.
     * @param event The event.
     * @param untargetedBinds The list of bindings which cannot be targeted.
     * @return true when the given bindings is the target of the event.
     */
    private boolean keyBindIsTarget(PressedReleasedKeyBindAbs bind, KeyEvent event,
            List<PressedReleasedKeyBindAbs> untargetedBinds)
    {
        // if the key is a modifier, ignore modifiers.
        boolean ignoreModifiers = bind.getKeyCode() == KeyEvent.VK_SHIFT || bind.getKeyCode() == KeyEvent.VK_ALT
                || bind.getKeyCode() == KeyEvent.VK_CONTROL;

        // TODO why do we use getAppicableModifiers()?
        int modifiersEx = getApplicableModifiers(event.getModifiersEx());
        if (bind.getKeyCode() == event.getKeyCode() && (ignoreModifiers || bind.getModifiersEx() == modifiersEx))
        {
            if (!bind.getListener().mustBeTargeted())
            {
                untargetedBinds.add(bind);
            }
            else if (bind.getListener().isTargeted())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the binding is applicable for the event.
     *
     * @param bind The binding to check.
     * @param event The event to check against.
     * @param ignoreModifiers when true, ignore the event modifiers when
     *            determining binding applicability.
     * @return true when the the binding is applicable to the event.
     */
    private boolean mouseBindIsApplicable(MouseBindingAbs bind, MouseEvent event, boolean ignoreModifiers)
    {
        boolean eventMatches = false;
        // if this is a release or a drag, compound mouse event listeners want
        // this event even though they only registered for the press.
        if ((event.getID() == MouseEvent.MOUSE_RELEASED || event.getID() == MouseEvent.MOUSE_DRAGGED)
                && bind.getListener() instanceof CompoundEventListener && bind.getEventId() == MouseEvent.MOUSE_PRESSED)
        {
            eventMatches = true;
        }
        else
        {
            eventMatches = bind.getEventId() == event.getID();
        }

        boolean modifierMatches = bind.getModifiersEx() == event.getModifiersEx() || ignoreModifiers;

        return eventMatches && modifierMatches;
    }

    /**
     * Helper method to perform an action against a particular binding if the
     * binding is applicable for the event.
     *
     * @param bind The binding to perform the action against.
     * @param event The event associated with the action.
     * @param worker The worker which will perform the action.
     * @param ignoreModifiers when true, ignore the event modifiers when
     *            determining binding applicability.
     */
    private void performActionIfApplicable(MouseBindingAbs bind, MouseEvent event, MouseBindActionWorker worker,
            boolean ignoreModifiers)
    {
        if (bind.getListener().mustBeTargeted() && !bind.getListener().isTargeted())
        {
            return;
        }

        if (event instanceof MouseWheelEvent)
        {
            if (bind instanceof MouseWheelBind)
            {
                final MouseWheelBind mwBind = (MouseWheelBind)bind;
                if (mwBind.getEventId() == event.getID() && mwBind.getModifiersEx() == event.getModifiersEx()
                        && MathUtil.sameSign(mwBind.getWheelDirection(), ((MouseWheelEvent)event).getWheelRotation()))
                {
                    worker.doAction(bind, event);
                    return;
                }
            }
        }
        else if (mouseBindIsApplicable(bind, event, ignoreModifiers))
        {
            worker.doAction(bind, event);
            if (event.isConsumed())
            {
                return;
            }
        }
    }

    /**
     * Find the appropriate binding for the event and have the worker execute
     * the correct for the event against the binding.
     *
     * @param event The event which has occurred.
     * @param worker The worker which will execute the action.
     * @param ignoreModifiers When true, ignore the event modifiers when
     *            choosing the binding to execute against.
     */
    private void performMouseBindAction(MouseEvent event, MouseBindActionWorker worker, boolean ignoreModifiers)
    {
        Pair<Collection<MouseBindingAbs>, Collection<MouseBindingAbs>> bindings = getMouseBindings();

        for (MouseBindingAbs bind : bindings.getFirstObject())
        {
            performActionIfApplicable(bind, event, worker, ignoreModifiers);
            if (event.isConsumed())
            {
                return;
            }
        }

        for (MouseBindingAbs bind : bindings.getSecondObject())
        {
            performActionIfApplicable(bind, event, worker, ignoreModifiers);
            if (event.isConsumed())
            {
                return;
            }
        }
    }

    /** A worker to perform a particular event action against a binding. */
    @FunctionalInterface
    interface MouseBindActionWorker
    {
        /**
         * Perform the action on the binding.
         *
         * @param bind The binding for which the action is being executed.
         * @param event The event associated with the binding.
         */
        void doAction(Binding bind, InputEvent event);
    }
}
