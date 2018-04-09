package io.opensphere.core.common.geom.search;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Bifurcated2DAreaTree
{

    Rectangle2D.Double myAreaBounds;

    Node myMainNode;

    private Bifurcated2DAreaTree()
    {
    };

    /**
     * Creates an Bifurcated2DAreaTree with a set overall bounds to which shapes
     * can then be added. Shapes falling outside the overall area are not
     * allowed to be added into the existing tree. ( i.e. the tree dosen't
     * dynamically resize )
     *
     * @param areaBounds - the bounds of the main tre area.
     */
    public Bifurcated2DAreaTree(Rectangle2D areaBounds)
    {
        this();
        if (areaBounds.getWidth() == 0.0 || areaBounds.getHeight() == 0.0)
        {
            throw new IllegalArgumentException("Area bounds for tree cannot be zero, zero width or zero height detected");
        }
        myAreaBounds = new Rectangle2D.Double(areaBounds.getX(), areaBounds.getY(), areaBounds.getWidth(),
                areaBounds.getHeight());

        myMainNode = new Node(myAreaBounds, false);
    }

    /**
     * Creates a {@link Bifurcated2DAreaTree} setting the overall bounds to
     * match the extents of the provided shapes.
     *
     * @param baseShapes - the Collection of base shapes on which to base the
     *            area tree.
     */
    public Bifurcated2DAreaTree(Collection<Shape> baseShapes)
    {
        this();
        double overallMinX = Double.POSITIVE_INFINITY;
        double overallMaxX = Double.NEGATIVE_INFINITY;
        double overallMinY = Double.POSITIVE_INFINITY;
        double overallMaxY = Double.NEGATIVE_INFINITY;

        // Find the extends for the shapes provided to establish the base
        // node bounds.
        for (Shape aShape : baseShapes)
        {
            double minX = aShape.getBounds2D().getMinX();
            double maxX = aShape.getBounds2D().getMaxX();
            double minY = aShape.getBounds2D().getMinY();
            double maxY = aShape.getBounds2D().getMaxY();

            if (minX < overallMinX)
            {
                overallMinX = minX;
            }

            if (maxX > overallMaxX)
            {
                overallMaxX = maxX;
            }

            if (minY < overallMinY)
            {
                overallMinY = minY;
            }

            if (maxY > overallMaxY)
            {
                overallMaxY = maxY;
            }
        }

        myAreaBounds = new Rectangle2D.Double(overallMinX, overallMinY, overallMaxX - overallMinX, overallMaxY - overallMinY);
        myMainNode = new Node(myAreaBounds, false);
        addShapesToTree(baseShapes);
    }

    /**
     * Adds a single {@link Shape} into the area tree, creating subnodes as
     * necessary
     *
     * @param aShape
     * @return true if added, or false if outside tree area bounds.
     */
    public boolean addShapeToTree(Shape aShape)
    {
        boolean added = false;
        synchronized (myMainNode)
        {
            added = myMainNode.addShape(aShape);
            myMainNode.prune();
        }
        return added;
    }

    /**
     * Adds the {@link Collection} of {@link Shape} into the area tree if a
     * shape falls outside the main bounds of the tree it will not be added.
     *
     * @param baseShapesCollection - the collection to add
     * @return the {@link Set} of {@link Shape}'s not added into the tree
     */
    public Set<Shape> addShapesToTree(Collection<Shape> baseShapesCollection)
    {
        HashSet<Shape> notAddedSet = new HashSet<>();
        if (baseShapesCollection != null)
        {
            synchronized (myMainNode)
            {
                for (Shape aShape : baseShapesCollection)
                {
                    if (!myMainNode.addShape(aShape))
                    {
                        notAddedSet.add(aShape);
                    }
                }
                myMainNode.prune();
            }
        }
        return notAddedSet;
    }

    /**
     *
     * @param aShape - the shape to check for intersections
     * @param fineSearch - if  true the fine Area.intersects is used, rather than
     *            just bounds intersection
     * @return a {@link Set} of {@link Shape} that intersected the search shape
     */
    public Set<Shape> findIntersectingShapes(Shape aShape, boolean fineSearch)
    {
        Set<Shape> results = null;
        synchronized (myMainNode)
        {
            results = myMainNode.findIntersectingShapes(aShape, null, fineSearch);
        }
        return results;
    }

    /**
     * Clears all shapes out of the tree and cleans up sub-nodes
     */
    public void clearTree()
    {
        synchronized (myMainNode)
        {
            myMainNode.clear();
        }
    }

    /**
     * A Node for the area tree, node represents an area in the tree area space,
     * each node splits in two either horizontally or vertically as data is
     * added. Nodes can be searched for data that intersects the test shape.
     */
    protected static class Node
    {
        ArrayList<Shape> myShapes;

        Node mySubNode1;

        Node mySubNode2;

        Rectangle2D.Double myBounds;

        Rectangle2D.Double mySubNode1Bounds;

        Rectangle2D.Double mySubNode2Bounds;

        boolean mySplitOnX = true;

        /**
         * Constructor for a Node
         *
         * @param bounds - the bounding region for bounds
         * @param splitOnX - true if this Node splits its area on the X-axis, or
         *            false to split on the y-axis
         */
        public Node(Rectangle2D.Double bounds, boolean splitOnX)
        {
            mySplitOnX = splitOnX;
            myBounds = bounds;

            // Define the sub-area bounds for when data is added
            if (mySplitOnX)
            {
                double wOver2 = myBounds.getWidth() / 2.0;
                mySubNode1Bounds = new Rectangle2D.Double(myBounds.x, myBounds.y, wOver2, myBounds.height);
                mySubNode2Bounds = new Rectangle2D.Double(myBounds.x + wOver2, myBounds.y, wOver2, myBounds.height);
            }
            else
            {
                double hOver2 = myBounds.getHeight() / 2.0;
                mySubNode1Bounds = new Rectangle2D.Double(myBounds.x, myBounds.y, myBounds.width, hOver2);
                mySubNode2Bounds = new Rectangle2D.Double(myBounds.x, myBounds.y + hOver2, myBounds.width, hOver2);
            }
        }

        /**
         * Search through this node and sub nodes for shapes that intersect the
         * test shape
         *
         * @param shapeToTest - the shape to check for intersections
         * @param foundShapes - the set to add result to when found, if null one
         *            is created and returned
         * @param fineSearch - if  true the fine Area.intersects is used, rather
         *            than just bounds intersection
         * @return the result set, same object as foundShapes if foundShapes
         *         wasn't null or a new set.
         */
        public Set<Shape> findIntersectingShapes(Shape shapeToTest, Set<Shape> foundShapes, boolean fineSearch)
        {
            // Make sure our found set is created incase we need it.
            if (foundShapes == null)
            {
                foundShapes = new HashSet<>();
            }

            if (shapeToTest != null)
            {
                // If the shape we're testing is in this nodes bounds then check
                // both sub-nodes if they exist, and any shapes stored in this
                // node
                // to add to the found set.
                if (shapeToTest.getBounds2D().intersects(myBounds))
                {
                    if (mySubNode1 != null)
                    {
                        mySubNode1.findIntersectingShapes(shapeToTest, foundShapes, fineSearch);
                    }

                    if (mySubNode2 != null)
                    {
                        mySubNode2.findIntersectingShapes(shapeToTest, foundShapes, fineSearch);
                    }

                    if (myShapes != null)
                    {
                        for (Shape aShape : myShapes)
                        {
                            // Don't allow same shape object to succeed in
                            // matching
                            if (aShape == shapeToTest)
                            {
                                continue;
                            }

                            // Check for coarse bounds intersection
                            if (aShape.getBounds2D().intersects(shapeToTest.getBounds2D()))
                            {
                                // If we're fine searching then do the full
                                // blown
                                // Area intersect test
                                if (fineSearch)
                                {
                                    Area areaA = new Area(aShape);
                                    Area areaB = new Area(shapeToTest);
                                    areaA.intersect(areaB);
                                    if (!areaA.isEmpty())
                                    {
                                        foundShapes.add(aShape);
                                    }
                                }
                                else // Not fine, just use the bounds
                                     // intersection
                                {
                                    foundShapes.add(aShape);
                                }
                            }
                        }
                    }
                }
            }
            return foundShapes;
        }

        /**
         * Attempts to add a shape to this Node, if the shapeis entirely
         * contained within the node, it is added ( possibly creating sub-nodes
         * if the shape is entirely within one of the sub-node partitions ), or
         * false if not added. Shape must be entirely within the bounds of this
         * node.
         *
         * @param shapeToAdd - the shape to try to add to this node.
         * @return true if added to this node or one of this node's sub-nodes,
         *         false if not
         */
        public boolean addShape(Shape shapeToAdd)
        {
            if (shapeToAdd == null)
            {
                return false;
            }

            boolean added = false;
            Rectangle2D shapeBounds = shapeToAdd.getBounds2D();
            if (myBounds.contains(shapeBounds))
            {
                if (mySubNode1Bounds.contains(shapeBounds))
                {
                    if (mySubNode1 == null)
                    {
                        mySubNode1 = new Node(mySubNode1Bounds, !mySplitOnX);
                    }

                    added = mySubNode1.addShape(shapeToAdd);
                }
                else if (mySubNode2Bounds.contains(shapeBounds))
                {
                    if (mySubNode2 == null)
                    {
                        mySubNode2 = new Node(mySubNode2Bounds, !mySplitOnX);
                    }

                    added = mySubNode2.addShape(shapeToAdd);
                }
                else
                {
                    if (myShapes == null)
                    {
                        myShapes = new ArrayList<>();
                    }

                    added = myShapes.add(shapeToAdd);
                }
            }
            return added;
        }

        /**
         * Returns true if this Node has shapes ( does not check potential
         * sub-nodes )
         *
         * @return
         */
        public boolean hasShapes()
        {
            return myShapes != null && myShapes.size() > 0;
        }

        /**
         * Returns true if this node has sub-nodes
         *
         * @return true if there are sub-nodes
         */
        public boolean hasSubNodes()
        {
            return mySubNode1 != null || mySubNode2 != null;
        }

        /**
         * Prunes out any un-used sub-nodes in the tree
         */
        public void prune()
        {
            if (mySubNode1 != null)
            {
                mySubNode1.prune();
                if (!mySubNode1.hasSubNodes() && !mySubNode1.hasShapes())
                {
                    mySubNode1 = null;
                }
            }

            if (mySubNode2 != null)
            {
                mySubNode2.prune();
                if (!mySubNode2.hasSubNodes() && !mySubNode2.hasShapes())
                {
                    mySubNode2 = null;
                }
            }
        }

        /**
         * Clears out any data held by this node and any sub-nodes
         */
        public void clear()
        {
            if (myShapes != null)
            {
                myShapes.clear();
                myShapes = null;
            }
            if (mySubNode1 != null)
            {
                mySubNode1.clear();
                mySubNode1 = null;
            }
            if (mySubNode2 != null)
            {
                mySubNode2.clear();
                mySubNode2 = null;
            }
        }

        /**
         * Gets the {@link Rectangle2D} bounds for this Node
         *
         * @return the bounds
         */
        public Rectangle2D getBounds()
        {
            return myBounds;
        }

        /**
         * Returns true if this node's bounds intersects the other bounds
         *
         * @param other the {@link Rectangle2D} to test for intersection
         * @return true if intersects, false if not
         */
        public boolean intersects(Rectangle2D other)
        {
            return myBounds.intersects(other);
        }

    }

    public static void main(String[] args)
    {
        Rectangle2D rec1 = new Rectangle2D.Double(2, 2, 2, 2);
        Rectangle2D rec2 = new Rectangle2D.Double(1, 3, 2, 2);

        System.out.println("rec1 contains   rec2 " + rec1.contains(rec2));
        System.out.println("rec1 intersects rec2 " + rec1.intersects(rec2));

    }
}
