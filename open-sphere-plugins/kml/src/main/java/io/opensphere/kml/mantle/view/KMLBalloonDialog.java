package io.opensphere.kml.mantle.view;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import io.opensphere.core.cache.matcher.StringPropertyMatcher;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.javafx.WebDialog;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLFeatureUtils;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLLinkHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * KML Balloon Dialog.
 */
public class KMLBalloonDialog extends WebDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLBalloonDialog.class);

    /** The temp files that were created. */
    private final Collection<Path> myTmpFiles = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     */
    public KMLBalloonDialog(Frame owner)
    {
        super(owner);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                ThreadUtilities.runBackground(KMLBalloonDialog.this::deleteTmpFiles);
            }
        });
    }

    /**
     * Show the dialog.
     *
     * @param feature The feature
     */
    public void show(final KMLFeature feature)
    {
        if (KMLToolboxUtils.getKmlToolbox().getStyleCache().supportsBalloon(feature))
        {
            // Get stuff from the feature
            Color bgColor = null;
            String balloonStyleText = null;
            if (feature.getDataSource() != null)
            {
                Style style = KMLToolboxUtils.getKmlToolbox().getStyleCache().getStyle(feature, StyleState.NORMAL);
                if (style != null && style.getBalloonStyle() != null)
                {
                    bgColor = KMLSpatialTemporalUtils.convertColor(style.getBalloonStyle().getBgColor());
                    balloonStyleText = style.getBalloonStyle().getText();
                }
            }
            String title = StringUtilities.removeHTML(feature.getName());
            String text = createBalloonText(feature, balloonStyleText);

            // Update and show the dialog
            setBackground(bgColor);
            setTitle(title);
            if (feature.getDataSource() != null)
            {
                FXUtilities.runOnFXThread(() -> transformImagesThenShow(text, feature.getDataSource()));
            }
            else
            {
                showContent(text);
            }
        }
        else
        {
            setVisible(false);
        }
    }

    /**
     * Creates the balloon text.
     *
     * @param feature The feature
     * @param balloonStyleText The balloon style text
     * @return The replaced balloon text
     */
    private String createBalloonText(KMLFeature feature, String balloonStyleText)
    {
        String balloonText = null;
        if (balloonStyleText != null)
        {
            balloonText = replaceVariables(feature, balloonStyleText);
        }
        else if (feature.getDescription() != null)
        {
            balloonText = feature.getDescription();
        }
        else if (feature.getExtendedData() != null)
        {
            balloonText = createExtendedDataTable(feature);
        }
        return balloonText;
    }

    /**
     * Creates the extended data html table for the given feature.
     *
     * @param feature The feature
     * @return The extended data html table
     */
    private String createExtendedDataTable(KMLFeature feature)
    {
        StringBuilder sb = new StringBuilder(64).append("<html><body>");

        Map<String, String> extendedDataMap = KMLFeatureUtils.getExtendedDataMap(feature.getFeature());
        if (!extendedDataMap.isEmpty())
        {
            sb.append("<table border='1' cellspacing='0' cellpadding='2'>");
            for (Map.Entry<String, String> entry : extendedDataMap.entrySet())
            {
                sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
            }
            sb.append("</table>");
        }

        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * Replaces the variables in balloonStyleText with values from the feature.
     *
     * @param feature The feature
     * @param balloonStyleText The balloon style text
     * @return The balloonStyleText with variables replaced
     */
    @SuppressWarnings("deprecation")
    private String replaceVariables(KMLFeature feature, String balloonStyleText)
    {
        String balloonText = balloonStyleText.trim();

        Map<String, String> replacementMap = New.map();

        // Add the standard replacements
        replacementMap.put("name", feature.getName());
        replacementMap.put("description", feature.getDescription());
        replacementMap.put("address", feature.getFeature().getAddress());
        replacementMap.put("id", feature.getFeature().getId());
        replacementMap.put("Snippet",
                feature.getFeature().getSnippet() != null ? feature.getFeature().getSnippet().getValue() : "");

        // Add the extended data replacements
        if (feature.getExtendedData() != null)
        {
            for (Data data : feature.getExtendedData().getData())
            {
                String fieldName = StringUtilities.safeTrim(data.getName());
                replacementMap.put(fieldName, data.getValue());
                String displayKey = fieldName + "/displayName";
                String displayName = !StringUtils.isBlank(data.getDisplayName()) ? data.getDisplayName() : data.getName();
                replacementMap.put(displayKey, displayName);
            }
            for (SchemaData schemaData : feature.getExtendedData().getSchemaData())
            {
                for (SimpleData simpleData : schemaData.getSimpleData())
                {
                    String fieldName = StringUtilities.safeTrim(simpleData.getName());
                    replacementMap.put(fieldName, simpleData.getValue());
                    String displayKey = fieldName + "/displayName";
                    // TODO this should use displayName instead of fieldName
                    replacementMap.put(displayKey, fieldName);
                }
            }
        }

        // Perform the replacements
        for (Map.Entry<String, String> entry : replacementMap.entrySet())
        {
            String regex = new StringBuilder("\\$\\[").append(entry.getKey()).append("\\]").toString();
            String replacement = StringUtilities.safeTrim(entry.getValue());
            balloonText = balloonText.replaceAll(regex, replacement);
        }

        return balloonText;
    }

    /**
     * Loads the web content, transforming any image URLs as necessary, then
     * loads the modified content into the dialog.
     *
     * @param content the web content
     * @param dataSource the KML data source
     */
    private void transformImagesThenShow(String content, KMLDataSource dataSource)
    {
        WebEngine engine = new WebEngine();
        engine.getLoadWorker().stateProperty()
                .addListener((observable, oldValue, newValue) -> handleContentLoaded(engine, dataSource, newValue));
        engine.loadContent(content);
    }

    /**
     * Handles the loaded content.
     *
     * @param engine the web engine
     * @param dataSource the KML data source
     * @param state the load state
     */
    private void handleContentLoaded(WebEngine engine, KMLDataSource dataSource, State state)
    {
        if (state == State.SUCCEEDED)
        {
//            ThreadUtilities.runBackground(() -> transformImageUrls(engine, dataSource));
            transformImageUrls(engine, dataSource);
        }
    }

    /**
     * Writes to the file system images that wouldn't normally load, and updates
     * the document to contain the new URLs.
     *
     * @param engine the web engine
     * @param dataSource the KML data source
     */
    private void transformImageUrls(WebEngine engine, KMLDataSource dataSource)
    {
        Document document = engine.getDocument();

        for (Node imgSrcNode : XMLUtilities.getAttributes(document, "img", "src"))
        {
            String imgSrc = imgSrcNode.getNodeValue();
            String fullImgSrc = StringUtilities.concat(KMLLinkHelper.toBaseURL(dataSource).toExternalForm(), "/", imgSrc);
            if (isMissingFile(fullImgSrc))
            {
                try (InputStream inputStream = queryImageStream(dataSource, imgSrc))
                {
                    if (inputStream != null)
                    {
                        // Write the input stream to the file system
                        Path path = Files.createTempFile("kmlImage", null);
                        writeStream(inputStream, path);
                        myTmpFiles.add(path);

                        // Update the document's image URLs
                        imgSrcNode.setNodeValue(path.toUri().toString());
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        // Show the updated content
        String newContent = XMLUtilities.format(document);
        EventQueueUtilities.invokeLater(() -> showContent(newContent));
    }

    /**
     * Determines if the given URL represents a file that is missing from the
     * file system.
     *
     * @param urlString the URL string
     * @return whether the URL is for a missing file
     */
    private static boolean isMissingFile(String urlString)
    {
        boolean isMissingFile = false;
        URL url = UrlUtilities.toURL(urlString);
        if (UrlUtilities.isFile(url))
        {
            File imageFile = new File(url.getPath());
            isMissingFile = !imageFile.exists();
        }
        return isMissingFile;
    }

    /**
     * Queries the data registry for the image input stream at the image source.
     *
     * @param dataSource the KML data source
     * @param imgSrc the image source
     * @return the image input stream, or null
     */
    private static InputStream queryImageStream(KMLDataSource dataSource, String imgSrc)
    {
        InputStream inputStream = null;

        StringPropertyMatcher matcher = new StringPropertyMatcher(KMLDataRegistryHelper.URL_PROPERTY_DESCRIPTOR, imgSrc);
        DataModelCategory category = KMLDataRegistryHelper.getIconCategory(dataSource, null);
        SimpleQuery<InputStream> imageQuery = new SimpleQuery<>(category, KMLDataRegistryHelper.ICON_PROPERTY_DESCRIPTOR,
                matcher);
        KMLToolboxUtils.getToolbox().getDataRegistry().performLocalQuery(imageQuery);

        if (imageQuery.getResults() != null && !imageQuery.getResults().isEmpty())
        {
            inputStream = imageQuery.getResults().get(0);
        }

        return inputStream;
    }

    /**
     * Deletes temp files.
     */
    private void deleteTmpFiles()
    {
        if (!myTmpFiles.isEmpty())
        {
            for (Path path : myTmpFiles)
            {
                try
                {
                    Files.delete(path);
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            myTmpFiles.clear();
        }
    }

    /**
     * Writes the input stream to the path.
     *
     * @param stream the input stream
     * @param path the path
     * @throws IOException if any exception occurs either reading or writing
     */
    private static void writeStream(InputStream stream, Path path) throws IOException
    {
        try (OutputStream out = Files.newOutputStream(path))
        {
            byte[] bytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(bytes)) > 0)
            {
                out.write(bytes, 0, bytesRead);
            }
        }
    }
}
