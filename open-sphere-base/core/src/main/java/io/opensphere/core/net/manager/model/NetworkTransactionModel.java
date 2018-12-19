package io.opensphere.core.net.manager.model;

import java.util.Map;
import java.util.stream.Collectors;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.ObservableBuffer;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/** The model in which network transactions are stored. */
public class NetworkTransactionModel
{
    /** The transactions stored by the model. */
    // private final ObservableList<NetworkTransaction> myTransactions;
    private final ObservableBuffer<NetworkTransaction> myTransactions;

    /** A lookup table mapping the transaction ID to the transaction. */
    private final Map<String, NetworkTransaction> myTransactionDictionary;

    /**
     * Creates a new model.
     */
    public NetworkTransactionModel()
    {
        // myTransactions = FXCollections.observableArrayList();
        myTransactions = new ObservableBuffer<>(100);
        myTransactionDictionary = New.map();

        myTransactions.addListener((ListChangeListener.Change<? extends NetworkTransaction> e) ->
        {
            while (e.next())
            {
                if (e.wasAdded())
                {
                    Map<String, NetworkTransaction> newEvents = e.getAddedSubList().stream()
                            .collect(Collectors.toMap(t -> t.getTransactionId(), t -> t));
                    myTransactionDictionary.putAll(newEvents);
                }

                if (e.wasRemoved())
                {
                    e.getRemoved().stream().forEach(t -> myTransactionDictionary.remove(t.getTransactionId()));
                }
                // permutations can be ignored
            }
        });
    }

    /**
     * Gets the value of the {@link #myTransactions} field.
     *
     * @return the value stored in the {@link #myTransactions} field.
     */
    public ObservableList<NetworkTransaction> getTransactions()
    {
        return myTransactions;
    }

    /**
     * Gets the transaction associated with the supplied ID.
     *
     * @param transactionId the ID for which to get the transaction.
     * @return the transaction associated with the supplied ID, or null if none
     *         is known.
     */
    public NetworkTransaction getTransaction(String transactionId)
    {
        return myTransactionDictionary.get(transactionId);
    }
}
