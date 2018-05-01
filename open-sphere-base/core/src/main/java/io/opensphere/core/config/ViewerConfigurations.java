package io.opensphere.core.config;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.order.OrderManager;
import io.opensphere.core.projection.AbstractGeographicProjection;
import io.opensphere.core.projection.AbstractProjection;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Configuration that describes the available viewers and projections.
 */
@XmlRootElement(name = "ViewerConfigurations")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ViewerConfigurations
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ViewerConfigurations.class);

    /**
     * The list of viewer configurations.
     */
    @XmlElement(name = "ViewerConfiguration")
    private List<ViewerConfiguration> myViewerConfigurations;

    /**
     * Load the configuration from a URL.
     *
     * @param url The URL.
     * @return The configuration.
     */
    public static ViewerConfigurations load(URL url)
    {
        try
        {
            return XMLUtilities.readXMLObject(url, ViewerConfigurations.class);
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to load " + url + ": " + e, e);
            return null;
        }
    }

    /** Constructor for JAXB. */
    protected ViewerConfigurations()
    {
    }

    /**
     * Accessor for the viewerConfigurations.
     *
     * @return The viewerConfigurations.
     */
    public List<ViewerConfiguration> getViewerConfigurations()
    {
        return myViewerConfigurations;
    }

    /**
     * Projection configuration.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProjectionConfiguration
    {
        /** The class name for the projection. */
        @XmlAttribute(name = "classname", required = true)
        private String myClassname;

        /** Constructor for JAXB. */
        protected ProjectionConfiguration()
        {
        }

        /**
         * Get the class name for the projection.
         *
         * @return The class name.
         */
        public String getClassname()
        {
            return myClassname;
        }
    }

    /**
     * View control translator configuration.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ViewControlTranslatorConfiguration
    {
        /** The class name for the view control translator. */
        @XmlAttribute(name = "classname", required = true)
        private String myClassname;

        /** Constructor for JAXB. */
        protected ViewControlTranslatorConfiguration()
        {
        }

        /**
         * Get the class name for the view control translator.
         *
         * @return The class name.
         */
        public String getClassname()
        {
            return myClassname;
        }
    }

    /**
     * Viewer configuration.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ViewerConfiguration
    {
        /** The class name for the viewer. */
        @XmlAttribute(name = "classname", required = true)
        private String myClassname;

        /** The list of projections that work with this viewer. */
        @XmlElement(name = "ProjectionConfiguration", required = true)
        private List<ProjectionConfiguration> myProjectionConfigurations;

        /** The view control translator configuration. */
        @XmlElement(name = "ViewControlTranslatorConfiguration", required = true)
        private ViewControlTranslatorConfiguration myViewControlTranslatorConfiguration;

        /** Constructor for JAXB. */
        protected ViewerConfiguration()
        {
        }

        /**
         * Get the class name for the viewer.
         *
         * @return The class name.
         */
        public String getClassname()
        {
            return myClassname;
        }

        /**
         * Accessor for the projection configurations.
         *
         * @return The projection configurations.
         */
        public List<ProjectionConfiguration> getProjectionConfigurations()
        {
            return myProjectionConfigurations;
        }

        /**
         * Create a projection for each of my projection configurations.
         *
         * @param elevationOrderManager The manager for elevation orders to be
         *            used by projections created here.
         * @return The projections.
         */
        public Collection<? extends AbstractProjection> getProjections(OrderManager elevationOrderManager)
        {
            Collection<AbstractProjection> result = New.collection();
            for (ProjectionConfiguration projectionConfig : getProjectionConfigurations())
            {
                String projectionClassname = projectionConfig.getClassname();
                Class<?> projectionClass;
                try
                {
                    projectionClass = Class.forName(projectionClassname);
                }
                catch (ClassNotFoundException e)
                {
                    LOGGER.error("Failed to load projection class: " + projectionClassname + " " + e, e);
                    continue;
                }

                try
                {
                    AbstractProjection proj = (AbstractProjection)projectionClass.getDeclaredConstructor().newInstance();
                    result.add(proj);
                    if (proj instanceof AbstractGeographicProjection)
                    {
                        proj.useElevationOrderManager(elevationOrderManager);
                    }
                }
                catch (ReflectiveOperationException | IllegalArgumentException e)
                {
                    LOGGER.error("Failed to create projection: " + projectionClass + " " + e, e);
                }
            }
            return result;
        }

        /**
         * Get the view control translator configuration.
         *
         * @return The viewControlTranslatorConfiguration.
         */
        public ViewControlTranslatorConfiguration getViewControlTranslatorConfiguration()
        {
            return myViewControlTranslatorConfiguration;
        }
    }
}
