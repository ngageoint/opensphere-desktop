package io.opensphere.core.util.fx.tabpane.skin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.util.fx.tabpane.OSDragState;
import io.opensphere.core.util.fx.tabpane.OSTabAnimation;
import io.opensphere.core.util.fx.tabpane.OSTabAnimationState;
import io.opensphere.core.util.fx.tabpane.OSTabContentRegion;
import io.opensphere.core.util.fx.tabpane.OSTabMenuItem;
import io.opensphere.core.util.fx.tabpane.OSTabPaneBehavior;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleableObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * A tab pane skin rewritten to allow for additional controls to be added in the
 * header space.
 */
public class OSTabPaneSkin extends SkinBase<TabPane>
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(OSTabPaneSkin.class);

    /** Constant in which the size of the close button is defined. */
    public static final int CLOSE_BTN_SIZE = 16;

    /** The CSS Pseudo-class for selected tabs. */
    public static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("selected");

    /** The CSS pseudo-class for the top tab alignment. */
    public static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top");

    /** The CSS pseudo-class for the bottom tab alignment. */
    public static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom");

    /** The CSS pseudo-class for the left tab alignment. */
    public static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left");

    /** The CSS pseudo-class for the right tab alignment. */
    public static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right");

    /** The CSS pseudo-class for a disabled tab. */
    public static final PseudoClass DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("disabled");

    /** The duration of a drag animation. */
    static final double ANIMATION_DURATION = 120;

    /** The speed of a drag animation. */
    private static final double ANIMATION_SPEED = 150;

    /** A constant defining the drag distance threshold. */
    private static final double DRAG_DISTANCE_THRESHOLD = 0.75;

    /** A constant defining the minimum to maximum drag direction. */
    private static final int MIN_TO_MAX = 1;

    /** A constant defining the maximum to minimum drag direction. */
    private static final int MAX_TO_MIN = -1;

    /** The header area in which tabs are rendered. */
    OSTabHeaderArea myTabHeaderArea;

    /** The regions in which the tab contents are rendered. */
    private final ObservableList<OSTabContentRegion> myTabContentRegions;

    /** The rectangle in which the clipping area is rendered. */
    private final Rectangle myClipRectangle;

    /** The tab header area clipping rectangle. */
    private final Rectangle myTabHeaderAreaClipRectangle;

    /** The currently selected tab. */
    private Tab mySelectedTab;

    /** Field to track the tab selecting state for the skin. */
    boolean mySelectingTab;

    /** The region in which the headers are rendered. */
    private StackPane myHeadersRegion;

    /** The state of the drag operations in the tab area. */
    OSDragState myDragState;

    /** The layout direction of the tabs. */
    private int myLayoutDirectionX;

    /** The previous direction of the drag operation. */
    private int myPreviousDragDirection = MIN_TO_MAX;

    /** The previous location of the tab during the drag operation. */
    private double myDragEventPreviousLocation;

    /** The index of the tab being dragged. */
    private int myDragTabHeaderIndex;

    /** The skin applied to the header when it is being dragged. */
    OSTabHeaderSkin myDragTabHeader;

    /** The skin applied to the header when it is being dropped. */
    private OSTabHeaderSkin myDropTabHeader;

    /** The skin used to animate dropping a tab in the header. */
    OSTabHeaderSkin myDropAnimationHeader;

    /** The behavior applied to the tab during drag events. */
    final OSTabPaneBehavior myBehavior;

    /** The event handler called for drag events. */
    private final EventHandler<MouseEvent> myHeaderDraggedHandler = this::handleHeaderDragged;

    /** The event handler called for mouse pressed events. */
    private final EventHandler<MouseEvent> myHeaderMousePressedHandler = this::handleHeaderMousePressed;

    /** The event handler called for mouse released events. */
    private final EventHandler<MouseEvent> myHeaderMouseReleasedHandler = this::handleHeaderMouseReleased;

    /** The tab with which the held tab is swapped during animation events. */
    private Tab mySwapTab;

    /** The source location of the header being dropped. */
    private double myDropHeaderSourceX;

    /** The transition location of the header being dropped. */
    private double myDropHeaderTransitionX;

    /** The animation used for showing a header when it drops. */
    private final Animation myDropHeaderAnimation = new HeaderAnimation(this::completeHeaderReordering,
            this::getDropAnimationHeader, this::getDropHeaderSourceX, this::getDropHeaderTransitionX);

    /** The start location of the tab being dragged. */
    private double myDragHeaderStartX;

    /** The destination location of the tab being dragged. */
    private double myDragHeaderDestinationX;

    /** The source location of the tab being dragged. */
    private double myDragHeaderSourceX;

    /** The transition location of the tab being dragged. */
    private double myDragHeaderTransitionX;

    /** The animation used for showing a header when it is dragged. */
    private final Animation myDragHeaderAnimation = new HeaderAnimation(this::resetDrag, this::getDragTabHeader,
            this::getDragHeaderSourceX, this::getDragHeaderTransitionX);

    /** A property in which the animation for opening a tab is maintained. */
    private final ObjectProperty<OSTabAnimation> myOpenTabAnimation = new StyleableObjectProperty<>(OSTabAnimation.GROW)
    {
        @Override
        public CssMetaData<TabPane, OSTabAnimation> getCssMetaData()
        {
            return OSStyleableProperties.OPEN_TAB_ANIMATION;
        }

        @Override
        public Object getBean()
        {
            return OSTabPaneSkin.this;
        }

        @Override
        public String getName()
        {
            return "openTabAnimation";
        }
    };

    /** A property in which the animation for closing a tab is maintained. */
    private final ObjectProperty<OSTabAnimation> myCloseTabAnimation = new StyleableObjectProperty<>(OSTabAnimation.GROW)
    {
        @Override
        public CssMetaData<TabPane, OSTabAnimation> getCssMetaData()
        {
            return OSStyleableProperties.CLOSE_TAB_ANIMATION;
        }

        @Override
        public Object getBean()
        {
            return OSTabPaneSkin.this;
        }

        @Override
        public String getName()
        {
            return "closeTabAnimation";
        }
    };

    /** A list change listener to react to children being added or removed. */
    private final ListChangeListener<Node> myChildListener = change ->
    {
        while (change.next())
        {
            if (change.wasAdded())
            {
                for (final Node n1 : change.getAddedSubList())
                {
                    addReorderListeners(n1);
                }
            }
            if (change.wasRemoved())
            {
                for (final Node n2 : change.getRemoved())
                {
                    removeReorderListeners(n2);
                }
            }
        }
    };

    /**
     * Creates a new skin bound to the supplied control. Installs the necessary
     * child nodes into the Control children list, as well as the necessary
     * input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public OSTabPaneSkin(final TabPane control)
    {
        super(control);
        myBehavior = new OSTabPaneBehavior(control);

        myClipRectangle = new Rectangle(control.getWidth(), control.getHeight());
        getSkinnable().setClip(myClipRectangle);

        myTabContentRegions = FXCollections.<OSTabContentRegion>observableArrayList();

        getSkinnable().getTabs().forEach(this::addTabContent);

        myTabHeaderAreaClipRectangle = new Rectangle();
        myTabHeaderArea = new OSTabHeaderArea(this);
        myTabHeaderArea.setClip(myTabHeaderAreaClipRectangle);
        getChildren().add(myTabHeaderArea);
        if (getSkinnable().getTabs().size() == 0)
        {
            myTabHeaderArea.setVisible(false);
        }

        initializeTabListener();

        registerChangeListener(control.getSelectionModel().selectedItemProperty(), e ->
        {
            mySelectingTab = true;
            mySelectedTab = getSkinnable().getSelectionModel().getSelectedItem();
            getSkinnable().requestLayout();
        });
        registerChangeListener(control.sideProperty(), e -> updateTabPosition());
        registerChangeListener(control.widthProperty(), e -> myClipRectangle.setWidth(getSkinnable().getWidth()));
        registerChangeListener(control.heightProperty(), e -> myClipRectangle.setHeight(getSkinnable().getHeight()));

        mySelectedTab = getSkinnable().getSelectionModel().getSelectedItem();
        // Could not find the selected tab try and get the selected tab using
        // the selected index
        if (mySelectedTab == null && getSkinnable().getSelectionModel().getSelectedIndex() != -1)
        {
            getSkinnable().getSelectionModel().select(getSkinnable().getSelectionModel().getSelectedIndex());
            mySelectedTab = getSkinnable().getSelectionModel().getSelectedItem();
        }
        if (mySelectedTab == null)
        {
            // getSelectedItem and getSelectedIndex failed select the first.
            getSkinnable().getSelectionModel().selectFirst();
        }
        mySelectedTab = getSkinnable().getSelectionModel().getSelectedItem();
        mySelectingTab = false;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        super.dispose();

        if (myBehavior != null)
        {
            myBehavior.dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected double computePrefWidth(final double height, final double topInset, final double rightInset,
            final double bottomInset, final double leftInset)
    {
        // The TabPane can only be as wide as it widest content width.
        double maxw = 0.0;
        for (final OSTabContentRegion contentRegion : myTabContentRegions)
        {
            maxw = Math.max(maxw, snapSizeX(contentRegion.prefWidth(-1)));
        }

        final boolean isHorizontal = isHorizontal();
        final double tabHeaderAreaSize = isHorizontal ? snapSizeX(myTabHeaderArea.prefWidth(-1))
                : snapSizeY(myTabHeaderArea.prefHeight(-1));

        final double prefWidth = isHorizontal ? Math.max(maxw, tabHeaderAreaSize) : maxw + tabHeaderAreaSize;
        return snapSizeX(prefWidth) + rightInset + leftInset;
    }

    /** {@inheritDoc} */
    @Override
    protected double computePrefHeight(final double width, final double topInset, final double rightInset,
            final double bottomInset, final double leftInset)
    {
        // The TabPane can only be as high as it highest content height.
        double maxh = 0.0;
        for (final OSTabContentRegion contentRegion : myTabContentRegions)
        {
            maxh = Math.max(maxh, snapSizeY(contentRegion.prefHeight(-1)));
        }

        final boolean isHorizontal = isHorizontal();
        final double tabHeaderAreaSize = isHorizontal ? snapSizeY(myTabHeaderArea.prefHeight(-1))
                : snapSizeX(myTabHeaderArea.prefWidth(-1));

        final double prefHeight = isHorizontal ? maxh + snapSizeY(tabHeaderAreaSize) : Math.max(maxh, tabHeaderAreaSize);
        return snapSizeY(prefHeight) + topInset + bottomInset;
    }

    /** {@inheritDoc} */
    @Override
    public double computeBaselineOffset(final double topInset, final double rightInset, final double bottomInset,
            final double leftInset)
    {
        final Side tabPosition = getSkinnable().getSide();
        if (tabPosition == Side.TOP)
        {
            return myTabHeaderArea.getBaselineOffset() + topInset;
        }
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h)
    {
        final TabPane tabPane = getSkinnable();
        final Side tabPosition = tabPane.getSide();

        final double headerHeight = tabPosition.isHorizontal() ? snapSizeY(myTabHeaderArea.prefHeight(-1))
                : snapSizeX(myTabHeaderArea.prefHeight(-1));
        final double tabsStartX = tabPosition.equals(Side.RIGHT) ? x + w - headerHeight : x;
        final double tabsStartY = tabPosition.equals(Side.BOTTOM) ? y + h - headerHeight : y;

        final double leftInset = snappedLeftInset();
        final double topInset = snappedTopInset();

        if (tabPosition == Side.TOP)
        {
            myTabHeaderArea.resize(w, headerHeight);
            myTabHeaderArea.relocate(tabsStartX, tabsStartY);
            myTabHeaderArea.getTransforms().clear();
            myTabHeaderArea.getTransforms().add(new Rotate(getRotation(Side.TOP)));
        }
        else if (tabPosition == Side.BOTTOM)
        {
            myTabHeaderArea.resize(w, headerHeight);
            myTabHeaderArea.relocate(w + leftInset, tabsStartY - headerHeight);
            myTabHeaderArea.getTransforms().clear();
            myTabHeaderArea.getTransforms().add(new Rotate(getRotation(Side.BOTTOM), 0, headerHeight));
        }
        else if (tabPosition == Side.LEFT)
        {
            myTabHeaderArea.resize(h, headerHeight);
            myTabHeaderArea.relocate(tabsStartX + headerHeight, h - headerHeight + topInset);
            myTabHeaderArea.getTransforms().clear();
            myTabHeaderArea.getTransforms().add(new Rotate(getRotation(Side.LEFT), 0, headerHeight));
        }
        else if (tabPosition == Side.RIGHT)
        {
            myTabHeaderArea.resize(h, headerHeight);
            myTabHeaderArea.relocate(tabsStartX, y - headerHeight);
            myTabHeaderArea.getTransforms().clear();
            myTabHeaderArea.getTransforms().add(new Rotate(getRotation(Side.RIGHT), 0, headerHeight));
        }

        myTabHeaderAreaClipRectangle.setX(0);
        myTabHeaderAreaClipRectangle.setY(0);
        if (isHorizontal())
        {
            myTabHeaderAreaClipRectangle.setWidth(w);
        }
        else
        {
            myTabHeaderAreaClipRectangle.setWidth(h);
        }
        myTabHeaderAreaClipRectangle.setHeight(headerHeight);

        // ==================================
        // position the tab content for the selected tab only
        // ==================================
        // if the tabs are on the left, the content needs to be indented
        double contentStartX = 0;
        double contentStartY = 0;

        if (tabPosition == Side.TOP)
        {
            contentStartX = x;
            contentStartY = y + headerHeight;
            if (isFloatingStyleClass())
            {
                // This is to hide the top border content
                contentStartY -= 1;
            }
        }
        else if (tabPosition == Side.BOTTOM)
        {
            contentStartX = x;
            contentStartY = y + topInset;
            if (isFloatingStyleClass())
            {
                // This is to hide the bottom border content
                contentStartY = 1 + topInset;
            }
        }
        else if (tabPosition == Side.LEFT)
        {
            contentStartX = x + headerHeight;
            contentStartY = y;
            if (isFloatingStyleClass())
            {
                // This is to hide the left border content
                contentStartX -= 1;
            }
        }
        else if (tabPosition == Side.RIGHT)
        {
            contentStartX = x + leftInset;
            contentStartY = y;
            if (isFloatingStyleClass())
            {
                // This is to hide the right border content
                contentStartX = 1 + leftInset;
            }
        }

        final double contentWidth = w - (isHorizontal() ? 0 : headerHeight);
        final double contentHeight = h - (isHorizontal() ? headerHeight : 0);

        for (int i = 0, max = myTabContentRegions.size(); i < max; i++)
        {
            final OSTabContentRegion tabContent = myTabContentRegions.get(i);

            tabContent.setAlignment(Pos.TOP_LEFT);
            if (tabContent.getClip() != null)
            {
                ((Rectangle)tabContent.getClip()).setWidth(contentWidth);
                ((Rectangle)tabContent.getClip()).setHeight(contentHeight);
            }

            // we need to size all tabs, even if they aren't visible. For
            // example,
            // see RT-29167
            tabContent.resize(contentWidth, contentHeight);
            tabContent.relocate(contentStartX, contentStartY);
        }
    }

    /**
     * A utility method used to get the rotation for the supplied side.
     *
     * @param pos the side for which to get the rotation value.
     * @return the rotation amount, in degrees, for the tabs on the given side.
     */
    protected static int getRotation(final Side pos)
    {
        switch (pos)
        {
            default:
            case TOP:
                return 0;
            case BOTTOM:
                return 180;
            case LEFT:
                return -90;
            case RIGHT:
                return 90;
        }
    }

    /**
     * Skins and adds the supplied tab.
     *
     * @param tab the tab to skin and add.
     */
    protected void addTabContent(final Tab tab)
    {
        final OSTabContentRegion tabContentRegion = new OSTabContentRegion(tab);
        tabContentRegion.setClip(new Rectangle());
        myTabContentRegions.add(tabContentRegion);
        // We want the tab content to always sit below the tab headers
        getChildren().add(0, tabContentRegion);
    }

    /**
     * Removes the supplied tabs.
     *
     * @param removedList the list of tabs to remove.
     */
    protected void removeTabs(final List<? extends Tab> removedList)
    {
        for (final Tab tab : removedList)
        {
            stopCurrentAnimation(tab);
            // Animate the tab removal
            final OSTabHeaderSkin tabRegion = myTabHeaderArea.getTabHeaderSkin(tab);
            if (tabRegion != null)
            {
                tabRegion.setClosing(true);

                tabRegion.removeListeners(tab);
                removeTabContent(tab);

                // remove the menu item from the popup menu
                final ContextMenu popupMenu = myTabHeaderArea.getControlButtons().getPopup();
                OSTabMenuItem tabItem = null;
                if (popupMenu != null)
                {
                    for (final MenuItem item : popupMenu.getItems())
                    {
                        tabItem = (OSTabMenuItem)item;
                        if (tab == tabItem.getTab())
                        {
                            break;
                        }
                        tabItem = null;
                    }
                }
                if (tabItem != null)
                {
                    tabItem.dispose();
                    if (popupMenu != null)
                    {
                        popupMenu.getItems().remove(tabItem);
                    }
                }
                // end of removing menu item

                final EventHandler<ActionEvent> cleanup = ae ->
                {
                    tabRegion.setAnimationState(OSTabAnimationState.NONE);

                    myTabHeaderArea.removeTab(tab);
                    myTabHeaderArea.requestLayout();
                    if (getSkinnable().getTabs().isEmpty())
                    {
                        myTabHeaderArea.setVisible(false);
                    }
                };

                if (myCloseTabAnimation.get() == OSTabAnimation.GROW)
                {
                    tabRegion.setAnimationState(OSTabAnimationState.HIDING);
                    tabRegion.setCurrentAnimation(createTimeline(tabRegion, Duration.millis(ANIMATION_SPEED), 0.0F, cleanup));
                    final Timeline closedTabTimeline = tabRegion.getCurrentAnimation();
                    closedTabTimeline.play();
                }
                else
                {
                    cleanup.handle(null);
                }
            }
        }
    }

    /**
     * Stops the animation occurring on the supplied tab.
     *
     * @param tab the tab for which to stop the animation.
     */
    protected void stopCurrentAnimation(final Tab tab)
    {
        final OSTabHeaderSkin tabRegion = myTabHeaderArea.getTabHeaderSkin(tab);
        if (tabRegion != null)
        {
            // Execute the code immediately, don't wait for the animation to
            // finish.
            final Timeline timeline = tabRegion.getCurrentAnimation();
            if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING)
            {
                timeline.getOnFinished().handle(null);
                timeline.stop();
                tabRegion.setCurrentAnimation(null);
            }
        }
    }

    /**
     * Adds the supplied tabs at the given location.
     *
     * @param addedList the list to add.
     * @param from the location at which to add the tabs.
     */
    protected void addTabs(final List<? extends Tab> addedList, final int from)
    {
        int i = 0;

        // RT-39984: check if any other tabs are animating - they must be
        // completed first.
        final List<Node> headers = new ArrayList<>(myTabHeaderArea.getHeadersDisplayRegion().getChildren());
        for (final Node n : headers)
        {
            final OSTabHeaderSkin header = (OSTabHeaderSkin)n;
            if (header.getAnimationState() == OSTabAnimationState.HIDING)
            {
                stopCurrentAnimation(header.getTab());
            }
        }
        // end of fix for RT-39984

        for (final Tab tab : addedList)
        {
            stopCurrentAnimation(tab); // Note that this must happen before
                                       // addTab() call below
            // A new tab was added - animate it out
            if (!myTabHeaderArea.isVisible())
            {
                myTabHeaderArea.setVisible(true);
            }
            final int index = from + i++;
            myTabHeaderArea.addTab(tab, index);
            addTabContent(tab);
            final OSTabHeaderSkin tabRegion = myTabHeaderArea.getTabHeaderSkin(tab);
            if (tabRegion != null)
            {
                if (myOpenTabAnimation.get() == OSTabAnimation.GROW)
                {
                    tabRegion.setAnimationState(OSTabAnimationState.SHOWING);
                    tabRegion.animationTransitionProperty().setValue(0.0);
                    tabRegion.setVisible(true);
                    tabRegion.setCurrentAnimation(createTimeline(tabRegion, Duration.millis(ANIMATION_SPEED), 1.0, event ->
                    {
                        tabRegion.setAnimationState(OSTabAnimationState.NONE);
                        tabRegion.setVisible(true);
                        tabRegion.getInner().requestLayout();
                    }));
                    tabRegion.getCurrentAnimation().play();
                }
                else
                {
                    tabRegion.setVisible(true);
                    tabRegion.getInner().requestLayout();
                }
            }
        }
    }

    /** Initializes the listeners for tabs. */
    protected void initializeTabListener()
    {
        getSkinnable().getTabs().addListener((ListChangeListener<Tab>)c ->
        {
            final List<Tab> tabsToRemove = new ArrayList<>();
            final List<Tab> tabsToAdd = new ArrayList<>();
            int insertPos = -1;

            while (c.next())
            {
                if (c.wasPermutated())
                {
                    if (myDragState != OSDragState.REORDER)
                    {
                        final TabPane tabPane = getSkinnable();
                        final List<Tab> tabs = tabPane.getTabs();

                        // tabs sorted : create list of permutated tabs.
                        // clear selection, set tab animation to NONE
                        // remove permutated tabs, add them back in correct
                        // order.
                        // restore old selection, and old tab animation states.
                        final int size = c.getTo() - c.getFrom();
                        final Tab selTab = tabPane.getSelectionModel().getSelectedItem();
                        final List<Tab> permutatedTabs = new ArrayList<>(size);
                        getSkinnable().getSelectionModel().clearSelection();

                        // save and set tab animation to none - as it is not a
                        // good idea
                        // to animate on the same data for open and close.
                        final OSTabAnimation prevOpenAnimation = myOpenTabAnimation.get();
                        final OSTabAnimation prevCloseAnimation = myCloseTabAnimation.get();
                        myOpenTabAnimation.set(OSTabAnimation.NONE);
                        myCloseTabAnimation.set(OSTabAnimation.NONE);
                        for (int i = c.getFrom(); i < c.getTo(); i++)
                        {
                            permutatedTabs.add(tabs.get(i));
                        }

                        removeTabs(permutatedTabs);
                        addTabs(permutatedTabs, c.getFrom());
                        myOpenTabAnimation.set(prevOpenAnimation);
                        myCloseTabAnimation.set(prevCloseAnimation);
                        getSkinnable().getSelectionModel().select(selTab);
                    }
                }

                if (c.wasRemoved())
                {
                    tabsToRemove.addAll(c.getRemoved());
                }
                if (c.wasAdded())
                {
                    tabsToAdd.addAll(c.getAddedSubList());
                    insertPos = c.getFrom();
                }
            }

            // now only remove the tabs that are not in the tabsToAdd list
            tabsToRemove.removeAll(tabsToAdd);
            removeTabs(tabsToRemove);

            // and add in any new tabs (that we don't already have showing)
            if (!tabsToAdd.isEmpty())
            {
                for (final OSTabContentRegion tabContentRegion : myTabContentRegions)
                {
                    final Tab tab = tabContentRegion.getTab();
                    final OSTabHeaderSkin tabHeader = myTabHeaderArea.getTabHeaderSkin(tab);
                    if (!tabHeader.isClosing() && tabsToAdd.contains(tabContentRegion.getTab()))
                    {
                        tabsToAdd.remove(tabContentRegion.getTab());
                    }
                }

                addTabs(tabsToAdd, insertPos == -1 ? myTabContentRegions.size() : insertPos);
            }

            // Fix for RT-34692
            getSkinnable().requestLayout();
        });
    }

    /**
     * Removes the content area for the supplied tab.
     *
     * @param tab the tab for which to remove the content area.
     */
    protected void removeTabContent(final Tab tab)
    {
        for (final OSTabContentRegion contentRegion : myTabContentRegions)
        {
            if (contentRegion.getTab().equals(tab))
            {
                contentRegion.removeListeners(tab);
                getChildren().remove(contentRegion);
                myTabContentRegions.remove(contentRegion);
                break;
            }
        }
    }

    /** Updates the position of the currently selected tab. */
    protected void updateTabPosition()
    {
        myTabHeaderArea.setScrollOffset(0.0F);
        getSkinnable().applyCss();
        getSkinnable().requestLayout();
    }

    /**
     * Gets the value of the {@link #mySelectedTab} field.
     *
     * @return the value stored in the {@link #mySelectedTab} field.
     */
    public Tab getSelectedTab()
    {
        return mySelectedTab;
    }

    /**
     * Gets the value of the {@link #myOpenTabAnimation} field.
     *
     * @return the value stored in the {@link #myOpenTabAnimation} field.
     */
    public ObjectProperty<OSTabAnimation> openTabAnimationProperty()
    {
        return myOpenTabAnimation;
    }

    /**
     * Gets the value of the {@link #myCloseTabAnimation} field.
     *
     * @return the value stored in the {@link #myCloseTabAnimation} field.
     */
    public ObjectProperty<OSTabAnimation> closeTabAnimationProperty()
    {
        return myCloseTabAnimation;
    }

    /**
     * VERY HACKY - this lets us 'duplicate' Label and ImageView nodes to be
     * used in a Tab and the tabs menu at the same time.
     *
     * @param n the node to clone.
     * @return the cloned node.
     */
    public static Node clone(final Node n)
    {
        if (n == null)
        {
            return null;
        }
        if (n instanceof ImageView)
        {
            final ImageView iv = (ImageView)n;
            final ImageView imageview = new ImageView();
            imageview.imageProperty().bind(iv.imageProperty());
            return imageview;
        }
        if (n instanceof Label)
        {
            final Label l = (Label)n;
            final Label label = new Label(l.getText(), clone(l.getGraphic()));
            label.textProperty().bind(l.textProperty());
            return label;
        }
        return null;
    }

    /**
     * Creates an animation timeline using the supplied parameters.
     *
     * @param tabRegion the header skin to animate.
     * @param duration the duration of the animation.
     * @param endValue the end state of the animation.
     * @param func the function to call when the animation is complete.
     * @return an animation timeline configured for the supplied parameters.
     */
    protected Timeline createTimeline(final OSTabHeaderSkin tabRegion, final Duration duration, final double endValue,
            final EventHandler<ActionEvent> func)
    {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);

        final KeyValue keyValue = new KeyValue(tabRegion.animationTransitionProperty(), endValue, Interpolator.LINEAR);
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(duration, keyValue));

        timeline.setOnFinished(func);
        return timeline;
    }

    /**
     * Gets the value of the {@link #myDragTabHeader} field.
     *
     * @return the value stored in the {@link #myDragTabHeader} field.
     */
    public OSTabHeaderSkin getDragTabHeader()
    {
        return myDragTabHeader;
    }

    /**
     * Gets the value of the {@link #myDragHeaderSourceX} field.
     *
     * @return the value stored in the {@link #myDragHeaderSourceX} field.
     */
    public double getDragHeaderSourceX()
    {
        return myDragHeaderSourceX;
    }

    /**
     * Gets the value of the {@link #myDragHeaderTransitionX} field.
     *
     * @return the value stored in the {@link #myDragHeaderTransitionX} field.
     */
    public double getDragHeaderTransitionX()
    {
        return myDragHeaderTransitionX;
    }

    /**
     * Gets the value of the {@link #myDropAnimationHeader} field.
     *
     * @return the value stored in the {@link #myDropAnimationHeader} field.
     */
    public OSTabHeaderSkin getDropAnimationHeader()
    {
        return myDropAnimationHeader;
    }

    /**
     * Gets the value of the {@link #myDropHeaderSourceX} field.
     *
     * @return the value stored in the {@link #myDropHeaderSourceX} field.
     */
    public double getDropHeaderSourceX()
    {
        return myDropHeaderSourceX;
    }

    /**
     * Gets the value of the {@link #myDropHeaderTransitionX} field.
     *
     * @return the value stored in the {@link #myDropHeaderTransitionX} field.
     */
    public double getDropHeaderTransitionX()
    {
        return myDropHeaderTransitionX;
    }

    /**
     * Tests to determine if the tab state is horizontal or vertical.
     *
     * @return true if the tabs are displayed horizontally, false otherwise.
     */
    protected boolean isHorizontal()
    {
        final Side tabPosition = getSkinnable().getSide();
        return Side.TOP.equals(tabPosition) || Side.BOTTOM.equals(tabPosition);
    }

    /**
     * Tests to determine if the tabs are floating (by way of the CSS 'floating'
     * class).
     *
     * @return true if the tabs are floating.
     */
    protected boolean isFloatingStyleClass()
    {
        return getSkinnable().getStyleClass().contains(TabPane.STYLE_CLASS_FLOATING);
    }

    /**
     * Adds the reordering listeners to the supplied node.
     *
     * @param n the node to which to add the listeners.
     */
    protected void addReorderListeners(final Node n)
    {
        n.addEventHandler(MouseEvent.MOUSE_PRESSED, myHeaderMousePressedHandler);
        n.addEventHandler(MouseEvent.MOUSE_RELEASED, myHeaderMouseReleasedHandler);
        n.addEventHandler(MouseEvent.MOUSE_DRAGGED, myHeaderDraggedHandler);
    }

    /**
     * Removes the reordering listeners to the supplied node.
     *
     * @param n the node from which to remove the listeners.
     */
    protected void removeReorderListeners(final Node n)
    {
        n.removeEventHandler(MouseEvent.MOUSE_PRESSED, myHeaderMousePressedHandler);
        n.removeEventHandler(MouseEvent.MOUSE_RELEASED, myHeaderMouseReleasedHandler);
        n.removeEventHandler(MouseEvent.MOUSE_DRAGGED, myHeaderDraggedHandler);
    }

    /**
     * Updates the listeners within the tabs to account for tab dragging.
     */
    protected void updateListeners()
    {
        if (getSkinnable().getTabDragPolicy() == TabDragPolicy.FIXED || getSkinnable().getTabDragPolicy() == null)
        {
            for (final Node n : myHeadersRegion.getChildren())
            {
                removeReorderListeners(n);
            }
            myHeadersRegion.getChildren().removeListener(myChildListener);
        }
        else if (getSkinnable().getTabDragPolicy() == TabDragPolicy.REORDER)
        {
            for (final Node n : myHeadersRegion.getChildren())
            {
                addReorderListeners(n);
            }
            myHeadersRegion.getChildren().addListener(myChildListener);
        }
    }

    /**
     * Sets up the supplied stack pane for reordering operations.
     *
     * @param headersRegion the region to reorder.
     */
    protected void setupReordering(final StackPane headersRegion)
    {
        myDragState = OSDragState.NONE;
        myHeadersRegion = headersRegion;
        updateListeners();
        getSkinnable().tabDragPolicyProperty().addListener((observable, oldValue, newValue) ->
        {
            if (oldValue != newValue)
            {
                updateListeners();
            }
        });
    }

    /**
     * An event handler method used to handle when a mouse press is detected on
     * the header.
     *
     * @param event the event fired by the mouse press.
     */
    protected void handleHeaderMousePressed(final MouseEvent event)
    {
        if (event.getClickCount() == 1)
        {
            ((StackPane)event.getSource()).setMouseTransparent(true);
            startDrag(event);
        }
    }

    /**
     * An event handler method used to handle when a mouse release is detected
     * on the header.
     *
     * @param event the event fired by the mouse release.
     */
    protected void handleHeaderMouseReleased(final MouseEvent event)
    {
        ((StackPane)event.getSource()).setMouseTransparent(false);
        stopDrag();
        event.consume();
    }

    /**
     * An event handler method used to handle when a mouse drag event is
     * detected within the header.
     *
     * @param event the event fired by the mouse drag event.
     */
    protected void handleHeaderDragged(final MouseEvent event)
    {
        perfromDrag(event);
    }

    /**
     * Calculates the direction of the tab header layout.
     *
     * @return the direction of the tab header layout.
     */
    protected int deriveTabHeaderLayoutXDirection()
    {
        if (getSkinnable().getSide().equals(Side.TOP) || getSkinnable().getSide().equals(Side.RIGHT))
        {
            // TabHeaderSkin are laid out in left to right direction inside
            // headersRegion
            return MIN_TO_MAX;
        }
        // TabHeaderSkin are laid out in right to left direction inside
        // headersRegion
        return MAX_TO_MIN;
    }

    /**
     * Executes a drag operation based on the supplied mouse event.
     *
     * @param event the event for which to execute the drag operation.
     */
    protected void perfromDrag(final MouseEvent event)
    {
        if (myDragState == OSDragState.NONE)
        {
            // false alarm:
            return;
        }
        int dragDirection;
        double dragHeaderNewLayoutX;
        Bounds dragHeaderBounds;
        Bounds dropHeaderBounds;
        double draggedDist;
        final double mouseCurrentLoc = getHeaderRegionLocalX(event);
        final double dragDelta = getDragDelta(mouseCurrentLoc, myDragEventPreviousLocation);

        if (dragDelta > 0)
        {
            // Dragging the tab header towards higher indexed tab headers inside
            // headersRegion.
            dragDirection = MIN_TO_MAX;
        }
        else
        {
            // Dragging the tab header towards lower indexed tab headers inside
            // headersRegion.
            dragDirection = MAX_TO_MIN;
        }
        // Stop dropHeaderAnim if direction of drag is changed
        if (myPreviousDragDirection != dragDirection)
        {
            stopAnim(myDropHeaderAnimation);
            myPreviousDragDirection = dragDirection;
        }

        dragHeaderNewLayoutX = myDragTabHeader.getLayoutX() + myLayoutDirectionX * dragDelta;

        if (dragHeaderNewLayoutX >= 0 && dragHeaderNewLayoutX + myDragTabHeader.getWidth() <= myHeadersRegion.getWidth())
        {
            myDragState = OSDragState.REORDER;
            myDragTabHeader.setLayoutX(dragHeaderNewLayoutX);
            dragHeaderBounds = myDragTabHeader.getBoundsInParent();

            if (dragDirection == MIN_TO_MAX)
            {
                // Dragging the tab header towards higher indexed tab headers
                // Last tab header can not be dragged outside headersRegion.

                // When the mouse is moved too fast, sufficient number of events
                // are not generated. Hence it is required to check all possible
                // headers to be reordered.
                for (int i = myDragTabHeaderIndex + 1; i < myHeadersRegion.getChildren().size(); i++)
                {
                    myDropTabHeader = (OSTabHeaderSkin)myHeadersRegion.getChildren().get(i);

                    // dropTabHeader should not be already reordering.
                    if (myDropAnimationHeader != myDropTabHeader)
                    {
                        dropHeaderBounds = myDropTabHeader.getBoundsInParent();

                        if (myLayoutDirectionX == MIN_TO_MAX)
                        {
                            draggedDist = dragHeaderBounds.getMaxX() - dropHeaderBounds.getMinX();
                        }
                        else
                        {
                            draggedDist = dropHeaderBounds.getMaxX() - dragHeaderBounds.getMinX();
                        }

                        // A tab header is reordered when dragged tab header
                        // crosses DRAG_DIST_THRESHOLD% of next tab header's
                        // width.
                        if (draggedDist > dropHeaderBounds.getWidth() * DRAG_DISTANCE_THRESHOLD)
                        {
                            stopAnim(myDropHeaderAnimation);
                            // Distance by which tab header should be animated.
                            myDropHeaderTransitionX = myLayoutDirectionX * -dragHeaderBounds.getWidth();
                            if (myLayoutDirectionX == MIN_TO_MAX)
                            {
                                myDragHeaderDestinationX = dropHeaderBounds.getMaxX() - dragHeaderBounds.getWidth();
                            }
                            else
                            {
                                myDragHeaderDestinationX = dropHeaderBounds.getMinX();
                            }
                            startHeaderReorderingAnim();
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                // dragDirection is MAX_TO_MIN
                // Dragging the tab header towards lower indexed tab headers.
                // First tab header can not be dragged outside headersRegion.

                // When the mouse is moved too fast, sufficient number of events
                // are not generated. Hence it is required to check all possible
                // tab headers to be reordered.
                for (int i = myDragTabHeaderIndex - 1; i >= 0; i--)
                {
                    myDropTabHeader = (OSTabHeaderSkin)myHeadersRegion.getChildren().get(i);

                    // dropTabHeader should not be already reordering.
                    if (myDropAnimationHeader != myDropTabHeader)
                    {
                        dropHeaderBounds = myDropTabHeader.getBoundsInParent();

                        if (myLayoutDirectionX == MIN_TO_MAX)
                        {
                            draggedDist = dropHeaderBounds.getMaxX() - dragHeaderBounds.getMinX();
                        }
                        else
                        {
                            draggedDist = dragHeaderBounds.getMaxX() - dropHeaderBounds.getMinX();
                        }

                        // A tab header is reordered when dragged tab crosses
                        // DRAG_DIST_THRESHOLD% of next tab header's width.
                        if (draggedDist > dropHeaderBounds.getWidth() * DRAG_DISTANCE_THRESHOLD)
                        {
                            stopAnim(myDropHeaderAnimation);
                            // Distance by which tab header should be animated.
                            myDropHeaderTransitionX = myLayoutDirectionX * dragHeaderBounds.getWidth();
                            if (myLayoutDirectionX == MIN_TO_MAX)
                            {
                                myDragHeaderDestinationX = dropHeaderBounds.getMinX();
                            }
                            else
                            {
                                myDragHeaderDestinationX = dropHeaderBounds.getMaxX() - dragHeaderBounds.getWidth();
                            }
                            startHeaderReorderingAnim();
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }
        myDragEventPreviousLocation = mouseCurrentLoc;
        event.consume();
    }

    /**
     * Begins a drag animation.
     *
     * @param event the event that triggered the animation.
     */
    protected void startDrag(final MouseEvent event)
    {
        // Stop the animations if any are running from previous reorder.
        stopAnim(myDropHeaderAnimation);
        stopAnim(myDragHeaderAnimation);

        myDragTabHeader = (OSTabHeaderSkin)event.getSource();
        if (myDragTabHeader != null)
        {
            myDragState = OSDragState.START;
            mySwapTab = null;
            myLayoutDirectionX = deriveTabHeaderLayoutXDirection();
            myDragEventPreviousLocation = getHeaderRegionLocalX(event);
            myDragTabHeaderIndex = myHeadersRegion.getChildren().indexOf(myDragTabHeader);
            myDragTabHeader.setViewOrder(0);
            myDragHeaderStartX = myDragHeaderDestinationX = myDragTabHeader.getLayoutX();
        }
    }

    /**
     * The event is converted to tab header's parent i.e. headersRegion's local
     * space. This will provide a value of X coordinate with all transformations
     * of TabPane and transformations of all nodes in the TabPane's parent
     * hierarchy.
     *
     * @param ev the event to convert to the parent's coordinate system.
     * @return the location of the event within the parent's coordinate system.
     */
    protected double getHeaderRegionLocalX(final MouseEvent ev)
    {
        final Point2D sceneToLocalHR = myHeadersRegion.sceneToLocal(ev.getSceneX(), ev.getSceneY());
        return sceneToLocalHR.getX();
    }

    /** Ends a drag operation. */
    protected void stopDrag()
    {
        if (myDragState == OSDragState.NONE)
        {
            // false alarm:
            return;
        }
        if (myDragState == OSDragState.START)
        {
            // No drag action was performed.
            resetDrag();
            return;
        }
        // Animate tab header being dragged to its final position.
        myDragHeaderSourceX = myDragTabHeader.getLayoutX();
        myDragHeaderTransitionX = myDragHeaderDestinationX - myDragHeaderSourceX;
        myDragHeaderAnimation.playFromStart();

        // Reorder the tab list.
        if (myDragHeaderStartX != myDragHeaderDestinationX)
        {
            // this hack is necessary because the TabObservableList is not
            // exposed, but is bound into TabPane instead of the skin, so it
            // can't be replaced with a new class.
            try
            {
                final ObservableList<Tab> tabs = getSkinnable().getTabs();
                final Method reorderMethod = tabs.getClass().getDeclaredMethod("reorder", Tab.class, Tab.class);
                reorderMethod.setAccessible(true);
                reorderMethod.invoke(tabs, myDragTabHeader.getTab(), mySwapTab);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e)
            {
                LOG.error("Unable to invoke method via reflection", e);
            }
            mySwapTab = null;
        }
    }

    /** Resets the drag operation to its original state. */
    protected void resetDrag()
    {
        myDragState = OSDragState.NONE;
        myDragTabHeader.setViewOrder(1);
        myDragTabHeader = null;
        myDropTabHeader = null;
        myHeadersRegion.requestLayout();
    }

    /**
     * Begins reordering operations by animating the tab header, showing it
     * being dropped-on to its new position.
     */
    protected void startHeaderReorderingAnim()
    {
        myDropAnimationHeader = myDropTabHeader;
        mySwapTab = myDropAnimationHeader.getTab();
        myDropHeaderSourceX = myDropAnimationHeader.getLayoutX();
        myDropHeaderAnimation.playFromStart();
    }

    /**
     * Completes the reordering operations applied to the header, by removing
     * myDropAnimationHeader and add at the index position of dragTabHeader.
     */
    protected void completeHeaderReordering()
    {
        if (myDropAnimationHeader != null)
        {
            myHeadersRegion.getChildren().remove(myDropAnimationHeader);
            myHeadersRegion.getChildren().add(myDragTabHeaderIndex, myDropAnimationHeader);
            myDropAnimationHeader = null;
            myHeadersRegion.requestLayout();
            myDragTabHeaderIndex = myHeadersRegion.getChildren().indexOf(myDragTabHeader);
        }
    }

    /**
     * Helper method to stop an animation.
     *
     * @param anim the animation to stop.
     */
    protected void stopAnim(final Animation anim)
    {
        if (anim.getStatus() == Animation.Status.RUNNING)
        {
            anim.getOnFinished().handle(null);
            anim.stop();
        }
    }

    /**
     * Gets the delta for the drag operation between the current and previous
     * items.
     *
     * @param curr the item for which to calculate the delta.
     * @param prev the previous item from which to calculate the delta.
     * @return the distance between the two items.
     */
    protected double getDragDelta(final double curr, final double prev)
    {
        if (getSkinnable().getSide().equals(Side.TOP) || getSkinnable().getSide().equals(Side.RIGHT))
        {
            return curr - prev;
        }
        return prev - curr;
    }
}
