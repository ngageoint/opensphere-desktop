package io.opensphere.mantle.datasources;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * The Interface IDataSourceEditor.
 */
public interface IDataSourceEditor
{
    /**
     * Adds a change listener that listens for changes.
     *
     * @param listener the listener
     */
    void addChangeListener(ChangeListener listener);

    /**
     * If a source has been edited/changed by the editor this will return the
     * original sources but with changes in the editor applied.
     *
     * @return the changed source
     */
    IDataSource getChangedSource();

    /**
     * Gets the editor component for editing of the source.
     *
     * @return the
     */
    JPanel getEditorPane();

    /**
     * Gets the original unchanged source the editor was given from the
     * openSource function.
     *
     * @return the {@link IDataSource} original
     */
    IDataSource getOriginalSource();

    /**
     * Returns true if the config for the source has been edited.
     *
     * @return true, if successful
     */
    boolean hasChanged();

    /**
     * Validates the contents of the editor to make sure they are valid errors
     * should be poped up for the user if not valid.
     *
     * @param listener the listener
     */
    void isEditorValid(ValidationDispostionListener listener);

    /**
     * Opens up the provided source and populates any GUI configurations.
     *
     * @param source the source
     * @param isNew the is new
     * @param otherSources the other sources
     * @param titleLabel the title label
     */
    void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources, JLabel titleLabel);

    /**
     * Removes a change detection listener.
     *
     * @param listener the listener
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Forces the editor into a changed state.
     */
    void setChanged();

    /**
     * This method is called when the cancel button is pressed It should only be
     * used for cleaning up resources if necessary.
     */
    void wasCancelled();
}
