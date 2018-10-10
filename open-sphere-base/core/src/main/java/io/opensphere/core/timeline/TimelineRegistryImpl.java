package io.opensphere.core.timeline;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.timeline.TimelineChangeEvent.TimelineChangeType;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/** Timeline registry implementation. */
@ThreadSafe
public class TimelineRegistryImpl implements TimelineRegistry
{
    /** The default entry object. */
    private static final Entry DEFAULT_ENTRY = new Entry(null, null, false);

    /** The map of key to entry. */
    @GuardedBy("this")
    private final Map<OrderParticipantKey, Entry> myMap = New.map();

    /** Change support. */
    private final ChangeSupport<Consumer<TimelineChangeEvent>> myChangeSupport = new WeakChangeSupport<>();

    @Override
    public void addLayer(OrderParticipantKey key, String name, Color color, boolean visible)
    {
        boolean added = false;
        synchronized (this)
        {
            if (myMap.containsKey(key))
            {
                Entry entry = myMap.get(key);
                entry.setName(name);
                entry.setColor(color);
                entry.setVisible(visible);
            }
            else
            {
                myMap.put(key, new Entry(name, color, visible));
                added = true;
            }
        }

        if (added)
        {
            notifyListeners(key, TimelineChangeType.LAYER_ADDED);
        }
        else
        {
            notifyListeners(key, TimelineChangeType.COLOR);
            notifyListeners(key, TimelineChangeType.VISIBILITY);
        }
    }

    @Override
    public void removeLayer(OrderParticipantKey key)
    {
        Entry entry;
        synchronized (this)
        {
            entry = myMap.remove(key);
        }

        if (entry != null)
        {
            notifyListeners(key, TimelineChangeType.LAYER_REMOVED);
        }
    }

    @Override
    public void addData(OrderParticipantKey key, Collection<? extends TimelineDatum> data)
    {
        boolean added = false;
        Entry entry;
        synchronized (this)
        {
            entry = myMap.get(key);
            if (entry == null)
            {
                // Create a temporary default entry to let the data get added
                entry = new Entry(key.getId(), Color.WHITE, true);
                myMap.put(key, entry);
                added = true;
            }
            entry.addData(data);
        }

        if (added)
        {
            notifyListeners(key, TimelineChangeType.LAYER_ADDED);
        }
        notifyListeners(key, TimelineChangeType.TIME_SPANS);
    }

    @Override
    public void removeData(OrderParticipantKey key, Collection<? extends Long> ids)
    {
        Entry entry;
        synchronized (this)
        {
            entry = myMap.get(key);
            if (entry != null)
            {
                entry.removeData(ids);
            }
        }

        if (entry != null)
        {
            notifyListeners(key, TimelineChangeType.TIME_SPANS);
        }
    }

    @Override
    public void setData(OrderParticipantKey key, Collection<? extends TimelineDatum> data)
    {
        Entry entry;
        synchronized (this)
        {
            entry = myMap.get(key);
            if (entry != null)
            {
                entry.clearData();
                entry.addData(data);
            }
        }

        if (entry != null)
        {
            notifyListeners(key, TimelineChangeType.TIME_SPANS);
        }
    }

    @Override
    public void setColor(OrderParticipantKey key, Color color)
    {
        boolean changed = false;
        synchronized (this)
        {
            Entry entry = myMap.get(key);
            if (entry != null)
            {
                changed = entry.setColor(color);
            }
        }

        if (changed)
        {
            notifyListeners(key, TimelineChangeType.COLOR);
        }
    }

    @Override
    public void setVisible(OrderParticipantKey key, boolean visible)
    {
        boolean changed = false;
        synchronized (this)
        {
            Entry entry = myMap.get(key);
            if (entry != null)
            {
                changed = entry.setVisible(visible);
            }
        }

        if (changed)
        {
            notifyListeners(key, TimelineChangeType.VISIBILITY);
        }
    }

    @Override
    public synchronized boolean hasKey(OrderParticipantKey key)
    {
        return myMap.containsKey(key);
    }

    @Override
    public synchronized Collection<OrderParticipantKey> getKeys()
    {
        return New.list(myMap.keySet());
    }

    @Override
    public synchronized Collection<TimelineDatum> getSpans(OrderParticipantKey key, Predicate<? super TimelineDatum> filter)
    {
        return StreamUtilities.filter(myMap.getOrDefault(key, DEFAULT_ENTRY).getSpans(), filter);
    }

    @Override
    public synchronized String getName(OrderParticipantKey key)
    {
        return myMap.getOrDefault(key, DEFAULT_ENTRY).getName();
    }

    @Override
    public synchronized Color getColor(OrderParticipantKey key)
    {
        return myMap.getOrDefault(key, DEFAULT_ENTRY).getColor();
    }

    @Override
    public synchronized boolean isVisible(OrderParticipantKey key)
    {
        return myMap.getOrDefault(key, DEFAULT_ENTRY).isVisible();
    }

    @Override
    public void addListener(Consumer<TimelineChangeEvent> listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void removeListener(Consumer<TimelineChangeEvent> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Notifies listeners.
     *
     * @param key The the key that was changed
     * @param changeType The change type
     */
    private void notifyListeners(OrderParticipantKey key, TimelineChangeType changeType)
    {
        if (!myChangeSupport.isEmpty())
        {
            TimelineChangeEvent event = new TimelineChangeEvent(key, changeType);
            myChangeSupport.notifyListeners(listener -> listener.accept(event));
        }
    }
}
