package io.opensphere.core.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;

/**
 * This class holds and displays information pertaining to the "About" dialog
 * window.
 */
public class AboutDialog extends JDialog
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AboutDialog.class);

    /** The image we will use. */
    private static final String IMAGE = "/images/about.png";

    /** The minimum height. */
    private static final int MIN_HEIGHT = 450;

    /** The minimum width. */
    private static final int MIN_WIDTH = 550;

    /** The serial id. */
    private static final long serialVersionUID = 1L;

    /** The main content pane. */
    private JPanel myContentPane;

    /** The software description. */
    private String myDescription;

    /** The software version. */
    private String myVersion;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Default Constructor.
     *
     * @param toolbox The toolbox through which application state is accessed.
     */
    public AboutDialog(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        setTitle("About z");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(getAboutContentPane());
        setAlwaysOnTop(true);
        pack();
        setSize(MIN_WIDTH, MIN_HEIGHT);
        setMinimumSize(getSize());
        setPreferredSize(getMinimumSize());
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    /**
     * Accessor for the content pane.
     *
     * @return The content pane.
     */
    private JPanel getAboutContentPane()
    {
        if (myContentPane == null)
        {
            myContentPane = new JPanel();
            myContentPane.setLayout(new BorderLayout());
            myContentPane.add(getScrollPane(), BorderLayout.CENTER);
        }
        return myContentPane;
    }

    /**
     * Determine the proper dimensions from the about image.
     *
     * @return The dimensions.
     */
    private Dimension getDimensions()
    {
        int width = 0;
        int height = 0;
        final float widthFactor = 0.1f;
        final float heightFactor = 0.3f;

        try
        {
            width = ImageIO.read(getImageUrl()).getWidth();
            width += (int)(width * widthFactor);
        }
        catch (IOException e)
        {
            LOGGER.debug("IO Exception encountered while attempting to read image URL.", e);
            width = MIN_WIDTH;
        }

        try
        {
            height = ImageIO.read(getImageUrl()).getHeight();
            height += (int)(height * heightFactor);
        }
        catch (IOException e)
        {
            LOGGER.debug("IO Exception encountered while attempting to read image URL.", e);
            height = MIN_HEIGHT;
        }

        width = width < MIN_WIDTH ? MIN_WIDTH : width;
        height = height < MIN_HEIGHT ? MIN_HEIGHT : height;
        return new Dimension(width, height);
    }

    /**
     * Create the HTML that will be displayed in the "About" frame.
     *
     * @return A string representation of HTML.
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private String getHtml()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" + "<html>\n" + "  <head>\n"
                + "  </head>\n" + "  <body bgcolor=\"#8A8A8A\">\n" + "    <center>\n" + "      <img src=\"")
                .append(getImageUrl().toString()).append("\"><br />\n" + "        <bold>" + "           ").append(getToolTitle())
                .append("<br />\n" + "           ").append(getVersion())
                .append("<br />\n" + "        </bold>" + "    </center>\n" + "  </body>" + "</html>\n");

        return sb.toString();
    }

    /**
     * Accessor for the HTML view.
     *
     * @return The HTML view.
     */
    private Component getHtmlView()
    {
        JTextPane tp = new JTextPane();
        tp.setContentType("text/html");
        tp.setText(getHtml());
        tp.setEditable(false);
        tp.setCaretPosition(0);

        return tp;
    }

    /**
     * Accessor for the image URL.
     *
     * @return The image URL.
     */
    private URL getImageUrl()
    {
        return AboutDialog.class.getResource(IMAGE);
    }

    /**
     * Accessor for the scroll pane.
     *
     * @return The scroll pane.
     */
    private Component getScrollPane()
    {
        JScrollPane sp = new JScrollPane();

        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        sp.setViewportView(getHtmlView());
        sp.setPreferredSize(getDimensions());
        sp.setMinimumSize(sp.getPreferredSize());

        return sp;
    }

    /**
     * Get the title of the software (specified in a config file).
     *
     * @return The title of the software.
     */
    private String getToolTitle()
    {
        if (myDescription == null)
        {
            myDescription = System.getProperty("manifest.implementation.title");
            if (myDescription == null)
            {
                myDescription = "OpenSphere ToolSuite";
            }
        }
        return myDescription;
    }

    /**
     * Get the version (specified in a config file).
     *
     * @return The version of the software.
     */
    private String getVersion()
    {
        if (StringUtils.isEmpty(myVersion))
        {
            myVersion = System.getProperty("manifest.implementation.version");
            if (StringUtils.isEmpty(myVersion))
            {
                myVersion = "Developer's Working Copy";
            }
        }
        return myVersion;
    }
}
