package io.opensphere.server.source;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Interface used to validate the current contents of an editor. Allows a class
 * to test whether the current editor seems valid and, if not, provide a
 * description of why it thinks it's not.
 */
public abstract class EditorValidResult
{
    /** Flag indicating whether validation was successful. */
    private boolean myIsValid;

    /**
     * Static factory method to create a new {@link EditorValidResult} that
     * accumulates errors and strings them together when "getError" is called.
     *
     * @return the accumulating {@link EditorValidResult}
     */
    public static EditorValidResult getAccumulatingResult()
    {
        return new AccumulatingEditorValidResult();
    }

    /**
     * Static factory method to create a new {@link EditorValidResult} that
     * launches a dialog with an error message to the user when a error occurs.
     *
     * @param parent the parent window for displaying warnings to user
     * @return the alerting {@link EditorValidResult}
     */
    public static EditorValidResult getAlertingResult(Component parent)
    {
        return new AlertingEditorValidResult(parent);
    }

    /**
     * Static factory method to create a default {@link EditorValidResult}.
     *
     * @return the default {@link EditorValidResult}
     */
    public static EditorValidResult getDefaultResult()
    {
        return new DefaultEditorValidResult();
    }

    /**
     * Gets the full error description. This is used to describe the error to a
     * level of detail that would allow a user to rectify the error in the
     * editor.
     *
     * @return the error
     */
    public abstract String getError();

    /**
     * Gets a very brief, high-level description of the error. This is generally
     * a 3-5 word description that categorizes the error. Examples include:
     * "Socket connection error" or "Invalid URL format"
     *
     * @return the error brief
     */
    public abstract String getErrorBrief();

    /**
     * Result of validating that the current contents of the editor are valid.
     *
     * @return true, if editor contents are valid
     */
    public boolean isValid()
    {
        return myIsValid;
    }

    /**
     * Sets the editor validation to error and provides a description of the
     * error.
     *
     * @param brief brief description that categorizes the error
     * @param error the full error description
     */
    public abstract void setError(String brief, String error);

    /**
     * Sets the editor validation to successful.
     */
    public void setSuccess()
    {
        setIsValid(true);
    }

    /**
     * Sets the isValid flag.
     *
     * @param isValid the new isValid value
     */
    protected void setIsValid(boolean isValid)
    {
        myIsValid = isValid;
    }

    /**
     * Implementation of {@link EditorValidResult} that launches a dialog with
     * an error message to the user when a error occurs.
     */
    public static class AlertingEditorValidResult extends DefaultEditorValidResult
    {
        /**
         * The parent component used to properly locate error dialogs on the
         * screen.
         */
        private final Component myParent;

        /**
         * Instantiates a new prompting editor valid result.
         *
         * @param parent the parent window for displaying warnings to user
         */
        public AlertingEditorValidResult(Component parent)
        {
            myParent = parent;
        }

        @Override
        public void setError(String brief, String error)
        {
            super.setError(brief, error);
            JOptionPane.showMessageDialog(myParent, getError(), getErrorBrief(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Implementation of {@link EditorValidResult} that accumulates errors and
     * strings them together when "getError" is called.
     */
    protected static class AccumulatingEditorValidResult extends EditorValidResult
    {
        /**
         * Brief description that categorizes the type of error that occurred.
         */
        private String myErrorCategory;

        /** The full error description. */
        private final List<String> myErrorDescriptions = New.list();

        @Override
        public String getError()
        {
            return StringUtilities.join(" : ", myErrorDescriptions);
        }

        @Override
        public String getErrorBrief()
        {
            return myErrorCategory;
        }

        @Override
        public void setError(String brief, String error)
        {
            setIsValid(false);
            myErrorDescriptions.add(error);
            if (myErrorDescriptions.size() == 1)
            {
                myErrorCategory = brief;
            }
            else
            {
                myErrorCategory = "Multiple Validation Errors";
            }
        }

        @Override
        public void setSuccess()
        {
            if (!CollectionUtilities.hasContent(myErrorDescriptions))
            {
                super.setSuccess();
            }
        }
    }

    /**
     * Default implementation of the {@link EditorValidResult} interface.
     */
    protected static class DefaultEditorValidResult extends EditorValidResult
    {
        /**
         * Brief description that categorizes the type of error that occurred.
         */
        private String myErrorCategory;

        /** The full error description. */
        private String myErrorDescription;

        @Override
        public String getError()
        {
            return myErrorDescription;
        }

        @Override
        public String getErrorBrief()
        {
            return myErrorCategory;
        }

        @Override
        public void setError(String brief, String error)
        {
            setIsValid(false);
            myErrorCategory = brief;
            myErrorDescription = error;
        }
    }
}
