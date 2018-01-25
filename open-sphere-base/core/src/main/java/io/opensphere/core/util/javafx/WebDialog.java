package io.opensphere.core.util.javafx;

import java.awt.Window;

import javax.swing.JDialog;

/**
 * A Swing dialog that renders web content using JavaFX.
 */
public class WebDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The web panel. */
    private final WebPanel myWebPanel;

    /**
     * Constructor.
     *
     * @param owner the owner
     */
    public WebDialog(Window owner)
    {
        super(owner);
        myWebPanel = new WebPanel();
        add(myWebPanel);
        setSize(600, 600);
        setLocationRelativeTo(owner);
    }

    /**
     * Loads a web page into this panel.
     *
     * @param url URL of the web page to load
     */
    public void load(String url)
    {
        myWebPanel.load(url);
    }

    /**
     * Loads the given HTML content directly.
     *
     * @param content the HTML content
     */
    public void loadContent(String content)
    {
        myWebPanel.loadContent(content);
    }

    /**
     * Shows the given HTML content directly.
     *
     * @param content the HTML content
     */
    public void showContent(String content)
    {
        setVisible(true);
        loadContent(content);
    }
}
