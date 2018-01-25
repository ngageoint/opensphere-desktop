package io.opensphere.mantle.icon.impl;

import java.util.List;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistryListener;

/**
 * An adapter for the {@link IconRegistryListener} that allows sub-classes to
 * implement only selective interface methods.
 */
public class IconRegistryListenerAdapter implements IconRegistryListener
{
    @Override
    public void iconAssigned(long iconId, List<Long> deIds, Object source)
    {
    }

    @Override
    public void iconsAdded(List<IconRecord> added, Object source)
    {
    }

    @Override
    public void iconsRemoved(List<IconRecord> removed, Object source)
    {
    }

    @Override
    public void iconsUnassigned(List<Long> deIds, Object source)
    {
    }
}
