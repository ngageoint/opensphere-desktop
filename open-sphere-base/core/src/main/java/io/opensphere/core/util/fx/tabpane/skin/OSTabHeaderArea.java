package io.opensphere.core.util.fx.tabpane.skin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.util.fx.tabpane.OSDragState;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Defines the header area which is responsible for painting all tabs.
 */
public class OSTabHeaderArea extends StackPane
{
    /** Constant in which a the space between items is defined. */
    private static final int SPACER = 10;

    /**
     * The parent skin to which the header tab is bound. This is protected to
     * prevent generation of a synthetic accessor method.
     */
    protected final OSTabPaneSkin myOsTabPaneSkin;

    /** The clip area of the header area. */
    private final Rectangle myHeaderClip;

    /**
     * The region in which the headers are actually displayed (the 'non-blank'
     * space)
     */
    private final StackPane myHeadersDisplayRegion;

    /** The background area (the 'blank' space). */
    private final StackPane myHeaderBackground;

    /**
     * The buttons used to control the tabs. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final OSTabControlAddButton myAddButton;

    /**
     * The buttons used to control the tabs.This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected final OSTabControlMenuButton myMenuButton;

    /**
     * A flag used to measure the closing tabs. This is protected to prevent
     * generation of a synthetic accessor method.
     */
    protected boolean measureClosingTabs = false;

    /** The offset accumulated by scrolling. */
    private double myScrollOffset;

    /**
     * The tabs currently being removed. This is protected to prevent generation
     * of a synthetic accessor method.
     */
    protected final List<OSTabHeaderSkin> myRemovedTabs = new ArrayList<>();

    /**
     * A simple extension of a StackPane with overrides to the size
     * calculations, to be used for tab pane header areas.
     */
    public final class HeaderRegionStackPane extends StackPane
    {
        @Override
        protected double computePrefWidth(final double height)
        {
            double width = 0.0F;
            for (final Node child : getChildren())
            {
                final OSTabHeaderSkin tabHeaderSkin = (OSTabHeaderSkin)child;
                if (tabHeaderSkin.isVisible() && (measureClosingTabs || !tabHeaderSkin.isClosing()))
                {
                    width += tabHeaderSkin.prefWidth(height);
                }
            }
            return snapSizeX(width) + snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(final double width)
        {
            double height = 0.0F;
            for (final Node child : getChildren())
            {
                final OSTabHeaderSkin tabHeaderSkin = (OSTabHeaderSkin)child;
                height = Math.max(height, tabHeaderSkin.prefHeight(width));
            }
            return snapSizeY(height) + snappedTopInset() + snappedBottomInset();
        }

        @Override
        protected void layoutChildren()
        {
            if (tabsFit())
            {
                setScrollOffset(0.0);
            }
            else
            {
                if (!myRemovedTabs.isEmpty())
                {
                    double offset = 0;
                    final double w = myOsTabPaneSkin.myTabHeaderArea.getWidth() - snapSizeX(myAddButton.prefWidth(-1))
                            - firstTabIndent() - SPACER;
                    final Iterator<Node> i = getChildren().iterator();
                    while (i.hasNext())
                    {
                        final OSTabHeaderSkin tabHeader = (OSTabHeaderSkin)i.next();
                        final double tabHeaderPrefWidth = snapSizeX(tabHeader.prefWidth(-1));
                        if (myRemovedTabs.contains(tabHeader))
                        {
                            if (offset < w)
                            {
                                myOsTabPaneSkin.mySelectingTab = true;
                            }
                            i.remove();
                            myRemovedTabs.remove(tabHeader);
                            if (myRemovedTabs.isEmpty())
                            {
                                break;
                            }
                        }
                        offset += tabHeaderPrefWidth;
                    }
                }
            }

            if (myOsTabPaneSkin.mySelectingTab)
            {
                ensureSelectedTabIsVisible();
                myOsTabPaneSkin.mySelectingTab = false;
            }
            else
            {
                validateScrollOffset();
            }

            final Side tabPosition = myOsTabPaneSkin.getSkinnable().getSide();
            final double tabBackgroundHeight = snapSizeY(prefHeight(-1));
            double tabX = tabPosition.equals(Side.LEFT) || tabPosition.equals(Side.BOTTOM)
                    ? snapSizeX(getWidth()) - getScrollOffset() : getScrollOffset();

            updateHeaderClip();
            for (final Node node : getChildren())
            {
                final OSTabHeaderSkin tabHeader = (OSTabHeaderSkin)node;

                // size and position the header relative to the other
                // headers
                final double tabHeaderPrefWidth = snapSizeX(
                        tabHeader.prefWidth(-1) * tabHeader.animationTransitionProperty().get());
                final double tabHeaderPrefHeight = snapSizeY(tabHeader.prefHeight(-1));
                tabHeader.resize(tabHeaderPrefWidth, tabHeaderPrefHeight);

                // This ensures that the tabs are located in the correct
                // position when there are tabs of differing heights.
                final double startY = tabPosition.equals(Side.BOTTOM) ? 0
                        : tabBackgroundHeight - tabHeaderPrefHeight - snappedBottomInset();
                if (tabPosition.equals(Side.LEFT) || tabPosition.equals(Side.BOTTOM))
                {
                    // build from the right
                    tabX -= tabHeaderPrefWidth;
                    if (myOsTabPaneSkin.myDragState != OSDragState.REORDER
                            || tabHeader != myOsTabPaneSkin.myDragTabHeader && tabHeader != myOsTabPaneSkin.myDropAnimationHeader)
                    {
                        tabHeader.relocate(tabX, startY);
                    }
                }
                else
                {
                    // build from the left
                    if (myOsTabPaneSkin.myDragState != OSDragState.REORDER
                            || tabHeader != myOsTabPaneSkin.myDragTabHeader && tabHeader != myOsTabPaneSkin.myDropAnimationHeader)
                    {
                        tabHeader.relocate(tabX, startY);
                    }
                    tabX += tabHeaderPrefWidth;
                }
            }
        }
    }

    /**
     * Creates a new header area in which to paint tabs.
     *
     * @param osTabPaneSkin the skin to which the header area is bound.
     */
    public OSTabHeaderArea(final OSTabPaneSkin osTabPaneSkin)
    {
        myOsTabPaneSkin = osTabPaneSkin;
        getStyleClass().setAll("tab-header-area");
        setManaged(false);
        final TabPane tabPane = myOsTabPaneSkin.getSkinnable();

        myHeaderClip = new Rectangle();

        myHeadersDisplayRegion = new HeaderRegionStackPane();
        myHeadersDisplayRegion.getStyleClass().setAll("headers-region");
        myHeadersDisplayRegion.setClip(myHeaderClip);
        myOsTabPaneSkin.setupReordering(myHeadersDisplayRegion);

        myHeaderBackground = new StackPane();
        myHeaderBackground.getStyleClass().setAll("tab-header-background");

        int i = 0;
        for (final Tab tab : tabPane.getTabs())
        {
            addTab(tab, i++);
        }

        myAddButton = new OSTabControlAddButton(myOsTabPaneSkin);
        myAddButton.setVisible(true);

        myMenuButton = new OSTabControlMenuButton(myOsTabPaneSkin);
        myMenuButton.setVisible(true);
        getChildren().addAll(myHeaderBackground, myHeadersDisplayRegion, myAddButton, myMenuButton);

        // support for mouse scroll of header area (for when the tabs exceed
        // the available space). Scrolling the mouse wheel downwards results
        // in the tabs scrolling left (i.e. exposing the right-most tabs)
        // Scrolling the mouse wheel upwards results in the tabs scrolling
        // right (i.e. exposing the left-most tabs)
        addEventHandler(ScrollEvent.SCROLL, (final ScrollEvent e) ->
        {
            Side side = myOsTabPaneSkin.getSkinnable().getSide();
            side = side == null ? Side.TOP : side;
            switch (side)
            {
                default:
                case TOP:
                case BOTTOM:
                    setScrollOffset(myScrollOffset + e.getDeltaY());
                    break;
                case LEFT:
                case RIGHT:
                    setScrollOffset(myScrollOffset - e.getDeltaY());
                    break;
            }

        });
    }

    /**
     * Gets the value of the {@link #myAddButton} field.
     *
     * @return the value stored in the {@link #myAddButton} field.
     */
    public OSTabControlMenuButton getControlButtons()
    {
        return myMenuButton;
    }

    /**
     * Gets the value of the {@link #myHeadersDisplayRegion} field.
     *
     * @return the value stored in the {@link #myHeadersDisplayRegion} field.
     */
    public StackPane getHeadersDisplayRegion()
    {
        return myHeadersDisplayRegion;
    }

    /**
     * Updates the clipping region to cover the header tabs, if needed.
     */
    protected void updateHeaderClip()
    {
        final Side tabPosition = myOsTabPaneSkin.getSkinnable().getSide();

        double x = 0;
        final double y = 0;
        double clipWidth = 0;
        double clipHeight = 0;
        double maxWidth = 0;
        double shadowRadius = 0;
        final double clipOffset = firstTabIndent();
        double menuButtonPreferredWidth = snapSizeX(myMenuButton.prefWidth(-1));
        final double addButtonPreferredWidth = snapSizeX(myAddButton.prefWidth(-1));

        measureClosingTabs = true;
        final double headersPrefWidth = snapSizeX(myHeadersDisplayRegion.prefWidth(-1));
        measureClosingTabs = false;

        final double headersPrefHeight = snapSizeY(myHeadersDisplayRegion.prefHeight(-1));
        menuButtonPreferredWidth = menuButtonPreferredWidth + SPACER;

        if (myHeadersDisplayRegion.getEffect() instanceof DropShadow)
        {
            final DropShadow shadow = (DropShadow)myHeadersDisplayRegion.getEffect();
            shadowRadius = shadow.getRadius();
        }

        maxWidth = snapSizeX(getWidth()) - menuButtonPreferredWidth - addButtonPreferredWidth - clipOffset;
        if (tabPosition.equals(Side.LEFT) || tabPosition.equals(Side.BOTTOM))
        {
            if (headersPrefWidth < maxWidth)
            {
                clipWidth = headersPrefWidth + shadowRadius;
            }
            else
            {
                x = headersPrefWidth - maxWidth;
                clipWidth = maxWidth + shadowRadius;
            }
            clipHeight = headersPrefHeight;
        }
        else
        {
            // If x = 0 the header region's drop shadow is clipped.
            x = -shadowRadius;
            clipWidth = (headersPrefWidth < maxWidth ? headersPrefWidth : maxWidth) + shadowRadius;
            clipHeight = headersPrefHeight;
        }

        myHeaderClip.setX(x);
        myHeaderClip.setY(y);
        myHeaderClip.setWidth(clipWidth);
        myHeaderClip.setHeight(clipHeight);
    }

    /**
     * Adds the supplied tab at the specified index.
     *
     * @param tab the tab to add to the region.
     * @param addToIndex the index at which to add the tab.
     */
    protected void addTab(final Tab tab, final int addToIndex)
    {
        final OSTabHeaderSkin tabHeaderSkin = new OSTabHeaderSkin(myOsTabPaneSkin, tab, myOsTabPaneSkin.myBehavior);
        myHeadersDisplayRegion.getChildren().add(addToIndex, tabHeaderSkin);
    }

    /**
     * Removes the supplied tab from the region.
     *
     * @param tab the tab to remove from the region.
     */
    protected void removeTab(final Tab tab)
    {
        final OSTabHeaderSkin tabHeaderSkin = getTabHeaderSkin(tab);
        if (tabHeaderSkin != null)
        {
            if (tabsFit())
            {
                myHeadersDisplayRegion.getChildren().remove(tabHeaderSkin);
            }
            else
            {
                // The tab will be removed during layout because
                // we need its width to compute the scroll offset.
                myRemovedTabs.add(tabHeaderSkin);
                tabHeaderSkin.removeListeners(tab);
            }
        }
    }

    /**
     * Gets the skin fro the header portion of the supplied tab.
     *
     * @param tab the tab for which to get the skin.
     * @return the skin for the supplied tab, or null if none is assigned.
     */
    protected OSTabHeaderSkin getTabHeaderSkin(final Tab tab)
    {
        return myHeadersDisplayRegion.getChildren().stream().map(c -> (OSTabHeaderSkin)c).filter(s -> s.getTab().equals(tab))
                .findFirst().orElse(null);
    }

    /**
     * Tests to determine if the tabs fit within the allocated space.
     *
     * @return true if they fit, false otherwise.
     */
    protected boolean tabsFit()
    {
        final double headerPrefWidth = snapSizeX(myHeadersDisplayRegion.prefWidth(-1));
        final double controlTabWidth = snapSizeX(myAddButton.prefWidth(-1));
        final double controlTabWidth2 = snapSizeX(myMenuButton.prefWidth(-1));
        final double visibleWidth = headerPrefWidth + controlTabWidth + controlTabWidth2 + firstTabIndent() + SPACER;
        return visibleWidth < getWidth();
    }

    /**
     * Upon selection change, moves the visible tab so that the selected one is
     * always visible.
     */
    protected void ensureSelectedTabIsVisible()
    {
        // work out the visible width of the tab header
        final double tabPaneWidth = snapSizeX(myOsTabPaneSkin.isHorizontal() ? myOsTabPaneSkin.getSkinnable().getWidth()
                : myOsTabPaneSkin.getSkinnable().getHeight());
        final double controlTabWidth = snapSizeX(myAddButton.getWidth());
        final double controlTabWidth2 = snapSizeX(myMenuButton.getWidth());
        final double visibleWidth = tabPaneWidth - controlTabWidth - controlTabWidth2 - firstTabIndent() - SPACER;

        // and get where the selected tab is in the header area
        double offset = 0.0;
        double selectedTabOffset = 0.0;
        double selectedTabWidth = 0.0;
        for (final Node node : myHeadersDisplayRegion.getChildren())
        {
            final OSTabHeaderSkin tabHeader = (OSTabHeaderSkin)node;

            final double tabHeaderPrefWidth = snapSizeX(tabHeader.prefWidth(-1));

            if (myOsTabPaneSkin.getSelectedTab() != null && myOsTabPaneSkin.getSelectedTab().equals(tabHeader.getTab()))
            {
                selectedTabOffset = offset;
                selectedTabWidth = tabHeaderPrefWidth;
            }
            offset += tabHeaderPrefWidth;
        }

        final double scrollOffset = getScrollOffset();
        final double selectedTabStartX = selectedTabOffset;
        final double selectedTabEndX = selectedTabOffset + selectedTabWidth;

        final double visibleAreaEndX = visibleWidth;

        if (selectedTabStartX < -scrollOffset)
        {
            setScrollOffset(-selectedTabStartX);
        }
        else if (selectedTabEndX > visibleAreaEndX - scrollOffset)
        {
            setScrollOffset(visibleAreaEndX - selectedTabEndX);
        }
    }

    /**
     * Gets the value of the {@link #myScrollOffset} field.
     *
     * @return the value stored in the {@link #myScrollOffset} field.
     */
    public double getScrollOffset()
    {
        return myScrollOffset;
    }

    /**
     * Validates the offset for the scroll bar.
     */
    protected void validateScrollOffset()
    {
        setScrollOffset(getScrollOffset());
    }

    /**
     * Calculates and configures the header area for offset due to scroll bar
     * placement.
     *
     * @param newScrollOffset the new offset from which to calculate.
     */
    protected void setScrollOffset(final double newScrollOffset)
    {
        // work out the visible width of the tab header
        final double tabPaneWidth = snapSizeX(myOsTabPaneSkin.isHorizontal() ? myOsTabPaneSkin.getSkinnable().getWidth()
                : myOsTabPaneSkin.getSkinnable().getHeight());
        final double controlTabWidth = snapSizeX(myAddButton.getWidth());
        final double controlTabWidth2 = snapSizeX(myMenuButton.getWidth());
        final double visibleWidth = tabPaneWidth - controlTabWidth - controlTabWidth2 - firstTabIndent() - SPACER;

        // measure the width of all tabs
        double offset = 0.0;
        for (final Node node : myHeadersDisplayRegion.getChildren())
        {
            final OSTabHeaderSkin tabHeader = (OSTabHeaderSkin)node;
            final double tabHeaderPrefWidth = snapSizeX(tabHeader.prefWidth(-1));
            offset += tabHeaderPrefWidth;
        }

        double actualNewScrollOffset;

        if (visibleWidth - newScrollOffset > offset && newScrollOffset < 0)
        {
            // need to make sure the right-most tab is attached to the
            // right-hand side of the tab header (e.g. if the tab header
            // area width is expanded), and if it isn't modify the scroll offset
            // to bring it into line. See RT-35194 for a test case.
            actualNewScrollOffset = visibleWidth - offset;
        }
        else if (newScrollOffset > 0)
        {
            // need to prevent the left-most tab from becoming detached
            // from the left-hand side of the tab header.
            actualNewScrollOffset = 0;
        }
        else
        {
            actualNewScrollOffset = newScrollOffset;
        }

        if (Math.abs(actualNewScrollOffset - myScrollOffset) > 0.001)
        {
            myScrollOffset = actualNewScrollOffset;
            myHeadersDisplayRegion.requestLayout();
        }
    }

    /**
     * Calculates the indent amount for the first tab.
     *
     * @return the indent amount for the first tab.
     */
    protected double firstTabIndent()
    {
        switch (myOsTabPaneSkin.getSkinnable().getSide())
        {
            case TOP:
            case BOTTOM:
                return snappedLeftInset();
            case RIGHT:
            case LEFT:
                return snappedTopInset();
            default:
                return 0;
        }
    }

    @Override
    protected double computePrefWidth(final double height)
    {
        final double padding = myOsTabPaneSkin.isHorizontal() ? snappedLeftInset() + snappedRightInset()
                : snappedTopInset() + snappedBottomInset();
        return snapSizeX(myHeadersDisplayRegion.prefWidth(height)) + myAddButton.prefWidth(height)
                + myMenuButton.prefWidth(height) + firstTabIndent() + SPACER + padding;
    }

    @Override
    protected double computePrefHeight(final double width)
    {
        final double padding = myOsTabPaneSkin.isHorizontal() ? snappedTopInset() + snappedBottomInset()
                : snappedLeftInset() + snappedRightInset();
        return snapSizeY(myHeadersDisplayRegion.prefHeight(-1)) + padding;
    }

    @Override
    public double getBaselineOffset()
    {
        if (myOsTabPaneSkin.getSkinnable().getSide() == Side.TOP)
        {
            return myHeadersDisplayRegion.getBaselineOffset() + snappedTopInset();
        }
        return 0;
    }

    @Override
    protected void layoutChildren()
    {
        final double leftInset = snappedLeftInset();
        final double rightInset = snappedRightInset();
        final double topInset = snappedTopInset();
        final double bottomInset = snappedBottomInset();
        final double w = snapSizeX(getWidth())
                - (myOsTabPaneSkin.isHorizontal() ? leftInset + rightInset : topInset + bottomInset);
        final double h = snapSizeY(getHeight())
                - (myOsTabPaneSkin.isHorizontal() ? topInset + bottomInset : leftInset + rightInset);
        final double tabBackgroundHeight = snapSizeY(prefHeight(-1));
        final double headersPrefWidth = snapSizeX(myHeadersDisplayRegion.prefWidth(-1));
        final double headersPrefHeight = snapSizeY(myHeadersDisplayRegion.prefHeight(-1));

        updateHeaderClip();
        myHeadersDisplayRegion.requestLayout();

        // RESIZE CONTROL BUTTONS
        final double btnWidth = snapSizeX(myAddButton.prefWidth(-1));
        final double btnWidth2 = snapSizeX(myMenuButton.prefWidth(-1));
        final double btnHeight = myAddButton.prefHeight(btnWidth);
        final double btnHeight2 = myMenuButton.prefHeight(btnWidth);
        myAddButton.resize(btnWidth, btnHeight);
        myMenuButton.resize(btnWidth2, btnHeight2);

        // POSITION TABS
        myHeadersDisplayRegion.resize(headersPrefWidth, headersPrefHeight);

        if (myOsTabPaneSkin.isFloatingStyleClass())
        {
            myHeaderBackground.setVisible(false);
        }
        else
        {
            myHeaderBackground.resize(snapSizeX(getWidth()), snapSizeY(getHeight()));
            myHeaderBackground.setVisible(true);
        }

        double startX = 0;
        double startY = 0;
        double controlStartX = 0;
        double controlStartY = 0;
        double controlStart2X = 0;
        double controlStart2Y = 0;
        final Side tabPosition = myOsTabPaneSkin.getSkinnable().getSide();

        if (tabPosition.equals(Side.TOP))
        {
            startX = leftInset;
            startY = tabBackgroundHeight - headersPrefHeight - bottomInset;
            controlStartX = w - btnWidth + leftInset;
            controlStartY = snapSizeY(getHeight()) - btnHeight - bottomInset;
            controlStart2X = w - btnWidth - btnWidth2 + leftInset;
            controlStart2Y = snapSizeY(getHeight()) - btnHeight2 - bottomInset;
        }
        else if (tabPosition.equals(Side.RIGHT))
        {
            startX = topInset;
            startY = tabBackgroundHeight - headersPrefHeight - leftInset;
            controlStartX = w - btnWidth + topInset;
            controlStartY = snapSizeY(getHeight()) - btnHeight - leftInset;
            controlStart2X = w - btnWidth2 + topInset;
            controlStart2Y = snapSizeY(getHeight()) - btnHeight2 - leftInset;
        }
        else if (tabPosition.equals(Side.BOTTOM))
        {
            startX = snapSizeX(getWidth()) - headersPrefWidth - leftInset;
            startY = tabBackgroundHeight - headersPrefHeight - topInset;
            controlStartX = rightInset;
            controlStartY = snapSizeY(getHeight()) - btnHeight - topInset;
            controlStart2X = rightInset;
            controlStart2Y = snapSizeY(getHeight()) - btnHeight2 - topInset;
        }
        else if (tabPosition.equals(Side.LEFT))
        {
            startX = snapSizeX(getWidth()) - headersPrefWidth - topInset;
            startY = tabBackgroundHeight - headersPrefHeight - rightInset;
            controlStartX = leftInset;
            controlStartY = snapSizeY(getHeight()) - btnHeight - rightInset;
            controlStart2X = leftInset;
            controlStart2Y = snapSizeY(getHeight()) - btnHeight2 - rightInset;
        }
        if (myHeaderBackground.isVisible())
        {
            positionInArea(myHeaderBackground, 0, 0, snapSizeX(getWidth()), snapSizeY(getHeight()),
                    /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
        }
        positionInArea(myHeadersDisplayRegion, startX, startY, w, h,
                /* baseline ignored */0, HPos.LEFT, VPos.CENTER);
        positionInArea(myAddButton, controlStartX, controlStartY, btnWidth, btnHeight,
                /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
        positionInArea(myMenuButton, controlStart2X, controlStart2Y, btnWidth2, btnHeight2,
                /* baseline ignored */0, HPos.CENTER, VPos.CENTER);
    }
}
