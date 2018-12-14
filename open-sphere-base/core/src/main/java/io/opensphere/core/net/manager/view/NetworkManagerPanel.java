/**
 *
 */
package io.opensphere.core.net.manager.view;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.net.manager.controller.NetworkManagerController;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.net.manager.model.NetworkTransactionModel;
import io.opensphere.core.util.lang.StringUtilities;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 */
public class NetworkManagerPanel extends VBox
{
    /** The table view in which the network events are rendered. */
    private final TableView<NetworkTransaction> myNetworkTableView;

    private final TransactionDetailPanel myDetailPanel;

    /** The model in which the transactions are stored for the table view. */
    private final ObservableList<NetworkTransaction> myEvents;

    /** The model backing the panel. */
    private final NetworkTransactionModel myModel;

    /** The controller used to orchestrate the network manager. */
    private final NetworkManagerController myController;

    /**
     * Creates a new network manager panel.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param controller the controller through which the manager is
     *            orchestrated.
     */
    public NetworkManagerPanel(Toolbox toolbox, NetworkManagerController controller)
    {
        myController = controller;
        myModel = myController.getModel();
        myEvents = myModel.getTransactions();

        myNetworkTableView = new TableView<>(myEvents);
        myNetworkTableView.setStyle("-fx-table-cell-border-color: transparent;");

        TableColumn<NetworkTransaction, Integer> statusColumn = new TableColumn<>("Status");
        statusColumn.prefWidthProperty().set(25);
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        myNetworkTableView.getColumns().add(statusColumn);

        TableColumn<NetworkTransaction, String> methodColumn = new TableColumn<>("Method");
        methodColumn.prefWidthProperty().set(75);
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("requestMethod"));
        myNetworkTableView.getColumns().add(methodColumn);

        TableColumn<NetworkTransaction, String> fileColumn = new TableColumn<>("File");
        fileColumn.prefWidthProperty().set(250);
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
        myNetworkTableView.getColumns().add(fileColumn);

        TableColumn<NetworkTransaction, String> domainColumn = new TableColumn<>("Domain");
        domainColumn.prefWidthProperty().set(100);
        domainColumn.setCellValueFactory(new PropertyValueFactory<>("domain"));
        myNetworkTableView.getColumns().add(domainColumn);

        TableColumn<NetworkTransaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.prefWidthProperty().set(50);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("contentType"));
        myNetworkTableView.getColumns().add(typeColumn);

        TableColumn<NetworkTransaction, Long> sentColumn = new TableColumn<>("Sent");
        sentColumn.prefWidthProperty().set(25);
        sentColumn.setCellValueFactory(new PropertyValueFactory<>("bytesSent"));
        myNetworkTableView.getColumns().add(sentColumn);

        TableColumn<NetworkTransaction, Long> receivedColumn = new TableColumn<>("Received");
        receivedColumn.prefWidthProperty().set(25);
        receivedColumn.setCellValueFactory(new PropertyValueFactory<>("bytesReceived"));
        myNetworkTableView.getColumns().add(receivedColumn);

        VBox.setVgrow(myNetworkTableView, Priority.ALWAYS);
        getChildren().add(myNetworkTableView);

        myDetailPanel = new TransactionDetailPanel();
        VBox.setVgrow(myDetailPanel, Priority.ALWAYS);
        getChildren().add(myDetailPanel);

        myNetworkTableView.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) ->
        {
            if (nv != null)
            {
                if (nv.getResponseBody() != null)
                {
                    try
                    {
                        myDetailPanel.getResponseProperty()
                                .set(IOUtils.toString(nv.getResponseBody(), StringUtilities.DEFAULT_CHARSET));
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
