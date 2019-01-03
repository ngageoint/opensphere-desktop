package io.opensphere.core.util.fx.tabpane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.opensphere.core.util.fx.tabpane.inputmap.Mapping;
import io.opensphere.core.util.fx.tabpane.inputmap.OSInputMap;
import javafx.scene.Node;

/**
 * A base class for behavior definitions on {@link Node} instances.
 *
 * @param <N> the node type for which the behavior is defined.
 */
public abstract class OSBehaviorBase<N extends Node>
{
    /** The node for which the behavior is defined. */
    private final N myNode;

    /** The mappings installed for the node. */
    private final List<Mapping<?>> myInstalledDefaultMappings;

    /** The map disposal handlers for the child nodes of {@link #myNode}. */
    private final List<Runnable> myChildInputMapDisposalHandlers;

    /**
     * Creates a new base using the supplied node.
     *
     * @param node the node for which to define the behavior.
     */
    public OSBehaviorBase(N node)
    {
        this.myNode = node;
        this.myInstalledDefaultMappings = new ArrayList<>();
        this.myChildInputMapDisposalHandlers = new ArrayList<>();
    }

    /**
     * Gets the input map defined in the behavior.
     *
     * @return the input map defined in the behavior.
     */
    public abstract OSInputMap<N> getInputMap();

    /**
     * Gets the value of the {@link #myNode} field.
     *
     * @return the value stored in the {@link #myNode} field.
     */
    public final N getNode()
    {
        return myNode;
    }

    /**
     * Disposes of all known and installed mappings, and uses the
     * {@link #myChildInputMapDisposalHandlers} to dispose of each.
     */
    public void dispose()
    {
        // when we dispose a behavior, we do NOT want to dispose the OSInputMap,
        // as that can remove input mappings that were not installed by the
        // behavior. Instead, we want to only remove mappings that the behavior
        // itself installed. This can be done by removing all input mappings
        // that were installed via the 'addDefaultMapping' method.

        // remove default mappings only
        for (Mapping<?> mapping : myInstalledDefaultMappings)
        {
            getInputMap().getMappings().remove(mapping);
        }

        // Remove all default child mappings
        for (Runnable r : myChildInputMapDisposalHandlers)
        {
            r.run();
        }
    }

    /**
     * Adds the supplied list of mappings to the default set.
     *
     * @param newMapping the list of mappings to add to the default group.
     */
    protected void addDefaultMapping(List<Mapping<?>> newMapping)
    {
        addDefaultMapping(getInputMap(), newMapping.toArray(new Mapping[newMapping.size()]));
    }

    /**
     * Adds the supplied mappings to the default set.
     *
     * @param newMapping the mappings to add to the default group.
     */
    protected void addDefaultMapping(Mapping<?>... newMapping)
    {
        addDefaultMapping(getInputMap(), newMapping);
    }

    /**
     * Adds the supplied mappings to the installed set.
     *
     * @param inputMap the input map in which to add the mappings.
     * @param newMapping the mappings to add to the input and default mappings.
     */
    protected void addDefaultMapping(OSInputMap<N> inputMap, Mapping<?>... newMapping)
    {
        // make a copy of the existing mappings, so we only check against those
        List<Mapping<?>> existingMappings = new ArrayList<>(inputMap.getMappings());

        for (Mapping<?> mapping : newMapping)
        {
            if (existingMappings.contains(mapping))
            {
                continue;
            }

            inputMap.getMappings().add(mapping);
            myInstalledDefaultMappings.add(mapping);
        }
    }

    /**
     * Adds a default map for the child maps.
     *
     * @param parentInputMap the parent map in which to store the child maps.
     * @param newChildInputMap the child input map to store.
     */
    protected <T extends Node> void addDefaultChildMap(OSInputMap<T> parentInputMap, OSInputMap<T> newChildInputMap)
    {
        parentInputMap.getChildInputMaps().add(newChildInputMap);

        myChildInputMapDisposalHandlers.add(() -> parentInputMap.getChildInputMaps().remove(newChildInputMap));
    }

    /**
     * Creates a new input map around the {@link #myNode}.
     *
     * @return a new input map wrapping the internal node.
     */
    protected OSInputMap<N> createInputMap()
    {
        return new OSInputMap<>(myNode);
    }

    /**
     * Removes the behavior mapping for the supplied key.
     *
     * @param key the key for which to remove the mapping.
     */
    protected void removeMapping(Object key)
    {
        OSInputMap<?> inputMap = getInputMap();
        inputMap.lookupMapping(key).ifPresent(mapping ->
        {
            inputMap.getMappings().remove(mapping);
            myInstalledDefaultMappings.remove(mapping);
        });
    }

    /**
     * Executes one of the supplied {@link Runnable} methods depending on the
     * node's orientation.
     *
     * @param node the node on which to test the orientation.
     * @param rtlMethod the method to run if the node is oriented right-to-left.
     * @param nonRtlMethod the method to run if the node is not oriented
     *            right-to-left.
     */
    void rtl(Node node, Runnable rtlMethod, Runnable nonRtlMethod)
    {
        switch (node.getEffectiveNodeOrientation())
        {
            case RIGHT_TO_LEFT:
                rtlMethod.run();
                break;
            default:
                nonRtlMethod.run();
                break;
        }
    }

    /**
     * Executes one of the supplied {@link Consumer} methods depending on the
     * node's orientation, making the consumer consume the supplied Object.
     *
     * @param node the node on which to test the orientation.
     * @param object the object to consume.
     * @param rtlMethod the method to run if the node is oriented right-to-left.
     * @param nonRtlMethod the method to run if the node is not oriented
     *            right-to-left.
     */
    <T> void rtl(Node node, T object, Consumer<T> rtlMethod, Consumer<T> nonRtlMethod)
    {
        switch (node.getEffectiveNodeOrientation())
        {
            case RIGHT_TO_LEFT:
                rtlMethod.accept(object);
                break;
            default:
                nonRtlMethod.accept(object);
                break;
        }
    }

    /**
     * Tests to determine if the supplied node's orientation is right-to-left.
     *
     * @param n the node to test.
     * @return <code>true</code> if the node's orientation is right-to-left.
     */
    boolean isRightToLeft(Node n)
    {
        switch (n.getEffectiveNodeOrientation())
        {
            case RIGHT_TO_LEFT:
                return true;
            default:
                return false;
        }
    }
}
