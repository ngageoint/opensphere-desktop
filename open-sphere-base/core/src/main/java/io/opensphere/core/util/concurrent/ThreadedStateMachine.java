package io.opensphere.core.util.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionProvider;
import io.opensphere.core.util.collections.ListProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * A state machine that uses multiple threads for state transitions. A number of
 * states may be defined into which objects may be placed. Handlers may then be
 * assigned such that they are executed when objects enter their states. The
 * handler may then move the objects to another state, triggering another
 * handler, etc. Each handler is assigned an {@link Executor} which will be used
 * by the state machine to execute the handler.
 *
 * @param <E> The type of objects in the state machine.
 */
@SuppressWarnings("PMD.GodClass")
public class ThreadedStateMachine<E>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ThreadedStateMachine.class);

    /**
     * Cached collection of all objects known to the state machine. This should
     * be set to {@code null} at the beginning of each synchronization block
     * that modifies {@link #myObjectToStateMap}.
     */
    private volatile List<? extends E> myAllObjects;

    /**
     * Map of objects to states. This object's monitor is used to synchronize
     * each individual state change. Whenever this is changed,
     * {@link #myAllObjects} should be set to {@code null}.
     */
    private final Map<E, State> myObjectToStateMap = New.map();

    /** Map of states to state change handlers. */
    private final ConcurrentMap<State, HandlerExecutors<E>> myStateChangeHandlers;

    /**
     * Map of states to lists of objects. Note that only <i>holding</i> states
     * appear in this map. Access or modification of this map must be
     * synchronized using {@link #myObjectToStateMap}'s monitor.
     */
    private final Map<State, List<E>> myStateToObjectsMap = New.map();

    /**
     * Comparator for tasks that puts them in the order of their "from" state
     * order.
     */
    private final Comparator<Task> myTaskComparator = new Comparator<Task>()
    {
        @Override
        public int compare(Task o1, Task o2)
        {
            State o1FromState = o1.getFromState();
            State o2FromState = o2.getFromState();
            int o2StateOrder = o2FromState == null ? 0 : o2FromState.getStateOrder();
            int o1StateOrder = o1FromState == null ? 0 : o1FromState.getStateOrder();
            return o2StateOrder > o1StateOrder ? -1 : o2StateOrder == o1StateOrder ? 0 : 1;
        }
    };

    /**
     * Map of objects to tasks. Changes to this map, including changes to tasks,
     * must always be synchronized on this object's monitor.
     */
    private final Map<E, Task> myTaskMap = New.map();

    /**
     * Construct the state machine with no holding states.
     */
    public ThreadedStateMachine()
    {
        myStateChangeHandlers = new ConcurrentHashMap<>();
    }

    /**
     * Construct the state machine.
     *
     * @param holdingStates Set of states that may be retrieved using
     *            {@link #getObjectsInState(State)} or
     *            {@link #getObjectsInState(Set)}.
     */
    public ThreadedStateMachine(Set<? extends State> holdingStates)
    {
        myStateChangeHandlers = new ConcurrentHashMap<>();
        for (State holdingState : holdingStates)
        {
            myStateToObjectsMap.put(holdingState, New.<E>list());
        }
    }

    /**
     * Get all of the objects currently being handled by this state machine.
     *
     * @return All of the objects currently being handled by this state machine.
     */
    public List<? extends E> getAllObjects()
    {
        List<? extends E> allObjects = myAllObjects;
        if (allObjects == null)
        {
            allObjects = populateAllObjects();
        }
        return allObjects;
    }

    /**
     * Get all of the objects currently being handled by this state machine.
     *
     * @param provider Provider to produce the collection to contain the
     *            results.
     * @return All of the objects currently being handled by this state machine.
     */
    public Collection<E> getAllObjects(CollectionProvider<E> provider)
    {
        synchronized (myObjectToStateMap)
        {
            return myObjectToStateMap.isEmpty() ? provider.getEmpty() : provider.get(myObjectToStateMap.keySet());
        }
    }

    /**
     * Get all of the objects currently being handled by this state machine.
     *
     * @param provider Provider to produce the collection to contain the
     *            results.
     * @return All of the objects currently being handled by this state machine.
     */
    public List<E> getAllObjects(ListProvider<E> provider)
    {
        synchronized (myObjectToStateMap)
        {
            return myObjectToStateMap.isEmpty() ? provider.getEmpty() : provider.get(myObjectToStateMap.keySet());
        }
    }

    /**
     * Get the objects currently in the provided set of states. The states must
     * be holding states as defined when the state machine was constructed.
     *
     * @param states The states.
     * @return The objects in the states.
     */
    public Collection<E> getObjectsInState(Set<? extends State> states)
    {
        return getObjectsInState(states, New.<E>collectionFactory());
    }

    /**
     * Get the objects currently in the provided set of states. The states must
     * be holding states as defined when the state machine was constructed.
     *
     * @param states The states.
     * @param provider Provider to produce the collection to contain the
     *            results.
     * @return The objects in the states.
     */
    public Collection<E> getObjectsInState(Set<? extends State> states, CollectionProvider<E> provider)
    {
        CollectionProvider<E> lazyProvider = New.<E>lazyCollectionProvider(provider);
        Collection<E> results = null;
        for (State state : states)
        {
            results = getObjectsInState(state, lazyProvider);
        }
        return results == null ? provider.getEmpty() : results;
    }

    /**
     * Get the objects currently in the provided states. The states must be
     * holding states as defined when the state machine was constructed.
     *
     * @param states The states.
     * @return The objects in the states.
     */
    public List<E> getObjectsInState(State... states)
    {
        List<E> result = New.list();
        CollectionProvider<E> provider = New.singletonCollectionProvider(result);
        for (State state : states)
        {
            getObjectsInState(state, provider);
        }
        return result;
    }

    /**
     * Get the objects currently in a particular state. The state must be a
     * holding state as defined when the state machine was constructed.
     *
     * @param state The state.
     * @return The objects in the state.
     */
    public Collection<E> getObjectsInState(State state)
    {
        return getObjectsInState(state, New.<E>collectionFactory());
    }

    /**
     * Get the objects currently in a particular state. The state must be a
     * holding state as defined when the state machine was constructed.
     *
     * @param state The state.
     * @param provider Provider to produce the collection to contain the
     *            results.
     * @return The objects in the state.
     */
    public Collection<E> getObjectsInState(State state, CollectionProvider<E> provider)
    {
        List<E> list;
        synchronized (myObjectToStateMap)
        {
            list = myStateToObjectsMap.get(state);
        }
        Collection<E> results;
        if (list == null)
        {
            results = provider.getEmpty();
        }
        else
        {
            synchronized (list)
            {
                results = list.isEmpty() ? provider.getEmpty() : provider.get(list);
            }
        }
        return results;
    }

    /**
     * Register a handler to be called when an object reaches any of the given
     * states.
     * <p>
     * Typically the handler will cause one or more runnables to be added back
     * into the executor, so it is important that the executor will not cause a
     * dead-lock in that case.
     *
     * @param states The states of interest.
     * @param handler The handler.
     * @param highVolumeExecutor The executor that will be used to execute the
     *            handler for large object volumes.
     * @param lowVolumeExecutor The executor that will be used to execute the
     *            handler for small object volumes.
     * @param highVolumeObjectCount The number of objects that trigger the
     *            high-volume executor to be used.
     */
    public void registerStateChangeHandler(Set<? extends State> states, StateChangeHandler<E> handler,
            Executor highVolumeExecutor, Executor lowVolumeExecutor, int highVolumeObjectCount)
    {
        for (State state : states)
        {
            myStateChangeHandlers.put(state,
                    new HandlerExecutors<>(handler, highVolumeExecutor, lowVolumeExecutor, highVolumeObjectCount));
        }
    }

    /**
     * Remove all objects from this state machine and cancel all tasks.
     */
    public void removeAll()
    {
        synchronized (myObjectToStateMap)
        {
            myAllObjects = null;
            synchronized (myTaskMap)
            {
                for (Task task : myTaskMap.values())
                {
                    task.cancelAll();
                }
            }
            myObjectToStateMap.clear();
            myStateToObjectsMap.clear();
        }
    }

    /**
     * Remove all objects from the given states.
     *
     * @param states The states.
     * @return The removed objects.
     */
    public Collection<E> removeAllFromState(Collection<? extends State> states)
    {
        return removeAllFromState(states, New.<E>collectionFactory());
    }

    /**
     * Remove all objects from the given states.
     *
     * @param states The states.
     * @param provider Optional provider to produce the collection to contain
     *            the removed objects.
     * @return The removed objects, or {@code null} if no provider was provided.
     */
    public Collection<E> removeAllFromState(Collection<? extends State> states, CollectionProvider<E> provider)
    {
        CollectionProvider<E> lazyProvider = provider == null ? null : New.lazyCollectionProvider(provider);
        Collection<E> result = null;
        synchronized (myObjectToStateMap)
        {
            for (State state : states)
            {
                result = removeAllFromState(state, lazyProvider);
            }
        }
        return provider == null ? null : result == null ? provider.getEmpty() : result;
    }

    /**
     * Remove all objects from the given state.
     *
     * @param state The state.
     * @return The removed objects.
     */
    public Collection<E> removeAllFromState(State state)
    {
        return removeAllFromState(state, New.<E>collectionFactory());
    }

    /**
     * Remove all objects from the given state.
     *
     * @param state The state.
     * @param provider Optional provider to produce the collection to contain
     *            the results.
     * @return The removed objects, or {@code null} if no provider was provided.
     */
    public Collection<E> removeAllFromState(State state, CollectionProvider<E> provider)
    {
        synchronized (myObjectToStateMap)
        {
            myAllObjects = null;
            List<E> list = myStateToObjectsMap.get(state);
            if (list != null)
            {
                synchronized (list)
                {
                    Collection<E> result = provider == null ? null : list.isEmpty() ? provider.getEmpty() : provider.get(list);
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Clearing list for state [" + state + "] (" + list.size() + " objects)");
                    }
                    for (E obj : list)
                    {
                        myObjectToStateMap.remove(obj);
                    }
                    cancelTasks(list);
                    list.clear();
                    return result;
                }
            }
            return provider == null ? null : provider.getEmpty();
        }
    }

    /**
     * Remove objects from the state machine.
     *
     * @param objects The objects to be removed.
     */
    public void removeFromState(Collection<?> objects)
    {
        if (!objects.isEmpty())
        {
            synchronized (myObjectToStateMap)
            {
                myAllObjects = null;
                doRemoveFromState(objects);
                cancelTasks(objects);
            }
        }
    }

    /**
     * Reset objects to an earlier state (as defined by
     * {@link State#getStateOrder()}. If the objects are in an earlier state
     * than {@code toState}, they are ignored. If the objects are unknown to the
     * state machine, they are added in the given state.
     *
     * @param objects The objects to reset.
     * @param toState The destination state.
     */
    public void resetState(Collection<? extends E> objects, State toState)
    {
        synchronized (myObjectToStateMap)
        {
            Collection<E> objectsNotInEarlierState = getObjectsNotInEarlierState(objects, toState);
            cancelTasks(objectsNotInEarlierState);
            moveToState(objectsNotInEarlierState, (State)null, toState, (Comparator<? super E>)null, true);
        }
    }

    /**
     * Find objects in {@code toState} or a state with a higher order than
     * {@code toState} and reset them to {@code toState}, cancelling any
     * existing processing.
     *
     * @param toState The state the objects will move to.
     */
    public void resetState(State toState)
    {
        Collection<E> objects = New.collection();
        synchronized (myObjectToStateMap)
        {
            for (Entry<E, State> entry : myObjectToStateMap.entrySet())
            {
                E object = entry.getKey();
                State state = entry.getValue();
                if (state.getStateOrder() >= toState.getStateOrder())
                {
                    objects.add(object);
                }
            }

            cancelTasks(objects);
            moveToState(objects, (State)null, toState, (Comparator<? super E>)null, true);
        }
    }

    /**
     * Stop the state machine. Cancel all transitions in progress and forget
     * about all objects.
     */
    public void stop()
    {
        myStateChangeHandlers.clear();
        removeAll();
    }

    /**
     * Helper method that puts objects into a state.
     *
     * @param objects The objects.
     * @param state The state.
     * @param comparator Optional comparator used to sort the objects in the
     *            destination state after the new objects are added.
     */
    protected void addToState(final Collection<? extends E> objects, final State state, Comparator<? super E> comparator)
    {
        for (E obj : objects)
        {
            myObjectToStateMap.put(obj, state);
        }
        List<E> list = myStateToObjectsMap.get(state);
        if (list != null)
        {
            synchronized (list)
            {
                // Eliminate duplicates.
                Set<E> set = New.set(objects);
                if (list.size() > 20)
                {
                    set.removeAll(New.set(list));
                }
                else
                {
                    set.removeAll(list);
                }
                list.addAll(set);
                if (comparator != null)
                {
                    Collections.sort(list, comparator);
                }
            }
        }
        if (LOGGER.isTraceEnabled())
        {
            Set<Entry<State, List<E>>> entrySet = myStateToObjectsMap.entrySet();
            for (Entry<State, List<E>> entry : entrySet)
            {
                State key = entry.getKey();
                List<E> value = entry.getValue();
                synchronized (value)
                {
                    LOGGER.trace(value.size() + " objects are now in state [" + key + "]");
                }
            }
        }
    }

    /**
     * Cancel any tasks associated with the objects.
     *
     * @param objs The objects.
     */
    protected void cancelTasks(Collection<?> objs)
    {
        if (objs.isEmpty())
        {
            return;
        }
        Map<Task, Collection<Object>> tasksToCancel = null;
        synchronized (myTaskMap)
        {
            if (!myTaskMap.isEmpty())
            {
                for (Object obj : objs)
                {
                    Task task = myTaskMap.remove(obj);
                    if (task != null)
                    {
                        if (tasksToCancel == null)
                        {
                            tasksToCancel = New.map();
                        }
                        Collection<Object> taskObjs = tasksToCancel.get(task);
                        if (taskObjs == null)
                        {
                            taskObjs = New.set();
                            tasksToCancel.put(task, taskObjs);
                        }
                        taskObjs.add(obj);
                    }
                }
            }
        }
        if (tasksToCancel != null)
        {
            for (Map.Entry<Task, Collection<Object>> entry : tasksToCancel.entrySet())
            {
                entry.getKey().cancelAll(entry.getValue());
            }
        }
    }

    /**
     * Find any existing tasks that conflict with the new tasks. When there is a
     * conflict, favor the task with the lower state order. Keep track of which
     * tasks own which objects using {@link #myTaskMap}.
     *
     * @param tasks The new tasks.
     */
    protected void deconflictTasks(Collection<Task> tasks)
    {
        Map<Task, Collection<Object>> tasksToCancel = New.map();
        synchronized (myTaskMap)
        {
            if (myTaskMap.isEmpty())
            {
                for (Task task : tasks)
                {
                    if (!task.isEmpty())
                    {
                        for (E obj : (Collection<? extends E>)task.getObjects())
                        {
                            myTaskMap.put(obj, task);
                        }
                    }
                }
            }
            else
            {
                for (Task task : tasks)
                {
                    if (!task.isEmpty())
                    {
                        for (E obj : (Collection<? extends E>)task.getObjects())
                        {
                            Task existingTask = myTaskMap.get(obj);
                            if (existingTask == null)
                            {
                                /* No existing task, so just store this task in
                                 * the map. */
                                myTaskMap.put(obj, task);
                            }
                            else
                            {
                                // Determine which task should take precedence.
                                Task taskToCancel;
                                if (existingTask.getToState().getStateOrder() >= task.getToState().getStateOrder())
                                {
                                    if (LOGGER.isTraceEnabled())
                                    {
                                        LOGGER.trace("Cancelling task [" + existingTask + "] in favor of [" + task + "]");
                                    }
                                    myTaskMap.put(obj, task);
                                    taskToCancel = existingTask;
                                }
                                else
                                {
                                    if (LOGGER.isTraceEnabled())
                                    {
                                        LOGGER.trace("Removing object [" + obj + "] from task [" + task + "] because task ["
                                                + existingTask + "] has a lower order.");
                                    }
                                    taskToCancel = task;
                                }
                                Collection<Object> taskObjs = tasksToCancel.get(taskToCancel);
                                if (taskObjs == null)
                                {
                                    taskObjs = New.set();
                                    tasksToCancel.put(taskToCancel, taskObjs);
                                }
                                taskObjs.add(obj);
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<Task, Collection<Object>> entry : tasksToCancel.entrySet())
        {
            entry.getKey().cancelAll(entry.getValue());
        }
    }

    /**
     * Helper method that removes objects from the object-state map and the
     * state-object map. This does not cancel tasks.
     *
     * @param objects The objects to be removed.
     */
    protected void doRemoveFromState(Collection<?> objects)
    {
        State lastState = null;
        Set<Object> objsInLastState = New.set();
        synchronized (myObjectToStateMap)
        {
            for (Object obj : objects)
            {
                State oldState = myObjectToStateMap.get(obj);
                if (oldState == null)
                {
                    myAllObjects = null;
                }
                else
                {
                    myObjectToStateMap.remove(obj);
                    if (!Utilities.sameInstance(oldState, lastState))
                    {
                        if (lastState != null)
                        {
                            removeFromStateToObjectsMap(lastState, objsInLastState);
                            objsInLastState.clear();
                        }
                        lastState = oldState;
                    }
                    objsInLastState.add(obj);
                }
            }
            if (lastState != null)
            {
                removeFromStateToObjectsMap(lastState, objsInLastState);
            }
        }
    }

    /**
     * Execute a task, changing the state of its objects to the destination
     * state and calling the state change handler. This should be called using
     * the appropriate executor.
     *
     * @param task The task to be executed.
     * @param handler The state handler.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    protected void executeTask(final Task task, final StateChangeHandler<E> handler)
    {
        boolean traceEnabled = LOGGER.isTraceEnabled();

        State toState = task.getToState();

        // Change the states.
        List<? extends E> objects;
        synchronized (myObjectToStateMap)
        {
            if (task.isEmpty())
            {
                return;
            }
            objects = New.list(task.getObjects());

            if (traceEnabled)
            {
                LOGGER.trace("Executing task [" + task + "]");
            }

            doRemoveFromState(objects);
            addToState(objects, toState, task.getComparator());

            if (traceEnabled)
            {
                LOGGER.trace("Task state change complete [" + task + "]");
            }
        }

        // Call the handler.
        if (handler != null)
        {
            long t0 = System.nanoTime();
            handler.handleStateChanged(objects, toState, task);
            if (traceEnabled)
            {
                long t1 = System.nanoTime();
                LOGGER.trace(StringUtilities.formatTimingMessage("Handler execution for [" + task + "] took ", t1 - t0));
            }
        }

        // Remove completed tasks. If the handler for the destination state
        // requested another state change, the task map will already be changed.
        // Otherwise it needs to be tidied up.
        synchronized (myTaskMap)
        {
            for (E obj : objects)
            {
                if (myTaskMap.get(obj) == task)
                {
                    myTaskMap.remove(obj);
                }
            }
        }

        if (traceEnabled)
        {
            LOGGER.trace("Handler execution complete [" + task + "]");
        }
    }

    /**
     * Move objects from one state to another.
     *
     * @param objects The objects that are moving.
     * @param fromState The origin state, or <code>null</code> if unknown.
     * @param toState The destination state.
     * @param comparator An optional comparator that will be used to sort the
     *            objects in the destination state after the new objects are
     *            added.
     * @param force If {@code true}, tasks will be generated even if the objects
     *            are already in the destination state.
     */
    protected void moveToState(final Collection<? extends E> objects, final State fromState, final State toState,
            Comparator<? super E> comparator, boolean force)
    {
        if (!objects.isEmpty())
        {
            synchronized (myObjectToStateMap)
            {
                Map<State, Collection<E>> movingObjects = determineMovingObjects(objects, fromState,
                        force ? (State)null : toState);
                if (movingObjects.isEmpty())
                {
                    return;
                }

                List<Task> tasks = New.list(movingObjects.size());
                for (Entry<State, Collection<E>> entry : movingObjects.entrySet())
                {
                    tasks.add(new Task(entry.getValue(), entry.getKey(), toState, comparator));
                }
                if (LOGGER.isTraceEnabled())
                {
                    for (Task task : tasks)
                    {
                        LOGGER.trace("Created task [" + task + "] 1st object type is ["
                                + objects.iterator().next().getClass().getSimpleName() + "]");
                    }
                }
                if (tasks.size() > 1)
                {
                    Collections.sort(tasks, myTaskComparator);
                }

                deconflictTasks(tasks);
                submitTasks(tasks);
            }
        }
    }

    /**
     * Schedule a new state change that is a follow-on to {@code task}. The
     * state change described by {@code task} should have already been completed
     * before this method is called.
     *
     * @param task The task that triggered this state change.
     * @param objects The objects whose states are changing.
     * @param fromState The previous state.
     * @param toState The destination state.
     * @param comparator An optional comparator for ordering the objects in the
     *            new state.
     */
    protected void moveToState(Task task, Collection<? extends E> objects, State fromState, State toState,
            Comparator<? super E> comparator)
    {
        if (!objects.isEmpty())
        {
            // Ensure that the task objects remain constant while the state is
            // changed.
            synchronized (myObjectToStateMap)
            {
                // Only take action on objects that are still in the task and
                // are requested to move.
                if (!task.isEmpty())
                {
                    Collection<E> intersection = New.collection(task.getObjects());
                    intersection.retainAll(objects instanceof Set<?> ? objects : New.set(objects));
                    if (!intersection.isEmpty())
                    {
                        cancelTasks(intersection);
                        moveToState(intersection, fromState, toState, comparator, false);
                    }
                }
            }
        }
    }

    /**
     * Helper method that removes objects from the state-to-object map.
     *
     * @param state The state from which the objects are being removed.
     * @param removes The objects being removed.
     */
    protected void removeFromStateToObjectsMap(State state, Set<?> removes)
    {
        List<E> list = myStateToObjectsMap.get(state);
        if (list != null)
        {
            synchronized (list)
            {
                boolean success;
                // Optimization in the case that the list is large.
                if (list.size() == removes.size() && list.size() > 20)
                {
                    if (removes.containsAll(list))
                    {
                        list.clear();
                        success = true;
                    }
                    else
                    {
                        Set<E> listSet = New.insertionOrderSet(list);
                        success = listSet.removeAll(removes);
                        list.clear();
                        list.addAll(listSet);
                    }
                }
                else
                {
                    success = list.removeAll(removes);
                }
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Removed " + removes.size() + " from state [" + state + "] (success: " + success + ")");
                }
            }
        }
    }

    /**
     * Submit a runnable that will execute the given tasks.
     *
     * @param tasks The tasks to be executed.
     */
    protected void submitTasks(final Collection<? extends Task> tasks)
    {
        for (final Task task : tasks)
        {
            if (!task.isEmpty())
            {
                State toState = task.getToState();
                HandlerExecutors<E> pair = myStateChangeHandlers.get(toState);
                if (pair == null)
                {
                    if (!myStateChangeHandlers.isEmpty())
                    {
                        LOGGER.warn("No handler registered for state [" + toState + "]");
                    }
                }
                else
                {
                    final StateChangeHandler<E> handler = pair.getStateChangeHandler();
                    Executor executor = pair.getExecutor(task.getObjects().size());
                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            executeTask(task, handler);
                        }
                    });
                }
            }
        }
    }

    /**
     * Determine which of the supplied objects are either in the
     * <code>fromState</code>, or (if the <code>fromState</code> is
     * <code>null</code>), not already in the <code>toState</code>.
     *
     * @param objects The candidate objects.
     * @param fromState The origin state.
     * @param toState The destination state.
     * @return A map of origin states to moving objects in those states.
     */
    @SuppressWarnings("null")
    private Map<State, Collection<E>> determineMovingObjects(Collection<? extends E> objects, State fromState, State toState)
    {
        Map<State, Collection<E>> movingObjects = New.map();
        State lastState = null;
        Collection<E> col = null;
        for (E obj : objects)
        {
            State currentState = myObjectToStateMap.get(obj);
            if ((toState == null || !Utilities.sameInstance(currentState, toState))
                    && (fromState == null || Utilities.sameInstance(currentState, fromState)))
            {
                State state = currentState == null ? fromState : currentState;
                if (!Utilities.sameInstance(lastState, state) || col == null)
                {
                    col = movingObjects.get(state);
                    if (col == null)
                    {
                        col = New.collection(objects.size());
                        movingObjects.put(state, col);
                    }
                    lastState = state;
                }
                col.add(obj);
            }
        }

        return movingObjects;
    }

    /**
     * Filter the input objects to include only those that are not in an earlier
     * state than {@code state}.
     *
     * @param objects The objects.
     * @param state The state.
     * @return The filtered objects.
     */
    private Collection<E> getObjectsNotInEarlierState(Collection<? extends E> objects, State state)
    {
        Collection<E> objectsNotInEarlierState = New.collection(objects.size());
        for (E obj : objects)
        {
            State currentState = myObjectToStateMap.get(obj);
            if (currentState == null || currentState.getStateOrder() >= state.getStateOrder())
            {
                objectsNotInEarlierState.add(obj);
            }
        }
        return objectsNotInEarlierState;
    }

    /**
     * Populate the cache of all objects.
     *
     * @return The all objects collection, which is guaranteed to not be
     *         {@code null}.
     */
    private List<? extends E> populateAllObjects()
    {
        synchronized (myObjectToStateMap)
        {
            List<? extends E> allObjects = myAllObjects;
            if (allObjects == null)
            {
                allObjects = Collections.unmodifiableList(getAllObjects(New.<E>listFactory()));
                myAllObjects = allObjects;
            }
            return allObjects;
        }
    }

    /**
     * Interface for a state in the state machine.
     */
    @FunctionalInterface
    public interface State
    {
        /**
         * The order of the state. Objects naturally move from lower-numbered
         * states to higher-number states.
         *
         * @return The order of this state.
         */
        int getStateOrder();
    }

    /**
     * Interface called after objects change state.
     *
     * @param <E> The type of objects in the state machine.
     */
    @FunctionalInterface
    public interface StateChangeHandler<E>
    {
        /**
         * Handle a state change.
         *
         * @param objects The objects whose states changed.
         * @param newState The new state of the objects.
         * @param controller A controller that can be used to initiate further
         *            state changes.
         */
        void handleStateChanged(List<? extends E> objects, State newState, StateController<E> controller);
    }

    /**
     * Interface for an object capable of changing the state of objects within
     * the state machine.
     *
     * @param <E> The type of objects in the state machine.
     */
    @FunctionalInterface
    public interface StateController<E>
    {
        /**
         * Initiate a state change for some objects.
         *
         * @param objects The objects.
         * @param toState The destination state.
         */
        void changeState(Collection<? extends E> objects, State toState);
    }

    /**
     * A structure that ties a state change handler and its executors together.
     *
     * @param <E> The type of objects in the state machine.
     */
    protected static class HandlerExecutors<E>
    {
        /**
         * The executor that will execute the state change handler for high
         * volumes of objects.
         */
        private final Executor myHighExecutor;

        /**
         * The executor that will execute the state change handler for low
         * volumes of objects.
         */
        private final Executor myLowExecutor;

        /** The number of objects that triggers the high executor to be used. */
        private final int myObjectThreshold;

        /** The handler executed when objects reach a certain state. */
        private final StateChangeHandler<E> myStateChangeHandler;

        /**
         * Constructor.
         *
         * @param stateChangeHandler The handler executed when objects reach a
         *            certain state. This may be <code>null</code>.
         * @param highExecutor The executor that will execute the state change
         *            handler for high volumes of objects. This cannot be
         *            <code>null</code>.
         * @param lowExecutor The executor that will execute the state change
         *            handler for low volumes of objects. This cannot be
         *            <code>null</code>.
         * @param objectThreshold The number of objects that triggers the high
         *            executor to be used.
         */
        public HandlerExecutors(StateChangeHandler<E> stateChangeHandler, Executor highExecutor, Executor lowExecutor,
                int objectThreshold)
        {
            if (highExecutor == null)
            {
                throw new IllegalArgumentException("highExecutor cannot be null");
            }
            if (objectThreshold > 1 && lowExecutor == null)
            {
                throw new IllegalArgumentException("lowExecutor cannot be null");
            }
            myStateChangeHandler = stateChangeHandler;
            myHighExecutor = highExecutor;
            myLowExecutor = objectThreshold > 1 ? lowExecutor : null;
            myObjectThreshold = objectThreshold;
        }

        /**
         * Get the executor that will execute the state change handler.
         *
         * @param count The number of objects to be sent to the executor.
         * @return The executor.
         */
        public Executor getExecutor(int count)
        {
            return count < myObjectThreshold ? myLowExecutor : myHighExecutor;
        }

        /**
         * Get the handler executed when objects reach a certain state.
         *
         * @return The handler.
         */
        public StateChangeHandler<E> getStateChangeHandler()
        {
            return myStateChangeHandler;
        }
    }

    /**
     * This class keeps track of objects that should change state.
     */
    private class Task implements StateController<E>
    {
        /** An optional comparator for sorting the objects. */
        private final Comparator<? super E> myComparator;

        /** The origin state. */
        private final State myFromState;

        /** The objects changing state. This must be synchronized externally. */
        private final Collection<? extends E> myObjects;

        /** The destination state. */
        private final State myToState;

        /**
         * Construct a task.
         *
         * @param objects The objects the task is processing.
         * @param fromState The origin state.
         * @param toState The destination state.
         * @param comparator An optional comparator for sorting the objects in
         *            the destination state.
         */
        public Task(Collection<? extends E> objects, State fromState, State toState, Comparator<? super E> comparator)
        {
            // Use set to speed up remove, but maintain order.
            myObjects = New.insertionOrderSet(objects);
            myFromState = fromState;
            myToState = toState;
            myComparator = comparator;
        }

        /**
         * Cancel this task completely.
         */
        public void cancelAll()
        {
            myObjects.clear();
        }

        /**
         * Cancel this task for a collection of objects.
         *
         * @param objs The objects for which to cancel the task.
         * @return If this task had the object.
         */
        public boolean cancelAll(Collection<?> objs)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Removing " + objs.size() + " objects for task [" + this + "]");
            }

            return myObjects.removeAll(objs instanceof Set<?> ? objs : New.set(objs));
        }

        @Override
        public void changeState(Collection<? extends E> objects, State toState)
        {
            ThreadedStateMachine.this.moveToState(this, objects, getToState(), toState, getComparator());
        }

        /**
         * Accessor for the comparator.
         *
         * @return The comparator.
         */
        public Comparator<? super E> getComparator()
        {
            return myComparator;
        }

        /**
         * Get the objects in this task. This must be synchronized externally.
         *
         * @return The objects.
         */
        public Collection<? extends E> getObjects()
        {
            return Collections.unmodifiableCollection(myObjects);
        }

        /**
         * Accessor for the toState.
         *
         * @return The toState.
         */
        public State getToState()
        {
            return myToState;
        }

        /**
         * Get if this task does not have any associated objects.
         *
         * @return {@code true} if this task is empty.
         */
        public boolean isEmpty()
        {
            return myObjects.isEmpty();
        }

        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer("Task [").append(Integer.toHexString(hashCode())).append(' ')
                    .append(myObjects.size()).append(' ').append(myFromState).append("->").append(myToState).append(']');
            return sb.toString();
        }

        /**
         * Accessor for the fromState.
         *
         * @return The fromState.
         */
        protected State getFromState()
        {
            return myFromState;
        }
    }
}
