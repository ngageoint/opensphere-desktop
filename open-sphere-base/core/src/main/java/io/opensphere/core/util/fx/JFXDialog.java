package io.opensphere.core.util.fx;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.swing.JDialog;

import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;

/** Swing dialog that contains a JavaFX component. */
public class JFXDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The button pane. */
    @ThreadConfined("JavaFX")
    private ButtonPaneNew myButtonPane;

    /** Attach to the button bar for handling acceptance events. */
    private Runnable myAcceptListener;

    /** Attach to the button bar for handling reject events. */
    private Runnable myRejectListener;

    /** The root GUI node. */
    //    @ThreadConfined("JavaFX")
    private volatile Node guiNode;

    /** Editor interface--may be the same as the GUI node. */
    private Editor guiEditor;

    /** The button click response. */
    private volatile ButtonData myResponse;

    /**
     * The Swing panel on which JavaFX components are rendered.
     */
    private final JFXPanel mainPanel;

    /**
     * A method reference used to support user confirmation prior to closing the
     * dialog.
     */
    private BooleanSupplier confirmer;

    /** Flag indicates whether to show the "Cancel" button. */
    private boolean showCancelButton = true;

    /**
     * Construct a JFXDialog capable of hosting a JavaFX Node as its main
     * display component. The Node is not supplied as an argument, but can be
     * assigned later using the setFxNode method (q.v.).
     *
     * @param owner the parent Window for this JDialog
     * @param title the dialog's title text
     */
    public JFXDialog(Window owner, String title)
    {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        // Please do not use "DISPOSE_ON_CLOSE"; JavaFX no like.
        addWindowListener(new WinCloseEar());

        assert EventQueue.isDispatchThread();

        mainPanel = new JFXPanel();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create a Swing JDialog capable of hosting a JavaFX Node. The dialog is
     * equipped with a status and button bar showing an "OK" button and an
     * optional "Cancel" button.
     *
     * @param owner the parent AWT Window
     * @param title the title for the JDialog
     * @param showCancel indicator for the optional "Cancel" button
     */
    public JFXDialog(Window owner, String title, boolean showCancel)
    {
        this(owner, title);
        showCancelButton = showCancel;
    }

    /**
     * Constructor.
     *
     * @param owner The owner
     * @param title The title
     * @param nodeSupp The supplier of the JavaFX component
     */
    public JFXDialog(Window owner, String title, Supplier<Node> nodeSupp)
    {
        this(owner, title, nodeSupp, true);
    }

    /**
     * Constructor.
     *
     * @param owner The owner
     * @param title The title
     * @param nodeSupp The supplier of the JavaFX component
     * @param showCancel indicator for the optional "Cancel" button
     */
    public JFXDialog(Window owner, String title, Supplier<Node> nodeSupp, boolean showCancel)
    {
        this(owner, title, showCancel);
        if (nodeSupp != null)
        {
            Platform.runLater(() ->
            {
                guiNode = nodeSupp.get();
                if (guiNode != null)
                {
                    if (guiNode instanceof Editor)
                    {
                        guiEditor = (Editor)guiNode;
                    }
                    mainPanel.setScene(createScene());
                }
            });
        }
    }

    /**
     * Specify an Editor for this dialog and pass it to the button pane, when
     * possible.
     *
     * @param ed the Editor
     */
    public void setEditor(Editor ed)
    {
        guiEditor = ed;
        if (myButtonPane != null)
        {
            myButtonPane.setEditor(guiEditor);
        }
    }

    /**
     * Installs an editor that does nothing but provide the ValidatorSupport.
     *
     * @param vSupp the ValidatorSupport
     */
    public void setValidatorSupport(ValidatorSupport vSupp)
    {
        setEditor(new Editor()
        {
            @Override
            public ValidatorSupport getValidatorSupport()
            {
                return vSupp;
            }

            @Override
            public void accept()
            {
                /* intentionally blank */
            }
        });
    }

    /**
     * Pass the simple acceptance listener to the source of such events.
     *
     * @param r a simple callback
     */
    public void setAcceptListener(Runnable r)
    {
        myAcceptListener = r;
        if (myButtonPane != null)
        {
            myButtonPane.setAcceptListener(myAcceptListener);
        }
    }

    /**
     * Sets the value of the {@link #myRejectListener} field.
     *
     * @param rejectListener the value to store in the {@link #myRejectListener}
     *            field.
     */
    public void setRejectListener(Runnable rejectListener)
    {
        myRejectListener = rejectListener;
        if (myButtonPane != null)
        {
            myButtonPane.setRejectListener(myRejectListener);
        }
    }

    /**
     * Pass the validation message to the button bar, where it is displayed.
     *
     * @param s status code
     * @param m message text
     */
    public void setStatusMsg(ValidationStatus s, String m)
    {
        myButtonPane.setValidationStatus(s, m);
    }

    /**
     * Install the main Node for this JFXDialog. The Node may be constructed on
     * any Thread, and this method may also be called from any Thread. However,
     * once installed, the Node should only be manipulated on the JavaFX
     * Application Thread.
     *
     * @param n the dialog's main Node
     */
    public void setFxNode(Node n)
    {
        if (n == null)
        {
            return;
        }
        guiNode = n;
        if (guiNode instanceof Editor)
        {
            guiEditor = (Editor)guiNode;
        }
        Platform.runLater(() -> mainPanel.setScene(createScene()));
    }

    /**
     * Introduce a call-back mechanism that can allow or veto the closing of the
     * dialog. In case the OK_DONE option is chosen, the argument will be
     * invoked, and the dialog will be allowed to close if and only if the
     * result is true. If anything other than OK_DONE is chosen, the confirmer
     * is not invoked.
     *
     * @param con A method reference used to support user confirmation prior to
     *            closing the dialog.
     */
    public void setConfirmer(BooleanSupplier con)
    {
        confirmer = con;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public ButtonData getResponse()
    {
        return myResponse;
    }

    /**
     * Gets the fxNode.
     *
     * @return the fxNode
     */
    public Node getFxNode()
    {
        assert Platform.isFxApplicationThread();

        return guiNode;
    }

    /**
     * Creates the JavaFX scene, and configures it for use in the application.
     *
     * @return a JavaFX scene configured for use in the application.
     */
    private Scene createScene()
    {
        if (showCancelButton)
        {
            myButtonPane = new ButtonPaneNew(OpenSphereButtonBar.okayCancel(), guiNode, guiEditor);
        }
        else
        {
            myButtonPane = new ButtonPaneNew(OpenSphereButtonBar.okay(), guiNode, guiEditor);
        }
        myButtonPane.addButtonClickListener(this::handleButtonClick);
        myButtonPane.setAcceptListener(myAcceptListener);
        myButtonPane.setRejectListener(myRejectListener);
        return FXUtilities.addDesktopStyle(new Scene(myButtonPane));
    }

    /**
     * Handles a button click.
     *
     * @param r the button clicked
     */
    private void handleButtonClick(ButtonData r)
    {
        assert Platform.isFxApplicationThread();

        if (r == ButtonData.OK_DONE && confirmer != null && !confirmer.getAsBoolean())
        {
            return;
        }

        myButtonPane.setResponse(r);

        myResponse = r;

        // https://bugs.openjdk.java.net/browse/JDK-8089371, setting the scene
        // to null must be done because of this bug.
        mainPanel.setScene(null);
        EventQueue.invokeLater(() -> dispose());
    }

    /** Better for JavaFX than "DISPOSE_ON_CLOSE". */
    private class WinCloseEar extends WindowAdapter
    {
        @Override
        public void windowClosed(WindowEvent e)
        {
            dispose();
        }
    }
}
