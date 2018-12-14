package io.opensphere.core.net.manager.view;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 *
 */
public class TransactionDetailPanel extends BorderPane
{
    private TextArea myTextArea;

    /**
     *
     */
    public TransactionDetailPanel()
    {
        myTextArea = new TextArea();
        myTextArea.setWrapText(true);
        setCenter(myTextArea);
    }

    public StringProperty getResponseProperty()
    {
        return myTextArea.textProperty();
    }

}
