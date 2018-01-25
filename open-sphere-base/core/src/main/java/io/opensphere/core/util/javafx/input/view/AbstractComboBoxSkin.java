package io.opensphere.core.util.javafx.input.view;

import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skinnable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * An abstract base class for custom editable combo boxes, with a display node and custom popup.
 *
 * @param <T> The datatype contained within the combo box.
 */
public abstract class AbstractComboBoxSkin<T> extends AbstractBehaviorSkin<ComboBoxBase<T>, AbstractComboBoxBehavior<T>>
{
    /**
     * The default editor height.
     */
    private static final int DEFAULT_EDITOR_HEIGHT = 21;

    /**
     * The content area in which the value is displayed when the popup is not shown.
     */
    private Node myDisplayNode;

    /**
     * The component used to display the popup.
     */
    private final StackPane myPopupLauncher;

    /**
     * The component in which the graphic is displayed. This is used as the {@link StackPane}'s graphic.
     */
    private final Region myIcon;

    /**
     * Creates a new skin tied to the supplied combo box.
     *
     * @param pParentComboBox the combo box associated with the skin.
     * @param pBehavior the behavior support to add to the skin.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AbstractComboBoxSkin(ComboBoxBase<T> pParentComboBox, final AbstractComboBoxBehavior<T> pBehavior)
    {
        super(pParentComboBox, pBehavior);

        // create the icon
        myIcon = new Region();
        myIcon.setFocusTraversable(false);
        myIcon.getStyleClass().setAll("arrow");
        myIcon.setId("arrow");
        myIcon.setMaxWidth(Region.USE_PREF_SIZE);
        myIcon.setMaxHeight(Region.USE_PREF_SIZE);
        myIcon.setMouseTransparent(true);

        // create the button
        myPopupLauncher = new StackPane();
        myPopupLauncher.setFocusTraversable(false);
        myPopupLauncher.setId("arrow-button");
        myPopupLauncher.getStyleClass().setAll("arrow-button");
        myPopupLauncher.getChildren().add(myIcon);

        if (pParentComboBox.isEditable())
        {
            // handle mouse events
            myPopupLauncher.addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> processMouseEvent(e));
            myPopupLauncher.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> processMouseEvent(e));
            myPopupLauncher.addEventHandler(MouseEvent.MOUSE_RELEASED, (e) -> processMouseEvent(e));
            myPopupLauncher.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> processMouseEvent(e));
        }
        getChildren().add(myPopupLauncher);

        // When ComboBoxBase focus shifts to another node, it should hide.
        getSkinnable().focusedProperty().addListener((observable, oldValue, newValue) -> focusChanged(newValue));

        // Register listeners
        registerChangeListener(pParentComboBox.editableProperty(), "EDITABLE");
        registerChangeListener(pParentComboBox.showingProperty(), "SHOWING");
        registerChangeListener(pParentComboBox.focusedProperty(), "FOCUSED");
        registerChangeListener(pParentComboBox.valueProperty(), "VALUE");
    }

    /**
     * This method should return a Node that will be positioned within the ComboBox 'button' area.
     *
     * @return the component used as the display / editor node.
     */
    public abstract Node getDisplayNode();

    /**
     * This method will be called when the ComboBox popup should be displayed. It is up to specific skin implementations to
     * determine how this is handled.
     */
    public abstract void show();

    /**
     * This method will be called when the ComboBox popup should be hidden. It is up to specific skin implementations to determine
     * how this is handled.
     */
    public abstract void hide();

    /**
     * Gets the value of the {@link #myIcon} field.
     *
     * @return the value stored in the {@link #myIcon} field.
     */
    public final Region getIcon()
    {
        return myIcon;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#layoutChildren(double, double, double, double)
     */
    @Override
    protected void layoutChildren(double pContentX, double pContentY, double pContentWidth, double pContentHeight)
    {
        // if the display / editor node hasn't been initialized, take care of that now:
        if (myDisplayNode == null)
        {
            updateDisplayArea();
        }

        final double arrowWidth = snapSize(myIcon.prefWidth(-1));
        final double arrowButtonWidth = myPopupLauncher.snappedLeftInset() + arrowWidth + myPopupLauncher.snappedRightInset();

        if (myDisplayNode != null)
        {
            myDisplayNode.resizeRelocate(pContentX, pContentY, pContentWidth - arrowButtonWidth, pContentHeight);
        }

        myPopupLauncher.setVisible(true);
        myPopupLauncher.resize(arrowButtonWidth, pContentHeight);
        positionInArea(myPopupLauncher, (pContentX + pContentWidth) - arrowButtonWidth, pContentY, arrowButtonWidth,
                pContentHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    /**
     * This method lazily initializes the display node, updating it from the implementing subclass. This also allows for the
     * editable state of the editor to be changed at runtime, and for the display node to be changed by the subclass.
     */
    protected void updateDisplayArea()
    {
        final List<Node> children = getChildren();
        final Node oldDisplayNode = myDisplayNode;
        myDisplayNode = getDisplayNode();

        // don't remove displayNode if it hasn't changed.
        if (oldDisplayNode != null && !oldDisplayNode.equals(myDisplayNode))
        {
            children.remove(oldDisplayNode);
        }

        if (myDisplayNode != null && !children.contains(myDisplayNode))
        {
            children.add(myDisplayNode);
            myDisplayNode.applyCss();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computePrefWidth(double, double, double, double, double)
     */
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        if (myDisplayNode == null)
        {
            updateDisplayArea();
        }

        final double arrowWidth = snapSize(myIcon.prefWidth(-1));
        final double arrowButtonWidth = myPopupLauncher.snappedLeftInset() + arrowWidth + myPopupLauncher.snappedRightInset();
        final double displayNodeWidth = myDisplayNode == null ? 0 : myDisplayNode.prefWidth(height);

        final double totalWidth = displayNodeWidth + arrowButtonWidth;
        return leftInset + totalWidth + rightInset;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computePrefHeight(double, double, double, double, double)
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        if (myDisplayNode == null)
        {
            updateDisplayArea();
        }

        double preferredHeight;
        if (myDisplayNode == null)
        {
            double arrowHeight = myPopupLauncher.snappedTopInset() + myIcon.prefHeight(-1) + myPopupLauncher.snappedBottomInset();
            preferredHeight = Math.max(DEFAULT_EDITOR_HEIGHT, arrowHeight);
        }
        else
        {
            preferredHeight = myDisplayNode.prefHeight(width);
        }

        return topInset + preferredHeight + bottomInset;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computeMaxWidth(double, double, double, double, double)
     */
    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        return getSkinnable().prefWidth(height);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computeMaxHeight(double, double, double, double, double)
     */
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        return getSkinnable().prefHeight(width);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.SkinBase#computeBaselineOffset(double, double, double, double)
     */
    @Override
    protected double computeBaselineOffset(double topInset, double rightInset, double bottomInset, double leftInset)
    {
        if (myDisplayNode == null)
        {
            updateDisplayArea();
        }

        if (myDisplayNode != null)
        {
            return myDisplayNode.getLayoutBounds().getMinY() + myDisplayNode.getLayoutY() + myDisplayNode.getBaselineOffset();
        }

        return super.computeBaselineOffset(topInset, rightInset, bottomInset, leftInset);
    }

    /**
     * An event handler method used to handle a focus change. When the focus is lost, the {@link #handleFocusLoss()} method is
     * called.
     *
     * @param pFocused true if the focus was gained, false otherwise.
     */
    protected void focusChanged(Boolean pFocused)
    {
        if (!pFocused)
        {
            handleFocusLoss();
        }
    }

    /**
     * An overridable focus handler method in which the skin handles the loss of focus. The default implementation hides the
     * underlying {@link Skinnable} component.
     */
    protected void handleFocusLoss()
    {
        getSkinnable().hide();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.view.AbstractBehaviorSkin#handleControlPropertyChanged(java.lang.String)
     */
    @Override
    protected void handleControlPropertyChanged(String pChangeEventName)
    {
        super.handleControlPropertyChanged(pChangeEventName);

        if ("SHOWING".equals(pChangeEventName))
        {
            if (getSkinnable().isShowing())
            {
                show();
            }
            else
            {
                hide();
            }
        }
        else if ("EDITABLE".equals(pChangeEventName))
        {
            updateDisplayArea();
        }
        else if ("VALUE".equals(pChangeEventName))
        {
            updateDisplayArea();
        }
    }
}
