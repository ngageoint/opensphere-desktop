package io.opensphere.core.pipeline.processor;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.XMLUtilities;

/**
 * Configurations for a geometry processor.
 */
@XmlRootElement(name = "GeometryProcessorConfigurations")
@XmlAccessorType(XmlAccessType.NONE)
public class GeometryProcessorConfigurations
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryProcessorConfigurations.class);

    /**
     * The list of viewer configurations.
     */
    @XmlElement(name = "GeometryProcessorConfiguration")
    private List<GeometryProcessorConfiguration> myGeometryProcessorConfigurations;

    /**
     * Load the configuration from a URL.
     *
     * @param url The URL.
     * @return The configuration.
     * @throws JAXBException If an error occurs.
     */
    public static GeometryProcessorConfigurations load(URL url) throws JAXBException
    {
        return XMLUtilities.readXMLObject(url, GeometryProcessorConfigurations.class);
    }

    /** Constructor for JAXB. */
    protected GeometryProcessorConfigurations()
    {
    }

    /**
     * Accessor for the viewerConfigurations.
     *
     * @return The viewerConfigurations.
     */
    public List<GeometryProcessorConfiguration> getGeometryProcessorConfigurations()
    {
        return myGeometryProcessorConfigurations;
    }

    /**
     * Configuration for a geometry processor.
     */
    @XmlAccessorType(XmlAccessType.NONE)
    public static class GeometryProcessorConfiguration
    {
        /** The geometry class. */
        private Class<? extends Geometry> myGeometryClass;

        /** The class name for the geometries handled by the processor. */
        @XmlAttribute(name = "geometryClassname", required = true)
        private String myGeometryClassname;

        /** The geometry processor class. */
        private Class<? extends GeometryProcessor<? extends Geometry>> myGeometryProcessorClass;

        /** The class name for the processor. */
        @XmlAttribute(name = "geometryProcessorClassname", required = true)
        private String myGeometryProcessorClassname;

        /**
         * The number of time frames that this processor type should be loaded
         * ahead.
         */
        @XmlAttribute(name = "loadAhead", required = true)
        private int myLoadAhead;

        /** Constructor for JAXB. */
        protected GeometryProcessorConfiguration()
        {
        }

        /**
         * Get the geometry class.
         *
         * @return The class, or {@code null} if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        public Class<? extends Geometry> getGeometryClass()
        {
            if (myGeometryClass == null)
            {
                Class<?> geomClass;
                try
                {
                    geomClass = Class.forName(getGeometryClassname());
                    if (Geometry.class.isAssignableFrom(geomClass))
                    {
                        myGeometryClass = (Class<? extends Geometry>)geomClass;
                    }
                    else
                    {
                        LOGGER.error("Class is not assignable to " + Geometry.class.getName() + ": " + getGeometryClassname());
                    }
                }
                catch (ClassNotFoundException e)
                {
                    LOGGER.error("Failed to load geometry class: " + getGeometryClassname() + " " + e, e);
                }
            }
            return myGeometryClass;
        }

        /**
         * Get the class name for the geometries handled by the processor.
         *
         * @return The class name.
         */
        public String getGeometryClassname()
        {
            return myGeometryClassname;
        }

        /**
         * Get the geometry processor class.
         *
         * @return The class, or {@code null} if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        public Class<? extends GeometryProcessor<? extends Geometry>> getGeometryProcessorClass()
        {
            if (myGeometryProcessorClass == null)
            {
                Class<?> geomClass;
                try
                {
                    geomClass = Class.forName(getGeometryProcessorClassname());
                    if (GeometryProcessor.class.isAssignableFrom(geomClass))
                    {
                        myGeometryProcessorClass = (Class<? extends GeometryProcessor<? extends Geometry>>)geomClass;
                    }
                    else
                    {
                        LOGGER.error("Class is not assignable to " + GeometryProcessor.class.getName() + ": "
                                + getGeometryClassname());
                    }
                }
                catch (ClassNotFoundException e)
                {
                    LOGGER.error("Failed to load geometry class: " + getGeometryProcessorClassname() + " " + e, e);
                }
            }
            return myGeometryProcessorClass;
        }

        /**
         * Get the class name for the processor.
         *
         * @return The class name.
         */
        public String getGeometryProcessorClassname()
        {
            return myGeometryProcessorClassname;
        }

        /**
         * The number of time frames that this processor type should be loaded
         * ahead.
         *
         * @return The load ahead.
         */
        public int getLoadAhead()
        {
            return myLoadAhead;
        }
    }
}
