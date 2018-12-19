package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.HttpKeyValuePair;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 * The panel on which the parameters used in the transaction are detailed. F
 */
public class ParametersPanel extends VBox
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

    /** Creates a new parameters panel bound to the selected transaction. */
    public ParametersPanel()
    {
        ListView<HttpKeyValuePair> queryParametersList = new ListView<>();
        queryParametersList.setCellFactory(p -> new HeaderListCell());
        TitledPane queryParametersPane = new TitledPane("Query Parameters", queryParametersList);

        VBox parametersAccordion = new VBox(queryParametersPane);
        queryParametersPane.setExpanded(true);
        myTransactionProperty.addListener((obs, ov, nv) ->
        {
            if (nv != null)
            {
                queryParametersList.itemsProperty().get().clear();
                queryParametersList.itemsProperty().get().addAll(myTransactionProperty.get().getRequestParameters());
            }
            else
            {
                queryParametersList.itemsProperty().get().clear();
            }
            queryParametersList.refresh();
        });
        getChildren().add(parametersAccordion);
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
