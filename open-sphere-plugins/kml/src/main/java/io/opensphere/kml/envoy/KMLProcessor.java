package io.opensphere.kml.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.xml.sax.SAXParseException;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import de.micromata.opengis.kml.v_2_2_0.Schema;
import de.micromata.opengis.kml.v_2_2_0.SimpleField;
import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.model.KMLContentType;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.FailureReason;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLFeatureUtils;
import io.opensphere.kml.common.util.KMLFeatureAccumulator;
import io.opensphere.kml.common.util.KMLLinkHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * Takes an input stream and does something with it. As follows:
 *
 * KML: Parses it into a Kml object, and builds a list of other data sources to
 * load KMZ: Parses the internal KML file, and adds other files to the KMZ cache
 * Overlays: Adds them to the KMZ cache
 *
 * Also takes care of doing XSLT transformations for Schema data.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLProcessor
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLProcessor.class);

    /** The parser pool. */
    public static final KMLParserPool PARSER_POOL = new KMLParserPool();

    /** The data source. */
    private final KMLDataSource myDataSource;

    /** The result of processing. */
    private KMLFeature myResult;

    /**
     * Constructor.
     *
     * @param dataSource The data source.
     */
    public KMLProcessor(KMLDataSource dataSource)
    {
        myDataSource = dataSource;
    }

    /**
     * Process an input stream. The input stream may be closed by this
     * operation.
     *
     * @param inputStream The input stream.
     * @param dataSourcesToLoad Return collection of additional data sources to
     *            load.
     * @throws IOException If the input stream cannot be parsed as KML.
     */
    public void process(InputStream inputStream, Collection<? super KMLDataSource> dataSourcesToLoad) throws IOException
    {
        KMLFeature rootFeature = parseKMLInputStream(inputStream);
        if (rootFeature != null)
        {
            // Accumulate all the schemas
            Collection<Schema> schemata = new ArrayList<>();
            new SchemaFeatureAccumulator().accumulate(rootFeature, schemata);

            // HACKISH: Call getSimpleField so that the Schema.equals() method
            // works
            for (Schema schema : schemata)
            {
                schema.getSimpleField();
            }

            // The data source must be reloaded if its schemas do not match the
            // schemas from the new document.
            if (myDataSource.getSchemata().size() != schemata.size() || !myDataSource.getSchemata().containsAll(schemata))
            {
                LOGGER.info("Found " + schemata.size() + " KML schemata in " + myDataSource.getPath() + "; reloading.");
                myDataSource.setSchemata(schemata);
                dataSourcesToLoad.add(myDataSource);
            }
            else
            {
                KMLDataEvent dataEvent = new KMLDataEvent(myDataSource, rootFeature);
                KMLToolboxUtils.getKmlToolbox().getStyleCache().addData(dataEvent, false);

                if (!myDataSource.isStyleSource())
                {
                    myResult = rootFeature;
                    dataSourcesToLoad.addAll(getRemoteStyleDataSources(rootFeature));
                }
            }
        }
        else
        {
            myDataSource.setErrorMessage("File contains invalid KML: " + myDataSource.getPath());
            if (myDataSource.getFailureReason() == FailureReason.NAMESPACE_PARSE_ERROR)
            {
                LOGGER.info("Failed to parse " + myDataSource.getPath() + "; reloading.");
                dataSourcesToLoad.add(myDataSource);
            }
            else
            {
                myDataSource.setFailureReason(FailureReason.OTHER);
                throw new IOException(myDataSource.getName() + ": Unable to parse KML input stream");
            }
        }
    }

    /**
     * Returns the result of the processing.
     *
     * @return The result
     */
    public KMLFeature getResult()
    {
        return myResult;
    }

    /**
     * Parses a KML input stream. This is CPU intensive.
     *
     * @param inputStream The input stream
     * @return The parsed Kml object
     */
    private KMLFeature parseKMLInputStream(final InputStream inputStream)
    {
        KMLFeature rootFeature = null;

        InputStream transformedInputStream = StreamUtilities.bufferifyInputStream(inputStream);

        // If the data source contains schemas, transform the data to be
        // parseable
        if (!myDataSource.getSchemata().isEmpty())
        {
            transformedInputStream = transformSchemas(transformedInputStream, myDataSource.getSchemata());
        }

        // Remove namespaces if necessary
        boolean namespacesRemoved = false;
        if (myDataSource.getFailureReason() == FailureReason.NAMESPACE_PARSE_ERROR)
        {
            transformedInputStream = removeNamespaces(transformedInputStream);
            namespacesRemoved = true;
        }

        // Parse the input stream using JAK
        Kml kml = null;
        try
        {
            kml = PARSER_POOL.process(transformedInputStream);
        }
        catch (JAXBException e)
        {
            Throwable le = e.getLinkedException();

            LOGGER.error(le);

            if (!namespacesRemoved && le instanceof SAXParseException && le.getMessage() != null
                    && le.getMessage().startsWith("The prefix "))
            {
                myDataSource.setFailureReason(FailureReason.NAMESPACE_PARSE_ERROR);
                Notify.error(myDataSource.getName() + ": Unable to parse KML input stream", Method.ALERT_HIDDEN);
            }
        }

        if (kml != null && kml.getFeature() != null)
        {
            // Convert the Kml tree to a KMLFeature tree
            rootFeature = convertToKMLFeature(kml.getFeature(), null);

            // Create a fake data source folder to wrap the document node
            if (rootFeature.getCreatingDataSource().getCreatingFeature() == null)
            {
                Folder dataSourceFolder = new Folder();
                dataSourceFolder.setName(rootFeature.getCreatingDataSource().getName());
                dataSourceFolder.addToFeature(rootFeature.getFeature());
                dataSourceFolder.setOpen(Boolean.TRUE);

                KMLFeature folderFeature = new KMLFeature(dataSourceFolder, rootFeature.getCreatingDataSource());
                folderFeature.addChild(rootFeature);

                rootFeature = folderFeature;
            }

            // Log
            LOGGER.info(StringUtilities.concat("Loaded ", rootFeature.getName()));
        }

        return rootFeature;
    }

    /**
     * HACK Removes namespaces from all XML elements in the input stream and
     * returns the result as a new input stream.
     *
     * @param inputStream The input stream
     * @return A new input stream with namespaces removed
     */
    private InputStream removeNamespaces(InputStream inputStream)
    {
        InputStream transformedInputStream = inputStream;

        LOGGER.info("Removing namespaces for " + myDataSource.getPath());

        try
        {
            String str = new StreamReader(inputStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
            str = str.replaceAll("<\\w+:", "<");
            str = str.replaceAll("</\\w+:", "</");
            str = str.replaceAll("xsi:", "");
            transformedInputStream = new ByteArrayInputStream(str.getBytes(StringUtilities.DEFAULT_CHARSET));

            myDataSource.setFailureReason(null);
        }
        catch (IOException e)
        {
            Notify.error("An error occurred while trying to cleanup a KML namespace." + e.getMessage(), Method.ALERT_HIDDEN);
            LOGGER.error(e.getMessage());

            // Prevent infinite loop
            myDataSource.setFailureReason(FailureReason.OTHER);
        }

        return transformedInputStream;
    }

    /**
     * Transforms the given input stream, substituting custom schema tags with
     * extended data. This is only needed to support pre 2.2 documents.
     *
     * @param inputStream The input stream
     * @param schemas The list of schemas
     * @return A transformed input stream
     */
    private InputStream transformSchemas(final InputStream inputStream, final Collection<? extends Schema> schemas)
    {
        // Read the first part of the input stream to get the kml namespace
        int bufferSize = 1024;
        byte[] firstBytes = new byte[bufferSize];
        int bytesRead = 0;
        try
        {
            inputStream.mark(firstBytes.length);
            bytesRead = inputStream.read(firstBytes);
            inputStream.reset();
        }
        catch (IOException e)
        {
            Notify.error("An error occurred while trying to read the KML inputstream." + e.getMessage(), Method.ALERT_HIDDEN);
            LOGGER.error(e.getMessage(), e);
        }

        // Get the kml namespace
        String namespace;
        String firstPart = new String(firstBytes, 0, bytesRead, StringUtilities.DEFAULT_CHARSET);
        Pattern p = Pattern.compile(".*xmlns\\s*=\\s*[\"'](.+?)[\"'].*", Pattern.DOTALL);
        Matcher m = p.matcher(firstPart);
        if (m.matches())
        {
            namespace = m.group(1);
        }
        else
        {
            namespace = KMLNamespaceFilterHandler.KML_DEFAULT_NAMESPACE;
        }

        // Set the default output
        InputStream transformedInputStream = inputStream;

        // Create the XSL source
        Source xslSource = createXSLSource(schemas, namespace);

        // Create the transformer factory
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setErrorListener(new TransformErrorListener());

        try
        {
            // Create the transformer
            Transformer transformer = factory.newTemplates(xslSource).newTransformer();
            transformer.setErrorListener(new TransformErrorListener());

            // Create the output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Do the transform
            transformer.transform(new StreamSource(inputStream), new StreamResult(outputStream));

            // Store the results in a new input stream
            byte[] bytes = outputStream.toByteArray();
            transformedInputStream = new ByteArrayInputStream(bytes);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Transformed XML:\n" + new String(bytes, StringUtilities.DEFAULT_CHARSET));
            }
        }
        catch (TransformerConfigurationException e)
        {
            Notify.error("A configuration exeception has occurred while reading the KML input stream." + e.getMessage(),
                    Method.ALERT_HIDDEN);
            LOGGER.error(e.getMessage(), e);
        }
        catch (TransformerException e)
        {
            Notify.error("An error occurred while transforming the KML input stream." + e.getMessage(), Method.ALERT_HIDDEN);
            LOGGER.error(e.getMessage(), e);
        }

        return transformedInputStream;
    }

    /**
     * Creates the XSL source for the given schemas.
     *
     * @param schemas The schemas
     * @param namespace The kml namespace
     * @return the XSL source
     */
    private Source createXSLSource(final Collection<? extends Schema> schemas, String namespace)
    {
        StringBuilder sb = new StringBuilder(1024);

        // Begin stylesheet
        sb.append("<?xml version=\"1.0\"?>"
                + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\" xmlns:kml=\"");
        sb.append(namespace).append("\">");

        // Process each schema
        for (Schema schema : schemas)
        {
            // Create select statement to filter out custom data tags
            StringBuilder selectStmt = new StringBuilder("*");
            if (!schema.getSimpleField().isEmpty())
            {
                selectStmt.append("[not(self::kml:ExtendedData");
                for (SimpleField field : schema.getSimpleField())
                {
                    selectStmt.append("|self::kml:").append(field.getName());
                }
                selectStmt.append(")]");
            }

            // @formatter:off

            // Replace custom placemark tags with Placemark
            // Put the non-custom (normal kml) tags at the top
            sb.append("<xsl:template match=\"kml:").append(schema.getName()).append("\">"
                    + "  <Placemark>"
                    + "    <xsl:for-each select=\"").append(selectStmt.toString()).append("\">"
                    + "      <xsl:copy>"
                    + "        <xsl:apply-templates select=\"@*|node()\"/>"
                    + "      </xsl:copy>"
                    + "    </xsl:for-each>");

            // Put the custom data tags within ExtendedData
            if (!schema.getSimpleField().isEmpty())
            {
                sb.append("<ExtendedData>"
                        + "<xsl:for-each select=\"kml:ExtendedData\">"
                        + "  <xsl:apply-templates select=\"@*|node()\"/>"
                        + "</xsl:for-each>");
                for (SimpleField field : schema.getSimpleField())
                {
                    sb.append("<xsl:for-each select=\"kml:").append(field.getName()).append("\">"
                            + "  <Data>"
                            + "    <xsl:attribute name=\"name\">").append(field.getName()).append("</xsl:attribute>"
                            + "    <value>"
                            + "      <xsl:value-of select=\".\"/>"
                            + "    </value>"
                            + "  </Data>"
                            + "</xsl:for-each>");
                }
                sb.append("</ExtendedData>");
            }

            sb.append("  </Placemark>"
                    + "</xsl:template>");
        }

        // Pass-thru on everything else
        sb.append("<xsl:template match=\"@*|node()\">"
                + "  <xsl:copy>"
                + "    <xsl:apply-templates select=\"@*|node()\"/>"
                + "  </xsl:copy>"
                + "</xsl:template>"

                // End stylesheet
                + "</xsl:stylesheet>");

        // @formatter:on

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("XSL for " + myDataSource.getName() + ":\n" + XMLUtilities.format(sb.toString()));
        }

        return new StreamSource(new StringReader(sb.toString()));
    }

    /**
     * Converts a tree of Features to a tree of KMLFeatures.
     *
     * @param feature The root feature
     * @param parent The parent feature
     * @return The KMLFeature
     */
    private KMLFeature convertToKMLFeature(Feature feature, KMLFeature parent)
    {
        KMLFeature kmlFeature = new KMLFeature(feature, myDataSource);
        kmlFeature.setResultingDataSource(createFeatureDataSource(kmlFeature));
        if (feature instanceof GroundOverlay)
        {
            GroundOverlay groundOverlay = (GroundOverlay)feature;
            if (groundOverlay.getLatLonBox() != null)
            {
                kmlFeature
                        .setGeoBoundingBox(KMLSpatialTemporalUtils.calculateGeographicBoundingBox(groundOverlay.getLatLonBox()));
            }
        }
        kmlFeature.setShowBalloon(KMLToolboxUtils.getKmlToolbox().getStyleCache().supportsBalloon(kmlFeature));

        // Tie this into the parent
        if (parent != null)
        {
            parent.addChild(kmlFeature);
        }

        // If the feature has a region, set the region to inactive initially
        // This has to happen after the parent has been set
        if (kmlFeature.getRegion() != null)
        {
            kmlFeature.setRegionActive(false);
        }

        // Get the children
        Collection<Feature> childFeatures;
        if (feature instanceof Folder)
        {
            childFeatures = ((Folder)feature).getFeature();
        }
        else if (feature instanceof Document)
        {
            childFeatures = ((Document)feature).getFeature();
        }
        else
        {
            childFeatures = Collections.emptyList();
        }

        // Add the children
        for (Feature childFeature : childFeatures)
        {
            convertToKMLFeature(childFeature, kmlFeature);
        }

        return kmlFeature;
    }

    /**
     * Returns a collection of any remote style data sources to load.
     *
     * @param rootFeature The root feature
     * @return The collection of data sources to load
     */
    private Collection<KMLDataSource> getRemoteStyleDataSources(KMLFeature rootFeature)
    {
        Collection<KMLDataSource> remoteStyleDataSources = new ArrayList<>();

        Collection<String> styleUrls = new HashSet<>();
        new StyleURLAccumulator().accumulate(rootFeature, styleUrls);

        String dataSourceUrlString = null;
        URL dataSourceUrl = KMLLinkHelper.toURL(myDataSource);
        if (dataSourceUrl != null)
        {
            dataSourceUrlString = dataSourceUrl.toExternalForm();
        }

        Collection<String> fullStyleUrls = new HashSet<>();
        for (String styleUrl : styleUrls)
        {
            // Create a data source for the remote style
            URL url = KMLLinkHelper.createFullURL(styleUrl, myDataSource);
            if (url != null)
            {
                String fullUrlString = url.toExternalForm();
                if (!fullStyleUrls.contains(fullUrlString) && !fullUrlString.equals(dataSourceUrlString))
                {
                    fullStyleUrls.add(fullUrlString);

                    KMLDataSource styleDataSource = createStyleDataSource(myDataSource, url);
                    remoteStyleDataSources.add(styleDataSource);

                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Will download style URL: " + url);
                    }
                }
            }
        }

        return remoteStyleDataSources;
    }

    /**
     * Creates an internal data source for the given feature.
     *
     * @param feature The feature from which to create the internal data source
     * @return The internal data source for the feature, or null
     */
    private static KMLDataSource createFeatureDataSource(final KMLFeature feature)
    {
        KMLDataSource internalDataSource = null;

        // Create a data source for certain features
        KMLDataSource dataSource = feature.getCreatingDataSource();
        if (feature.getFeature() instanceof NetworkLink)
        {
            NetworkLink networkLink = (NetworkLink)feature.getFeature();

            URL url = KMLLinkHelper.getFullUrlFromBasicLink(KMLFeatureUtils.getLink(networkLink), dataSource);
            if (url != null)
            {
                internalDataSource = createInternalDataSource(dataSource);
                internalDataSource.setPath(url.toExternalForm());
            }
        }
        else if (feature.getFeature() instanceof Overlay)
        {
            Overlay overlay = (Overlay)feature.getFeature();

            if (overlay.getIcon() != null && overlay.getIcon().getHref() != null)
            {
                String iconHref = StringUtilities.trim(overlay.getIcon().getHref());
                if (!isUrlLocal(feature.getDataSource(), iconHref))
                {
                    URL url = KMLLinkHelper.getFullUrlFromBasicLink(overlay.getIcon(), dataSource);
                    if (url != null)
                    {
                        internalDataSource = createInternalDataSource(dataSource);
                        internalDataSource.setPath(iconHref);
                    }
                }
            }
        }

        // Set common methods
        if (internalDataSource != null)
        {
            internalDataSource.setName(feature.getName());
            internalDataSource.setCreatingKMLFeature(feature);
            internalDataSource.setActive(feature.isVisibility().booleanValue());
            internalDataSource.setType(Type.URL);

            // Now set the name of the data source to something really unique so
            // it doesn't get confused with another data source of the same name
            internalDataSource.setName(String.valueOf(internalDataSource.hashCode()));
        }

        return internalDataSource;
    }

    /**
     * Creates a data source for the given style URL.
     *
     * @param dataSource The original data source
     * @param styleURL The style URL
     * @return The new data source for the style
     */
    private static KMLDataSource createStyleDataSource(final KMLDataSource dataSource, final URL styleURL)
    {
        KMLDataSource internalDataSource = createInternalDataSource(dataSource);
        internalDataSource.setName(styleURL.toExternalForm());
        internalDataSource.setStyleSource(true);
        internalDataSource.setPath(styleURL.toExternalForm());
        internalDataSource.setType(Type.URL);
        internalDataSource.setActive(true);
        return internalDataSource;
    }

    /**
     * Creates an internal URL data source for the given URL string.
     *
     * @param dataSource The original data source
     * @return The internal data source
     */
    private static KMLDataSource createInternalDataSource(KMLDataSource dataSource)
    {
        KMLDataSource internalDataSource = new KMLDataSource();

        // Set parent/child links
        internalDataSource.setParentDataSource(dataSource);

        internalDataSource.setIncludeInTimeline(dataSource.isIncludeInTimeline());
        internalDataSource.setUseIcons(dataSource.isUseIcons());
        internalDataSource.setClampToTerrain(dataSource.isClampToTerrain());

        internalDataSource.setOverrideDataGroupKey(dataSource.getOverrideDataGroupKey());

        return internalDataSource;
    }

    /**
     * Determine if the URL refers to a file inside a KMZ data source.
     *
     * @param dataSource The data source.
     * @param url The "URL".
     * @return {@code true} if the URL is probably local.
     */
    private static boolean isUrlLocal(KMLDataSource dataSource, String url)
    {
        if (dataSource.getContentType() == KMLContentType.KMZ)
        {
            // Attempt to create a URL from the path to determine if the
            // path is local or external.
            try
            {
                new URL(url);
                return false;
            }
            catch (MalformedURLException e)
            {
                // The path is not a valid URL, so assume it is local.
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Schema feature accumulator.
     */
    private static class SchemaFeatureAccumulator extends KMLFeatureAccumulator<Schema>
    {
        @Override
        protected boolean process(KMLFeature feature, Collection<? super Schema> values)
        {
            if (feature.getFeature() instanceof Document)
            {
                Document document = (Document)feature.getFeature();
                values.addAll(document.getSchema());
            }
            return true;
        }
    }

    /**
     * Style URL accumulator.
     */
    private static class StyleURLAccumulator extends KMLFeatureAccumulator<String>
    {
        @Override
        protected boolean process(KMLFeature feature, Collection<? super String> values)
        {
            String styleUrl = feature.getStyleUrl();
            if (styleUrl != null)
            {
                // Only download remote style URLs
                styleUrl = styleUrl.trim();
                int hashIndex = styleUrl.indexOf('#');
                if (hashIndex > 0)
                {
                    // Remove the trailing style identifier for the data source
                    styleUrl = styleUrl.substring(0, hashIndex);

                    values.add(styleUrl);
                }
            }
            return true;
        }
    }
}
