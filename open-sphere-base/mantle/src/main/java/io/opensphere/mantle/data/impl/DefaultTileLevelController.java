package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.mantle.data.TileLevelController;

/**
 * The Class DefaultTileLevelController.
 */
public class DefaultTileLevelController implements TileLevelController
{
    /** The Dividers. */
    private final WeakHashSet<AbstractDivider<GeographicPosition>> myDividers;

    /** The Divider lock. */
    private final ReentrantLock myDividerLock;

    /** The Max generation. -1 implies unknown */
    private int myMaxGeneration = -1;

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

    @Override
    public int getMaxGeneration()
    {
        return myMaxGeneration;
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
        int gen;
        if (pGen < 0)
        {
            throw new IllegalArgumentException("Division hold generation can not be less than zero.");
        }
        else if (myMaxGeneration != -1 && pGen > myMaxGeneration)
        {
            gen = myMaxGeneration;
        }
        else
        {
            gen = pGen;
        }
        myDividerLock.lock();
        try
        {
            myDividers.forEach(d -> d.setDivisionHoldGeneration(gen));
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
                myDividers.forEach(d -> d.setDivisionHoldGeneration(curGen));
                myDividers.forEach(d -> d.setDivisionOverrideEnabled(enabled));
            }
            else
            {
                myDividers.forEach(d -> d.setDivisionOverrideEnabled(enabled));
                myDividers.forEach(d -> d.setDivisionHoldGeneration(0));
            }
        }
        finally
        {
            myDividerLock.unlock();
        }
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
