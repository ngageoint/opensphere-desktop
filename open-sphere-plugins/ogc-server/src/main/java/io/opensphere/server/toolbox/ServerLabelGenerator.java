package io.opensphere.server.toolbox;

/**
 * Defines a contract for a label generator. A label generator is used to create
 * a snippet of human readable text to describe a server type.
 */
public interface ServerLabelGenerator
{
    /**
     * Builds a label from the supplied server type.
     *
     * @param type the type of server instantiation
     * @return a string that identifies the type to a user
     */
    String buildLabelFromType(String type);
}
