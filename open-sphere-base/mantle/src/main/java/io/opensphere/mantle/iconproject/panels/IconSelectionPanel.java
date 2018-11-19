package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.iconproject.model.IconRegistryChangeListener;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/** An panel on which the icon may be selected by the user. */
public class IconSelectionPanel extends BorderPane
{
    /** The model in which state is maintained. */
    private final PanelModel myPanelModel;

    /** The tab pane in which icons for each set are rendered. */
    private final TabPane myIconTabs;

    /** The detail panel in which information about an icon is displayed. */
    private final Node myDetailPane;

    /** The slider in which icon zoom is managed. */
    private final Slider myZoomControlPane;

    /** The component in which set controls are managed. */
    private final Node mySetControlPane;

    /** A dictionary of tabs of icons, organized by icon set. */
    private final Map<String, Pair<Tab, IconGridView>> myTabs;

    /** A flag used to track the sorting state of the editor. */
    private transient boolean mySorting;

    /**
     * Creates a new component bound to the supplied model.
     *
     * @param panelModel the model through which state is maintained.
     */
    public IconSelectionPanel(PanelModel panelModel)
    {
        myPanelModel = panelModel;
        myDetailPane = new IconDetail(panelModel);
        myZoomControlPane = new Slider(20, 150, 60);
        myZoomControlPane.setTooltip(new Tooltip("Adjust the size of the displayed icons"));

        myPanelModel.tileWidthProperty().bindBidirectional(myZoomControlPane.valueProperty());

        mySetControlPane = new TopMenuBar(panelModel);

        List<String> collectionNames = New.list(myPanelModel.getIconRegistry().getCollectionNames());
        Collections.sort(collectionNames);

        myIconTabs = new TabPane();
        myTabs = New.map();

        for (String collection : collectionNames)
        {
            IconGridView content = new IconGridView(panelModel, r -> r.collectionNameProperty().get().equals(collection));
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

        // HBox of control buttons
        Button addSetButton = new Button();
        addSetButton.setGraphic(FxIcons.createClearIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.LIME, 12));
        addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-content-display: graphic-only;");
        addSetButton.setOnMouseEntered(e -> addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-effect: dropshadow(three-pass-box, lime, 15,.25, 0, 0); -fx-content-display: graphic-only;"));
        addSetButton.setOnMouseExited(e -> addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-content-display: graphic-only;"));
        addSetButton.setTooltip(new Tooltip("Add a new icon set."));
        addSetButton.setOnAction(e -> createSet());

        // Anchor the controls
        AnchorPane anchor = new AnchorPane();
        anchor.getChildren().addAll(myIconTabs, addSetButton);
        AnchorPane.setTopAnchor(addSetButton, 3.0);
        AnchorPane.setRightAnchor(addSetButton, 5.0);
        AnchorPane.setTopAnchor(myIconTabs, 1.0);
        AnchorPane.setRightAnchor(myIconTabs, 1.0);
        AnchorPane.setLeftAnchor(myIconTabs, 1.0);
        AnchorPane.setBottomAnchor(myIconTabs, 1.0);

        myIconTabs.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) ->
        {
            if (!mySorting)
            {
                mySorting = true;
                FXCollections.sort(myIconTabs.getTabs(), (o1, o2) -> o1.getText().compareTo(o2.getText()));
                mySorting = false;
            }
        });

        myPanelModel.addRegistryChangeListener((IconRegistryChangeListener.Change c) ->
        {
            myTabs.values().stream().map(p -> p.getSecondObject()).forEach(g -> g.refresh());
        });

        AnchorPane anchorPane = new AnchorPane(mySetControlPane, anchor, myZoomControlPane);

        AnchorPane.setRightAnchor(mySetControlPane, 0.0);
        AnchorPane.setLeftAnchor(mySetControlPane, 0.0);
        AnchorPane.setTopAnchor(mySetControlPane, 0.0);

        AnchorPane.setRightAnchor(anchor, 0.0);
        AnchorPane.setLeftAnchor(anchor, 0.0);
        AnchorPane.setTopAnchor(anchor, 30.0);
        AnchorPane.setBottomAnchor(anchor, 15.0);

        AnchorPane.setRightAnchor(myZoomControlPane, 0.0);
        AnchorPane.setLeftAnchor(myZoomControlPane, 0.0);
        AnchorPane.setBottomAnchor(myZoomControlPane, 0.0);

        setCenter(anchorPane);
        setRight(myDetailPane);

        if (myPanelModel.selectedRecordProperty().get() != null)
        {
            String name = myPanelModel.selectedRecordProperty().get().collectionNameProperty().get();
            myIconTabs.getSelectionModel().select(myTabs.get(name).getFirstObject());
        }
    }

    private void createSet()
    {
        Tab tab = new Tab();
        TextField tabName = new TextField("<New Icon Set>");
        Label label = new Label();
        tabName.textProperty().bindBidirectional(label.textProperty());
        tab.setGraphic(tabName);
        tabName.onActionProperty().set(e -> tab.setGraphic(label));
        label.setOnMouseClicked(e ->
        {
            if (e.getClickCount() >= 2)
            {
                tabName.setText(label.getText());
                tab.setGraphic(tabName);
                tabName.selectAll();
                tabName.requestFocus();
            }
        });
        IconGridView content = new IconGridView(myPanelModel,
                r -> r.collectionNameProperty().get().equals(label.textProperty().get()));
        content.displayProperty().set(true);
        tab.contentProperty().set(content);
        myIconTabs.getTabs().add(tab);
        tabName.selectAll();
        tabName.requestFocus();
    }
}
