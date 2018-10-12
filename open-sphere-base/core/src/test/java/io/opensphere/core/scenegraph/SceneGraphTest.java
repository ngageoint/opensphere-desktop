package io.opensphere.core.scenegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.scenegraph.SceneGraph.GroupNode;
import io.opensphere.core.scenegraph.SceneGraph.LeafNode;
import io.opensphere.core.scenegraph.SceneGraph.Node;

/**
 * Test for {@link SceneGraph}.
 */
public class SceneGraphTest
{
    /**
     * Test a scene graph with zero levels (no geometries).
     */
    @Test
    public void test0Levels()
    {
        SceneGraph<TestGeometry, Param1> sceneGraph = new SceneGraph<>();

        GroupNode<TestGeometry, Param1> root = sceneGraph.buildTree();
        assertNotNull(root);
        List<Node<TestGeometry, Param1>> nodes = root.getNodes();
        assertEquals(0, nodes.size());
    }

    /**
     * Test a scene graph with one level of parameters. Add three test
     * geometries to the graph, all with type1 parameters. Two of them have a
     * "3" parameter and the other one has a "4" parameter. The two geometries
     * with a "3" parameter should be grouped on one node, and the one geometry
     * with a "4" parameter should be on a node by itself.
     */
    @Test
    public void test1Level()
    {
        SceneGraph<TestGeometry, Param1> sceneGraph = new SceneGraph<>();
        TestGeometry geo1 = new TestGeometry("geo1");
        TestGeometry geo2 = new TestGeometry("geo2");
        TestGeometry geo3 = new TestGeometry("geo3");
        sceneGraph.add(geo1, new Param1(3));
        sceneGraph.add(geo2, new Param1(4));
        sceneGraph.add(geo3, new SameAsParam1(3));

        GroupNode<TestGeometry, Param1> root = sceneGraph.buildTree();
        assertNotNull(root);
        List<Node<TestGeometry, Param1>> nodes = root.getNodes();
        assertEquals(2, nodes.size());
        Node<TestGeometry, Param1> node0 = nodes.get(0);
        assertEquals(new Param1(3), node0.getParameter());
        List<TestGeometry> geometries0 = node0.getObjects();
        assertEquals(2, geometries0.size());
        assertTrue(geometries0.contains(geo1));
        assertTrue(geometries0.contains(geo3));

        Node<TestGeometry, Param1> node1 = nodes.get(1);
        assertEquals(new Param1(4), node1.getParameter());
        List<TestGeometry> geometries1 = node1.getObjects();
        assertEquals(1, geometries1.size());
        assertTrue(geometries1.contains(geo2));

        List<LeafNode<TestGeometry, Param1>> leafNodes = root.getLeafNodes();
        assertNotNull(leafNodes);
        assertEquals(2, leafNodes.size());
        for (LeafNode<TestGeometry, Param1> leafNode : leafNodes)
        {
            Set<Param1> parameters = leafNode.getParameters();
            assertEquals(1, parameters.size());
            Param1 param = parameters.iterator().next();
            switch (param.getValue())
            {
                case 3:
                    assertTrue(leafNode.getObjects().contains(geo1));
                    assertTrue(leafNode.getObjects().contains(geo3));
                    break;
                case 4:
                    assertTrue(leafNode.getObjects().contains(geo2));
                    break;
                default:
                    fail("Unexpected parameter value.");
                    break;
            }
        }
    }

    /**
     * Test a scene graph with three levels of parameters.
     */
    @Test
    public void test3Levels()
    {
        SceneGraph<TestGeometry, Param1> sceneGraph = new SceneGraph<>();
        TestGeometry geo1 = new TestGeometry("geo1");
        TestGeometry geo2 = new TestGeometry("geo2");
        TestGeometry geo3 = new TestGeometry("geo3");
        TestGeometry geo4 = new TestGeometry("geo4");
        TestGeometry geo5 = new TestGeometry("geo5");
        TestGeometry geo6 = new TestGeometry("geo6");
        TestGeometry geo7 = new TestGeometry("geo7");
        TestGeometry geo8 = new TestGeometry("geo8");
        TestGeometry geo9 = new TestGeometry("geo9");

        sceneGraph.add(geo1, new Param1(3), new Param2(1), new Param3(4));
        sceneGraph.add(geo2, new SameAsParam1(4), new Param2(2), new Param3(4));

        // test two parameters of the same type
        sceneGraph.add(geo3, new Param1(4), new Param2(3), new Param3(5), new Param2(4));

        sceneGraph.add(geo4, new Param1(5), new Param2(4), new Param3(4));
        sceneGraph.add(geo5, new SameAsParam1(5), new Param2(5), new Param3(4));
        sceneGraph.add(geo6, new Param1(5), new Param2(6), new Param3(4));
        sceneGraph.add(geo7, new Param1(5), new Param2(6), new Param3(4));
        sceneGraph.add(geo8, new Param1(5), new Param2(6), new Param3(4));

        sceneGraph.add(geo9, new Param2(5));

        /**
         * <tt>
         * Expected tree:
         *
         *             Param3(4)                                       Param3(5)           null
         *        ---------|-----------------                             |                 |
         *       /         |                 \                            |                 |
         *    Param1(3) Param1(4)         Param1(5)                    Param1(4)           null
         *       |         |           -------|----------           ------|------           |
         *       |         |          /       |          \         /             \          |
         *    Param2(1) Param2(2) Param2(4) Param2(5) Param2(6)  Param2(3)     Param2(4) Param2(5)
         *       |         |          |       |           |        |             |          |
         *      geo1      geo2       geo4   geo5       geo6,7,8   geo3          geo3       geo9
         * </tt>
         *
         */
        GroupNode<TestGeometry, Param1> root = sceneGraph.buildTree();
        assertNotNull(root);
        List<Node<TestGeometry, Param1>> nodes = root.getNodes();
        assertEquals(3, nodes.size());

        Node<TestGeometry, Param1> level0node0 = findNodeWithParameter(nodes, new Param3(4));
        Node<TestGeometry, Param1> level0node1 = findNodeWithParameter(nodes, new Param3(5));
        Node<TestGeometry, Param1> level0node2 = findNodeWithParameter(nodes, null);

        nodes = level0node0.getNodes();
        assertEquals(3, nodes.size());
        Node<TestGeometry, Param1> level1node0 = findNodeWithParameter(nodes, new Param1(3));
        Node<TestGeometry, Param1> level1node1 = findNodeWithParameter(nodes, new Param1(4));
        Node<TestGeometry, Param1> level1node2 = findNodeWithParameter(nodes, new Param1(5));

        nodes = level0node1.getNodes();
        assertEquals(1, nodes.size());
        Node<TestGeometry, Param1> level1node3 = nodes.get(0);

        assertEquals(new Param1(4), level1node3.getParameter());

        nodes = level0node2.getNodes();
        assertEquals(1, nodes.size());
        Node<TestGeometry, Param1> level1node4 = nodes.get(0);

        assertNull(level1node4.getParameter());

        nodes = level1node0.getNodes();
        assertEquals(1, nodes.size());
        Node<TestGeometry, Param1> level2node0 = nodes.get(0);

        nodes = level1node1.getNodes();
        assertEquals(1, nodes.size());
        Node<TestGeometry, Param1> level2node1 = nodes.get(0);

        nodes = level1node2.getNodes();
        assertEquals(3, nodes.size());
        Node<TestGeometry, Param1> level2node2 = findNodeWithParameter(nodes, new Param2(4));
        Node<TestGeometry, Param1> level2node3 = findNodeWithParameter(nodes, new Param2(5));
        Node<TestGeometry, Param1> level2node4 = findNodeWithParameter(nodes, new Param2(6));

        nodes = level1node3.getNodes();
        assertEquals(2, nodes.size());
        Node<TestGeometry, Param1> level2node5 = findNodeWithParameter(nodes, new Param2(3));
        Node<TestGeometry, Param1> level2node6 = findNodeWithParameter(nodes, new Param2(4));

        nodes = level1node4.getNodes();
        assertEquals(1, nodes.size());
        Node<TestGeometry, Param1> level2node7 = nodes.get(0);

        assertEquals(new Param2(1), level2node0.getParameter());
        assertEquals(new Param2(2), level2node1.getParameter());
        assertEquals(new Param2(4), level2node2.getParameter());
        assertEquals(new Param2(5), level2node3.getParameter());
        assertEquals(new Param2(6), level2node4.getParameter());
        assertEquals(new Param2(3), level2node5.getParameter());
        assertEquals(new Param2(4), level2node6.getParameter());
        assertEquals(new Param2(5), level2node7.getParameter());

        List<TestGeometry> geometries = level2node0.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo1));

        geometries = level2node1.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo2));

        geometries = level2node2.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo4));

        geometries = level2node3.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo5));

        geometries = level2node4.getObjects();
        assertEquals(3, geometries.size());
        assertTrue(geometries.contains(geo6));
        assertTrue(geometries.contains(geo7));
        assertTrue(geometries.contains(geo8));

        geometries = level2node5.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo3));

        geometries = level2node6.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo3));

        geometries = level2node7.getObjects();
        assertEquals(1, geometries.size());
        assertTrue(geometries.contains(geo9));

        List<LeafNode<TestGeometry, Param1>> leafNodes = root.getLeafNodes();
        assertNotNull(leafNodes);
        assertEquals(8, leafNodes.size());
        for (LeafNode<TestGeometry, Param1> leafNode : leafNodes)
        {
            Set<Param1> parameters = leafNode.getParameters();
            geometries = leafNode.getObjects();
            if (geometries.contains(geo1))
            {
                assertEquals(1, geometries.size());
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(3), new Param2(1), new Param3(4))));
            }
            else if (geometries.contains(geo2))
            {
                assertEquals(1, geometries.size());
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(4), new Param2(2), new Param3(4))));
            }
            else if (geometries.contains(geo3))
            {
                assertEquals(1, geometries.size());
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(4), new Param3(5))));
                assertTrue(parameters.contains(new Param2(3)) || parameters.contains(new Param2(4)));
            }
            else if (geometries.contains(geo4))
            {
                assertEquals(1, geometries.size());
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(5), new Param2(4), new Param3(4))));
            }
            else if (geometries.contains(geo5))
            {
                assertEquals(1, geometries.size());
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(5), new Param2(5), new Param3(4))));
            }
            else if (geometries.contains(geo6))
            {
                assertEquals(3, geometries.size());
                assertTrue(geometries.contains(geo7));
                assertTrue(geometries.contains(geo8));
                assertEquals(3, parameters.size());
                assertTrue(parameters.containsAll(Arrays.asList(new Param1(5), new Param2(6), new Param3(4))));
            }
            else if (geometries.contains(geo9))
            {
                assertEquals(1, geometries.size());
                assertEquals(1, parameters.size());
                assertTrue(parameters.contains(new Param2(5)));
            }
            else
            {
                fail("Unexpected node.");
            }
        }
    }

    /**
     * Find the node whose parameter equals the object passed in.
     *
     * @param nodes The nodes to search.
     * @param parameter The parameter.
     * @return The found node, or {@code null}.
     */
    private Node<TestGeometry, Param1> findNodeWithParameter(List<Node<TestGeometry, Param1>> nodes, Object parameter)
    {
        for (Node<TestGeometry, Param1> node : nodes)
        {
            if (Objects.equals(node.getParameter(), parameter))
            {
                return node;
            }
        }
        Assert.fail();
        return null;
    }

    /** A parameter for the scene graph, with a type of "type1". */
    private static class Param1 implements SceneGraphParameter
    {
        /** The value of the parameter. */
        private final int myValue;

        /**
         * Construct the parameter.
         *
         * @param value The value of the parameter.
         */
        public Param1(int value)
        {
            myValue = value;
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
            if (!(obj instanceof Param1))
            {
                return false;
            }
            Param1 other = (Param1)obj;
            return getType().equals(other.getType()) && myValue == other.myValue;
        }

        @Override
        public Object getType()
        {
            return "type1";
        }

        @Override
        public int hashCode()
        {
            return 31 + myValue;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "(" + myValue + ")";
        }

        /**
         * Get the value of the parameter.
         *
         * @return The value of the parameter.
         */
        protected int getValue()
        {
            return myValue;
        }
    }

    /** A parameter for the scene graph, with a type of "type2". */
    private static class Param2 extends Param1
    {
        /**
         * Construct the parameter.
         *
         * @param value The value of the parameter.
         */
        public Param2(int value)
        {
            super(value);
        }

        @Override
        public Object getType()
        {
            return "type2";
        }
    }

    /** A parameter for the scene graph, with a type of "type3". */
    private static class Param3 extends Param1
    {
        /**
         * Construct the parameter.
         *
         * @param value The value of the parameter.
         */
        public Param3(int value)
        {
            super(value);
        }

        @Override
        public Object getType()
        {
            return "type3";
        }
    }

    /**
     * A parameter for the scene graph, with a type of "type1", which is the
     * same type as {@link Param1}.
     */
    private static class SameAsParam1 extends Param1
    {
        /**
         * Construct the parameter.
         *
         * @param value The value of the parameter.
         */
        public SameAsParam1(int value)
        {
            super(value);
        }

        @Override
        public Object getType()
        {
            return "type1";
        }
    }

    /** A simple test geometry. */
    private static class TestGeometry
    {
        /** The name of the geometry. */
        private final String myName;

        /**
         * Construct a geometry.
         *
         * @param name The name of the geometry.
         */
        public TestGeometry(String name)
        {
            myName = name;
        }

        @Override
        public String toString()
        {
            return myName;
        }
    }
}
