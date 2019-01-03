package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 * OSInputMap is a class that is set on a given {@link Node}. When the Node
 * receives an input event from the system, it passes this event in to the
 * OSInputMap where the OSInputMap can check all installed {@link Mapping
 * mappings} to see if there is any suitable mapping, and if so, fire the
 * provided {@link EventHandler}.
 *
 * @param <N> The type of the Node that the OSInputMap is installed in.
 */
public class OSInputMap<N extends Node> implements EventHandler<Event>
{
    /** The node to which the input map is attached. */
    private final N myNode;

    /** An observable list of the input mappings defined for child nodes. */
    private final ObservableList<OSInputMap<N>> myChildInputMaps;

    /** The mappings contained within the input map. */
    private final ObservableList<Mapping<?>> myMappings;

    /**
     * The map of the installed event handlers, using the event type as the key,
     * and the list of handlers as the value.
     */
    private final Map<EventType<?>, List<EventHandler<? super Event>>> myInstalledEventHandlers;

    /**
     * A dictionary of installed mappings bound to the event types and mappings.
     */
    private final Map<EventType<?>, List<Mapping<?>>> myEventTypeMappings;

    /** The input map of the parent node. */
    private ReadOnlyObjectWrapper<OSInputMap<N>> myParentInputMap = new ReadOnlyObjectWrapper<>(this, "parentInputMap")
    {
        @Override
        protected void invalidated()
        {
            // whenever the parent OSInputMap changes, we uninstall all
            // mappings and then reprocess them so that they are installed in
            // the correct root.
            reprocessAllMappings();
        }
    };

    /**
     * The role of the interceptor is to block the OSInputMap on which it is set
     * from executing any myMappings (contained within itself, or within a
     * {@link #getChildInputMaps() child OSInputMap}, whenever the interceptor
     * returns true. The interceptor is called every time an input event is
     * received, and is allowed to reason on the given input event before
     * returning a boolean value, where boolean true means block execution, and
     * boolean false means to allow execution.
     */
    private ObjectProperty<Predicate<? extends Event>> myInterceptor = new SimpleObjectProperty<>(this, "interceptor");

    /**
     * Creates the new OSInputMap instance which is related specifically to the
     * given Node.
     *
     * @param node The Node for which this OSInputMap is attached.
     */
    public OSInputMap(N node)
    {
        if (node == null)
        {
            throw new IllegalArgumentException("Node can not be null");
        }

        this.myNode = node;
        this.myEventTypeMappings = new HashMap<>();
        this.myInstalledEventHandlers = new HashMap<>();
        // this.interceptors = FXCollections.observableArrayList();

        // listeners
        this.myMappings = FXCollections.observableArrayList();
        myMappings.addListener((ListChangeListener<Mapping<?>>)c ->
        {
            while (c.next())
            {
                // TODO handle mapping removal
                if (c.wasRemoved())
                {
                    for (Mapping<?> mapping : c.getRemoved())
                    {
                        removeMapping(mapping);
                    }
                }

                if (c.wasAdded())
                {
                    List<Mapping<?>> toRemove = new ArrayList<>();
                    for (Mapping<?> mapping : c.getAddedSubList())
                    {
                        if (mapping == null)
                        {
                            toRemove.add(null);
                        }
                        else
                        {
                            addMapping(mapping);
                        }
                    }

                    if (!toRemove.isEmpty())
                    {
                        getMappings().removeAll(toRemove);
                        throw new IllegalArgumentException("Null myMappings not permitted");
                    }
                }
            }
        });

        myChildInputMaps = FXCollections.observableArrayList();
        myChildInputMaps.addListener((ListChangeListener<OSInputMap<N>>)c ->
        {
            while (c.next())
            {
                if (c.wasRemoved())
                {
                    for (OSInputMap<N> map : c.getRemoved())
                    {
                        map.setParentInputMap(null);
                    }
                }

                if (c.wasAdded())
                {
                    List<OSInputMap<N>> toRemove = new ArrayList<>();
                    for (OSInputMap<N> map : c.getAddedSubList())
                    {
                        // we check that the child input map maps to the same
                        // node
                        // as this input map
                        if (map.getNode() != getNode())
                        {
                            toRemove.add(map);
                        }
                        else
                        {
                            map.setParentInputMap(this);
                        }
                    }

                    if (!toRemove.isEmpty())
                    {
                        getChildInputMaps().removeAll(toRemove);
                        throw new IllegalArgumentException("Child OSInputMap intances need to share a common Node object");
                    }
                }
            }
        });
    }

    /**
     * Adds the supplied event type to the input map.
     *
     * @param et the type to add to the input map.
     */
    private void addEventHandler(EventType<?> et)
    {
        List<EventHandler<? super Event>> eventHandlers = myInstalledEventHandlers.computeIfAbsent(et, f -> new ArrayList<>());

        final EventHandler<? super Event> eventHandler = this::handle;

        if (eventHandlers.isEmpty())
        {
            myNode.addEventHandler(et, eventHandler);
        }

        // We need to store these event handlers so we can dispose cleanly.
        eventHandlers.add(eventHandler);
    }

    /**
     * Adds the supplied mapping to this instance.
     *
     * @param mapping the mapping to add to this instance.
     */
    private void addMapping(Mapping<?> mapping)
    {
        OSInputMap<N> rootInputMap = getRootInputMap();

        // we want to track the event handlers we install, so that we can clean
        // up in the dispose() method (and also so that we don't duplicate
        // event handlers for a single event type). Because this is all handled
        // in the root OSInputMap, we firstly find it, and then we defer to it.
        rootInputMap.addEventHandler(mapping.myEventType);

        // we maintain a separate map of all myMappings, which maps from the
        // mapping event type into a list of myMappings. This allows for easier
        // iteration in the lookup methods.
        EventType<?> et = mapping.getEventType();
        List<Mapping<?>> _eventTypeMappings = this.myEventTypeMappings.computeIfAbsent(et, f -> new ArrayList<>());
        _eventTypeMappings.add(mapping);
    }

    /**
     * Disposes all child InputMaps, removes all event handlers from the Node,
     * and clears the myMappings list.
     */
    public void dispose()
    {
        for (OSInputMap<N> childInputMap : getChildInputMaps())
        {
            childInputMap.dispose();
        }

        // uninstall event handlers
        removeAllEventHandlers();

        // clear out all myMappings
        getMappings().clear();
    }

    /**
     * A mutable list of child InputMaps. An OSInputMap may have child input
     * maps, as this allows for easy addition of myMappings that are
     * state-specific. For example, if a Node can be in two different states,
     * and the input myMappings are different for each, then it makes sense to
     * have one root (and empty) OSInputMap, with two children input maps, where
     * each is populated with the specific input myMappings for one of the two
     * states. To prevent the wrong input map from being considered, it is
     * simply a matter of setting an appropriate {@link #interceptorProperty()
     * myInterceptor} on each map, so that they are only considered in one of
     * the two states.
     *
     * @return A mutable list of child InputMaps.
     */
    public ObservableList<OSInputMap<N>> getChildInputMaps()
    {
        return myChildInputMaps;
    }

    /**
     * Gets the property in which the interceptor is stored.
     *
     * @return the property in which the interceptor is stored.
     */
    public final ObjectProperty<Predicate<? extends Event>> interceptorProperty()
    {
        return myInterceptor;
    }

    /**
     * Gets the value of the {@link #myInterceptor} field.
     *
     * @return the value stored in the {@link #myInterceptor} field.
     */
    public final Predicate<? extends Event> getInterceptor()
    {
        return myInterceptor.get();
    }

    /**
     * Sets the value of the {@link #myInterceptor} field.
     *
     * @param value the value to store in the {@link #myInterceptor} field.
     */
    public final void setInterceptor(Predicate<? extends Event> value)
    {
        myInterceptor.set(value);
    }

    /**
     * A mutable list of input mappings. Each will be considered whenever an
     * input event is being looked up, and one of which may be used to handle
     * the input event, based on the specificity returned by each mapping (that
     * is, the mapping with the highest specificity wins).
     *
     * @return A mutable list of input mappings.
     */
    public ObservableList<Mapping<?>> getMappings()
    {
        return myMappings;
    }

    /**
     * Gets the Node for which this OSInputMap is attached.
     *
     * @return the Node for which this OSInputMap is attached.
     */
    public final N getNode()
    {
        return myNode;
    }

    /**
     * Gets the value of the {@link #myParentInputMap} field.
     *
     * @return the value stored in the {@link #myParentInputMap} field.
     */
    private final OSInputMap<N> getParentInputMap()
    {
        return myParentInputMap.get();
    }

    /**
     * Gets the input map of the root node in the scene graph.
     *
     * @return the input map of the root node in the scene graph.
     */
    private OSInputMap<N> getRootInputMap()
    {
        OSInputMap<N> rootInputMap = this;
        while (true)
        {
            OSInputMap<N> parentInputMap = rootInputMap.getParentInputMap();
            if (parentInputMap == null)
            {
                break;
            }
            rootInputMap = parentInputMap;
        }
        return rootInputMap;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.event.EventHandler#handle(javafx.event.Event)
     */
    @Override
    public void handle(Event e)
    {
        if (e == null || e.isConsumed())
        {
            return;
        }

        List<Mapping<?>> mappings = lookup(e, true);
        for (Mapping<?> mapping : mappings)
        {
            EventHandler<Event> eventHandler = (EventHandler<Event>)mapping.getEventHandler();
            if (eventHandler != null)
            {
                eventHandler.handle(e);
            }

            if (mapping.isAutoConsume())
            {
                e.consume();
            }

            if (e.isConsumed())
            {
                break;
            }

            // If we are here, the event has not been consumed, so we continue
            // looping through our list of matches. Refer to the documentation
            // in lookup(Event) for more details on the list ordering.
        }
    }

    /**
     * Returns a List of Mapping instances, in priority order (from highest
     * priority to lowest priority). All myMappings in the list have the same
     * value specificity, so are ranked based on the input map (with the leaf
     * input maps taking precedence over parent / root input maps).
     *
     * @param event the event for which to search.
     * @param testInterceptors <code>true</code> to force the search to test the
     *            interceptors for results.
     * @return a list of {@link Mapping}s matching the supplied event.
     */
    private List<Mapping<?>> lookup(Event event, boolean testInterceptors)
    {
        // firstly we look at ourselves to see if we have a mapping, assuming
        // our interceptors are valid
        if (testInterceptors)
        {
            boolean interceptorsApplies = testInterceptor(event, (Predicate<Event>)getInterceptor());

            if (interceptorsApplies)
            {
                return Collections.emptyList();
            }
        }

        List<Mapping<?>> mappings = new ArrayList<>();

        int minSpecificity = 0;
        List<Pair<Integer, Mapping<?>>> results = lookupMappingAndSpecificity(event, minSpecificity);
        if (!results.isEmpty())
        {
            minSpecificity = results.get(0).getKey();
            mappings.addAll(results.stream().map(pair -> pair.getValue()).collect(Collectors.toList()));
        }

        // but we always descend into our child input maps as well, to see if
        // there is a more specific mapping there. If there is a mapping of
        // equal specificity, we take the child mapping over the parent mapping.
        for (int i = 0; i < getChildInputMaps().size(); i++)
        {
            OSInputMap<?> childInputMap = getChildInputMaps().get(i);
            minSpecificity = scanRecursively(childInputMap, event, testInterceptors, minSpecificity, mappings);
        }

        return mappings;
    }

    /**
     * Looks up the most specific mapping given the input, ignoring all
     * interceptors. The valid values that can be passed into this method is
     * based on the values returned by the {@link Mapping#getMappingKey()}
     * method. Based on the subclasses of Mapping that ship with JavaFX, the
     * valid values are therefore:
     *
     * <ul>
     * <li><strong>KeyMapping:</strong> A valid {@link OSKeyBinding}.</li>
     * <li><strong>MouseMapping:</strong> A valid {@link MouseEvent} event type
     * (e.g. {@code MouseEvent.MOUSE_PRESSED}).</li>
     * </ul>
     *
     * For other Mapping subclasses, refer to their javadoc, and specifically
     * what is returned by {@link Mapping#getMappingKey()},
     *
     * @param mappingKey the key for which to search.
     * @return an {@link Optional} instance containing mappings that match the
     *         supplied key.
     */
    public Optional<Mapping<?>> lookupMapping(Object mappingKey)
    {
        if (mappingKey == null)
        {
            return Optional.empty();
        }

        List<Mapping<?>> mappings = lookupMappingKey(mappingKey);

        // descend into our child input maps as well
        for (int i = 0; i < getChildInputMaps().size(); i++)
        {
            OSInputMap<N> childInputMap = getChildInputMaps().get(i);

            List<Mapping<?>> childMappings = childInputMap.lookupMappingKey(mappingKey);
            mappings.addAll(0, childMappings);
        }

        return mappings.size() > 0 ? Optional.of(mappings.get(0)) : Optional.empty();
    }

    /**
     * Searches for mappings, and generates a list of mappings and
     * specificities.
     *
     * @param event the event for which to search.
     * @param minSpecificity the minimum specificity to qualify as a search
     *            result.
     * @return a list of paired mappings and specificities.
     */
    private List<Pair<Integer, Mapping<?>>> lookupMappingAndSpecificity(final Event event, final int minSpecificity)
    {
        int _minSpecificity = minSpecificity;

        List<Mapping<?>> mappings = this.myEventTypeMappings.getOrDefault(event.getEventType(), Collections.emptyList());
        List<Pair<Integer, Mapping<?>>> result = new ArrayList<>();
        for (Mapping<?> mapping : mappings)
        {
            if (mapping.isDisabled())
            {
                continue;
            }

            // test if mapping has an interceptor that will block this event.
            // Interceptors return true if the interception should occur.
            boolean interceptorsApplies = testInterceptor(event, (Predicate<Event>)mapping.getInterceptor());
            if (interceptorsApplies)
            {
                continue;
            }

            int specificity = mapping.getSpecificity(event);
            if (specificity > 0 && specificity == _minSpecificity)
            {
                result.add(new Pair<>(specificity, mapping));
            }
            else if (specificity > _minSpecificity)
            {
                result.clear();
                result.add(new Pair<>(specificity, mapping));
                _minSpecificity = specificity;
            }
        }

        return result;
    }

    /**
     * Searches for the mappings associated with the supplied key.
     *
     * @param mappingKey the key for which to search.
     * @return the mappings associated with the supplied key, empty if none
     *         found.
     */
    private List<Mapping<?>> lookupMappingKey(Object mappingKey)
    {
        return getMappings().stream().filter(mapping -> !mapping.isDisabled())
                .filter(mapping -> mappingKey.equals(mapping.getMappingKey())).collect(Collectors.toList());
    }

    /** Removes all event handlers from the input map. */
    private void removeAllEventHandlers()
    {
        for (EventType<?> et : myInstalledEventHandlers.keySet())
        {
            List<EventHandler<? super Event>> handlers = myInstalledEventHandlers.get(et);
            for (EventHandler<? super Event> handler : handlers)
            {
                // System.out.println("Removed event handler for type " + et);
                myNode.removeEventHandler(et, handler);
            }
        }
    }

    /**
     * Removes the supplied mapping from the input map.
     *
     * @param mapping the mapping to remove.
     */
    private void removeMapping(Mapping<?> mapping)
    {
        EventType<?> et = mapping.getEventType();
        if (this.myEventTypeMappings.containsKey(et))
        {
            List<?> _eventTypeMappings = this.myEventTypeMappings.get(et);
            _eventTypeMappings.remove(mapping);
        }
    }

    /**
     * Removes all event handlers and re-adds them. Package-level visibility to
     * avoid creation of synthetic accessors.
     */
    void reprocessAllMappings()
    {
        removeAllEventHandlers();
        this.myMappings.stream().forEach(this::addMapping);

        // now do the same for all children
        for (OSInputMap<N> child : getChildInputMaps())
        {
            child.reprocessAllMappings();
        }
    }

    /**
     * Recursively searches for the supplied mappings in the input map.
     *
     * @param inputMap the map in which to search.
     * @param event the event type for which to test.
     * @param testInterceptors a flag to force the testing of interceptors.
     * @param minSpecificity the minimum specificity on which to consider a
     *            match.
     * @param mappings the mappings that matched the search.
     * @return the minimum specificity matched during the search.
     */
    private int scanRecursively(OSInputMap<?> inputMap, Event event, boolean testInterceptors, int minSpecificity,
            List<Mapping<?>> mappings)
    {
        // test if the childInputMap should be considered
        if (testInterceptors)
        {
            boolean interceptorsApplies = testInterceptor(event, (Predicate<Event>)inputMap.getInterceptor());
            if (interceptorsApplies)
            {
                return minSpecificity;
            }
        }

        // look at the given OSInputMap
        List<Pair<Integer, Mapping<?>>> childResults = inputMap.lookupMappingAndSpecificity(event, minSpecificity);
        if (!childResults.isEmpty())
        {
            int specificity = childResults.get(0).getKey();
            List<Mapping<?>> childMappings = childResults.stream().map(pair -> pair.getValue()).collect(Collectors.toList());
            if (specificity == minSpecificity)
            {
                mappings.addAll(0, childMappings);
            }
            else if (specificity > minSpecificity)
            {
                mappings.clear();
                minSpecificity = specificity;
                mappings.addAll(childMappings);
            }
        }

        // now look at the children of this input map, if any exist
        for (int i = 0; i < inputMap.getChildInputMaps().size(); i++)
        {
            minSpecificity = scanRecursively(inputMap.getChildInputMaps().get(i), event, testInterceptors, minSpecificity,
                    mappings);
        }

        return minSpecificity;
    }

    /**
     * Sets the value of the {@link #myParentInputMap} field.
     *
     * @param value the value to store in the {@link #myParentInputMap} field.
     */
    private final void setParentInputMap(OSInputMap<N> value)
    {
        myParentInputMap.set(value);
    }

    /**
     * Tests the interceptors to determine if interception should occur.
     *
     * @param e the event to test.
     * @param interceptor the interceptor to test.
     * @return <code>true</code> if the interception should occur.
     */
    private boolean testInterceptor(Event e, Predicate<Event> interceptor)
    {
        return interceptor != null && interceptor.test(e);
    }
}
