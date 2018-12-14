package io.opensphere.core.net.manager.view;

import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;

/**
 *
 */
public class ParametersPanel extends VBox
{
    /**
     * The model of the network transaction currently being detailed by the
     * panel.
     */
    private final ObjectProperty<NetworkTransaction> myTransactionProperty = new ConcurrentObjectProperty<>();

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
