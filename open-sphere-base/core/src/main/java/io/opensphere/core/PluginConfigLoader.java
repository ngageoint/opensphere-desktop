package io.opensphere.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.util.collections.New;

/**
 * Loader for plug-in configurations.
 */
public class PluginConfigLoader
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PluginConfigLoader.class);

    /** Path to XML file. */
    private static final String PLUGIN_LOADER_PROPERTIES_FILENAME = "pluginLoader.xml";

    /** Path to XSD file. */
    private static final String PLUGIN_LOADER_XSD_FILENAME = "/pluginLoader.xsd";

    /**
     * Get the list of plug-in configurations.
     *
     * @return The list of plug-in configurations.
     */
    public List<PluginLoaderData> getPluginConfigurations()
    {
        URL schemaURL = getSchemaURL();
        Schema schema = getSchema(schemaURL);
        Unmarshaller unmarshaller;
        try
        {
            unmarshaller = setUpUnmarshaller(schema);
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to create unmarshaller for plugin configurations: " + e, e);
            throw new IllegalStateException("Plugin configurations could not be loaded.", e);
        }

        List<PluginLoaderData> configurations = New.list();
        try
        {
            Enumeration<URL> urls = PluginConfigLoader.class.getClassLoader().getResources(PLUGIN_LOADER_PROPERTIES_FILENAME);
            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();
                InputStream is = url.openStream();
                try
                {
                    List<PluginLoaderData> pluginLoaderData = ((PluginLoaderCollection)unmarshaller.unmarshal(is))
                            .getPluginLoaderData();
                    if (!configurations.isEmpty())
                    {
                        pluginLoaderData.removeAll(configurations);
                    }
                    configurations.addAll(pluginLoaderData);
                }
                catch (JAXBException e)
                {
                    LOGGER.error("Failed to unmarshal plugin config at " + url + ": " + e, e);
                }
                finally
                {
                    is.close();
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Error trying to read plugin configurations: " + e, e);
        }

        return configurations;
    }

    /**
     * Get the schema at the specified URL.
     *
     * @param schemaURL The URL for the schema.
     * @return The schema.
     */
    private Schema getSchema(URL schemaURL)
    {
        Schema schema;
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try
        {
            schema = schemaFactory.newSchema(schemaURL);
        }
        catch (SAXException e)
        {
            String errorStr = "Error parsing plugin schema file: " + PLUGIN_LOADER_XSD_FILENAME;
            LOGGER.error(errorStr, e);
            throw new IllegalStateException(errorStr, e);
        }
        return schema;
    }

    /**
     * Get a URL for the XSD.
     *
     * @return The XSD URL.
     */
    private URL getSchemaURL()
    {
        URL schemaURL = PluginConfigLoader.class.getResource(PLUGIN_LOADER_XSD_FILENAME);
        if (schemaURL == null)
        {
            String errorStr = "Unable to load plugin schema file: " + PLUGIN_LOADER_XSD_FILENAME.substring(1)
            + " not found in the classpath.";
            LOGGER.error(errorStr);
            throw new IllegalStateException(errorStr);
        }
        return schemaURL;
    }

    /**
     * Set up the unmarshaller for the XML.
     *
     * @param schema The XML schema.
     * @return The unmarshaller.
     * @throws JAXBException if the unmarshaller cannot be created.
     */
    private Unmarshaller setUpUnmarshaller(Schema schema) throws JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance(PluginLoaderCollection.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new PluginXMLValidatorHandler());
        return unmarshaller;
    }

    /** Handler for validation errors. */
    private static final class PluginXMLValidatorHandler implements ValidationEventHandler
    {
        @Override
        public boolean handleEvent(ValidationEvent event)
        {
            String error = "Problem validating xml, continuing but objects may be invalid. " + event.getMessage();
            LOGGER.error(error);
            return true;
        }
    }
}
