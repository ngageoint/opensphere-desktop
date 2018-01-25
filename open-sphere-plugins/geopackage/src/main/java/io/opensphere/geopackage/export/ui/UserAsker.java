package io.opensphere.geopackage.export.ui;

/**
 * Asks the user a yes no question and returns the results.
 */
public interface UserAsker
{
    /**
     * Asks the user the given question and return the results.
     *
     * @param question The question to ask.
     * @param title The title of the question.
     * @return True if the user answered yes, false if the user answered no.
     */
    boolean askYesNo(String question, String title);
}
