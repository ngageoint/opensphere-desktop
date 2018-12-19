package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * A panel implementation in which details of a specific network transaction are
 * displayed.
 */
public class TransactionDetailPanel extends BorderPane
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    /** The panel on which the tabs are displayed. */
    private TabPane myTabPane;

    /** The panel on which the headers are displayed. */
    private HeadersPanel myHeadersPanel;

    /** The panel on which the parameters are displayed. */
    private ParametersPanel myParametersPanel;

    /** The panel on which cookies are displayed. */
    private CookiePanel myCookiePanel;

    /** Creates a new detail panel. */
    public TransactionDetailPanel()
    {
        myHeadersPanel = new HeadersPanel();
        myParametersPanel = new ParametersPanel();
        myCookiePanel = new CookiePanel();

        myHeadersPanel.transactionProperty().bind(myTransactionProperty);
        myParametersPanel.transactionProperty().bind(myTransactionProperty);
        myCookiePanel.transactionProperty().bind(myTransactionProperty);

        myTabPane = new TabPane();
        myTabPane.getTabs().add(new Tab("Headers", myHeadersPanel));
        myTabPane.getTabs().add(new Tab("Parameters", myParametersPanel));
        myTabPane.getTabs().add(new Tab("Cookies", myCookiePanel));

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
