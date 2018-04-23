package io.opensphere.core.options.impl;

import javax.swing.JComponent;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import io.opensphere.core.util.fx.FXUtilities;

/**
 * A generic JavaFX-based options provider.
 */
public abstract class AbstractJFXOptionsProvider extends AbstractOptionsProvider
{
    /**
     * Creates a new options provider, using the supplied topic name.
     *
     * @param topic the topic to apply to the provider.
     */
    public AbstractJFXOptionsProvider(String topic)
    {
        super(topic);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractOptionsProvider#usesApply()
     */
    @Override
    public boolean usesApply()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractOptionsProvider#usesRestore()
     */
    @Override
    public boolean usesRestore()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#applyChanges()
     */
    @Override
    public void applyChanges()
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#getOptionsPanel()
     */
    @Override
    public JComponent getOptionsPanel()
    {
        JFXPanel panel = new JFXPanel();

        Platform.runLater(() ->
        {
            Node form = getJFXOptionsPanel();

            HBox hbox = new HBox();
            hbox.setPadding(new Insets(10, 10, 10, 10));
            hbox.getChildren().add(form);
            HBox.setHgrow(form, Priority.ALWAYS);

            Scene scene = new Scene(hbox);
            panel.setScene(FXUtilities.addDesktopStyle(scene));
        });

        return panel;
    }

    /**
     * Gets the JavaFX UI panel with the controls for this provider. The panel
     * does not need to implement its own Apply/Cancel/RestoreDefaults UI
     * Buttons, that will be handled elsewhere.
     *
     * @return the options panel
     */
    public abstract Node getJFXOptionsPanel();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#restoreDefaults()
     */
    @Override
    public void restoreDefaults()
    {
        /* intentionally blank */
    }
}
