package io.opensphere.core.net.manager.view;

import io.opensphere.core.Toolbox;
import io.opensphere.core.net.manager.controller.NetworkManagerController;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.net.manager.model.NetworkTransactionModel;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The panel in which the network manager displays all transactions.
 */
public class NetworkManagerPanel extends SplitPane
{
    /** The table view in which the network events are rendered. */
    private final TableView<NetworkTransaction> myNetworkTableView;

    /** Panel in which the details of the selected transaction are displayed. */
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
    public NetworkManagerPanel(final Toolbox toolbox, final NetworkManagerController controller)
    {
        orientationProperty().set(Orientation.VERTICAL);
        setDividerPositions(.1);
        myController = controller;
        myModel = myController.getModel();
        myEvents = myModel.getTransactions();

        myNetworkTableView = new TableView<>(myEvents);
        myNetworkTableView.setStyle("-fx-table-cell-border-color: transparent;");

        final DoubleBinding tableWidthProperty = myNetworkTableView.widthProperty().subtract(21);

        final TableColumn<NetworkTransaction, Integer> statusColumn = new TableColumn<>("Status");
        statusColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.0544));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        myNetworkTableView.getColumns().add(statusColumn);

        final TableColumn<NetworkTransaction, String> methodColumn = new TableColumn<>("Method");
        methodColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.0628));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("requestMethod"));
        myNetworkTableView.getColumns().add(methodColumn);

        final TableColumn<NetworkTransaction, String> domainColumn = new TableColumn<>("Domain");
        domainColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.3015));
        domainColumn.setCellValueFactory(new PropertyValueFactory<>("domain"));
        myNetworkTableView.getColumns().add(domainColumn);

        final TableColumn<NetworkTransaction, String> fileColumn = new TableColumn<>("File");
        fileColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.4012));
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
        myNetworkTableView.getColumns().add(fileColumn);

        final TableColumn<NetworkTransaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.1089));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("contentType"));
        myNetworkTableView.getColumns().add(typeColumn);

        final TableColumn<NetworkTransaction, Long> receivedColumn = new TableColumn<>("Received");
        receivedColumn.prefWidthProperty().bind(tableWidthProperty.multiply(.0712));
        receivedColumn.setCellValueFactory(new PropertyValueFactory<>("bytesReceived"));
        myNetworkTableView.getColumns().add(receivedColumn);

        getItems().add(myNetworkTableView);
        VBox.setVgrow(myNetworkTableView, Priority.ALWAYS);

        myDetailPanel = new TransactionDetailPanel();
        VBox.setVgrow(myDetailPanel, Priority.ALWAYS);
        getItems().add(myDetailPanel);

        myDetailPanel.transactionProperty().bind(myNetworkTableView.getSelectionModel().selectedItemProperty());
    }
}
