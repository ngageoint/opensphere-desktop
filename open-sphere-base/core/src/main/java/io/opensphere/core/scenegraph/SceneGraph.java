package io.opensphere.core.scenegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * This class models a scene graph, which allows objects to be organized by
 * their similarities in a tree. Similarities are determined by
 * {@link SceneGraphParameter}s associated with the objects.
 *
 * @param <E> The type of object being organized.
 * @param <T> The type of the parameter modeling the object attributes.
 */
@SuppressWarnings("PMD.GodClass")
public class SceneGraph<E, T extends SceneGraphParameter>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SceneGraph.class);

    /**
     * This is a comparator that will order classes of parameters such that the
     * class of parameter with less disparate parameters will be before the
     * class of parameter with more disparate parameters.
     */
    private final Comparator<Entry<Object, Set<SceneGraphParameter>>> myEntryComparator = (o1, o2) ->
    {
        Object type1 = o1.getKey();
        Object type2 = o2.getKey();
        if (type1.equals(type2))
        {
            return 0;
        }
        int size1 = o1.getValue().size();
        int size2 = o2.getValue().size();
        return size1 < size2 ? -1 : size1 > size2 ? 1 : 0;
    };

    /** A map of parameters to objects. */
    private final Map<SceneGraphParameter, Set<E>> myParameterMap = new HashMap<>();

    /** A map of parameter types to parameters. */
    private final Map<Object, Set<SceneGraphParameter>> myParameterTypeMap = new HashMap<>();

    /**
     * Add an object to this scene graph.
     *
     * @param obj The object to add.
     * @param parameters The parameters associated with this object.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public final void add(E obj, T... parameters)
    {
        Set<Object> missingParameterTypes = new HashSet<>(myParameterTypeMap.keySet());
        for (T parameter : parameters)
        {
            Object paramType = parameter.getType();
            Set<SceneGraphParameter> paramSet = myParameterTypeMap.get(paramType);
            if (paramSet == null)
            {
                paramSet = new HashSet<>();
                myParameterTypeMap.put(paramType, paramSet);

                // Check for objects already in the scene graph that are missing
                // this parameter.
                if (!myParameterMap.isEmpty())
                {
                    NullParameter np = new NullParameter(parameter.getType());
                    paramSet.add(np);
                    Set<E> objectSet = new HashSet<>();
                    for (Set<E> set : myParameterMap.values())
                    {
                        objectSet.addAll(set);
                    }
                    myParameterMap.put(np, objectSet);
                }
            }
            else
            {
                missingParameterTypes.remove(paramType);
            }
            paramSet.add(parameter);
        }

        Collection<SceneGraphParameter> parametersAndNulls = new ArrayList<>(
                parameters.length + missingParameterTypes.size());
        for (T parameter : parameters)
        {
            parametersAndNulls.add(parameter);
        }
        for (Object paramType : missingParameterTypes)
        {
            NullParameter np = new NullParameter(paramType);
            myParameterTypeMap.get(paramType).add(np);
            parametersAndNulls.add(np);
        }

        for (SceneGraphParameter parameter : parametersAndNulls)
        {
            Set<E> objectSet = myParameterMap.get(parameter);
            if (objectSet == null)
            {
                objectSet = new HashSet<>();
                myParameterMap.put(parameter, objectSet);
            }
            objectSet.add(obj);
        }
    }

    /**
     * Build the tree from the objects that have been added.
     *
     * @return The root node of the tree.
     */
    public GroupNode<E, T> buildTree()
    {
        List<Entry<Object, Set<SceneGraphParameter>>> list = new LinkedList<>(
                myParameterTypeMap.entrySet());

        Collections.sort(list, myEntryComparator);

        GroupNode<E, T> root = new GroupNode<>(null, null);
        Stack<GroupNode<E, T>> parentStack = new Stack<>();
        parentStack.add(root);

        if (list.isEmpty())
        {
            return root;
        }

        Map<SceneGraphParameter, Set<E>> parameterMap = new HashMap<>(myParameterMap.size());
        for (Entry<SceneGraphParameter, Set<E>> entry : myParameterMap.entrySet())
        {
            parameterMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        Stack<Iterator<SceneGraphParameter>> iterStack = new Stack<>();
        Stack<Set<E>> intersectingObjectsStack = new Stack<>();
        int level = 0;
        final int lastLevel = list.size() - 1;
        while (level >= 0)
        {
            Iterator<SceneGraphParameter> iter = getIter(list, iterStack, level);
            if (iter.hasNext())
            {
                SceneGraphParameter param = iter.next();
                Set<E> objs = parameterMap.get(param);
                Set<E> intersectingObjects = getIntersectingObjects(objs, level, intersectingObjectsStack);
                if (!intersectingObjects.isEmpty())
                {
                    Node<E, T> node;
                    GroupNode<E, T> parentNode = parentStack.get(level);
                    T paramForNode = castNode(param);
                    if (level == lastLevel)
                    {
                        LeafNode<E, T> leafNode = new LeafNode<>(parentNode, paramForNode);
                        leafNode.getObjects().addAll(intersectingObjects);
                        node = leafNode;
                        objs.removeAll(intersectingObjects);
                    }
                    else
                    {
                        intersectingObjectsStack.add(intersectingObjects);
                        GroupNode<E, T> groupNode = new GroupNode<>(parentNode, paramForNode);
                        parentStack.add(groupNode);
                        node = groupNode;
                    }
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Adding " + node.getClass().getSimpleName() + " with parameter [" + param
                                + "] and intersecting objects [" + intersectingObjects + "]");
                    }
                    if (level < lastLevel)
                    {
                        ++level;
                    }
                }
            }
            else
            {
                --level;
                parentStack.pop();
                iterStack.pop();
                if (level >= 0)
                {
                    intersectingObjectsStack.pop();
                }
            }
        }

        return root;
    }

    /**
     * Cast a parameter to P, unless it is a null parameter.
     *
     * @param param The parameter
     * @return The cast parameter or <code>null</code> if it is a null
     *         parameter.
     */
    @SuppressWarnings("unchecked")
    private T castNode(SceneGraphParameter param)
    {
        if (param instanceof NullParameter)
        {
            return null;
        }
        return (T)param;
    }

    /**
     * Intersect a set of objects with the objects from the previous level. If
     * these are level 0 objects, the objects are simply returned.
     *
     * @param objs The objects to intersect.
     * @param level The level of these objects.
     * @param intersectingObjectsStack The stack of objects at each level.
     * @return The intersecting objects.
     */
    private Set<E> getIntersectingObjects(Set<E> objs, int level, Stack<Set<E>> intersectingObjectsStack)
    {
        if (objs.isEmpty())
        {
            return objs;
        }

        Set<E> intersectingObjects;
        if (level == 0)
        {
            intersectingObjects = new HashSet<>(objs);
        }
        else
        {
            intersectingObjects = new HashSet<>(intersectingObjectsStack.get(level - 1));
            intersectingObjects.retainAll(objs);
        }
        return intersectingObjects;
    }

    /**
     * Get the iterator over the parameter set at a particular level, or create
     * one if an iterator has not been started.
     *
     * @param list The list of object-to-parameter-set entries.
     * @param iterStack The stack of iterators.
     * @param level The level of iterator desired.
     * @return A parameter iterator.
     */
    private Iterator<SceneGraphParameter> getIter(List<Entry<Object, Set<SceneGraphParameter>>> list,
            Stack<Iterator<SceneGraphParameter>> iterStack, int level)
    {
        Iterator<SceneGraphParameter> iter;
        if (iterStack.size() <= level)
        {
            iter = list.get(level).getValue().iterator();
            iterStack.add(iter);
        }
        else
        {
            iter = iterStack.get(level);
        }
        return iter;
    }

    /**
     * Group node in the graph. Group nodes own other group nodes or leaf nodes.
     *
     * @param <E> The type of object being organized.
     * @param <T> The type of the parameter modeling the object attributes.
     */
    public static class GroupNode<E, T> extends Node<E, T>
    {
        /** The child nodes of this group node. */
        private final List<Node<E, T>> myNodes = new ArrayList<>();

        /**
         * Construct a group node.
         *
         * @param parent the parent of this node (<code>null</code> for the root
         *            node)
         * @param parameter the parameter of this group node (<code>null</code>
         *            for the root node)
         */
        public GroupNode(GroupNode<E, T> parent, T parameter)
        {
            super(parent, parameter);
        }

        /**
         * Add a node to this group.
         *
         * @param node The new node.
         */
        public void addNode(Node<E, T> node)
        {
            myNodes.add(node);
        }

        @Override
        public List<LeafNode<E, T>> getLeafNodes()
        {
            List<LeafNode<E, T>> leafNodes = new ArrayList<>();
            for (Node<E, T> node : myNodes)
            {
                leafNodes.addAll(node.getLeafNodes());
            }
            return leafNodes;
        }

        @Override
        public List<Node<E, T>> getNodes()
        {
            return new ArrayList<>(myNodes);
        }
    }

    /**
     * Leaf node in the graph. Leaf nodes own the objects.
     *
     * @param <E> The type of object being organized.
     * @param <T> The type of the parameter modeling the object attributes.
     */
    public static class LeafNode<E, T> extends Node<E, T>
    {
        /** The objects attached to this node. */
        private final List<E> myObjects = new ArrayList<>();

        /**
         * Construct a leaf node.
         *
         * @param parent the parent
         * @param parameter the parameter
         */
        public LeafNode(GroupNode<E, T> parent, T parameter)
        {
            super(parent, parameter);
        }

        @Override
        public List<LeafNode<E, T>> getLeafNodes()
        {
            return Collections.singletonList(this);
        }

        @Override
        public List<E> getObjects()
        {
            return myObjects;
        }
    }

    /**
     * Abstract superclass for leaf and group nodes.
     *
     * @param <E> The type of object being organized.
     * @param <T> The type of the parameter modeling the object attributes.
     */
    public abstract static class Node<E, T>
    {
        /** The parameter for this node. */
        private final T myParameter;

        /** The parent of this node. */
        private final GroupNode<E, T> myParent;

        /**
         * Construct the node.
         *
         * @param parent the parent of this node (<code>null</code> for the root
         *            node)
         * @param parameter the parameter of this group node (<code>null</code>
         *            for the root node)
         */
        public Node(GroupNode<E, T> parent, T parameter)
        {
            myParameter = parameter;
            myParent = parent;
            if (parent != null)
            {
                parent.addNode(this);
            }
        }

        /**
         * Get all leaf nodes attached to this node, including those owned by
         * its descendants.
         *
         * @return the leaf nodes
         */
        public abstract List<LeafNode<E, T>> getLeafNodes();

        /**
         * Get the immediate child nodes of this node.
         *
         * @return the child nodes (empty for non-group nodes)
         */
        public List<Node<E, T>> getNodes()
        {
            return Collections.emptyList();
        }

        /**
         * Get the objects attached to this node.
         *
         * @return the objects (empty for non-leaf nodes)
         */
        public List<E> getObjects()
        {
            return Collections.emptyList();
        }

        /**
         * Get the parameter associated with this node.
         *
         * @return the parameter
         */
        public T getParameter()
        {
            return myParameter;
        }

        /**
         * Get all parameters associated with this node including the parameters
         * for its ancestors.
         *
         * @return the parameter set
         */
        public Set<T> getParameters()
        {
            Set<T> parameters = new HashSet<>();
            if (myParameter != null)
            {
                parameters.add(myParameter);
            }
            if (myParent != null)
            {
                parameters.addAll(myParent.getParameters());
            }
            return parameters;
        }
    }

    /**
     * Null parameter to use for objects that are supplied without the full
     * complement of parameters.
     */
    private static class NullParameter implements SceneGraphParameter
    {
        /** The type of the null parameter. */
        private final Object myType;

        /**
         * Construct the null parameter.
         *
         * @param type The type for the parameter.
         */
        public NullParameter(Object type)
        {
            myType = type;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            NullParameter other = (NullParameter)obj;
            return Objects.equals(myType, other.myType);
        }

        @Override
        public Object getType()
        {
            return myType;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myType == null ? 0 : myType.hashCode());
            return result;
        }
    }
}
