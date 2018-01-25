package io.opensphere.core.pipeline.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.processor.GeometryProcessorConfigurations.GeometryProcessorConfiguration;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.collections.New;

/**
 * Factory for geometry processors.
 */
public class GeometryProcessorFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryProcessorFactory.class);

    /** Map of geometry types to processor types. */
    private final Map<Class<? extends Geometry>, GeometryProcessorConfiguration> myProcessorMap = Collections
            .synchronizedMap(New.<Class<? extends Geometry>, GeometryProcessorConfiguration>map());

    /**
     * Collection containing warnings regarding processors that are inviable.
     */
    private final Collection<String> myWarnings = New.collection();

    /**
     * Create a geometry processor.
     *
     * @param <T> The geometry type.
     * @param geometryClass The geometry class.
     * @param builder The processor builder.
     * @param warnings Optional warnings to be populated if a processor cannot
     *            be created.
     * @return A new geometry processor.
     */
    public <T extends Geometry> GeometryProcessor<? extends Geometry> createProcessorForClass(Class<T> geometryClass,
            ProcessorBuilder builder, Collection<String> warnings)
    {
        GeometryProcessorConfiguration processorConfig = getProcessorConfig(geometryClass);
        if (processorConfig == null)
        {
            if (warnings != null)
            {
                warnings.add("No processor configuration found for " + geometryClass.getSimpleName());
            }
            return null;
        }

        try
        {
            if (!RenderableGeometryProcessor.class.isAssignableFrom(processorConfig.getGeometryProcessorClass()))
            {
                Constructor<? extends GeometryProcessor<? extends Geometry>> constructor = processorConfig
                        .getGeometryProcessorClass().getConstructor(ProcessorBuilder.class);
                return constructor.newInstance(builder);
            }
            else
            {
                GeometryRenderer<?> renderer = builder.getRendererSet().getRenderer(geometryClass);
                if (renderer == null)
                {
                    if (warnings != null)
                    {
                        warnings.add("No renderer found for geometry type: " + geometryClass);
                    }
                    return null;
                }
                Constructor<? extends GeometryProcessor<? extends Geometry>> constructor = processorConfig
                        .getGeometryProcessorClass().getConstructor(ProcessorBuilder.class, GeometryRenderer.class);
                return constructor.newInstance(builder, renderer);
            }
        }
        catch (SecurityException | IllegalArgumentException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e)
        {
            logError(e, warnings);
        }

        return null;
    }

    /**
     * Get the geometry types that this factory can handle.
     *
     * @return The geometry types.
     */
    public Set<? extends Class<? extends Geometry>> getGeometryTypes()
    {
        return New.unmodifiableSet(myProcessorMap.keySet());
    }

    /**
     * Get the load ahead for a geometry type. The load ahead is the number of
     * time frames that should be loaded in advance of the current time frame
     * when running an animation.
     *
     * @param geometryType The geometry type.
     * @return The load ahead.
     */
    public int getLoadAhead(Class<? extends Geometry> geometryType)
    {
        GeometryProcessorConfiguration processorConfig = getProcessorConfig(geometryType);
        return processorConfig == null ? 0 : processorConfig.getLoadAhead();
    }

    /**
     * Accessor for the warnings.
     *
     * @return The warnings.
     */
    public Collection<? extends String> getWarnings()
    {
        return New.unmodifiableCollection(myWarnings);
    }

    /**
     * Initialize the factory, using the given render context to vet processors.
     *
     * @param rc The render context.
     * @param builder The processor builder.
     */
    public void initialize(RenderContext rc, ProcessorBuilder builder)
    {
        final String configFilename = "geometryProcessorConfig.xml";
        try
        {
            for (GeometryProcessorConfiguration config : GeometryProcessorConfigurations
                    .load(GeometryProcessorFactory.class.getResource(configFilename)).getGeometryProcessorConfigurations())
            {
                if (config.getGeometryClass() != null && config.getGeometryProcessorClass() != null)
                {
                    myProcessorMap.put(config.getGeometryClass(), config);
                    if (RenderableGeometryProcessor.class.isAssignableFrom(config.getGeometryProcessorClass()))
                    {
                        RenderableGeometryProcessor<? extends Geometry> proc = (RenderableGeometryProcessor<? extends Geometry>)createProcessorForClass(
                                config.getGeometryClass(), builder, myWarnings);
                        if (proc == null || !proc.isViable(rc, myWarnings))
                        {
                            myProcessorMap.remove(config.getGeometryClass());
                        }
                    }
                }
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to load " + configFilename + ": " + e, e);
        }
    }

    /**
     * Get the processor configuration for a geometry type.
     *
     * @param <T> The geometry type.
     * @param geometryClass The geometry type.
     * @return The processor config, or {@code null} if one could not be found.
     */
    @SuppressWarnings("unchecked")
    protected <T extends Geometry> GeometryProcessorConfiguration getProcessorConfig(Class<T> geometryClass)
    {
        GeometryProcessorConfiguration processorConfig = null;
        Class<? extends Geometry> geometryType = geometryClass;
        while (processorConfig == null && geometryType != null)
        {
            processorConfig = myProcessorMap.get(geometryType);
            if (processorConfig == null)
            {
                Class<?> superclass = geometryType.getSuperclass();
                if (Geometry.class.isAssignableFrom(superclass))
                {
                    geometryType = (Class<? extends Geometry>)superclass;
                }
                else
                {
                    geometryType = null;
                }
            }
        }
        if (processorConfig == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("No processor found for geometry type: " + geometryClass);
            }
            return null;
        }
        if (!geometryClass.equals(geometryType))
        {
            myProcessorMap.put(geometryClass, processorConfig);
        }
        return processorConfig;
    }

    /**
     * Helper method to log an exception.
     *
     * @param e The exception.
     * @param warnings Optional output collection of warnings.
     */
    private void logError(Exception e, Collection<String> warnings)
    {
        LOGGER.error("Exception instantiating processor: " + e, e);
        if (warnings != null)
        {
            warnings.add("Exception instantiating processor: " + e);
        }
    }
}
