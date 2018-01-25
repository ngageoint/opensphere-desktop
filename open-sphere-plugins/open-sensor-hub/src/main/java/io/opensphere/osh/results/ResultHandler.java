package io.opensphere.osh.results;

import java.io.IOException;
import java.util.List;

import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;

/** A handler of results. */
public interface ResultHandler
{
    /**
     * Determines if the output can be handled by this handler.
     *
     * @param outputs the output
     * @return whether the output can be handled
     */
    List<Output> canHandle(List<Output> outputs);

    /**
     * Performs any additional initialization on the data type.
     *
     * @param dataType the data type
     */
    void initializeType(OSHDataTypeInfo dataType);

    /**
     * Gets the property to query from the offering/output.
     *
     * @param offering the offering
     * @param output the output
     * @return the property to query
     */
    String getQueryProperty(Offering offering, Output output);

    /**
     * Handles results.
     *
     * @param dataType the data type
     * @param outputs The outputs related to the streams.
     * @param streams the result streams
     * @throws IOException if an problem occurs reading the stream
     */
    void handleResults(OSHDataTypeInfo dataType, List<Output> outputs, List<CancellableInputStream> streams) throws IOException;

    /**
     * Handles a change in group activation.
     *
     * @param dataType the data type
     * @param state the activation state
     */
    void handleGroupActivation(DataTypeInfo dataType, ActivationState state);
}
