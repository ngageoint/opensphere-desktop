package io.opensphere.core.util.fx;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import javax.swing.JDialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * An alert dialog that renders a JavaFX component and buttons within a Swing
 * dialog. Patterned to closely match the behavior of the JavaFX
 * {@link javafx.scene.control.Alert} class to allow easy swapping in and out.
 */
public class JFXAlert extends JDialog
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = 5027864687845823680L;

    /** The default width of the dialog. */
    public static final int DEFAULT_WIDTH = 350;

    /** The default height of the dialog. */
    public static final int DEFAULT_HEIGHT = 150;

    /** The bar on which buttons are displayed. */
    private final Node myButtonBar;

    /** An observable list of buttons, used to react to user choices. */
    private final ObservableList<ButtonType> myButtons = FXCollections.observableArrayList();

    /** The set of nodes used to render buttons. */
    private final Map<ButtonType, Node> myButtonNodes = new WeakHashMap<>();

    /** The Swing panel on which JavaFX components are rendered. */
    private final JFXPanel myMainPanel;

    /** The width with which to size the dialog. */
    private int myWidth = DEFAULT_WIDTH;

    /** The height with which to size the dialog. */
    private int myHeight = DEFAULT_HEIGHT;

    /** The scene used in the {@link JFXPanel}. */
    private Scene myScene;

    /** The response button clicked by the user. */
    private volatile ButtonType myResponse;

    /** The supplier used to generate the content area of the dialog. */
    private final Supplier<Node> myMessageSupplier;

    /** The alert type created by the user. */
    private final JFXAlertType myAlertType;

    /**
     * Creates a new modal dialog bound to the supplied window, configured as
     * the supplied alert type.
     *
     * @param owner the window to which the modal dialog is bound.
     * @param alertType the type of alert to create.
     */
    public JFXAlert(Window owner, JFXAlertType alertType)
    {
        this(owner, alertType, alertType.getDefaultTitle());
    }

    /**
     * Creates a new modal dialog bound to the supplied window, configured as
     * the supplied alert type.
     *
     * @param owner the window to which the modal dialog is bound.
     * @param alertType the type of alert to create.
     * @param title the custom title displayed in the dialog window.
     */
    public JFXAlert(Window owner, JFXAlertType alertType, String title)
    {
        this(owner, alertType, title, alertType.getDefaultMessage());
    }

    /**
     * Creates a new modal dialog bound to the supplied window, configured as
     * the supplied alert type.
     *
     * @param owner the window to which the modal dialog is bound.
     * @param alertType the type of alert to create.
     * @param title the custom title displayed in the dialog window.
     * @param message the textual message displayed in the content area of the
     *            dialog.
     */
    public JFXAlert(Window owner, JFXAlertType alertType, String title, String message)
    {
        this(owner, alertType, title, message, alertType.getDefaultButtons());
    }

    /**
     * Creates a new modal dialog bound to the supplied window, configured as
     * the supplied alert type.
     *
     * @param owner the window to which the modal dialog is bound.
     * @param alertType the type of alert to create.
     * @param title the custom title displayed in the dialog window.
     * @param message the textual message displayed in the content area of the
     *            dialog.
     * @param buttons the buttons provided to the user for interaction.
     */
    public JFXAlert(Window owner, JFXAlertType alertType, String title, String message, ButtonType... buttons)
    {
        this(owner, alertType, title, () -> new Label(message), buttons);
    }

    /**
     * Creates a new modal dialog bound to the supplied window, configured as
     * the supplied alert type.
     *
     * @param owner the window to which the modal dialog is bound.
     * @param alertType the type of alert to create.
     * @param title the custom title displayed in the dialog window.
     * @param messageSupplier a {@link Node} supplier used to populate the
     *            message area of the dialog.
     * @param buttons the buttons provided to the user for interaction.
     */
    public JFXAlert(Window owner, JFXAlertType alertType, String title, Supplier<Node> messageSupplier, ButtonType... buttons)
    {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        // Do not use "DISPOSE_ON_CLOSE"; JavaFX doesn't behave well with it.
        addWindowListener(new WindowCloseListener());
        myAlertType = alertType;

        myMessageSupplier = messageSupplier;

        assert EventQueue.isDispatchThread();
        myMainPanel = new JFXPanel();

        myButtonBar = createButtonBar();
        myButtons.addListener((ListChangeListener<ButtonType>)c ->
        {
            while (c.next())
            {
                if (c.wasRemoved())
                {
                    for (ButtonType cmd : c.getRemoved())
                    {
                        myButtonNodes.remove(cmd);
                    }
                }
                if (c.wasAdded())
                {
                    for (ButtonType cmd : c.getAddedSubList())
                    {
                        if (!myButtonNodes.containsKey(cmd))
                        {
                            myButtonNodes.put(cmd, createButton(cmd));
                        }
                    }
                }
            }
        });
        for (ButtonType buttonType : buttons)
        {
            if (buttonType != null)
            {
                getButtonTypes().add(buttonType);
            }
        }
    }

    /**
     * Sets the value of the {@link #myWidth} field.
     *
     * @param width the value to store in the {@link #myWidth} field.
     */
    public void setWidth(int width)
    {
        myWidth = width;
    }

    /**
     * Sets the value of the {@link #myHeight} field.
     *
     * @param height the value to store in the {@link #myHeight} field.
     */
    public void setHeight(int height)
    {
        myHeight = height;
    }

    /**
     * Creates and returns a new {@link ButtonBar} instance.
     *
     * @return the Node in which the buttons are displayed.
     */
    private Node createButtonBar()
    {
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setMaxWidth(Double.MAX_VALUE);

        updateButtons(buttonBar);
        getButtonTypes().addListener((ListChangeListener<? super ButtonType>)c -> updateButtons(buttonBar));

        return buttonBar;
    }

    /**
     * Modifies the buttons displayed on the button bar to reflect those stored
     * within the values returned by {@link #getButtonTypes()}.
     *
     * @param buttonBar the bar on which to update the buttons.
     */
    private void updateButtons(ButtonBar buttonBar)
    {
        buttonBar.getButtons().clear();

        boolean hasDefault = false;
        for (ButtonType cmd : getButtonTypes())
        {
            Node button = myButtonNodes.computeIfAbsent(cmd, dialogButton -> createButton(cmd));

            // keep only first default button
            if (button instanceof Button)
            {
                ButtonData buttonType = cmd.getButtonData();

                ((Button)button).setDefaultButton(!hasDefault && buttonType != null && buttonType.isDefaultButton());
                ((Button)button).setCancelButton(buttonType != null && buttonType.isCancelButton());

                hasDefault |= buttonType != null && buttonType.isDefaultButton();
            }
            buttonBar.getButtons().add(button);
        }
    }

    /**
     * Observable list of button types used for the dialog button bar area
     * (created via the {@link #createButtonBar()} method). Modifying the
     * contents of this list will immediately change the buttons displayed to
     * the user within the dialog pane.
     *
     * @return The {@link ObservableList} of {@link ButtonType button types}
     *         available to the user.
     */
    public final ObservableList<ButtonType> getButtonTypes()
    {
        return myButtons;
    }

    /**
     * Gets the node corresponding to the supplied button type, if defined.
     * 
     * @param buttonType the button type for which to get the node.
     * @return the node for the supplied button type, if present,
     *         <code>null</code> otherwise.
     */
    public final Node getButton(ButtonType buttonType)
    {
        return myButtonNodes.get(buttonType);
    }

    /**
     * This method can be overridden by subclasses to create a custom button
     * that will subsequently inserted into the DialogPane button area (created
     * via the {@link #createButtonBar()} method, but mostly commonly it is an
     * instance of {@link ButtonBar}.
     *
     * @param buttonType The {@link ButtonType} to create a button from.
     * @return A JavaFX {@link Node} that represents the given
     *         {@link ButtonType}, most commonly an instance of {@link Button}.
     */
    protected Node createButton(ButtonType buttonType)
    {
        final Button button = new Button(buttonType.getText());
        final ButtonData buttonData = buttonType.getButtonData();
        ButtonBar.setButtonData(button, buttonData);
        button.setDefaultButton(buttonData.isDefaultButton());
        button.setCancelButton(buttonData.isCancelButton());
        button.addEventHandler(ActionEvent.ACTION, ae ->
        {
            myResponse = buttonType;
            setVisible(false);
        });

        return button;
    }

    /**
     * Creates the JavaFX scene, and configures it for use in the application.
     *
     * @return a JavaFX scene configured for use in the application.
     */
    private Scene createScene()
    {
        if (myScene == null)
        {
            BorderPane contentPane = new BorderPane();

            Node node = myMessageSupplier.get();
            node.getStyleClass().add("alert");
            node.getStyleClass().add(myAlertType.getStyleClass());
            contentPane.setCenter(node);

            if (myButtonBar != null)
            {
                contentPane.setBottom(myButtonBar);
            }
            myScene = new Scene(contentPane);
            return FXUtilities.addDesktopStyle(myScene);
        }
        return myScene;
    }

    /**
     * Shows the dialog and waits for the user response (in other words, brings
     * up a blocking dialog, with the returned value the users input).
     * <p>
     * This method must be called on the JavaFX Application thread.
     * Additionally, it must either be called from an input event handler or
     * from the run method of a Runnable passed to
     * {@link javafx.application.Platform#runLater Platform.runLater}. It must
     * not be called during animation or layout processing.
     * </p>
     *
     * @return An {@link Optional} that contains the result. Refer to the
     *         {@link Dialog} class documentation for more detail.
     * @throws IllegalStateException if this method is called on a thread other
     *             than the JavaFX Application Thread.
     * @throws IllegalStateException if this method is called during animation
     *             or layout processing.
     */
    public final Optional<ButtonType> showAndWait()
    {
        Platform.runLater(() -> myMainPanel.setScene(createScene()));

        // Let's assume that a nested event loop is possible no matter what.
        // What's the worst that could happen?
        // if (!Toolkit.getToolkit().canStartNestedEventLoop())
        // {
        // throw new IllegalStateException("showAndWait is not allowed during
        // animation or layout processing");
        // }

        getContentPane().add(myMainPanel, BorderLayout.CENTER);
        pack();
        setSize(new Dimension(myWidth, myHeight));
        setLocationRelativeTo(getOwner());
        setVisible(true);

        myMainPanel.setScene(null);
        EventQueue.invokeLater(() -> dispose());
        return Optional.ofNullable(myResponse);
    }

    /** Better for JavaFX than "DISPOSE_ON_CLOSE". */
    private class WindowCloseListener extends WindowAdapter
    {
        @Override
        public void windowClosed(WindowEvent e)
        {
            dispose();
        }
    }
}
