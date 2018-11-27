package io.opensphere.core.util.fx.tabpane.skin;

import io.opensphere.core.util.fx.tabpane.OSTabMenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/** A pane in which the buttons used to control tabs are rendered. */
public class OSTabControlMenuButton extends StackPane
{
    /** The tab pane skin to which the control buttons are bound. */
    private final OSTabPaneSkin myOsTabPaneSkin;

    /** The inner container in which controls are rendered. */
    private StackPane myInnerContainer;

    /**
     * The down arrow displayed within the button. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final StackPane myDownArrow;

    /**
     * The "button" used to display the tab-selection menu. This is protected to
     * prevent generation of a synthetic accessor method.
     */
    protected final Pane myDownArrowButton;

    /** A flag used to determine if the control buttons should be shown. */
    private boolean myShowControlButtons;

    /** The menu shown win the button is clicked. */
    private ContextMenu myPopupMenu;

    /**
     * Creates a new set of control buttons, bound to the supplied parent skin.
     *
     * @param osTabPaneSkin the skin to which to bind.
     */
    public OSTabControlMenuButton(OSTabPaneSkin osTabPaneSkin)
    {
        myOsTabPaneSkin = osTabPaneSkin;
        getStyleClass().setAll("os-control-buttons-tab");

        TabPane tabPane = myOsTabPaneSkin.getSkinnable();

        myDownArrowButton = new Pane();
        myDownArrowButton.getStyleClass().setAll("tab-down-button");
        myDownArrowButton.setVisible(true);
        myDownArrow = new StackPane();
        myDownArrow.setManaged(false);
        myDownArrow.getStyleClass().setAll("arrow");
        myDownArrow.setRotate(tabPane.getSide().equals(Side.BOTTOM) ? 180.0F : 0.0F);
        myDownArrowButton.getChildren().add(myDownArrow);
        myDownArrowButton.setOnMouseClicked(me -> showPopupMenu());

        setupPopupMenu();

        myInnerContainer = new StackPane()
        {
            @Override
            protected double computePrefWidth(double height)
            {
                return snapSizeX(myDownArrow.prefWidth(getHeight())) + snapSizeX(myDownArrowButton.prefWidth(getHeight()));
            }

            @Override
            protected double computePrefHeight(double width)
            {
                double height = Math.max(0.0F, snapSizeY(myDownArrowButton.prefHeight(width)));
                if (height > 0)
                {
                    height += snappedTopInset() + snappedBottomInset();
                }
                return height;
            }

            @Override
            protected void layoutChildren()
            {
                double x = 0;
                double y = snappedTopInset();
                double w = snapSizeX(getWidth()) - x + snappedLeftInset();
                double h = snapSizeY(getHeight()) - y + snappedBottomInset();
                positionArrow(myDownArrowButton, myDownArrow, x, y, w, h);
            }

            private void positionArrow(Pane btn, StackPane arrow, double x, double y, double width, double height)
            {
                btn.resize(width, height);
                positionInArea(btn, x, y, width, height, /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
                // center arrow region within arrow button
                double arrowWidth = snapSizeX(arrow.prefWidth(-1));
                double arrowHeight = snapSizeY(arrow.prefHeight(-1));
                arrow.resize(arrowWidth, arrowHeight);
                positionInArea(arrow, btn.snappedLeftInset(), btn.snappedTopInset(),
                        width - btn.snappedLeftInset() - btn.snappedRightInset(),
                        height - btn.snappedTopInset() - btn.snappedBottomInset(),
                        /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
            }
        };
        myInnerContainer.getStyleClass().add("container");
        myInnerContainer.getChildren().add(myDownArrowButton);

        getChildren().add(myInnerContainer);

        tabPane.sideProperty().addListener(valueModel ->
        {
            Side tabPosition = myOsTabPaneSkin.getSkinnable().getSide();
            myDownArrow.setRotate(tabPosition.equals(Side.BOTTOM) ? 180.0F : 0.0F);
        });
        tabPane.getTabs().addListener((ListChangeListener<Tab>)c -> setupPopupMenu());
        myShowControlButtons = true;
        requestLayout();
        getProperties().put(ContextMenu.class, myPopupMenu);
    }

    /**
     * Gets the value of the {@link #myPopupMenu} field.
     *
     * @return the value stored in the {@link #myPopupMenu} field.
     */
    public ContextMenu getPopup()
    {
        return myPopupMenu;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.StackPane#computePrefWidth(double)
     */
    @Override
    protected double computePrefWidth(double height)
    {
        double pw = snapSizeX(myInnerContainer.prefWidth(height));
        if (pw > 0)
        {
            pw += snappedLeftInset() + snappedRightInset();
        }
        return pw;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.StackPane#computePrefHeight(double)
     */
    @Override
    protected double computePrefHeight(double width)
    {
        return Math.max(myOsTabPaneSkin.getSkinnable().getTabMinHeight(), snapSizeX(myInnerContainer.prefHeight(width)))
                + snappedTopInset() + snappedBottomInset();
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.StackPane#layoutChildren()
     */
    @Override
    protected void layoutChildren()
    {
        double x = snappedLeftInset();
        double y = snappedTopInset();
        double w = snapSizeX(getWidth()) - x + snappedRightInset();
        double h = snapSizeY(getHeight()) - y + snappedBottomInset();

        if (myShowControlButtons)
        {
            showControlButtons();
            myShowControlButtons = true;
        }

        myInnerContainer.resize(w, h);
        positionInArea(myInnerContainer, x, y, w, h, /* baseline ignored */0, HPos.CENTER, VPos.BOTTOM);
    }

    /** Shows the control buttons. */
    protected void showControlButtons()
    {
        setVisible(true);
        if (myPopupMenu == null)
        {
            setupPopupMenu();
        }
    }

    /** Configures the popup mneu and adds all sub-menu items. */
    protected void setupPopupMenu()
    {
        if (myPopupMenu == null)
        {
            myPopupMenu = new ContextMenu();
        }
        myPopupMenu.getItems().clear();
        ToggleGroup group = new ToggleGroup();
        ObservableList<RadioMenuItem> menuitems = FXCollections.<RadioMenuItem>observableArrayList();
        for (final Tab tab : myOsTabPaneSkin.getSkinnable().getTabs())
        {
            OSTabMenuItem item = new OSTabMenuItem(tab);
            item.setToggleGroup(group);
            item.setOnAction(t -> myOsTabPaneSkin.getSkinnable().getSelectionModel().select(tab));
            menuitems.add(item);
        }
        myPopupMenu.getItems().addAll(menuitems);
    }

    /** Displays the popup menu. */
    protected void showPopupMenu()
    {
        for (MenuItem mi : myPopupMenu.getItems())
        {
            OSTabMenuItem tmi = (OSTabMenuItem)mi;
            if (myOsTabPaneSkin.getSelectedTab().equals(tmi.getTab()))
            {
                tmi.setSelected(true);
                break;
            }
        }
        myPopupMenu.show(myDownArrowButton, Side.BOTTOM, 0, 0);
    }
}
