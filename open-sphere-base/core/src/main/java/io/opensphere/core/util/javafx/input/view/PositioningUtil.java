package io.opensphere.core.util.javafx.input.view;

import java.util.List;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * A set of static methods used to aid in positioning items on the screen.
 */
public final class PositioningUtil
{
    /**
     * Private constructor, hidden from use.
     */
    private PositioningUtil()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Gets the intersection between the supplied vector and the supplied
     * position.
     *
     * @param v0 the zeroth position of the 'a' node to calculate.
     * @param v1 the zeroth position of the 'a' node to calculate.
     * @param v position to test.
     * @return the intersection of the locations.
     */
    public static double getIntersectionLengthImpl(final double v0, final double v1, final double v)
    {
        if (v <= v0)
        {
            return 0;
        }

        return (v <= v1) ? v - v0 : v1 - v0;
    }

    /**
     * Gets the intersection between the supplied locations.
     *
     * @param a0 the zeroth parameter of the 'a' node to calculate.
     * @param a1 the first parameter of the 'a' node to calculate.
     * @param b0 the zeroth parameter of the 'b' node to calculate.
     * @param b1 the first parameter of the 'b' node to calculate.
     * @return the intersection of the locations.
     */
    public static double getIntersectionLength(final double a0, final double a1, final double b0, final double b1)
    {
        return (a0 <= b0) ? getIntersectionLengthImpl(b0, b1, a1) : getIntersectionLengthImpl(a0, a1, b1);
    }

    /**
     * Calculates the distance between the supplied parameters.
     *
     * @param a0 the zeroth parameter of the 'a' node to calculate.
     * @param a1 the first parameter of the 'a' node to calculate.
     * @param b0 the zeroth parameter of the 'b' node to calculate.
     * @param b1 the first parameter of the 'b' node to calculate.
     * @return the distance between the corners of the node.
     */
    public static double getOuterDistance(final double a0, final double a1, final double b0, final double b1)
    {
        if (a1 <= b0)
        {
            return b0 - a1;
        }

        if (b1 <= a0)
        {
            return b1 - a0;
        }

        return 0;
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must
     * allow for Objects to be passed in. This method handles determining the
     * bounds of the given Object. If the Object type is not supported, a
     * default Bounds will be returned.
     *
     * @param pObject the object for which to get the boundaries.
     * @return the boundaries of the supplied object.
     */
    public static Bounds getBounds(Object pObject)
    {
        if (pObject instanceof Node)
        {
            final Node n = (Node)pObject;
            Bounds b = n.localToScreen(n.getLayoutBounds());
            return b != null ? b : new BoundingBox(0, 0, 0, 0);
        }
        else if (pObject instanceof Window)
        {
            final Window window = (Window)pObject;
            return new BoundingBox(window.getX(), window.getY(), window.getWidth(), window.getHeight());
        }
        else
        {
            return new BoundingBox(0, 0, 0, 0);
        }
    }

    /**
     * This function attempts to determine the best screen given the parent
     * object from which we are wanting to position another item relative to.
     * This is particularly important when we want to keep items from going off
     * screen, and for handling multiple monitor support.
     *
     * @param obj the object for which to get the screen.
     * @return the screen associated with the supplied object.
     */
    public static Screen getScreen(Object obj)
    {
        final Bounds parentBounds = getBounds(obj);

        final Rectangle2D rect = new Rectangle2D(parentBounds.getMinX(), parentBounds.getMinY(), parentBounds.getWidth(),
                parentBounds.getHeight());
        final List<Screen> screens = Screen.getScreens();

        final double rectX0 = rect.getMinX();
        final double rectX1 = rect.getMaxX();
        final double rectY0 = rect.getMinY();
        final double rectY1 = rect.getMaxY();

        Screen selectedScreen;

        selectedScreen = null;
        double maxIntersection = 0;
        for (final Screen screen : screens)
        {
            final Rectangle2D screenBounds = screen.getBounds();
            final double intersection = getIntersectionLength(rectX0, rectX1, screenBounds.getMinX(), screenBounds.getMaxX())
                    * getIntersectionLength(rectY0, rectY1, screenBounds.getMinY(), screenBounds.getMaxY());

            if (maxIntersection < intersection)
            {
                maxIntersection = intersection;
                selectedScreen = screen;
            }
        }

        if (selectedScreen != null)
        {
            return selectedScreen;
        }

        selectedScreen = Screen.getPrimary();
        double minDistance = Double.MAX_VALUE;
        for (final Screen screen : screens)
        {
            final Rectangle2D screenBounds = screen.getBounds();
            final double dx = getOuterDistance(rectX0, rectX1, screenBounds.getMinX(), screenBounds.getMaxX());
            final double dy = getOuterDistance(rectY0, rectY1, screenBounds.getMinY(), screenBounds.getMaxY());
            final double distance = dx * dx + dy * dy;

            if (minDistance > distance)
            {
                minDistance = distance;
                selectedScreen = screen;
            }
        }

        return selectedScreen;
    }

    /**
     * Gets the boundaries of the screen on which the node is rendered.
     *
     * @param pNode the node for which to get the screen boundaries.
     * @return the boundaries of the current screen.
     */
    public static Rectangle2D getScreenBounds(Node pNode)
    {
        final Screen currentScreen = getScreen(pNode);
        final Rectangle2D screenBounds = currentScreen.getVisualBounds();
        return screenBounds;
    }

    /**
     * Gets an adjusted position within the supplied min and max boundaries. The
     * supplied location is compared with the dimensions, and moved as needed.
     * This method is axis-agnostic, in that it works exactly the same with the
     * X axis as it does with the Y axis.
     *
     * @param pMinBounds the minimum value for the edge of the rendering area.
     * @param pMaxBounds the maximum value for the edge of the rendering area.
     * @param pLocation the original location of the component.
     * @param pParentLocation the original location of the component's parent.
     * @param pDimension the size of the component.
     * @return an adjusted position, modified to fit within the boundaries.
     */
    public static double getAdjustedPosition(double pMinBounds, double pMaxBounds, double pLocation, double pParentLocation,
            double pDimension)
    {
        double returnValue = pLocation;
        if ((pLocation + pDimension) > pMaxBounds)
        {
            returnValue -= (pLocation + pDimension - pMaxBounds);
        }
        if (pLocation < pMinBounds)
        {
            returnValue = pMinBounds;
        }
        return returnValue;
    }

    /**
     * Gets an position within the supplied min and max boundaries. The supplied
     * location is compared with the dimensions, and moved as needed. This
     * method is axis-agnostic, in that it works exactly the same with the X
     * axis as it does with the Y axis.
     *
     * @param pMinBounds the minimum value for the edge of the rendering area.
     * @param pMaxBounds the maximum value for the edge of the rendering area.
     * @param pLocation the original location of the component.
     * @param pParentLocation the original location of the component's parent.
     * @param pDimension the size of the component.
     * @return an adjusted position, modified to fit within the boundaries.
     */
    public static double getPosition(double pMinBounds, double pMaxBounds, double pLocation, double pParentLocation,
            double pDimension)
    {
        double returnValue = pLocation;
        if ((pLocation + pDimension) > pMaxBounds)
        {
            returnValue = pParentLocation - pDimension;
        }
        else if (pLocation < pMinBounds)
        {
            returnValue = pParentLocation;
        }
        return returnValue;
    }

    /**
     * Gets the target position at which the popup should be displayed. It takes
     * care specifically of the repositioning of the item such that it remains
     * onscreen as best it can, given it's unique qualities.
     *
     * As will all other functions, this one returns a Point2D that represents
     * an x,y location that should safely position the item onscreen as best as
     * possible.
     *
     * Note that <code>width</code> and <code>height</code> refer to the width
     * and height of the node/popup that is needing to be repositioned, not of
     * the parent.
     *
     * @param parent the node from which to calculate the offset (this is the
     *            node that the point is 'relative to').
     * @param width the width of the node for which the calculation is being
     *            performed.
     * @param height the height of the node for which the calculation is being
     *            performed.
     * @return the point at which the component should be drawn.
     */
    public static Point2D pointRelativeTo(Node parent, double width, double height)
    {
        final Bounds parentBounds = getBounds(parent);
        double screenX = parentBounds.getMinX();
        double screenY = parentBounds.getMaxY();
        final Rectangle2D screenBounds = getScreenBounds(parent);

        double finalScreenX = getPosition(screenBounds.getMinX(), screenBounds.getMaxX(), screenX, parentBounds.getMinX(), width);
        double finalScreenY = getPosition(screenBounds.getMinY(), screenBounds.getMaxY(), screenY, parentBounds.getMinY(),
                height);

        // --- after all the moving around, we do one last check / rearrange.
        // Unlike the check above, this time we are just fully
        // committed to keeping the item on screen at all costs, regardless of
        // whether or not that results in overlapping the
        // parent object.
        finalScreenX = getAdjustedPosition(screenBounds.getMinX(), screenBounds.getMaxX(), finalScreenX, screenBounds.getMaxX(),
                width);
        finalScreenY = getAdjustedPosition(screenBounds.getMinY(), screenBounds.getMaxY(), finalScreenY, screenBounds.getMaxY(),
                height);

        return new Point2D(finalScreenX, finalScreenY);
    }
}
