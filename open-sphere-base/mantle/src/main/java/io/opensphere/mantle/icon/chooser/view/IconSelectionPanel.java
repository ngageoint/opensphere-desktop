package io.opensphere.mantle.icon.chooser.view;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.fx.OSTab;
import io.opensphere.core.util.fx.OSTabPane;
import io.opensphere.core.util.fx.tabpane.skin.TabEditPhase;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.IconChooserModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/** An panel on which the icon may be selected by the user. */
public class IconSelectionPanel extends BorderPane
{
    /** The default zoom level. */
    private static final int DEFAULT_ICON_SCALE = 60;

    /** The model in which state is maintained. */
    final IconModel myPanelModel;

    /** The tab pane in which icons for each set are rendered. */
    private final OSTabPane myIconTabs;

    /** The detail panel in which information about an icon is displayed. */
    private final IconDetail myDetailPane;

    /** The slider in which icon zoom is managed. */
    private final Slider myZoomControlPane;

    /** The component in which set controls are managed. */
    private final Node mySetControlPane;

    /** A dictionary of tabs of icons, organized by icon set. */
    private final Map<String, Pair<Tab, IconGridView>> myTabs;

    /** A flag used to track the sorting state of the editor. */
    private transient boolean mySorting;

    /** The model to which the icon chooser is bound. */
    private IconChooserModel myIconChooserModel;

    /**
     * Creates a new component bound to the supplied model.
     *
     * @param panelModel the model through which state is maintained.
     */
    public IconSelectionPanel(IconModel panelModel)
    {
        myPanelModel = panelModel;
        myIconChooserModel = myPanelModel.getModel();
        myDetailPane = new IconDetail(panelModel, this::refresh);

        HBox box = new HBox(5);
        Label iconScaleLabel = new Label("Icon Scale:");
        box.getChildren().add(iconScaleLabel);

        myZoomControlPane = new Slider(20, 150, DEFAULT_ICON_SCALE);
        myZoomControlPane.setTooltip(new Tooltip("Adjust the size of the displayed icons"));
        box.getChildren().add(myZoomControlPane);

        Label scaleReset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES, Color.ORANGERED, 14);
        scaleReset.setOnMouseClicked(e -> myPanelModel.tileWidthProperty().set(DEFAULT_ICON_SCALE));
        scaleReset.setAlignment(Pos.CENTER);
        box.getChildren().add(scaleReset);

        HBox.setHgrow(myZoomControlPane, Priority.ALWAYS);
        HBox.setHgrow(iconScaleLabel, Priority.NEVER);
        HBox.setHgrow(scaleReset, Priority.NEVER);

        myPanelModel.tileWidthProperty().bindBidirectional(myZoomControlPane.valueProperty());

        mySetControlPane = new SearchControlBar(panelModel);

        SortedList<String> collectionNames = myIconChooserModel.getCollectionNames().sorted();

        myIconTabs = new OSTabPane();
        myIconTabs.tabDragPolicyProperty().set(TabDragPolicy.REORDER);
        myIconTabs.newTabAction().set(e -> createSet());
        myTabs = New.map();

        for (String collection : collectionNames)
        {
            IconGridView content;
            if (StringUtils.equalsIgnoreCase(IconRecord.FAVORITES_COLLECTION, collection))
            {
                content = new IconGridView(panelModel, r -> r.favoriteProperty().get());
            }
            else
            {
                content = new IconGridView(panelModel, r -> r.collectionNameProperty().get().equals(collection));
            }

            Tab tab = new Tab(collection, content);

            content.displayProperty().addListener((obs, ov, nv) ->
            {
                if (nv)
                {
                    myIconTabs.getTabs().add(tab);
                }
                else
                {
                    myIconTabs.getTabs().remove(tab);
                }
            });

            myTabs.put(collection, new Pair<>(tab, content));
            if (content.displayProperty().get())
            {
                myIconTabs.getTabs().add(tab);
            }
        }

        myIconTabs.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) ->
        {
            if (!mySorting)
            {
                mySorting = true;
                FXCollections.sort(myIconTabs.getTabs(), (o1, o2) -> o1.getText().compareTo(o2.getText()));
                mySorting = false;
            }
        });

        myPanelModel.getCustomizationModel().sourceProperty().addListener((obs, ov, nv) ->
        {
            if (StringUtils.isNotBlank(myPanelModel.getCustomizationModel().sourceProperty().get()))
            {
                myIconTabs.selectionModelProperty().get()
                        .select(myTabs.get(myPanelModel.getCustomizationModel().sourceProperty().get()).getFirstObject());
            }
        });

        AnchorPane anchorPane = new AnchorPane(mySetControlPane, myIconTabs, box);

        AnchorPane.setRightAnchor(mySetControlPane, 0.0);
        AnchorPane.setLeftAnchor(mySetControlPane, 0.0);
        AnchorPane.setTopAnchor(mySetControlPane, 0.0);

        AnchorPane.setRightAnchor(myIconTabs, 0.0);
        AnchorPane.setLeftAnchor(myIconTabs, 0.0);
        AnchorPane.setTopAnchor(myIconTabs, 30.0);
        AnchorPane.setBottomAnchor(myIconTabs, 15.0);

        AnchorPane.setRightAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);

        setCenter(anchorPane);
        setRight(myDetailPane);

        myPanelModel.searchTextProperty().addListener((obs, ov, nv) ->
        {
            refresh();
        });
    }

    /** An event handler used to create and add a new icon set. */
    private void createSet()
    {
        OSTab tab = new OSTab("<New Icon Set>");
        myIconTabs.getTabs().add(myIconTabs.getTabs().size(), tab);
        myIconTabs.getSelectionModel().select(tab);
        tab.tabEditPhaseProperty().set(TabEditPhase.EDITING);
    }

    /**
     * Gets the value of the {@link #myDetailPane} field.
     *
     * @return the value of the myDetailPane field.
     */
    public IconDetail getDetailPane()
    {
        return myDetailPane;
    }

    /** Refreshes the content on all of the tabs. */
    private void refresh()
    {
        myPanelModel.getIconRegistry().iconStateChanged();
    }
}
