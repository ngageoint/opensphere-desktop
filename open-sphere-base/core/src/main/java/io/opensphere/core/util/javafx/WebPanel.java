package io.opensphere.core.util.javafx;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import io.opensphere.core.util.awt.BrowserUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * A Swing panel that renders web content using JavaFX.
 */
public class WebPanel extends JFXPanel
{
    /** The click event type. */
    private static final String EVENT_TYPE_CLICK = "click";

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(WebPanel.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The DOM event listener. */
    private final transient EventListener myEventListener;

    /** The link handler. */
    private transient volatile Consumer<URL> myLinkHandler;

    /** The title listener. */
    private transient volatile Consumer<String> myTitleListener;

    /**
     * An optional Cascading Style Sheet that can be used to modify rendering behavior.
     */
    private transient volatile String myStylesheet;

    /** The web view. */
    private transient WebView myWebView;

    /**
     * Constructor.
     */
    public WebPanel()
    {
        super();
        myEventListener = createEventListener();
        myLinkHandler = url -> BrowserUtilities.browse(url, this);
        FXUtilities.runOnFXThread(this::initFX);
    }

    /**
     * Creates a new web panel, initialized with the supplied cascading style sheet.
     *
     * @param pStylesheet a full Cascading Style Sheet that can be used to modify rendering behavior, expressed as a String.
     */
    public WebPanel(String pStylesheet)
    {
        this();
        myStylesheet = pStylesheet;
    }

    /**
     * Gets the {@link WebEngine} used by this web panel. This call must be
     * called on the javafx thread.
     *
     * @return The underlying {@link WebEngine}.
     */
    public WebEngine getEngine()
    {
        assert Platform.isFxApplicationThread();
        return myWebView.getEngine();
    }

    /**
     * Get the web view.
     *
     * @return The web view.
     */
    public WebView getWebView()
    {
        return myWebView;
    }

    /**
     * Loads a web page into this panel.
     *
     * @param url URL of the web page to load
     */
    public void load(String url)
    {
        FXUtilities.runOnFXThread(() -> myWebView.getEngine().load(url));
    }

    /**
     * Loads the given HTML content directly.
     *
     * @param content the HTML content
     */
    public void loadContent(String content)
    {
        FXUtilities.runOnFXThread(() -> myWebView.getEngine().loadContent(content));
    }

    /**
     * Sets the link handler. A null value will result in the default behavior
     * of opening links within the panel.
     *
     * @param linkHandler the link handler
     */
    public void setLinkHandler(Consumer<URL> linkHandler)
    {
        myLinkHandler = linkHandler;
    }

    /**
     * Sets the title listener.
     *
     * @param titleListener the title listener
     */
    public void setTitleListener(Consumer<String> titleListener)
    {
        myTitleListener = titleListener;
    }

    /**
     * Adds the event listener to the document.
     *
     * @param document the document
     */
    private void addEventListener(Document document)
    {
        if (myEventListener != null)
        {
            NodeList anchorElements = document.getElementsByTagName("a");
            for (int i = 0; i < anchorElements.getLength(); ++i)
            {
                ((EventTarget)anchorElements.item(i)).addEventListener(EVENT_TYPE_CLICK, myEventListener, false);
            }
        }
    }

    /**
     * Creates the event listener that opens links in an external browser
     * instead of in the web view.
     *
     * @return the listener
     */
    private EventListener createEventListener()
    {
        return new EventListener()
        {
            @Override
            public void handleEvent(Event event)
            {
                String type = event.getType();
                if (EVENT_TYPE_CLICK.equals(type))
                {
                    handleClickEvent(event);
                }
            }

            /**
             * Handles a click event.
             *
             * @param event the event
             */
            private void handleClickEvent(Event event)
            {
                if (myLinkHandler != null)
                {
                    event.preventDefault();
                    String href = ((Element)event.getTarget()).getAttribute("href");
                    try
                    {
                        myLinkHandler.accept(new URL(href));
                    }
                    catch (MalformedURLException e)
                    {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    /**
     * Handles a change in load worker state.
     *
     * @param state the state
     */
    private void handleStateChange(State state)
    {
        if (state == State.SUCCEEDED)
        {
            Document document = myWebView.getEngine().getDocument();
            notifyTitleListener(document);
            addEventListener(document);

            if (StringUtils.isNotBlank(myStylesheet))
            {
                Element styleNode = document.createElement("style");
                styleNode.appendChild(document.createTextNode(myStylesheet));
                document.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);
            }
        }
    }

    /**
     * Initializes the JavaFX components.
     */
    private void initFX()
    {
        myWebView = new WebView();
        myWebView.setZoom(.85);
        setScene(new Scene(myWebView));

        myWebView.getEngine().getLoadWorker().stateProperty()
        .addListener((observable, oldValue, newValue) -> handleStateChange(newValue));
    }

    /**
     * Notifies the title listener of a title change.
     *
     * @param document the document
     */
    private void notifyTitleListener(Document document)
    {
        if (myTitleListener != null)
        {
            NodeList titleElements = document.getElementsByTagName("title");
            final String title = titleElements.getLength() > 0 ? titleElements.item(0).getTextContent() : null;
            EventQueueUtilities.invokeLater(() -> myTitleListener.accept(title));
        }
    }
}
