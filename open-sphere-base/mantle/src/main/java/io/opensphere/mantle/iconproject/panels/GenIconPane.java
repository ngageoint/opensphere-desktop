package io.opensphere.mantle.iconproject.panels;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/** Creates a Pane to enclose the web browser to generate an icon. */
public class GenIconPane extends BorderPane
{
    /** Creates the wrapper. */
    public GenIconPane()
    {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("http://www.google.com");
        setCenter(browser);
    }

}
