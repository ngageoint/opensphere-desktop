package io.opensphere.core.terrain.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import gnu.trove.map.TObjectIntMap;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.terrain.util.ElevationChangedEvent.ProviderChangeType;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.lang.NamedThreadFactory;

/** A manager for elevation providers. */
@SuppressWarnings("PMD.GodClass")
public class ElevationManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ElevationManager.class);

    /** Change support helper. */
    private final ChangeSupport<ElevationChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** Executor for sending notifications of changed elevations. */
    private final ExecutorService myNotificationExecutor;

    /** Listener to changes to the order of elevation providers. */
    private final OrderChangeListener myOrderListener = event ->
    {
        TObjectIntMap<OrderParticipantKey> participants = event.getChangedParticipants();
        Collection<AbsoluteElevationProvider> providers = New.collection(participants.size());
        myProvidersLock.readLock().lock();
        try
        {
            for (OrderParticipantKey key : participants.keySet())
            {
                AbsoluteElevationProvider provider = myProviders.get(key.getId());
                if (provider != null)
                {
                    providers.add(provider);
                }
            }
        }
        finally
        {
            myProvidersLock.readLock().unlock();
        }
        if (!providers.isEmpty())
        {
            ElevationChangedEvent elevationEvent = null;
            if (event.getChangeType() == ParticipantChangeType.ACTIVATED)
            {
                elevationEvent = new ElevationChangedEvent(providers, null, ProviderChangeType.PROVIDER_ADDED);
            }
            else if (event.getChangeType() == ParticipantChangeType.DEACTIVATED)
            {
                elevationEvent = new ElevationChangedEvent(providers, null, ProviderChangeType.PROVIDER_REMOVED);
            }
            else if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
            {
                elevationEvent = new ElevationChangedEvent(providers, null, ProviderChangeType.PROVIDER_PRIORITY_CHANGED);
            }

            if (elevationEvent != null)
            {
                notifyElevationListeners(elevationEvent);
            }
        }
    };

    /**
     * The manager which controls the priority order for the elevation
     * providers.
     */
    private OrderManager myOrderManager;

    /** The providers which I manager. */
    private final Map<String, AbsoluteElevationProvider> myProviders = New.map();

    /** Lock to control concurrent access to the providers. */
    private final ReentrantReadWriteLock myProvidersLock = new ReentrantReadWriteLock();

    /**
     * Test to see whether the provider is occluded by a set of higher ordered
     * providers. TODO this does not account for the provider being occluded by
     * a combination of the higher order providers or for providers with
     * multiple disjoint regions.
     *
     * @param provider The provider for which to determine occlusion.
     * @param higherOrdered The occluding providers.
     * @return true when the provider is occluded.
     */
    public static boolean isOccludedBy(AbsoluteElevationProvider provider, List<AbsoluteElevationProvider> higherOrdered)
    {
        for (AbsoluteElevationProvider higherProvider : higherOrdered)
        {
            GeographicPolygon higherRegion = new GeographicConvexPolygon(higherProvider.getBoundingBox().getVertices());
            for (GeographicPolygon providerRegion : provider.getRegions())
            {
                if (!higherRegion.contains(providerRegion, 0.))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /** Constructor. */
    public ElevationManager()
    {
        myNotificationExecutor = new FixedThreadPoolExecutor(1, new NamedThreadFactory("ElevationNotification"));
    }

    /**
     * Add a listener for provider changes.
     *
     * @param listener The listener.
     */
    public void addElevationChangeListener(ElevationChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * De-register a provider.
     *
     * @param provider the providers to de-register.
     */
    public void deregisterProvider(AbsoluteElevationProvider provider)
    {
        myProvidersLock.writeLock().lock();
        try
        {
            myProviders.remove(provider.getElevationOrderId());
        }
        finally
        {
            myProvidersLock.writeLock().unlock();
        }

        ElevationChangedEvent event = new ElevationChangedEvent(Collections.singleton(provider), null,
                ProviderChangeType.PROVIDER_REMOVED);
        notifyElevationListeners(event);
    }

    /**
     * Get the elevation for the position in meters. If multiple providers can
     * provide an elevation for this position, provider with the highest
     * priority will be used.
     *
     * @param position The position for which the elevation is desired.
     * @param approximate When true, return the an approximate value when the
     *            actual value is missing.
     * @return The elevation in meters or zero if no provider is available.
     */
    public double getElevationM(GeographicPosition position, boolean approximate)
    {
        AbsoluteElevationProvider provider = getProviderForPosition(position);
        if (provider != null)
        {
            return provider.getElevationM(position, approximate);
        }
        return 0;
    }

    /**
     * For all elevation providers which overlap the polygon, get the highest
     * density resolution hint.
     *
     * @param region the region for which the resolution is desired.
     * @return the highest density resolution.
     */
    public double getHighestOverlappingResolution(GeographicPolygon region)
    {
        Map<String, AbsoluteElevationProvider> providers = getProviders();

        if (providers.isEmpty() || myOrderManager == null)
        {
            return -1.;
        }

        double resolution = -1.;
        List<OrderParticipantKey> ordered = myOrderManager.getActiveParticipants();
        List<AbsoluteElevationProvider> higherOrdered = New.list();
        for (int i = ordered.size() - 1; i >= 0; --i)
        {
            OrderParticipantKey key = ordered.get(i);
            AbsoluteElevationProvider provider = providers.get(key.getId());

            // providers which are ordered, may not be registered
            if (provider != null)
            {
                if (provider.overlaps(region) && !isOccludedBy(provider, higherOrdered) && provider.getResolutionHintM() != -1)
                {
                    resolution = Math.min(resolution, provider.getResolutionHintM());
                }
                higherOrdered.add(provider);
            }
        }

        return resolution;
    }

    /**
     * Get the minimum variance from the highest order provider which overlaps
     * any part of the region.
     *
     * @param region The region for which the minimum variance is desired.
     * @return the minimum variance.
     */
    public double getMinVariance(GeographicPolygon region)
    {
        Map<String, AbsoluteElevationProvider> providers = getProviders();

        if (providers.isEmpty() || myOrderManager == null)
        {
            return 0.;
        }

        List<OrderParticipantKey> ordered = myOrderManager.getActiveParticipants();
        for (int i = ordered.size() - 1; i >= 0; --i)
        {
            OrderParticipantKey key = ordered.get(i);
            AbsoluteElevationProvider provider = providers.get(key.getId());

            // providers which are ordered, may not be registered

            // We do not need to check for occlusion since we have
            // already checked the higher order providers. If a higher
            // order provider did not over lap this region and this one
            // does, then this provider is not occluded.
            if (provider != null && provider.overlaps(region))
            {
                return provider.getMinVariance();
            }
        }

        return 0.;
    }

    /**
     * Get the bounds of the terrain region which is petrified if any exists.
     *
     * @return The bounds of the petrified terrain.
     */
    public GeographicBoundingBox getPetrifiedTerrainBounds()
    {
        Map<String, AbsoluteElevationProvider> providers = getProviders();
        if (providers.isEmpty() || myOrderManager == null)
        {
            return null;
        }

        List<OrderParticipantKey> ordered = myOrderManager.getActiveParticipants();
        List<AbsoluteElevationProvider> higherOrdered = New.list();
        for (int i = ordered.size() - 1; i >= 0; --i)
        {
            OrderParticipantKey key = ordered.get(i);
            AbsoluteElevationProvider provider = providers.get(key.getId());

            // providers which are ordered, may not be registered
            if (provider != null)
            {
                if (provider.petrifiesTerrain())
                {
                    // It is only allowable to have one petrified region, so
                    // if we found it, we do not need to continue checking.
                    if (isOccludedBy(provider, higherOrdered))
                    {
                        return null;
                    }
                    return provider.getBoundingBox();
                }
                higherOrdered.add(provider);
            }
        }

        return null;
    }

    /**
     * Get the highest priority provider for the position if any.
     *
     * @param position The position for which the provider is desired.
     * @return highest priority provider for the position if any.
     */
    public AbsoluteElevationProvider getProviderForPosition(GeographicPosition position)
    {
        Map<String, AbsoluteElevationProvider> providers = getProviders();

        if (providers.isEmpty() || myOrderManager == null)
        {
            return null;
        }

        List<OrderParticipantKey> ordered = myOrderManager.getActiveParticipants();
        for (int i = ordered.size() - 1; i >= 0; --i)
        {
            OrderParticipantKey key = ordered.get(i);
            AbsoluteElevationProvider provider = providers.get(key.getId());
            // providers which are ordered, may not be registered
            if (provider != null && provider.providesForPosition(position))
            {
                return provider;
            }
        }

        return null;
    }

    /**
     * Get a copy of the providers.
     *
     * @return A copy of the providers.
     */
    public Map<String, AbsoluteElevationProvider> getProviders()
    {
        myProvidersLock.readLock().lock();
        try
        {
            return New.map(myProviders);
        }
        finally
        {
            myProvidersLock.readLock().unlock();
        }
    }

    /**
     * Check to see whether the provider is fully occluded within the region.
     * The provider is occluded any time a higher order provider covers the same
     * region. TODO this does not test the case where multiple higher order
     * providers occlude the region in combination.
     *
     * @param testProvider The provider to test for occlusion.
     * @param region The region over which to test for occlusion.
     * @return true when the region is occluded for the provider.
     */
    public boolean isOccluded(AbsoluteElevationProvider testProvider, GeographicPolygon region)
    {
        Map<String, AbsoluteElevationProvider> providers = getProviders();

        if (!providers.values().contains(testProvider) || myOrderManager == null)
        {
            return false;
        }

        // If a higher order provider contains the region, then the testProvider
        // is occluded.
        List<OrderParticipantKey> ordered = myOrderManager.getActiveParticipants();
        for (int i = ordered.size() - 1; i >= 0; --i)
        {
            OrderParticipantKey key = ordered.get(i);
            AbsoluteElevationProvider provider = providers.get(key.getId());
            // providers which are ordered, may not be registered
            if (provider != null)
            {
                if (Utilities.sameInstance(testProvider, provider))
                {
                    return false;
                }
                for (GeographicPolygon testRegion : testProvider.getRegions())
                {
                    if (testRegion.contains(region, 0.))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Create and send a notification that the elevation values have been
     * updated.
     *
     * @param elevationProvider The provider which has modified values.
     * @param changedRegions The regions over which the values have changed.
     */
    public void notifyElevationsModified(AbsoluteElevationProvider elevationProvider,
            Collection<GeographicPolygon> changedRegions)
    {
        ElevationChangedEvent event = new ElevationChangedEvent(Collections.singleton(elevationProvider), changedRegions,
                ProviderChangeType.TERRAIN_MODIFIED);
        notifyElevationListeners(event);
    }

    /**
     * Register a provider to be managed and make it available to be used by
     * consumers of elevation data.
     *
     * @param provider the provider to register.
     */
    public void registerProvider(AbsoluteElevationProvider provider)
    {
        myProvidersLock.writeLock().lock();
        try
        {
            if (provider.petrifiesTerrain())
            {
                for (AbsoluteElevationProvider existingProvider : myProviders.values())
                {
                    if (existingProvider.petrifiesTerrain())
                    {
                        LOGGER.error("Only one terrain petrifying provider may be registered at a time. Registration of "
                                + provider.getElevationOrderId() + " failed.");
                        return;
                    }
                }
            }
            myProviders.put(provider.getElevationOrderId(), provider);
        }
        finally
        {
            myProvidersLock.writeLock().unlock();
        }

        // Only notify if the provider is also active in the order manager. If
        // it isn't, notification should happen when it is activated the
        // manager.
        if (myOrderManager.hasActiveParticipant(provider.getElevationOrderId()))
        {
            ElevationChangedEvent event = new ElevationChangedEvent(Collections.singleton(provider), null,
                    ProviderChangeType.PROVIDER_ADDED);
            notifyElevationListeners(event);
        }
    }

    /**
     * Remove a listener for provider changes.
     *
     * @param listener The listener to be removed.
     */
    public void removeElevationChangeListener(ElevationChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Use the given manager to determine the priority order of the elevation
     * providers this manager manages.
     *
     * @param manager the order manager to use for priority order.
     */
    public void useOrderManager(OrderManager manager)
    {
        if (myOrderManager != null && !Utilities.sameInstance(myOrderManager, manager))
        {
            LOGGER.warn("Switching the order manager used by the ElevationManager.");
            myOrderManager.removeParticipantChangeListener(myOrderListener);
        }
        myOrderManager = manager;
        myOrderManager.addParticipantChangeListener(myOrderListener);
    }

    /**
     * Notify listeners of changes to registered providers.
     *
     * @param event The event which contains the details of the change.
     */
    private void notifyElevationListeners(final ElevationChangedEvent event)
    {
        myNotificationExecutor.execute(() -> myChangeSupport.notifyListeners(listener -> listener.handleElevationChange(event)));
    }
}
