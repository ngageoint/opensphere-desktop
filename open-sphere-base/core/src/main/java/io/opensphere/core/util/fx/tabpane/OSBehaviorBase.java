package io.opensphere.core.util.fx.tabpane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.opensphere.core.util.fx.tabpane.inputmap.Mapping;
import io.opensphere.core.util.fx.tabpane.inputmap.OSInputMap;
import javafx.scene.Node;

/**
 *
 */
public abstract class OSBehaviorBase<N extends Node>
{
    private final N node;

    private final List<Mapping<?>> installedDefaultMappings;

    private final List<Runnable> childInputMapDisposalHandlers;

    public OSBehaviorBase(N node)
    {
        this.node = node;
        this.installedDefaultMappings = new ArrayList<>();
        this.childInputMapDisposalHandlers = new ArrayList<>();
    }

    public abstract OSInputMap<N> getInputMap();

    public final N getNode()
    {
        return node;
    }

    public void dispose()
    {
        // when we dispose a behavior, we do NOT want to dispose the OSInputMap,
        // as that can remove input mappings that were not installed by the
        // behavior. Instead, we want to only remove mappings that the behavior
        // itself installed. This can be done by removing all input mappings
        // that
        // were installed via the 'addDefaultMapping' method.

        // remove default mappings only
        for (Mapping<?> mapping : installedDefaultMappings)
        {
            getInputMap().getMappings().remove(mapping);
        }

        // Remove all default child mappings
        for (Runnable r : childInputMapDisposalHandlers)
        {
            r.run();
        }

//        OSInputMap<N> inputMap = getInputMap();
//        if (inputMap != null) {
//            inputMap.dispose();
//        }
    }

    protected void addDefaultMapping(List<Mapping<?>> newMapping)
    {
        addDefaultMapping(getInputMap(), newMapping.toArray(new Mapping[newMapping.size()]));
    }

    protected void addDefaultMapping(Mapping<?>... newMapping)
    {
        addDefaultMapping(getInputMap(), newMapping);
    }

    protected void addDefaultMapping(OSInputMap<N> inputMap, Mapping<?>... newMapping)
    {
        // make a copy of the existing mappings, so we only check against those
        List<Mapping<?>> existingMappings = new ArrayList<>(inputMap.getMappings());

        for (Mapping<?> mapping : newMapping)
        {
            // check if a mapping already exists, and if so, do not add this
            // mapping
            // TODO this is insufficient as we need to check entire OSInputMap
            // hierarchy
//            for (Mapping<?> existingMapping : existingMappings) {
//                if (existingMapping != null && existingMapping.equals(mapping)) {
//                    return;
//                }
//            }
            if (existingMappings.contains(mapping))
            {
                continue;
            }

            inputMap.getMappings().add(mapping);
            installedDefaultMappings.add(mapping);
        }
    }

    protected <T extends Node> void addDefaultChildMap(OSInputMap<T> parentInputMap, OSInputMap<T> newChildInputMap)
    {
        parentInputMap.getChildInputMaps().add(newChildInputMap);

        childInputMapDisposalHandlers.add(() -> parentInputMap.getChildInputMaps().remove(newChildInputMap));
    }

    protected OSInputMap<N> createInputMap()
    {
        // TODO re-enable when OSInputMap moves back to Node / Control
//        return node.getInputMap() != null ?
//                (OSInputMap<N>)node.getInputMap() :
//                new OSInputMap<>(node);
        return new OSInputMap<>(node);
    }

    protected void removeMapping(Object key)
    {
        OSInputMap<?> inputMap = getInputMap();
        inputMap.lookupMapping(key).ifPresent(mapping ->
        {
            inputMap.getMappings().remove(mapping);
            installedDefaultMappings.remove(mapping);
        });
    }

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

    boolean isRTL(Node n)
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
