package io.opensphere.core.control.action.impl;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import io.opensphere.core.control.ContextMenuSelectionListener;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.ContextActionProvider;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.OverridingContextMenuProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WeakHashSet;

/**
 * Implementation of {@link ActionContext}.
 *
 * @param <T> The context menu key type.
 */
public class ActionContextImpl<T> implements ActionContext<T>
{
    /** The action provider. */
    private ContextSingleActionProvider<T> myActionProvider;

    /** The context identifier. */
    private final String myContextIdentifier;

    /**
     * The default action providers if no other provider is specified. When the
     * action provider is de-registered, this context will fall back to the
     * default behavior.
     */
    private final List<ContextActionProvider<T>> myDefaultActionProviders = New.list();

    /** The menu providers. */
    private final Collection<ContextMenuProvider<T>> myMenuProviders = Collections
            .synchronizedCollection(new WeakHashSet<ContextMenuProvider<T>>());

    /**
     * Instantiates a new control menu option context.
     *
     * @param contextIdentifier the context identifier
     */
    public ActionContextImpl(String contextIdentifier)
    {
        myContextIdentifier = contextIdentifier;
    }

    @Override
    public boolean doAction(T key, Component comp, int x, int y, ContextMenuSelectionListener listener)
    {
        if (myActionProvider != null)
        {
            myActionProvider.doAction(myContextIdentifier, key, x, y);
            return false;
        }

        for (ContextActionProvider<T> provider : myDefaultActionProviders)
        {
            if (provider.doAction(myContextIdentifier, key, x, y))
            {
                return false;
            }
        }

        // If there are no providers, do not assert that we are on the dispatch
        // thread.
        if (myMenuProviders.isEmpty())
        {
            return false;
        }

        return doMenuProvidersAction(key, comp, x, y, listener);
    }

    @Override
    public String getContextIdentifier()
    {
        return myContextIdentifier;
    }

    @Override
    public List<Component> getMenuItems(T key)
    {
        List<Component> menus = New.list();
        Collection<ContextMenuProvider<T>> menuProviders;
        synchronized (myMenuProviders)
        {
            menuProviders = prioritizeProviders(New.collection(myMenuProviders));
        }

        Set<String> overrideItems = menuProviders.stream().filter(p -> p instanceof OverridingContextMenuProvider)
                .flatMap(p -> ((OverridingContextMenuProvider<?>)p).getOverrideItems().stream()).collect(Collectors.toSet());
        for (ContextMenuProvider<T> provider : menuProviders)
        {
            Collection<? extends Component> provItems = provider.getMenuItems(myContextIdentifier, key);
            if (CollectionUtilities.hasContent(provItems))
            {
                if (!menus.isEmpty() && !(provItems.iterator().next() instanceof JSeparator)
                        && !(menus.get(menus.size() - 1) instanceof JSeparator))
                {
                    menus.add(new JSeparator());
                }
                for (Component opt : provItems)
                {
                    String text = opt instanceof AbstractButton ? ((AbstractButton)opt).getText() : null;
                    if (!overrideItems.contains(text) || provider instanceof OverridingContextMenuProvider)
                    {
                        menus.add(opt);
                    }
                }
            }
        }
        return menus;
    }

    @Override
    public boolean isUsed()
    {
        return myActionProvider != null || !myMenuProviders.isEmpty() || !myDefaultActionProviders.isEmpty();
    }

    /**
     * Adds the menu option provider.
     *
     * @param provider the provider
     */
    protected void addMenuOptionProvider(ContextMenuProvider<T> provider)
    {
        myMenuProviders.add(provider);
    }

    /**
     * Clear and invalidate any single action provider which is not the default
     * single action provider.
     */
    protected void clearSingleActionProvider()
    {
        if (myActionProvider != null)
        {
            myActionProvider.invalidated();
        }
        myActionProvider = null;
    }

    /**
     * Register a default provider of a single action for a context. A default
     * provider will only be called if another non-default provider isn't
     * currently registered. Multiple defaults may be registered; they will be
     * called in order of registration until one of them accepts the action.
     *
     * @param provider The provider to register.
     */
    protected void registerDefaultActionProvider(ContextActionProvider<T> provider)
    {
        myDefaultActionProviders.add(provider);
    }

    /**
     * Register a single action provider to perform actions associated with this
     * context and invalidate any existing registered single action provider.
     *
     * @param provider The provider to register.
     */
    protected void registerSingleActionProvider(ContextSingleActionProvider<T> provider)
    {
        if (myActionProvider != null && !Utilities.sameInstance(myActionProvider, provider))
        {
            myActionProvider.invalidated();
        }
        myActionProvider = provider;
    }

    /**
     * Remove a default action provider.
     *
     * @param provider The provider to remove.
     */
    protected void removeDefaultActionProvider(ContextActionProvider<?> provider)
    {
        myDefaultActionProviders.remove(provider);
    }

    /**
     * Removes the menu option provider.
     *
     * @param provider the provider
     */
    protected void removeMenuOptionProvider(ContextMenuProvider<T> provider)
    {
        myMenuProviders.remove(provider);
    }

    /**
     * Remove the single action provider if it is the current provider.
     *
     * @param provider The provider to remove.
     */
    protected void removeSingleActionProvider(ContextSingleActionProvider<T> provider)
    {
        if (Utilities.sameInstance(myActionProvider, provider))
        {
            myActionProvider = null;
        }
    }

    /**
     * Add a listener for action on the item. This will add listeners to the
     * children as necessary.
     *
     * @param item The item on which to listen.
     * @param listener The listener interested in actions on the item.
     */
    private void addListener(AbstractButton item, ContextMenuSelectionListener listener)
    {
        if (item instanceof JMenu)
        {
            JMenu menu = (JMenu)item;
            for (int i = 0; i < menu.getItemCount(); ++i)
            {
                addListener(menu.getItem(i), listener);
            }
        }
        else
        {
            item.addActionListener(listener);
        }
    }

    /**
     * Check if any menu providers want to take action.
     *
     * @param key The key for the context menu.
     * @param comp The component in whose space the popup menu is to appear if a
     *            menu is appropriate..
     * @param x The x coordinate in the component's space at which the popup
     *            menu should be displayed if a menu is appropriate.
     * @param y The y coordinate in the component's space at which the popup
     *            menu should be displayed if a menu is appropriate.
     * @param listener The listener for selection on the created menu.
     * @return true when the menu is shown, false when no providers can provided
     *         menu items for the context.
     */
    private boolean doMenuProvidersAction(T key, Component comp, int x, int y, ContextMenuSelectionListener listener)
    {
        assert EventQueue.isDispatchThread();

        boolean itemAdded = false;
        JPopupMenu menu = new JPopupMenu();
        for (Component item : getMenuItems(key))
        {
            itemAdded = true;
            if (item instanceof AbstractButton && listener != null)
            {
                addListener((AbstractButton)item, listener);
            }
            menu.add(item);
        }

        if (itemAdded)
        {
            menu.show(comp, x, y);
        }
        else if (listener != null)
        {
            listener.popupMenuCanceled(null);
            return false;
        }

        if (listener != null)
        {
            menu.addPopupMenuListener(listener);
        }

        return itemAdded;
    }

    /**
     * Sorts the providers by priority. The tree map uses a collection in case
     * there are several providers with the same priority.
     *
     * @param collection the collection
     * @return the collection
     */
    private Collection<ContextMenuProvider<T>> prioritizeProviders(Collection<ContextMenuProvider<T>> collection)
    {
        TreeMap<Integer, Collection<ContextMenuProvider<T>>> treeMap = new TreeMap<>();
        for (Iterator<ContextMenuProvider<T>> iter = collection.iterator(); iter.hasNext();)
        {
            ContextMenuProvider<T> provider = iter.next();
            Integer priority = Integer.valueOf(provider.getPriority());
            if (treeMap.get(priority) == null)
            {
                Collection<ContextMenuProvider<T>> mp = New.collection();
                mp.add(provider);
                treeMap.put(priority, mp);
            }
            else
            {
                treeMap.get(priority).add(provider);
            }
        }

        Collection<ContextMenuProvider<T>> sorted = New.collection();
        for (Collection<ContextMenuProvider<T>> context : treeMap.values())
        {
            sorted.addAll(context);
        }

        return sorted;
    }
}
