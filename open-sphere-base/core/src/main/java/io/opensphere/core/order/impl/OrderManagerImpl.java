package io.opensphere.core.order.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.config.v1.OrderCategoryConfig;
import io.opensphere.core.order.impl.config.v1.OrderManagerConfig;
import io.opensphere.core.order.impl.config.v1.OrderManagerParticipant;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * Manager for handling the order of collection of participants. The orders will
 * be consecutive starting from the minimum order allowed by this manager. Each
 * manager is unique for the context and key type it manages.
 */
@SuppressWarnings("PMD.GodClass")
public class OrderManagerImpl implements OrderManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OrderManagerImpl.class);

    /** The participants which are active. */
    private final Set<OrderParticipantKey> myActiveParticipants = New.set();

    /** The category for this manager. */
    private final OrderCategory myCategory;

    /** Change support helper. */
    private final ChangeSupport<OrderChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** The family for this manager. */
    private final String myFamily;

    /** Executor for performing notifications. */
    private final ExecutorService myNotificationExecutor = new FixedThreadPoolExecutor(1,
            new NamedThreadFactory("OrderNotification"));

    /** The map of order numbers to the associated participants. */
    private final TIntObjectMap<OrderParticipantKey> myOrderToParticipants = new TIntObjectHashMap<>();

    /** The registry which owns this order manager. */
    private OrderManagerRegistryImpl myOwningRegistry;

    /** The map of participants to the associated order numbers. */
    private final TObjectIntMap<OrderParticipantKey> myParticipantToOrder = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY,
            Constants.DEFAULT_LOAD_FACTOR, -1);

    /**
     * Construct using a configuration.
     *
     * @param config The configuration.
     */
    public OrderManagerImpl(OrderManagerConfig config)
    {
        myFamily = config.getFamily();
        myCategory = new DefaultOrderCategory(config.getCategory());

        if (config.getParticipants() != null)
        {
            fixOrders(config.getParticipants());
            for (OrderManagerParticipant p : config.getParticipants())
            {
                insertParticipant(new DefaultOrderParticipantKey(myFamily, myCategory, p.getId()), p.getOrder());
            }
        }

        /* If the config has too many values, it's probably the result of a
         * faulty plugin, so clear the saved orders. */
        if (myParticipantToOrder.size() >= myCategory.getOrderRange().getMaximumInteger()
                - myCategory.getOrderRange().getMinimumInteger() + 1)
        {
            myParticipantToOrder.clear();
            myOrderToParticipants.clear();
        }
    }

    /**
     * Constructor.
     *
     * @param family The family for this manager.
     * @param category The category for this manager.
     * @param participants Initialize this manager with theses existing
     *            participants, {@code null} may be used when there are no
     *            existing participants.
     */
    public OrderManagerImpl(String family, OrderCategory category, TIntObjectMap<OrderParticipantKey> participants)
    {
        myFamily = family;
        myCategory = category;

        if (participants != null)
        {
            participants.forEachEntry(new TIntObjectProcedure<OrderParticipantKey>()
            {
                @Override
                public boolean execute(int key, OrderParticipantKey value)
                {
                    insertParticipant(value, key);
                    return true;
                }
            });
        }
    }

    @Override
    public synchronized int activateParticipant(OrderParticipantKey participant)
    {
        int order;
        if (!isManaged(Utilities.checkNull(participant, "participant")))
        {
            order = addParticipant(participant);
        }
        else
        {
            order = myParticipantToOrder.get(participant);
        }

        if (myActiveParticipants.add(participant))
        {
            notifyParticipantChanged(participant, order, ParticipantChangeType.ACTIVATED);
        }
        return order;
    }

    @Override
    public synchronized void activateParticipants(Collection<OrderParticipantKey> participants)
    {
        TObjectIntMap<OrderParticipantKey> notify = new TObjectIntHashMap<>(participants.size());
        for (OrderParticipantKey activate : participants)
        {
            int order;
            if (!isManaged(Utilities.checkNull(activate, "participant")))
            {
                order = addParticipant(activate);
            }
            else
            {
                order = myParticipantToOrder.get(activate);
            }

            if (myActiveParticipants.add(activate))
            {
                notify.put(activate, order);
            }
        }

        notifyParticipantChanged(notify, ParticipantChangeType.ACTIVATED);
    }

    @Override
    public synchronized int addParticipant(OrderParticipantKey participant)
    {
        if (isManaged(Utilities.checkNull(participant, "participant")))
        {
            return myParticipantToOrder.get(participant);
        }

        if (myParticipantToOrder.size() >= myCategory.getOrderRange().getMaximumInteger()
                - myCategory.getOrderRange().getMinimumInteger() + 1)
        {
            LOGGER.error("OrderManager has no room to insert new participant");
            return -1;
        }

        int order;
        if (myParticipantToOrder.isEmpty())
        {
            order = myCategory.getOrderRange().getMinimumInteger();
        }
        else
        {
            int[] orders = getAllOrders();
            order = orders[orders.length - 1] + 1;
        }
        insertParticipant(participant, order);

        saveOrders();
        return order;
    }

    @Override
    public void addParticipantChangeListener(OrderChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public synchronized void addParticipants(Collection<OrderParticipantKey> adds)
    {
        for (OrderParticipantKey participant : adds)
        {
            addParticipant(Utilities.checkNull(participant, "participant"));
        }
    }

    @Override
    public synchronized int deactivateParticipant(OrderParticipantKey participant)
    {
        if (!isManaged(participant))
        {
            LOGGER.warn("Attempting to deactivate unmanaged participant " + participant.getId());
            return -1;
        }

        int order = myParticipantToOrder.get(participant);
        if (myActiveParticipants.remove(participant))
        {
            notifyParticipantChanged(participant, order, ParticipantChangeType.DEACTIVATED);
        }
        return order;
    }

    @Override
    public synchronized void deactivateParticipants(Collection<OrderParticipantKey> participants)
    {
        TObjectIntMap<OrderParticipantKey> notify = new TObjectIntHashMap<>(participants.size());
        for (OrderParticipantKey dectivate : participants)
        {
            if (!isManaged(dectivate))
            {
                LOGGER.warn("Attempting to deactivate unmanaged participant.");
                continue;
            }

            int order = myParticipantToOrder.get(dectivate);
            if (myActiveParticipants.remove(dectivate))
            {
                notify.put(dectivate, order);
            }
        }

        notifyParticipantChanged(notify, ParticipantChangeType.DEACTIVATED);
    }

    @Override
    public synchronized int expungeParticipant(OrderParticipantKey participant)
    {
        if (!isManaged(participant))
        {
            LOGGER.error("Attempting to remove unmanaged participant.");
            return -1;
        }

        deactivateParticipant(participant);
        int order = removeParticipant(participant);

        compressOrders();

        saveOrders();

        return order;
    }

    @Override
    public synchronized void expungeParticipants(Collection<OrderParticipantKey> removes)
    {
        TObjectIntMap<OrderParticipantKey> notify = new TObjectIntHashMap<>(removes.size());
        for (OrderParticipantKey remove : removes)
        {
            int order = myParticipantToOrder.remove(remove);
            myOrderToParticipants.remove(order);

            if (myActiveParticipants.remove(remove))
            {
                notify.put(remove, order);
            }
        }

        notifyParticipantChanged(notify, ParticipantChangeType.DEACTIVATED);

        compressOrders();

        saveOrders();
    }

    @Override
    public synchronized List<OrderParticipantKey> getActiveParticipants()
    {
        int[] orders = getAllOrders();
        List<OrderParticipantKey> ordered = New.list(orders.length);
        for (int order : orders)
        {
            OrderParticipantKey participant = myOrderToParticipants.get(order);
            if (myActiveParticipants.contains(participant))
            {
                ordered.add(participant);
            }
        }

        return ordered;
    }

    @Override
    public OrderCategory getCategory()
    {
        return myCategory;
    }

    /**
     * Get a JAXB configuration object representing the state of this order
     * manager.
     *
     * @return The config.
     */
    public OrderManagerConfig getConfig()
    {
        OrderCategoryConfig category = new OrderCategoryConfig(myCategory.getCategoryId(),
                myCategory.getOrderRange().getMaximumInteger(), myCategory.getOrderRange().getMinimumInteger());
        return new OrderManagerConfig(category, myFamily, getParticipantMap());
    }

    @Override
    public String getFamily()
    {
        return myFamily;
    }

    @Override
    public synchronized int getOrder(OrderParticipantKey participant)
    {
        int order = myParticipantToOrder.get(participant);
        if (order == -1)
        {
            LOGGER.debug("Requesting order for unmanaged participant : " + participant);
        }
        return order;
    }

    /**
     * Get the owningRegistry.
     *
     * @return the owningRegistry
     */
    public synchronized OrderManagerRegistryImpl getOwningRegistry()
    {
        return myOwningRegistry;
    }

    @Override
    public synchronized TObjectIntMap<OrderParticipantKey> getParticipantMap()
    {
        return new TObjectIntHashMap<>(myParticipantToOrder);
    }

    @Override
    public synchronized boolean hasActiveParticipant(String orderId)
    {
        for (OrderParticipantKey participant : myActiveParticipants)
        {
            if (participant.getId().equals(orderId))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean isManaged(OrderParticipantKey participant)
    {
        return myParticipantToOrder.containsKey(participant);
    }

    @Override
    public synchronized int moveAbove(OrderParticipantKey participant, OrderParticipantKey reference)
    {
        if (!isManaged(participant) || !isManaged(reference))
        {
            LOGGER.error("Attempting to reorder unmanaged participants.");
            return -1;
        }

        if (Utilities.sameInstance(participant, reference))
        {
            LOGGER.error("Participant cannot be moved above itself.");
            return myParticipantToOrder.get(participant);
        }

        int currentOrder = myParticipantToOrder.get(participant);
        int referenceOrder = myParticipantToOrder.get(reference);

        if (referenceOrder == currentOrder - 1)
        {
            // The participant is already in the desired spot.
            return currentOrder;
        }
        else
        {
            removeParticipant(participant);
        }

        int assignedOrder;
        TObjectIntMap<OrderParticipantKey> moved = new TObjectIntHashMap<>(
                Math.abs(referenceOrder - currentOrder));
        if (currentOrder < referenceOrder)
        {
            // Move the ones from currentOrder + 1 to referenceOrder down one
            // and insert at referenceOrder.
            for (int i = currentOrder + 1; i <= referenceOrder; ++i)
            {
                if (myOrderToParticipants.containsKey(i))
                {
                    OrderParticipantKey move = myOrderToParticipants.get(i);
                    removeParticipant(move);
                    insertParticipant(move, i - 1);
                    moved.put(move, i - 1);
                }
            }
            assignedOrder = referenceOrder;
        }
        else
        {
            // Move the ones from referenceOrder + 1 to currentOrder - 1 up one
            // and insert at referenceOrder + 1
            for (int i = currentOrder - 1; i >= referenceOrder + 1; --i)
            {
                if (myOrderToParticipants.containsKey(i))
                {
                    OrderParticipantKey move = myOrderToParticipants.get(i);
                    removeParticipant(move);
                    insertParticipant(move, i + 1);
                    moved.put(move, i + 1);
                }
            }
            assignedOrder = referenceOrder + 1;
        }

        insertParticipant(participant, assignedOrder);
        moved.put(participant, assignedOrder);
        notifyParticipantChanged(moved, ParticipantChangeType.ORDER_CHANGED);

        saveOrders();

        return assignedOrder;
    }

    @Override
    public synchronized int moveBelow(OrderParticipantKey participant, OrderParticipantKey reference)
    {
        if (!isManaged(participant) || !isManaged(reference))
        {
            LOGGER.error("Attempting to reorder unmanaged participants.");
            return -1;
        }

        if (Utilities.sameInstance(participant, reference))
        {
            LOGGER.error("Participant cannot be moved above itself.");
            return myParticipantToOrder.get(participant);
        }

        int currentOrder = myParticipantToOrder.get(participant);
        int referenceOrder = myParticipantToOrder.get(reference);

        if (referenceOrder == currentOrder + 1)
        {
            // The participant is already in the desired spot.
            return currentOrder;
        }
        else
        {
            removeParticipant(participant);
        }

        int assignedOrder;
        TObjectIntMap<OrderParticipantKey> moved = new TObjectIntHashMap<>(
                Math.abs(referenceOrder - currentOrder));
        if (currentOrder < referenceOrder)
        {
            // Move the ones from currentOrder + 1 to referenceOrder - 1 down
            // one
            // and insert at referenceOrder - 1.
            for (int i = currentOrder + 1; i < referenceOrder; ++i)
            {
                OrderParticipantKey move = myOrderToParticipants.get(i);
                removeParticipant(move);
                insertParticipant(move, i - 1);
                moved.put(move, i - 1);
            }
            assignedOrder = referenceOrder - 1;
        }
        else
        {
            // Move the ones from referenceOrder to currentOrder - 1 up one
            // and insert at referenceOrder
            for (int i = currentOrder - 1; i >= referenceOrder; --i)
            {
                OrderParticipantKey move = myOrderToParticipants.get(i);
                removeParticipant(move);
                insertParticipant(move, i + 1);
                moved.put(move, i + 1);
            }
            assignedOrder = referenceOrder;
        }

        insertParticipant(participant, assignedOrder);
        moved.put(participant, assignedOrder);
        notifyParticipantChanged(moved, ParticipantChangeType.ORDER_CHANGED);

        saveOrders();

        return assignedOrder;
    }

    @Override
    public int moveToBottom(OrderParticipantKey participant)
    {
        return moveBelow(participant, myOrderToParticipants.get(myCategory.getOrderRange().getMinimumInteger()));
    }

    @Override
    public int moveToTop(OrderParticipantKey participant)
    {
        int maxOrder = Arrays.stream(myOrderToParticipants.keys()).max().orElse(myCategory.getOrderRange().getMinimumInteger());
        return moveAbove(participant, myOrderToParticipants.get(maxOrder));
    }

    @Override
    public void removeParticipantChangeListener(OrderChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Set the owningRegistry.
     *
     * @param owningRegistry the owningRegistry to set
     */
    public synchronized void setOwningRegistry(OrderManagerRegistryImpl owningRegistry)
    {
        myOwningRegistry = owningRegistry;
    }

    /**
     * Remove gaps from the used orders to make them consecutive.
     */
    private void compressOrders()
    {
        int[] orders = getAllOrders();
        TObjectIntMap<OrderParticipantKey> notify = new TObjectIntHashMap<>();

        int orderAssignment = myCategory.getOrderRange().getMinimumInteger();
        for (int currentOrder : orders)
        {
            if (currentOrder != orderAssignment)
            {
                OrderParticipantKey participant = myOrderToParticipants.get(currentOrder);
                removeParticipant(participant);
                insertParticipant(participant, orderAssignment);
                notify.put(participant, orderAssignment);
            }
            ++orderAssignment;
        }

        notifyParticipantChanged(notify, ParticipantChangeType.ORDER_CHANGED);
    }

    /**
     * Get all of the order numbers in use by this manager sorted for least to
     * greatest.
     *
     * @return all of the order numbers in use by this manager.
     */
    private synchronized int[] getAllOrders()
    {
        int[] orders = myOrderToParticipants.keys();
        if (orders != null)
        {
            Arrays.sort(orders);
        }
        return orders;
    }

    /**
     * Insert a participant into both maps. This method is only used internally
     * and does not
     *
     * @param participant The participant to add.
     * @param order The order number for the participant.
     */
    private synchronized void insertParticipant(OrderParticipantKey participant, int order)
    {
        OrderParticipantKey existingKey = myOrderToParticipants.get(order);
        if (existingKey != null)
        {
            StringBuilder sb = new StringBuilder(200);
            sb.append("Inserting participant with duplicate order: ").append(order).append(": ").append(participant.getId());
            sb.append(" conflicts with existing key: ").append(existingKey.getId());
            LOGGER.error(sb.toString());
        }
        myParticipantToOrder.put(participant, order);
        myOrderToParticipants.put(order, participant);
    }

    /**
     * A convenience method for creating and sending the notification.
     *
     * @param participant the participants which has changed.
     * @param order the order of the participant.
     * @param type the type of change which has occurred.
     */
    private void notifyParticipantChanged(OrderParticipantKey participant, int order, ParticipantChangeType type)
    {
        TObjectIntMap<OrderParticipantKey> ordersToNotify = new TObjectIntHashMap<>(1);
        ordersToNotify.put(participant, order);
        ParticipantOrderChangeEvent event = new ParticipantOrderChangeEvent(ordersToNotify, type);
        notifyParticipantChangeListeners(event);
    }

    /**
     * A convenience method for creating and sending the notification.
     *
     * @param participants the participants which have changed.
     * @param type the type of change which has occurred.
     */
    private void notifyParticipantChanged(TObjectIntMap<OrderParticipantKey> participants, ParticipantChangeType type)
    {
        if (!participants.isEmpty())
        {
            ParticipantOrderChangeEvent event = new ParticipantOrderChangeEvent(participants, type);
            notifyParticipantChangeListeners(event);
        }
    }

    /**
     * Send a change notification event to interested parties.
     *
     * @param event The event for the change which has occurred.
     */
    private void notifyParticipantChangeListeners(final ParticipantOrderChangeEvent event)
    {
        // Use an executor here since this may be on the AWT thread when changes
        // are a result of GUI interaction.
        myNotificationExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                ChangeSupport.Callback<OrderChangeListener> callback = new ChangeSupport.Callback<OrderChangeListener>()
                {
                    @Override
                    public void notify(OrderChangeListener listener)
                    {
                        listener.orderChanged(event);
                    }
                };
                myChangeSupport.notifyListeners(callback);
            }
        });
    }

    /**
     * Remove the participant from both maps. This method does not deactivate
     * the participant, as it is expected that this is used when reordering
     * participants.
     *
     * @param participant the participant to remove
     * @return the order of the participant before removal.
     */
    private int removeParticipant(OrderParticipantKey participant)
    {
        int order = myParticipantToOrder.remove(participant);
        myOrderToParticipants.remove(order);
        return order;
    }

    /** Save all of the orders managed by this manager. */
    private void saveOrders()
    {
        if (myOwningRegistry != null)
        {
            myOwningRegistry.saveOrders(this);
        }
    }

    /**
     * Fixes the orders if necessary.
     *
     * @param participants the participants
     */
    private static void fixOrders(List<? extends OrderManagerParticipant> participants)
    {
        long uniqueOrders = participants.stream().mapToInt(p -> p.getOrder()).distinct().count();
        if (uniqueOrders != participants.size())
        {
            int order = participants.stream().mapToInt(p -> p.getOrder()).min().orElse(0);
            for (int i = participants.size() - 1; i >= 0; i--)
            {
                participants.get(i).setOrder(order++);
            }
        }
    }
}
