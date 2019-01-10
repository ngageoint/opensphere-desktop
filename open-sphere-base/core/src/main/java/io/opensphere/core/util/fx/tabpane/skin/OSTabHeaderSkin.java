package io.opensphere.core.util.fx.tabpane.skin;

import io.opensphere.core.util.fx.LambdaMultiplePropertyChangeListenerHandler;
import io.opensphere.core.util.fx.OSTab;
import io.opensphere.core.util.fx.OSUtil;
import io.opensphere.core.util.fx.tabpane.OSTabAnimationState;
import io.opensphere.core.util.fx.tabpane.OSTabPaneBehavior;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/** The skin for each tab within a tab pane. */
public class OSTabHeaderSkin extends StackPane
{
    /**
     * The skin wrapped around the parent tab pane. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final OSTabPaneSkin myOsTabPaneSkin;

    /**
     * The tab around which the skin is wrapped. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final Tab myTab;

    /**
     * The label on which the tab header is rendered. This is protected to
     * prevent generation of a synthetic accessor method.
     */
    protected final Label myLabel;

    /**
     * The text field in which the label is edited. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final TextField myTextField;

    /**
     * The stack pane acting as a close button. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final StackPane myCloseButton;

    /** The inner pane in which children are rendered. */
    private StackPane myInnerContainer;

    /** Handle to the prior tooltip to allow un-installation. */
    private Tooltip myOldTooltip;

    /** The current tooltip installed on the tab. */
    private Tooltip myTooltip;

    /** The clipping area of the tab. */
    private Rectangle myClip;

    /** A the edit state of the tab. */
    private final ObjectProperty<TabEditPhase> myEditPhase = new ConcurrentObjectProperty<>(TabEditPhase.NOT_EDITING);

    /**
     * A flag used to track the closing state of the tab header. This is
     * protected to prevent generation of a synthetic accessor method.
     */
    protected boolean myClosing = false;

    /** The current drag animation state of the tab. */
    private OSTabAnimationState myAnimationState = OSTabAnimationState.NONE;

    /** The current animation plan for the tab. */
    private Timeline myCurrentAnimation;

    /** The behavior applied to the tab during drag events. */
    protected final OSTabPaneBehavior myBehavior;

    /**
     * The listener used to react to multiple property changes within a single
     * action.
     */
    private LambdaMultiplePropertyChangeListenerHandler myListener = new LambdaMultiplePropertyChangeListenerHandler();

    /** The listener used to react to style class changes. */
    private final ListChangeListener<String> myStyleClassListener = (
            ListChangeListener.Change<? extends String> c) -> getStyleClass().setAll(getTab().getStyleClass());

    /** The weak listener handle to the style class listener. */
    private final WeakListChangeListener<String> myWeakStyleClassListener = new WeakListChangeListener<>(myStyleClassListener);

    /** The property used to handle the animation transition. */
    protected final DoubleProperty myAnimationTransition = new SimpleDoubleProperty(this, "animationTransition", 1.0)
    {
        @Override
        protected void invalidated()
        {
            requestLayout();
        }
    };

    /**
     * Creates a new tab header skin, bound to the supplied tab pane skin and
     * tab.
     *
     * @param osTabPaneSkin the parent skin to which the header is bound.
     * @param tab the tab to which the skin is bound.
     * @param behavior the behavior applied to the tab during drag events.
     */
    public OSTabHeaderSkin(OSTabPaneSkin osTabPaneSkin, final Tab tab, OSTabPaneBehavior behavior)
    {
        this.myTab = tab;
        myOsTabPaneSkin = osTabPaneSkin;

        getStyleClass().setAll(tab.getStyleClass());
        setId(tab.getId());
        setStyle(tab.getStyle());
        setAccessibleRole(AccessibleRole.TAB_ITEM);
        setViewOrder(1);

        myBehavior = behavior;
        myClip = new Rectangle();
        setClip(myClip);

        if (myTab instanceof OSTab)
        {
            myEditPhase.bindBidirectional(((OSTab)myTab).tabEditPhaseProperty());
        }
        myEditPhase.set(TabEditPhase.NOT_EDITING);

        myTextField = new TextField();
        myTextField.visibleProperty().bind(Bindings.equal(TabEditPhase.EDITING, myEditPhase));
        myLabel = new Label(tab.getText(), tab.getGraphic());

        myLabel.getStyleClass().setAll("tab-label");
        myLabel.visibleProperty().bind(Bindings.notEqual(TabEditPhase.EDITING, myEditPhase));
        myLabel.setOnMousePressed(e ->
        {
            if (e.getClickCount() == 2)
            {
                myEditPhase.set(TabEditPhase.EDITING);
                e.consume();
            }
        });

        myTextField.setPadding(Insets.EMPTY);
        myTextField.setOnAction(e -> myEditPhase.set(TabEditPhase.PERSISTING));
        myTextField.focusedProperty().addListener((obs, ov, inFocus) ->
        {
            if (myEditPhase.get() == TabEditPhase.EDITING && !myTextField.isFocused())
            {
                myEditPhase.set(TabEditPhase.CANCELLING);
            }
        });

        myTextField.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ESCAPE)
            {
                myEditPhase.set(TabEditPhase.CANCELLING);
                e.consume();
            }
            else if (e.getCode() == KeyCode.TAB)
            {
                myEditPhase.set(TabEditPhase.PERSISTING);
                e.consume();
            }
        });

        myEditPhase.addListener((obs, ov, nv) ->
        {
            switch (nv)
            {
                case CANCELLING:
                    myEditPhase.set(TabEditPhase.NOT_EDITING);
                    break;
                case PERSISTING:
                    myTab.setText(myTextField.getText());
                    myLabel.setText(myTextField.getText());
                    myEditPhase.set(TabEditPhase.NOT_EDITING);
                    break;
                case EDITING:
                    myTextField.setText(myTab.getText());
                    myTextField.selectAll();
                    myTextField.requestFocus();
                    break;
                default:
                case NOT_EDITING:
                    break;
            }
        });

        myCloseButton = new StackPane()
        {
            @Override
            protected double computePrefWidth(double h)
            {
                return OSTabPaneSkin.CLOSE_BTN_SIZE;
            }

            @Override
            protected double computePrefHeight(double w)
            {
                return OSTabPaneSkin.CLOSE_BTN_SIZE;
            }

            @Override
            public void executeAccessibleAction(AccessibleAction action, Object... parameters)
            {
                switch (action)
                {
                    case FIRE:
                    {
                        if (myBehavior.canCloseTab(myTab))
                        {
                            myBehavior.closeTab(myTab);
                            setOnMousePressed(null);
                        }
                        break;
                    }
                    default:
                        super.executeAccessibleAction(action, parameters);
                }
            }
        };
        myCloseButton.setAccessibleRole(AccessibleRole.BUTTON);
        myCloseButton.getStyleClass().setAll("tab-close-button");
        myCloseButton.setOnMousePressed(e ->
        {
            if (myBehavior.canCloseTab(myTab))
            {
                myBehavior.closeTab(myTab);
                setOnMousePressed(null);
                e.consume();
            }
        });

        updateGraphicRotation();

        final Region focusIndicator = new Region();
        focusIndicator.setMouseTransparent(true);
        focusIndicator.getStyleClass().add("focus-indicator");

        myInnerContainer = new StackPane()
        {
            @Override
            protected void layoutChildren()
            {
                final TabPane skinnable = myOsTabPaneSkin.getSkinnable();

                final double paddingTop = snappedTopInset();
                final double paddingRight = snappedRightInset();
                final double paddingBottom = snappedBottomInset();
                final double paddingLeft = snappedLeftInset();
                final double w = getWidth() - (paddingLeft + paddingRight);
                final double h = getHeight() - (paddingTop + paddingBottom);

                final double prefLabelWidth = snapSizeX(myLabel.prefWidth(-1));
                final double prefLabelHeight = snapSizeY(myLabel.prefHeight(-1));

                final double closeBtnWidth = showCloseButton() ? snapSizeX(myCloseButton.prefWidth(-1)) : 0;
                final double closeBtnHeight = showCloseButton() ? snapSizeY(myCloseButton.prefHeight(-1)) : 0;
                final double minWidth = snapSizeX(skinnable.getTabMinWidth());
                final double maxWidth = snapSizeX(skinnable.getTabMaxWidth());
                final double maxHeight = snapSizeY(skinnable.getTabMaxHeight());

                double labelAreaWidth = prefLabelWidth;
                double labelWidth = prefLabelWidth;
                double labelHeight = prefLabelHeight;

                final double childrenWidth = labelAreaWidth + closeBtnWidth;
                final double childrenHeight = Math.max(labelHeight, closeBtnHeight);

                if (childrenWidth > maxWidth && maxWidth != Double.MAX_VALUE)
                {
                    labelAreaWidth = maxWidth - closeBtnWidth;
                    labelWidth = maxWidth - closeBtnWidth;
                }
                else if (childrenWidth < minWidth)
                {
                    labelAreaWidth = minWidth - closeBtnWidth;
                }

                if (childrenHeight > maxHeight && maxHeight != Double.MAX_VALUE)
                {
                    labelHeight = maxHeight;
                }

                if (getAnimationState() != OSTabAnimationState.NONE)
                {
                    labelAreaWidth *= myAnimationTransition.get();
                    myCloseButton.setVisible(false);
                }
                else
                {
                    myCloseButton.setVisible(showCloseButton());
                }

                myLabel.resize(labelWidth, labelHeight);

                double labelStartX = paddingLeft;

                // If maxWidth is less than Double.MAX_VALUE, the user has
                // clamped the max width, but we should position the close
                // button at the end of the tab, which may not necessarily be
                // the entire width of the provided max width.
                double closeBtnStartX = (maxWidth < Double.MAX_VALUE ? Math.min(w, maxWidth) : w) - paddingRight - closeBtnWidth;

                if (myLabel.isVisible())
                {
                    positionInArea(myLabel, labelStartX, paddingTop, labelAreaWidth, h,
                            /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
                }

                if (myTextField.isVisible())
                {
                    myTextField.resize(labelWidth + 5, labelHeight + 7);
                    positionInArea(myTextField, labelStartX - 3, paddingTop, labelAreaWidth + 5, h + 7,
                            /* baseline ignored */0, HPos.CENTER, VPos.TOP);
                    myTextField.requestFocus();
                }

                if (myCloseButton.isVisible())
                {
                    myCloseButton.resize(closeBtnWidth, closeBtnHeight);
                    positionInArea(myCloseButton, closeBtnStartX, paddingTop, closeBtnWidth, h,
                            /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
                }

                // Magic numbers regretfully introduced for RT-28944 (so
                // that the focus rect appears as expected on Windows and Mac).
                // In short we use the vPadding to shift the focus rect down
                // into the content area (whereas previously it was being
                // clipped on Windows, whilst it still looked fine on Mac). In
                // the future we may want to improve this code to remove the
                // magic number. Similarly, the hPadding differs on Mac.
                final int vPadding = OSUtil.isMac() ? 2 : 3;
                final int hPadding = OSUtil.isMac() ? 2 : 1;
                focusIndicator.resizeRelocate(paddingLeft - hPadding, paddingTop + vPadding, w + 2 * hPadding, h - 2 * vPadding);
            }
        };
        myInnerContainer.getStyleClass().add("tab-container");
        myInnerContainer.setRotate(myOsTabPaneSkin.getSkinnable().getSide().equals(Side.BOTTOM) ? 180.0F : 0.0F);
        myInnerContainer.getChildren().addAll(myLabel, myTextField, myCloseButton, focusIndicator);

        getChildren().addAll(myInnerContainer);

        myTooltip = tab.getTooltip();
        if (myTooltip != null)
        {
            Tooltip.install(this, myTooltip);
            myOldTooltip = myTooltip;
        }

        myListener.registerChangeListener(tab.closableProperty(), e ->
        {
            myInnerContainer.requestLayout();
            requestLayout();
        });
        myListener.registerChangeListener(tab.selectedProperty(), e ->
        {
            pseudoClassStateChanged(OSTabPaneSkin.SELECTED_PSEUDOCLASS_STATE, tab.isSelected());
            // Need to request a layout pass for inner because if the width
            // and height didn't not change the label or close button may
            // have changed.
            myInnerContainer.requestLayout();
            requestLayout();
        });
        myListener.registerChangeListener(tab.textProperty(), e -> myLabel.setText(myTab.getText()));
        myListener.registerChangeListener(tab.graphicProperty(), e -> myLabel.setGraphic(myTab.getGraphic()));
        myListener.registerChangeListener(tab.tooltipProperty(), e ->
        {
            // un-install the old tooltip
            if (myOldTooltip != null)
            {
                Tooltip.uninstall(this, myOldTooltip);
            }
            myTooltip = tab.getTooltip();
            if (myTooltip != null)
            {
                // install new tooltip and save as old tooltip.
                Tooltip.install(this, myTooltip);
                myOldTooltip = myTooltip;
            }
        });
        myListener.registerChangeListener(tab.disabledProperty(), e ->
        {
            updateTabDisabledState();
        });
        myListener.registerChangeListener(tab.getTabPane().disabledProperty(), e ->
        {
            updateTabDisabledState();
        });
        myListener.registerChangeListener(tab.styleProperty(), e -> setStyle(tab.getStyle()));

        tab.getStyleClass().addListener(myWeakStyleClassListener);

        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().tabClosingPolicyProperty(), e ->
        {
            myInnerContainer.requestLayout();
            requestLayout();
        });
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().sideProperty(), e ->
        {
            final Side side = myOsTabPaneSkin.getSkinnable().getSide();
            pseudoClassStateChanged(OSTabPaneSkin.TOP_PSEUDOCLASS_STATE, (side == Side.TOP));
            pseudoClassStateChanged(OSTabPaneSkin.RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT));
            pseudoClassStateChanged(OSTabPaneSkin.BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM));
            pseudoClassStateChanged(OSTabPaneSkin.LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT));
            myInnerContainer.setRotate(side == Side.BOTTOM ? 180.0F : 0.0F);
            if (myOsTabPaneSkin.getSkinnable().isRotateGraphic())
            {
                updateGraphicRotation();
            }
        });
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().rotateGraphicProperty(), e -> updateGraphicRotation());
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().tabMinWidthProperty(), e ->
        {
            requestLayout();
            myOsTabPaneSkin.getSkinnable().requestLayout();
        });
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().tabMaxWidthProperty(), e ->
        {
            requestLayout();
            myOsTabPaneSkin.getSkinnable().requestLayout();
        });
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().tabMinHeightProperty(), e ->
        {
            requestLayout();
            myOsTabPaneSkin.getSkinnable().requestLayout();
        });
        myListener.registerChangeListener(myOsTabPaneSkin.getSkinnable().tabMaxHeightProperty(), e ->
        {
            requestLayout();
            myOsTabPaneSkin.getSkinnable().requestLayout();
        });

        getProperties().put(Tab.class, tab);
        getProperties().put(ContextMenu.class, tab.getContextMenu());

        setOnContextMenuRequested((ContextMenuEvent me) ->
        {
            if (myTab.getContextMenu() != null)
            {
                myTab.getContextMenu().show(myInnerContainer, me.getScreenX(), me.getScreenY());
                me.consume();
            }
        });
        setOnMousePressed(me ->
        {
            if (myTab.isDisable())
            {
                return;
            }
            if (me.getButton().equals(MouseButton.MIDDLE))
            {
                if (showCloseButton() && myBehavior.canCloseTab(myTab))
                {
                    removeListeners(myTab);
                    myBehavior.closeTab(myTab);
                }
            }
            else if (me.getButton().equals(MouseButton.PRIMARY))
            {
                myBehavior.selectTab(myTab);
            }
        });

        // initialize pseudo-class state
        pseudoClassStateChanged(OSTabPaneSkin.SELECTED_PSEUDOCLASS_STATE, tab.isSelected());
        pseudoClassStateChanged(OSTabPaneSkin.DISABLED_PSEUDOCLASS_STATE, tab.isDisabled());
        final Side side = myOsTabPaneSkin.getSkinnable().getSide();
        pseudoClassStateChanged(OSTabPaneSkin.TOP_PSEUDOCLASS_STATE, (side == Side.TOP));
        pseudoClassStateChanged(OSTabPaneSkin.RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT));
        pseudoClassStateChanged(OSTabPaneSkin.BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM));
        pseudoClassStateChanged(OSTabPaneSkin.LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT));
    }

    /**
     * Gets the value of the {@link #myTab} field.
     *
     * @return the value stored in the {@link #myTab} field.
     */
    public Tab getTab()
    {
        return myTab;
    }

    /**
     * Gets the value of the {@link #myAnimationState} field.
     *
     * @return the value stored in the {@link #myAnimationState} field.
     */
    public OSTabAnimationState getAnimationState()
    {
        return myAnimationState;
    }

    /**
     * Sets the value of the {@link #myAnimationState} field.
     *
     * @param animationState the value to store in the {@link #myAnimationState}
     *            field.
     */
    public void setAnimationState(OSTabAnimationState animationState)
    {
        this.myAnimationState = animationState;
    }

    /**
     * Gets the value of the {@link #myInnerContainer} field.
     *
     * @return the value stored in the {@link #myInnerContainer} field.
     */
    public StackPane getInner()
    {
        return myInnerContainer;
    }

    /**
     * Gets the value of the {@link #myCurrentAnimation} field.
     *
     * @return the value stored in the {@link #myCurrentAnimation} field.
     */
    public Timeline getCurrentAnimation()
    {
        return myCurrentAnimation;
    }

    /**
     * Sets the value of the {@link #myCurrentAnimation} field.
     *
     * @param currentAnimation the value to store in the
     *            {@link #myCurrentAnimation} field.
     */
    public void setCurrentAnimation(Timeline currentAnimation)
    {
        this.myCurrentAnimation = currentAnimation;
    }

    /** Updates the disabled state of the tab. */
    protected void updateTabDisabledState()
    {
        pseudoClassStateChanged(OSTabPaneSkin.DISABLED_PSEUDOCLASS_STATE, myTab.isDisabled());
        myInnerContainer.requestLayout();
        requestLayout();
    }

    /** Updates the rotation of the graphic within the tab. */
    protected void updateGraphicRotation()
    {
        if (myLabel.getGraphic() != null)
        {
            myLabel.getGraphic()
                    .setRotate(myOsTabPaneSkin.getSkinnable().isRotateGraphic() ? 0.0F
                            : (myOsTabPaneSkin.getSkinnable().getSide().equals(Side.RIGHT) ? -90.0F
                                    : (myOsTabPaneSkin.getSkinnable().getSide().equals(Side.LEFT) ? 90.0F : 0.0F)));
        }
    }

    /**
     * Displays the close button.
     *
     * @return true if the button is displayed.
     */
    protected boolean showCloseButton()
    {
        return myTab.isClosable() && (myOsTabPaneSkin.getSkinnable().getTabClosingPolicy().equals(TabClosingPolicy.ALL_TABS)
                || myOsTabPaneSkin.getSkinnable().getTabClosingPolicy().equals(TabClosingPolicy.SELECTED_TAB)
                        && myTab.isSelected());
    }

    /**
     * Disposes of all listeners applied to the supplied tab.
     *
     * @param tab the tab for which to remove all listeners.
     */
    public void removeListeners(Tab tab)
    {
        myListener.dispose();
        myInnerContainer.getChildren().clear();
        getChildren().clear();
        setOnContextMenuRequested(null);
        setOnMousePressed(null);
    }

    @Override
    protected double computePrefWidth(double height)
    {
        // if (animating) {
        // return prefWidth.getValue();
        // }
        double minWidth = snapSizeX(myOsTabPaneSkin.getSkinnable().getTabMinWidth());
        double maxWidth = snapSizeX(myOsTabPaneSkin.getSkinnable().getTabMaxWidth());
        double paddingRight = snappedRightInset();
        double paddingLeft = snappedLeftInset();
        double tmpPrefWidth = snapSizeX(myLabel.prefWidth(-1));

        // only include the close button width if it is relevant
        if (showCloseButton())
        {
            tmpPrefWidth += snapSizeX(myCloseButton.prefWidth(-1));
        }

        if (tmpPrefWidth > maxWidth)
        {
            tmpPrefWidth = maxWidth;
        }
        else if (tmpPrefWidth < minWidth)
        {
            tmpPrefWidth = minWidth;
        }
        tmpPrefWidth += paddingRight + paddingLeft;
        // prefWidth.setValue(tmpPrefWidth);
        return tmpPrefWidth;
    }

    @Override
    protected double computePrefHeight(double width)
    {
        double minHeight = snapSizeY(myOsTabPaneSkin.getSkinnable().getTabMinHeight());
        double maxHeight = snapSizeY(myOsTabPaneSkin.getSkinnable().getTabMaxHeight());
        double paddingTop = snappedTopInset();
        double paddingBottom = snappedBottomInset();
        double tmpPrefHeight = snapSizeY(myLabel.prefHeight(width));

        if (tmpPrefHeight > maxHeight)
        {
            tmpPrefHeight = maxHeight;
        }
        else if (tmpPrefHeight < minHeight)
        {
            tmpPrefHeight = minHeight;
        }
        tmpPrefHeight += paddingTop + paddingBottom;
        return tmpPrefHeight;
    }

    @Override
    protected void layoutChildren()
    {
        double w = (snapSizeX(getWidth()) - snappedRightInset() - snappedLeftInset()) * myAnimationTransition.getValue();
        myInnerContainer.resize(w, snapSizeY(getHeight()) - snappedTopInset() - snappedBottomInset());
        myInnerContainer.relocate(snappedLeftInset(), snappedTopInset());
    }

    @Override
    protected void setWidth(double value)
    {
        super.setWidth(value);
        myClip.setWidth(value);
    }

    @Override
    protected void setHeight(double value)
    {
        super.setHeight(value);
        myClip.setHeight(value);
    }

    /** {@inheritDoc} */
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters)
    {
        switch (attribute)
        {
            case TEXT:
                return myTab.getText();
            case SELECTED:
                return myOsTabPaneSkin.getSelectedTab() == myTab;
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters)
    {
        switch (action)
        {
            case REQUEST_FOCUS:
                myOsTabPaneSkin.getSkinnable().getSelectionModel().select(myTab);
                break;
            default:
                super.executeAccessibleAction(action, parameters);
        }
    }

    /**
     * Gets the value of the {@link #myClosing} field.
     *
     * @return the value stored in the {@link #myClosing} field.
     */
    public boolean isClosing()
    {
        return myClosing;
    }

    /**
     * Sets the value of the {@link #myClosing} field.
     *
     * @param closing the value to store in the {@link #myClosing} field.
     */
    public void setClosing(boolean closing)
    {
        this.myClosing = closing;
    }

    /**
     * Gets the value of the {@link #myAnimationTransition} field.
     *
     * @return the value stored in the {@link #myAnimationTransition} field.
     */
    public DoubleProperty animationTransitionProperty()
    {
        return myAnimationTransition;
    }
}
