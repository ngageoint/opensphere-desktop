package io.opensphere.mantle.iconproject.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class GenIconPane extends BorderPane
{

    public GenIconPane()
    {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("http://www.google.com");
        setCenter(browser);
    }

}
