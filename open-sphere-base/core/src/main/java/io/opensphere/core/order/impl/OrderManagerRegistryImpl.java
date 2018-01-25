package io.opensphere.core.order.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.impl.config.v1.OrderManagerConfig;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;

/** The registry for order managers. */
public class OrderManagerRegistryImpl implements OrderManagerRegistry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OrderManagerRegistryImpl.class);

    /**
     * The inner map is the category to the manager, the key on the outer map is
     * the order family.
     */
    private final Map<String, Map<OrderCategory, OrderManager>> myManagers = New.map();

    /** The preferences for this registry type. */
    private final Preferences myPreferences;

    /**
     * Constructor.
     *
     * @param prefsRegistry The preferences registry for storing orders.
     */
    public OrderManagerRegistryImpl(PreferencesRegistry prefsRegistry)
    {
        if (prefsRegistry != null)
        {
            myPreferences = prefsRegistry.getPreferences(OrderManagerRegistryImpl.class);
        }
        else
        {
            myPreferences = null;
        }
    }

    @Override
    public synchronized List<String> getAllCategories()
    {
        List<String> contexts = New.list();
        for (Map<OrderCategory, OrderManager> managers : myManagers.values())
        {
            for (OrderCategory category : managers.keySet())
            {
                contexts.add(category.getCategoryId());
            }
        }
        return contexts;
    }

    @Override
    public synchronized Set<OrderCategory> getCategoriesForFamily(String family)
    {
        Set<OrderCategory> categorySet = null;
        Map<OrderCategory, OrderManager> familyMap = myManagers.get(family);
        if (familyMap != null && !familyMap.isEmpty())
        {
            categorySet = New.set(familyMap.keySet());
        }
        return categorySet == null ? Collections.<OrderCategory>emptySet() : categorySet;
    }

    @Override
    public synchronized List<String> getFamilies()
    {
        List<String> families = New.list();
        families.addAll(myManagers.keySet());
        return families;
    }

    @Override
    public OrderManager getOrderManager(OrderParticipantKey participant)
    {
        return getOrderManager(participant.getFamily(), participant.getCategory());
    }

    @Override
    public synchronized OrderManager getOrderManager(String family, OrderCategory category)
    {
        Map<OrderCategory, OrderManager> familyMap = myManagers.get(family);
        if (familyMap == null)
        {
            familyMap = New.map();
            myManagers.put(family, familyMap);
        }

        OrderManager manager = familyMap.get(category);
        if (manager == null)
        {
            String preferenceKey = family + "::" + category.getCategoryId();
            if (myPreferences != null)
            {
                OrderManagerConfig config = myPreferences.getJAXBObject(OrderManagerConfig.class, preferenceKey, null);
                manager = config == null ? null : new OrderManagerImpl(config);
            }
            if (manager == null)
            {
                manager = new OrderManagerImpl(family, category, null);
                saveOrders((OrderManagerImpl)manager);
            }

            for (OrderManager existing : familyMap.values())
            {
                if (category.getOrderRange().overlapsRange(existing.getCategory().getOrderRange()))
                {
                    LOGGER.warn("Creating category " + category.getCategoryId() + " with range " + category.getOrderRange()
                            + " which overlaps category " + existing.getCategory().getCategoryId() + " with range "
                            + existing.getCategory().getOrderRange());
                }
            }

            familyMap.put(category, manager);
            ((OrderManagerImpl)manager).setOwningRegistry(this);
        }

        if (category.getOrderRange().getMaximumInteger() != manager.getCategory().getOrderRange().getMaximumInteger()
                || category.getOrderRange().getMinimumInteger() != manager.getCategory().getOrderRange().getMinimumInteger())
        {
            StringBuilder builder = new StringBuilder("Returning order manager with expected range ");
            builder.append(category.getOrderRange().getMinimumInteger()).append(" to ")
                    .append(category.getOrderRange().getMaximumInteger());
            builder.append(" which is using the range ");
            builder.append(manager.getCategory().getOrderRange().getMinimumInteger()).append(" to ")
                    .append(manager.getCategory().getOrderRange().getMaximumInteger());
            LOGGER.warn(builder.toString());
        }
        return manager;
    }

    /**
     * Store the current orders for the given manager.
     *
     * @param orderManager the order manager for which the orders should be
     *            stored.
     */
    public void saveOrders(OrderManagerImpl orderManager)
    {
        if (myPreferences != null)
        {
            String preferenceKey = orderManager.getFamily() + "::" + orderManager.getCategory().getCategoryId();
            myPreferences.putJAXBObject(preferenceKey, orderManager.getConfig(), false, this);
        }
    }
}
