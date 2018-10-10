package io.opensphere.core.appl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.messaging.ThreadedGenericTransceiver;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;

/** Helper for handling listeners for transformer and envoy adds/removes. */
public class ToolboxListenerHelper extends CompositeService
{
    /** The subscriber that handles envoy adds and removes. */
    private final GenericSubscriber<Envoy> myEnvoySubscriber;

    /** The manager for the executors. */
    private final ExecutorManager myExecutorManager;

    /** An one-up id from geometry transceivers. */
    private volatile AtomicInteger myGeometryTransceiverId = new AtomicInteger();

    /** The transceivers that handle geometries. */
    private final Map<Transformer, ThreadedGenericTransceiver<Geometry>> myGeometryTransceivers;

    /**
     * A collection of tools to be used to interact with the rest of the
     * application.
     */
    private final Toolbox myToolbox;

    /** The transformer executor. */
    private final ExecutorService myTransformerExecutor;

    /** The subscriber that handles transformer adds and removes. */
    private final GenericSubscriber<Transformer> myTransformerSubscriber;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox for which this helper listens.
     * @param executorManager The executor manager.
     */
    public ToolboxListenerHelper(Toolbox toolbox, ExecutorManager executorManager)
    {
        super(2);
        myToolbox = toolbox;
        myExecutorManager = executorManager;
        myTransformerExecutor = myExecutorManager.getTransformerExecutor();
        myGeometryTransceivers = New.weakMap();

        myEnvoySubscriber = (source, adds, removes) ->
        {
            removes.stream().filter(envoy1 -> envoy1 != null).forEach(envoy2 ->
            {
                ThreadPoolExecutor envoyExecutor1 = getExecutorForEnvoy(envoy2);
                envoyExecutor1.execute(() ->
                {
                    if (envoy2 instanceof DataRegistryDataProvider)
                    {
                        myToolbox.getDataRegistry().removeDataProvider((DataRegistryDataProvider)envoy2);
                    }
                    envoy2.close();
                });
            });

            adds.stream().filter(envoy3 -> envoy3 != null).forEach(envoy4 ->
            {
                final ThreadPoolExecutor envoyExecutor2 = getExecutorForEnvoy(envoy4);
                envoyExecutor2.execute(() ->
                {
                    if (envoy4 instanceof DataRegistryDataProvider)
                    {
                        myToolbox.getDataRegistry().addDataProvider((DataRegistryDataProvider)envoy4, envoyExecutor2);
                    }
                    envoy4.open(envoyExecutor2);
                });
            });
        };

        myTransformerSubscriber = (source, adds, removes) ->
        {
            removes.stream().filter(t1 -> t1 != null).forEach(transformer1 ->
            {
                final ThreadedGenericTransceiver<Geometry> transceiver1 = myGeometryTransceivers.remove(transformer1);
                myTransformerExecutor.execute(() ->
                {
                    transformer1.close();
                    if (transceiver1 != null)
                    {
                        transceiver1.removeSubscriber(myToolbox.getGeometryRegistry());
                        transformer1.removeSubscriber(transceiver1);
                    }
                });
            });

            adds.stream().filter(t2 -> t2 != null).forEach(transformer2 ->
            {
                final int priority = 5;
                ThreadedGenericTransceiver<Geometry> transceiver2 = new ThreadedGenericTransceiver<>(1, new NamedThreadFactory(
                        "GeometryReceiver" + myGeometryTransceiverId.getAndIncrement(), priority, Thread.MAX_PRIORITY));
                transceiver2.addSubscriber(myToolbox.getGeometryRegistry());
                transformer2.addSubscriber(transceiver2);
                myGeometryTransceivers.put(transformer2, transceiver2);

                myTransformerExecutor.execute(transformer2::open);
            });
        };

        addService(toolbox.getTransformerRegistry().getSubscriberService(myTransformerSubscriber));
        addService(toolbox.getEnvoyRegistry().getSubscriberService(myEnvoySubscriber));
    }

    /**
     * Get the executor service for an envoy.
     *
     * @param envoy The envoy.
     * @return The executor service.
     */
    private ThreadPoolExecutor getExecutorForEnvoy(final Envoy envoy)
    {
        return myExecutorManager.getEnvoyExecutor(
                envoy instanceof DataRegistryDataProvider ? ((DataRegistryDataProvider)envoy).getThreadPoolName() : "default");
    }
}
