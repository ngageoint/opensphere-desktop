package io.opensphere.core.util.fx.tabpane.skin;

import io.opensphere.core.util.fx.OSTabPane;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * A button used in the tab pane to add a new tab.
 */
public class OSTabControlAddButton extends StackPane
{
    /** The tab pane skin to which the control buttons are bound. */
    private final OSTabPaneSkin myOsTabPaneSkin;

    /** The inner container in which controls are rendered. */
    private StackPane myInnerContainer;

    /**
     * The down arrow displayed within the button. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final StackPane myGraphic;

    /**
     * The "button" used to display the tab-selection menu. This is protected to
     * prevent generation of a synthetic accessor method.
     */
    protected final Pane myGraphicButton;

    /**
     * Creates a new set of control buttons, bound to the supplied parent skin.
     *
     * @param osTabPaneSkin the skin to which to bind.
     */
    public OSTabControlAddButton(final OSTabPaneSkin osTabPaneSkin)
    {
        myOsTabPaneSkin = osTabPaneSkin;
        getStyleClass().setAll("os-control-buttons-tab");

        final TabPane tabPane = myOsTabPaneSkin.getSkinnable();

        myGraphicButton = new Pane();
        myGraphicButton.getStyleClass().setAll("add-tab-button");
        myGraphicButton.setVisible(true);
        myGraphic = new StackPane();
        myGraphic.setManaged(false);
        myGraphic.getStyleClass().setAll("plus");
        myGraphic.setRotate(tabPane.getSide().equals(Side.BOTTOM) ? 180.0F : 0.0F);
        final SVGPath path = new SVGPath();
        path.getStyleClass().setAll("plus-path");
        path.setContent(
                "M18,10h-4V6c0-1.104-0.896-2-2-2s-2,0.896-2,2l0.071,4H6c-1.104,0-2,0.896-2,2s0.896,2,2,2l4.071-0.071L10,18 c0,1.104,0.896,2,2,2s2-0.896,2-2v-4.071L18,14c1.104,0,2-0.896,2-2S19.104,10,18,10z");
        final Bounds bounds = path.getBoundsInParent();
        final double scale = Math.min(10 / bounds.getWidth(), 10 / bounds.getHeight());
        path.setScaleX(scale);
        path.setScaleY(scale);
        myGraphic.getChildren().add(path);
        myGraphicButton.setOnMouseClicked(e -> handleAction());

        myGraphicButton.getChildren().add(myGraphic);

        myInnerContainer = new StackPane()
        {
            @Override
            protected double computePrefWidth(final double height)
            {
                double pw;
                pw = snapSizeX(myGraphic.prefWidth(getHeight())) + snapSizeX(myGraphic.prefWidth(getHeight()));
                if (pw > 0)
                {
                    pw += snappedLeftInset() + snappedRightInset();
                }
                return pw;
            }

            @Override
            protected double computePrefHeight(final double width)
            {
                return Math.max(0.0F, snapSizeY(myGraphicButton.prefHeight(width)));
            }

            @Override
            protected void layoutChildren()
            {
                final double x = 0;
                final double y = snappedTopInset();
                final double w = snapSizeX(getWidth()) - x + snappedLeftInset();
                final double h = snapSizeY(getHeight()) - y + snappedBottomInset();
                positionGraphic(myGraphicButton, myGraphic, x, y, w, h);
            }

            private void positionGraphic(final Pane btn, final Node graphic, final double x, final double y, final double width,
                    final double height)
            {
                btn.resize(width, height);
                positionInArea(btn, x, y, width, height, /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
                // center arrow region within arrow button
                final double graphicWidth = snapSizeX(graphic.prefWidth(-1));
                final double graphicHeight = snapSizeY(graphic.prefHeight(-1));
                graphic.resize(graphicWidth, graphicHeight);
                positionInArea(graphic, btn.snappedLeftInset(), btn.snappedTopInset(),
                        width - btn.snappedLeftInset() - btn.snappedRightInset(),
                        height - btn.snappedTopInset() - btn.snappedBottomInset(),
                        /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
            }
        };
        myInnerContainer.getStyleClass().add("container");
        myInnerContainer.getChildren().add(myGraphicButton);

        getChildren().add(myInnerContainer);

        tabPane.sideProperty().addListener(valueModel ->
        {
            final Side tabPosition = myOsTabPaneSkin.getSkinnable().getSide();
            myGraphic.setRotate(tabPosition.equals(Side.BOTTOM) ? 180.0F : 0.0F);
        });
        requestLayout();
    }

    /**
     * Handles the new tab action. If the skinnable item is an
     * {@link OSTabPane}, the newTabAction is called, otherwise, a new blank tab
     * is generated with a default name.
     */
    private void handleAction()
    {
        if (myOsTabPaneSkin.getSkinnable() instanceof OSTabPane
                && ((OSTabPane)myOsTabPaneSkin.getSkinnable()).newTabAction().get() != null)
        {
            final ActionEvent actionEvent = new ActionEvent(myOsTabPaneSkin.getSkinnable(), myOsTabPaneSkin.getSkinnable());
            ((OSTabPane)myOsTabPaneSkin.getSkinnable()).newTabAction().get().handle(actionEvent);
        }
        else
        {
            myOsTabPaneSkin.getSkinnable().getTabs().add(new Tab("<New Tab>"));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.layout.StackPane#computePrefWidth(double)
     */
    @Override
    protected double computePrefWidth(final double height)
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
    protected double computePrefHeight(final double width)
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
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSizeX(getWidth()) - x + snappedRightInset();
        final double h = snapSizeY(getHeight()) - y + snappedBottomInset();

        myInnerContainer.resize(w, h);
        positionInArea(myInnerContainer, x, y, w, h, /* baseline ignored */0, HPos.CENTER, VPos.BOTTOM);
    }
}
