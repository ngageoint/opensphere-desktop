package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 *
 */
public class TransactionDetailPanel extends BorderPane
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    private TabPane myTabPane;

    private HeadersPanel myHeadersPanel;

    private ParametersPanel myParametersPanel;

    private ResponsePanel myResponsePanel;

    /**
     *
     */
    public TransactionDetailPanel()
    {
        myHeadersPanel = new HeadersPanel();
        myParametersPanel = new ParametersPanel();
        myResponsePanel = new ResponsePanel();

        myHeadersPanel.transactionProperty().bind(myTransactionProperty);
        myParametersPanel.transactionProperty().bind(myTransactionProperty);
        myResponsePanel.transactionProperty().bind(myTransactionProperty);

        myTabPane = new TabPane();
        myTabPane.getTabs().add(new Tab("Headers", myHeadersPanel));
        myTabPane.getTabs().add(new Tab("Parameters", myParametersPanel));
        myTabPane.getTabs().add(new Tab("Response", myResponsePanel));

        setCenter(myTabPane);
    }

    /**
     * Gets the value of the {@link #myTransactionProperty} field.
     *
     * @return the value stored in the {@link #myTransactionProperty} field.
     */
    public ObjectProperty<NetworkTransaction> transactionProperty()
    {
        return myTransactionProperty;
    }
}
