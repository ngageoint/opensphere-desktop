package io.opensphere.core.appl;

import java.util.Collection;
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
    private final GenericSubscriber<Envoy> myEnvoySubscriber = new GenericSubscriber<Envoy>()
    {
        @Override
        public void receiveObjects(Object source, Collection<? extends Envoy> adds, Collection<? extends Envoy> removes)
        {
            for (final Envoy envoy : removes)
            {
                if (envoy != null)
                {
                    ThreadPoolExecutor envoyExecutor = getExecutorForEnvoy(envoy);
                    envoyExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (envoy instanceof DataRegistryDataProvider)
                            {
                                myToolbox.getDataRegistry().removeDataProvider((DataRegistryDataProvider)envoy);
                            }
                            envoy.close();
                        }
                    });
                }
            }
            for (final Envoy envoy : adds)
            {
                if (envoy != null)
                {
                    final ThreadPoolExecutor envoyExecutor = getExecutorForEnvoy(envoy);
                    envoyExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (envoy instanceof DataRegistryDataProvider)
                            {
                                myToolbox.getDataRegistry().addDataProvider((DataRegistryDataProvider)envoy, envoyExecutor);
                            }
                            envoy.open(envoyExecutor);
                        }
                    });
                }
            }
        }
    };

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

    /**
     * The subscriber that handles transformer adds and removes.
     */
    private final GenericSubscriber<Transformer> myTransformerSubscriber = new GenericSubscriber<Transformer>()
    {
        @Override
        public void receiveObjects(Object source, Collection<? extends Transformer> adds,
                Collection<? extends Transformer> removes)
        {
            for (final Transformer transformer : removes)
            {
                if (transformer != null)
                {
                    final ThreadedGenericTransceiver<Geometry> transceiver = myGeometryTransceivers.remove(transformer);
                    myTransformerExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            transformer.close();
                            if (transceiver != null)
                            {
                                transceiver.removeSubscriber(myToolbox.getGeometryRegistry());
                                transformer.removeSubscriber(transceiver);
                            }
                        }
                    });
                }
            }
            for (final Transformer transformer : adds)
            {
                if (transformer != null)
                {
                    final int priority = 5;
                    ThreadedGenericTransceiver<Geometry> transceiver = new ThreadedGenericTransceiver<>(1, new NamedThreadFactory(
                            "GeometryReceiver" + myGeometryTransceiverId.getAndIncrement(), priority, Thread.MAX_PRIORITY));
                    transceiver.addSubscriber(myToolbox.getGeometryRegistry());
                    transformer.addSubscriber(transceiver);
                    myGeometryTransceivers.put(transformer, transceiver);

                    myTransformerExecutor.execute(transformer::open);
                }
            }
        }
    };

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
