package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.mantle.data.TileLevelController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Class DefaultTileLevelController.
 */
public class DefaultTileLevelController implements TileLevelController
{
    /** The Dividers. */
    private final WeakHashSet<AbstractDivider<GeographicPosition>> myDividers;

    /** The Divider lock. */
    private final ReentrantLock myDividerLock;

    /** The Property for the hold level. */
    private final IntegerProperty myCurrentHoldLevelProperty;

    /** The Max generation. -1 implies unknown */
    private int myMaxGeneration = -1;

    /** The minimum hold level. */
    private int myMinimumHoldLevel;

    /** The Divider lock. */
    private final ReentrantLock myTileGeometryLock;

    /** The Tile geometry set. */
    private final WeakHashSet<AbstractTileGeometry<?>> myTileGeometrySet;

    /**
     * Instantiates a new default tile level controller.
     */
    public DefaultTileLevelController()
    {
        myDividerLock = new ReentrantLock();
        myTileGeometryLock = new ReentrantLock();
        myDividers = new WeakHashSet<>();
        myTileGeometrySet = new WeakHashSet<>();
        myCurrentHoldLevelProperty = new SimpleIntegerProperty(0);
    }

    /**
     * Adds the divider.
     *
     * @param divider the divider
     */
    public void addDivider(AbstractDivider<GeographicPosition> divider)
    {
        myDividerLock.lock();
        try
        {
            myDividers.add(divider);
        }
        finally
        {
            myDividerLock.unlock();
        }
    }

    /**
     * Adds the tile geometries.
     *
     * @param geoms the geoms
     */
    public void addTileGeometries(Collection<? extends AbstractTileGeometry<?>> geoms)
    {
        myTileGeometryLock.lock();
        try
        {
            myTileGeometrySet.addAll(geoms);
        }
        finally
        {
            myTileGeometryLock.unlock();
        }
    }

    /**
     * Clear dividers.
     */
    public void clearDividers()
    {
        myDividerLock.lock();
        try
        {
            myDividers.clear();
        }
        finally
        {
            myDividerLock.unlock();
        }
    }

    /**
     * Clear tile geometries.
     */
    public void clearTileGeometries()
    {
        myTileGeometryLock.lock();
        try
        {
            myTileGeometrySet.clear();
        }
        finally
        {
            myTileGeometryLock.unlock();
        }
    }

    @Override
    public int getCurrentGeneration()
    {
        int maxGenerationValue = 0;
        myTileGeometryLock.lock();
        try
        {
            for (AbstractTileGeometry<?> geom : myTileGeometrySet)
            {
                Collection<AbstractTileGeometry<?>> descendants = New.collection();
                geom.getDescendants(descendants);

                if (geom.getGeneration() > maxGenerationValue)
                {
                    maxGenerationValue = geom.getGeneration();
                }

                for (AbstractTileGeometry<?> desc : descendants)
                {
                    if (desc.getGeneration() > maxGenerationValue)
                    {
                        maxGenerationValue = desc.getGeneration();
                    }
                }
            }
        }
        finally
        {
            myTileGeometryLock.unlock();
        }
        return maxGenerationValue;
    }

    @Override
    public int getDivisionHoldGeneration()
    {
        int holdGen = 0;
        myDividerLock.lock();
        try
        {
            if (myDividers != null && !myDividers.isEmpty())
            {
                holdGen = myDividers.iterator().next().getHoldGeneration();
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
        return holdGen;
    }

    /**
     * Gets the current level that is being held at.
     *
     * @return the current hold level
     */
    public int getCurrentHoldLevel()
    {
        return myCurrentHoldLevelProperty.get();
    }

    /**
     * Gets the current hold level property.
     *
     * @return the current hold level property
     */
    public IntegerProperty getCurrentHoldLevelProperty()
    {
        return myCurrentHoldLevelProperty;
    }

    @Override
    public int getMaxGeneration()
    {
        return myMaxGeneration;
    }

    /**
     * Gets the minimum hold level.
     *
     * @return the minimum hold level
     */
    public int getMinimumHoldLevel()
    {
        return myMinimumHoldLevel;
    }

    @Override
    public boolean isDivisionOverride()
    {
        boolean divOverride = false;
        myDividerLock.lock();
        try
        {
            if (myDividers != null && !myDividers.isEmpty())
            {
                divOverride = myDividers.iterator().next().isDivisionOverride();
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
        return divOverride;
    }

    /**
     * Removes the divider.
     *
     * @param divider the divider
     */
    public void removeDivider(AbstractDivider<GeographicPosition> divider)
    {
        myDividerLock.lock();
        try
        {
            myDividers.remove(divider);
        }
        finally
        {
            myDividerLock.unlock();
        }
    }

    @Override
    public void setDivisionHoldGeneration(int pGen)
    {
        int gen = pGen;
        if (gen < 0)
        {
            throw new IllegalArgumentException("Division hold generation can not be less than zero.");
        }
        else if (myMaxGeneration != -1 && gen > myMaxGeneration)
        {
            gen = myMaxGeneration;
        }
        myDividerLock.lock();
        try
        {
            for (AbstractDivider<GeographicPosition> divider : myDividers)
            {
                divider.setDivisionHoldGeneration(gen);
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
    }

    @Override
    public void setDivisionOverride(boolean enabled)
    {
        myDividerLock.lock();
        try
        {
            if (enabled)
            {
                int curGen = getCurrentGeneration();
                for (AbstractDivider<GeographicPosition> divider : myDividers)
                {
                    divider.setDivisionHoldGeneration(curGen);
                }
                for (AbstractDivider<GeographicPosition> divider : myDividers)
                {
                    divider.setDivisionOverrideEnabled(enabled);
                }
            }
            else
            {
                for (AbstractDivider<GeographicPosition> divider : myDividers)
                {
                    divider.setDivisionOverrideEnabled(enabled);
                }
                for (AbstractDivider<GeographicPosition> divider : myDividers)
                {
                    divider.setDivisionHoldGeneration(0);
                }
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
    }

    /**
     * Sets the current level to be held at.
     *
     * @param holdLevel the new current hold level
     */
    public void setCurrentHoldLevel(int holdLevel)
    {
        myCurrentHoldLevelProperty.set(holdLevel);
    }

    /**
     * Sets the max generation. (-1 implies unknown or undetermined ).
     *
     * @param maxGen the new max generation (-1 implies unknown or undetermined
     *            ).
     */
    public void setMaxGeneration(int maxGen)
    {
        myMaxGeneration = maxGen;
    }

    /**
     * Sets the minimum hold level.
     *
     * @param holdLevel the new minimum hold level
     */
    public void setMinimumHoldLevel(int holdLevel)
    {
        myMinimumHoldLevel = holdLevel;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        myDividerLock.lock();
        try
        {
            sb.append("DefaultTileLevelController\n" + "    Dividers Count   : ").append(myDividers.size()).append('\n');
            if (!myDividers.isEmpty())
            {
                for (AbstractDivider<GeographicPosition> div : myDividers)
                {
                    sb.append("    Divider\n" + "      Division Override: ").append(div.isDivisionOverride())
                            .append("\n" + "      Hold Generation  : ").append(div.getHoldGeneration()).append('\n');
                }
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
        myTileGeometryLock.lock();
        try
        {
            sb.append("    TileGeometries Count   : ").append(myTileGeometrySet.size()).append('\n');
            for (AbstractTileGeometry<?> geom : myTileGeometrySet)
            {
                sb.append("       Geom: ").append(geom.getGeneration()).append('\n');
                Collection<AbstractTileGeometry<?>> descendants = New.collection();
                geom.getDescendants(descendants);
                for (AbstractTileGeometry<?> desc : descendants)
                {
                    sb.append("           Desc Geom: ").append(desc.getGeneration()).append('\n');
                }
            }
        }
        finally
        {
            myTileGeometryLock.unlock();
        }
        sb.append("    Max Generation   : ").append(myMaxGeneration).append('\n');
        return sb.toString();
    }
}
