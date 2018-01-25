package io.opensphere.controlpanels.layers.base;

/**
 * Interface to an object which will ask the user yes questions and return the
 * user response.
 *
 */
@FunctionalInterface
public interface UserConfirmer
{
    /**
     * Asks the user the specified question.
     *
     * @param question The question to ask the user.
     * @param title The title of the question.
     * @return True if the user responded yes, false if the user responded no.
     */
    boolean askUser(String question, String title);
}
