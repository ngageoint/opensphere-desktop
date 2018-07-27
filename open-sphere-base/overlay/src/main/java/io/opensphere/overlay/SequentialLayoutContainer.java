package io.opensphere.overlay;

import java.awt.Dimension;
import java.util.LinkedHashMap;

import io.opensphere.core.model.ScreenPosition;

/**
 * A container used to track items in a sequential layout. A sequential layout
 * is defined from an origin, and items are added / removed in a linear
 * progression in a given direction. A sequential layout container may be used
 * to track, for example, rows or columns of items.
 * 
 * @param <T> the type for which the container is configured.
 */
public class SequentialLayoutContainer<T>
{
    /** The coordinates of the origin of the container. */
    private final ScreenPosition myOrigin;

    /**
     * The spacing applied between each item in the container, including before
     * the first item.
     */
    private final int mySpacing;

    /**
     * The direction of the layout, used to infer the calculated value of the
     * positioning.
     */
    private final LayoutDirection myDirection;

    /**
     * An ordered map maintaining the order of insertion, and the dimensions of
     * the items.
     */
    private final LinkedHashMap<T, Dimension> myItemDimensions;

    /**
     * 
     * @param origin The coordinates of the origin of the container.
     * @param spacing The spacing applied between each item in the container,
     *            including before the first item.
     * @param direction The direction of the layout, used to infer the
     *            calculated value of the positioning.
     */
    public SequentialLayoutContainer(ScreenPosition origin, int spacing, LayoutDirection direction)
    {
        myOrigin = origin;
        mySpacing = spacing;
        myDirection = direction;
        myItemDimensions = new LinkedHashMap<>();
    }

    /**
     * Gets the value of the {@link #mySpacing} field.
     *
     * @return the value stored in the {@link #mySpacing} field.
     */
    public int getSpacing()
    {
        return mySpacing;
    }

    /**
     * Gets the value of the {@link #myDirection} field.
     *
     * @return the value stored in the {@link #myDirection} field.
     */
    public LayoutDirection getDirection()
    {
        return myDirection;
    }

    /**
     * Gets the value of the {@link #myOrigin} field.
     *
     * @return the value stored in the {@link #myOrigin} field.
     */
    public ScreenPosition getOrigin()
    {
        return myOrigin;
    }

    /**
     * Adds the supplied item to the container.
     * 
     * @param item the item to add to the container.
     * @param size the size of the item.
     */
    public void addItem(T item, Dimension size)
    {
        myItemDimensions.put(item, size);
    }

    /**
     * Removes the supplied item from the container.
     * 
     * @param item the item to remove from the container.
     */
    public void removeItem(T item)
    {
        myItemDimensions.remove(item);
    }

    /**
     * Gets the item at the specified index, or null if none could be found.
     * 
     * @param index the index from which to retrieve the item.
     * @return the item at the supplied index, or null if none are present at
     *         the specified index.
     */
    public T getItem(int index)
    {
        return myItemDimensions.keySet().stream().skip(index - 1).findFirst().orElse(null);
    }

    /**
     * Calculates the position of the supplied item's top left corner.
     * 
     * @param item the item for which to calculate the position.
     * @return the top-left {@link ScreenPosition} of the supplied item.
     */
    public ScreenPosition getTopLeftForItem(T item)
    {
        ScreenPosition returnValue;
        switch (myDirection)
        {
            case NORTH:
            {
                double yCoordinate = myOrigin.getY();
                yCoordinate -= mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    if (key.equals(item))
                    {
                        break;
                    }
                    yCoordinate -= myItemDimensions.get(key).getHeight();
                    yCoordinate -= mySpacing;
                }

                returnValue = new ScreenPosition(myOrigin.getX(), yCoordinate);
            }
                break;
            case EAST:
            {
                double xCoordinate = myOrigin.getY();
                xCoordinate += mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    if (key.equals(item))
                    {
                        break;
                    }
                    xCoordinate += myItemDimensions.get(key).getWidth();
                    xCoordinate += mySpacing;
                }

                returnValue = new ScreenPosition(xCoordinate, myOrigin.getY());
            }
                break;
            case SOUTH:
                double yCoordinate = myOrigin.getY();
                yCoordinate += mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    if (key.equals(item))
                    {
                        break;
                    }
                    yCoordinate += myItemDimensions.get(key).getHeight();
                    yCoordinate += mySpacing;
                }

                returnValue = new ScreenPosition(myOrigin.getX(), yCoordinate);
                break;
            case WEST:
                double xCoordinate = myOrigin.getY();
                xCoordinate -= mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    if (key.equals(item))
                    {
                        break;
                    }
                    xCoordinate -= myItemDimensions.get(key).getWidth();
                    xCoordinate -= mySpacing;
                }

                returnValue = new ScreenPosition(xCoordinate, myOrigin.getY());
                break;

            default:
                throw new UnsupportedOperationException("Direction not recognized: '" + myDirection.name() + "'");
        }

        return returnValue;
    }

    /**
     * Calculates the screen position for the top-left point of the next item,
     * using the direction, spacing and set of stored items.
     * 
     * @param itemDimension
     * 
     * @return the {@link ScreenPosition} of the top-left corner of the next
     *         item in the layout direction.
     */
    public ScreenPosition getTopLeftOfNextItem(Dimension itemDimension)
    {
        ScreenPosition returnValue;
        switch (myDirection)
        {
            case NORTH:
            {
                double yCoordinate = myOrigin.getY();
                yCoordinate -= mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    yCoordinate -= myItemDimensions.get(key).getHeight();
                    yCoordinate -= mySpacing;
                }

                yCoordinate -= itemDimension.getHeight();
                returnValue = new ScreenPosition(myOrigin.getX(), yCoordinate);
            }
                break;
            case EAST:
            {
                double xCoordinate = myOrigin.getY();
                xCoordinate += mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    xCoordinate += myItemDimensions.get(key).getWidth();
                    xCoordinate += mySpacing;
                }

                returnValue = new ScreenPosition(xCoordinate, myOrigin.getY());
            }
                break;
            case SOUTH:
                double yCoordinate = myOrigin.getY();
                yCoordinate += mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    yCoordinate += myItemDimensions.get(key).getHeight();
                    yCoordinate += mySpacing;
                }

                returnValue = new ScreenPosition(myOrigin.getX(), yCoordinate);
                break;
            case WEST:
                double xCoordinate = myOrigin.getY();
                xCoordinate -= mySpacing;
                for (T key : myItemDimensions.keySet())
                {
                    xCoordinate -= myItemDimensions.get(key).getWidth();
                    xCoordinate -= mySpacing;
                }

                xCoordinate -= itemDimension.getWidth();
                returnValue = new ScreenPosition(xCoordinate, myOrigin.getY());
                break;

            default:
                throw new UnsupportedOperationException("Direction not recognized: '" + myDirection.name() + "'");
        }

        return returnValue;
    }

    /**
     * Gets the maximum width of any item in the layout.
     * 
     * @return the maximum width of any item in the layout.
     */
    public double getWidth()
    {
        return myItemDimensions.values().stream().map(d -> d.getWidth()).max(Double::compare).orElse(0.0);
    }

    /**
     * Gets the maximum height of any item in the layout.
     * 
     * @return the maximum height of any item in the layout.
     */
    public double getHeight()
    {
        return myItemDimensions.values().stream().map(d -> d.getHeight()).max(Double::compare).orElse(0.0);
    }
}
