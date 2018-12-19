package io.opensphere.mantle.icon.chooser.view;

import java.awt.Dimension;
import java.awt.Window;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.controller.IconCustomizationController;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Main UI Frame. */
@SuppressWarnings("serial")
public class IconDialog extends JFXDialog
{
    /** The model to be shared between all the UI elements. */
    private final IconModel myPanelModel;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The consumer called when the user presses the accept button. */
    private Consumer<IconRecord> myAcceptListener;

    /** The controller used for managing customization operations. */
    private IconCustomizationController myCustomizationController;

    /**
     * Creates a new dialog with the supplied toolbox and owner.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param owner the window to which the dialog is to be bound.
     */
    public IconDialog(Toolbox toolbox, Window owner)
    {
        this(toolbox, owner, null);
    }

    /**
     * Creates a new dialog with the supplied toolbox and owner.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param owner the window to which the dialog is to be bound.
     * @param acceptListener the consumer called when the user presses the
     *            accept button (may be null).
     */
    public IconDialog(Toolbox toolbox, Window owner, Consumer<IconRecord> acceptListener)
    {
        this(toolbox, owner, acceptListener, null);
    }

    /**
     * Creates a new dialog with the supplied toolbox and owner.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param owner the window to which the dialog is to be bound.
     * @param acceptListener the consumer called when the user presses the
     *            accept button (may be null).
     * @param rejectListener the listener called when the user presses the
     *            cancel button (may be null).
     */
    public IconDialog(Toolbox toolbox, Window owner, Consumer<IconRecord> acceptListener, Runnable rejectListener)
    {
        this(toolbox, owner, acceptListener, rejectListener, null);
    }

    /**
     * Creates a new dialog with the supplied toolbox and owner.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param owner the window to which the dialog is to be bound.
     * @param acceptListener the consumer called when the user presses the
     *            accept button (may be null).
     * @param rejectListener the listener called when the user presses the
     *            cancel button (may be null).
     * @param initialValueSupplier the supplier used to populate the initially
     *            selected icon in the dialog (may be null).
     */
    public IconDialog(Toolbox toolbox, Window owner, Consumer<IconRecord> acceptListener, Runnable rejectListener,
            Supplier<String> initialValueSupplier)
    {
        super(owner, "Icon Manager", true);
        myToolbox = toolbox;
        myPanelModel = new IconModel(myToolbox);
        myPanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry());

        IconView iconView = new IconView(myPanelModel);
        myCustomizationController = new IconCustomizationController(myPanelModel, iconView);

        setInitialValueSupplier(initialValueSupplier);

        setMinimumSize(new Dimension(800, 600));
        setSize(875, 600);
        setFxNode(iconView);

        if (myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth() != 0)
        {
            myPanelModel.tileWidthProperty().set(myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth());
        }

        myAcceptListener = acceptListener;
        super.setAcceptListener(() ->
        {
            savePreferences();
            if (myAcceptListener != null)
            {
                myAcceptListener.accept(myPanelModel.selectedRecordProperty().get());
            }
        });
        setRejectListener(rejectListener);
        setLocationRelativeTo(owner);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.JFXDialog#setAcceptListener(java.lang.Runnable)
     */
    @Override
    public final void setAcceptListener(Runnable r)
    {
        throw new UnsupportedOperationException(
                "Unable to re-assign the accept listener. Call setAcceptListener(Consumer) instead.");
    }

    /**
     * Sets the value of the {@link #myAcceptListener} field.
     *
     * @param acceptListener the value to store in the {@link #myAcceptListener}
     *            field.
     */
    public void setAcceptListener(Consumer<IconRecord> acceptListener)
    {
        myAcceptListener = acceptListener;
    }

    /**
     * Sets the supplier used to populate the dialog with it's initially
     * selected icon.
     *
     * @param supplier the supplier used to populate the dialog.
     */
    public void setInitialValueSupplier(Supplier<String> supplier)
    {
        if (supplier != null)
        {
            URL url = UrlUtilities.toURL(supplier.get());
            if (url != null)
            {
                myPanelModel.selectedRecordProperty().set(myPanelModel.getIconRegistry().getIconRecord(url));
            }
        }
    }

    /**
     * Saves icon manager preferences such as display width, display view, tree
     * selection. This ONLY saves during session. NOT across sessions.
     */
    private void savePreferences()
    {
        MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().getManagerPrefs()
                .setIconWidth((int)myPanelModel.tileWidthProperty().get());
    }

    /**
     * Gets the model.
     *
     * @return the model used for the UI.
     */
    public IconModel getPanelModel()
    {
        return myPanelModel;
    }
}
