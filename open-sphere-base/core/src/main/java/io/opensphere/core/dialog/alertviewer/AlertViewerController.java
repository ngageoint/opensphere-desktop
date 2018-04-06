package io.opensphere.core.dialog.alertviewer;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.dialog.alertviewer.toast.ToastController;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.swing.EventQueueUtilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** The alert viewer controller. */
@ThreadSafe
class AlertViewerController extends EventListenerService
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The alerts. */
    @ThreadConfined("JavaFX")
    private final ObservableList<Alert> myAlerts = FXCollections.observableArrayList();

    /** Controller responsible for showing toast messages. */
    private final ToastController myToaster;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public AlertViewerController(Toolbox toolbox)
    {
        super(toolbox.getEventManager(), 1);
        myToolbox = toolbox;
        myToaster = new ToastController(toolbox.getUIRegistry().getMainFrameProvider());
        // Both PlatformImpl and this will throw if FX is already started; this
        // really should not be called
        Platform.startup(() ->
        {
        });
        bindEvent(UserMessageEvent.class, this::handleUserMessageEvent);
    }

    @Override
    public void open()
    {
        EventQueueUtilities.invokeLater(this::openEDT);
        super.open();
    }

    @Override
    public void close()
    {
        super.close();
        myToolbox.getUIRegistry().getToolbarComponentRegistry().deregisterToolbarComponent(ToolbarLocation.SOUTH, "AlertViewer");
        myToaster.stop();
    }

    /** Opens stuff on the EDT. */
    private void openEDT()
    {
        final AlertViewerButton button = new AlertViewerButton(myToolbox, myAlerts);
        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.SOUTH, "AlertViewer",
                button, 200, SeparatorLocation.NONE);

        JMenuItem menuItem = new JMenuItem(AlertViewerDialog.TITLE);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        menuItem.addActionListener(e -> button.getDialog().setVisible(true));
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU)
                .add(menuItem);
    }

    /**
     * Handles a UserMessageEvent.
     *
     * @param event the event
     */
    private void handleUserMessageEvent(UserMessageEvent event)
    {
        if (event.isShowToast())
        {
            myToaster.showToastMessage(event.getMessage(), event.getType());
        }

        final Alert alert = new Alert(event.getType(), event.getMessage(), event.isMakeVisible());
        Platform.runLater(() -> addAlert(alert));
    }

    /**
     * Adds the alert.
     *
     * @param alert the alert
     */
    private void addAlert(Alert alert)
    {
        if (myAlerts.size() == 500)
        {
            myAlerts.remove(0);
        }
        myAlerts.add(alert);
    }
}
