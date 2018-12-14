package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 */
public class HeadersPanel extends VBox
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    /**
     */
    public HeadersPanel()
    {
        Label urlLabel = new Label();
        urlLabel.textProperty().bind(Bindings.createStringBinding(
                () -> myTransactionProperty.get() == null ? null : myTransactionProperty.get().getUrl(), myTransactionProperty));
        getChildren().addAll(new HBox(new Label("Request URL: "), urlLabel));

        Label methodLabel = new Label();
        methodLabel.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> myTransactionProperty.get() == null ? null : myTransactionProperty.get().getRequestMethod(),
                        myTransactionProperty));
        getChildren().addAll(new HBox(new Label("Request Method: "), methodLabel));

        Label statusLabel = new Label();
        statusLabel.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> myTransactionProperty.get() == null ? null : myTransactionProperty.get().getStatus().toString(),
                        myTransactionProperty));
        getChildren().addAll(new HBox(new Label("Status Code: "), statusLabel));

        new Label("Response Headers");
        new Label("Request Headers");

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
