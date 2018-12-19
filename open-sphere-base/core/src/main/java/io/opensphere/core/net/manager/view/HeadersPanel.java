package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.HttpKeyValuePair;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** A panel on which header key / value pairs are rendered. */
public class HeadersPanel extends VBox
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    /** Creates a new headers panel bound to the selected transaction. */
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
                        () -> myTransactionProperty.get() == null || myTransactionProperty.get().getStatus() == null ? null
                                : myTransactionProperty.get().getStatus().toString(),
                        myTransactionProperty));
        getChildren().addAll(new HBox(new Label("Status Code: "), statusLabel));

        ListView<HttpKeyValuePair> responseList = new ListView<>();
        responseList.setCellFactory(p -> new HeaderListCell());
        TitledPane responsePane = new TitledPane("Response Headers", responseList);

        ListView<HttpKeyValuePair> requestList = new ListView<>();
        requestList.setCellFactory(p -> new HeaderListCell());
        TitledPane requestPane = new TitledPane("Request Headers", requestList);

        VBox headersAccordion = new VBox(responsePane, requestPane);
        responsePane.setExpanded(true);
        requestPane.setExpanded(true);
        myTransactionProperty.addListener((obs, ov, nv) ->
        {
            if (nv != null)
            {
                responseList.itemsProperty().get().clear();
                requestList.itemsProperty().get().clear();
                responseList.itemsProperty().get().addAll(myTransactionProperty.get().getResponseHeaders());
                requestList.itemsProperty().get().addAll(myTransactionProperty.get().getRequestHeaders());
            }
            else
            {
                responseList.itemsProperty().get().clear();
                requestList.itemsProperty().get().clear();
            }
            responseList.refresh();
            requestList.refresh();
        });
        getChildren().add(headersAccordion);
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
