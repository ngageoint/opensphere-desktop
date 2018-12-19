package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.HttpKeyValuePair;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 * A panel in which the cookies used in the transaction are detailed.
 */
public class CookiePanel extends VBox
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    /** Creates a new parameters panel bound to the selected transaction. */
    public CookiePanel()
    {
        ListView<HttpKeyValuePair> list = new ListView<>();
        list.setCellFactory(p -> new HeaderListCell());
        TitledPane pane = new TitledPane("Request Cookies", list);

        VBox accordion = new VBox(pane);
        pane.setExpanded(true);
        myTransactionProperty.addListener((obs, ov, nv) ->
        {
            if (nv != null)
            {

                list.itemsProperty().get().clear();
                list.itemsProperty().get().addAll(myTransactionProperty.get().getRequestCookies());
            }
            else
            {
                list.itemsProperty().get().clear();
            }
            list.refresh();
        });
        getChildren().add(accordion);
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
