package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.iconproject.model.IconRegistryChangeListener;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

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

        AnchorPane anchorPane = new AnchorPane(mySetControlPane, myIconTabs, myZoomControlPane);

        AnchorPane.setRightAnchor(mySetControlPane, 0.0);
        AnchorPane.setLeftAnchor(mySetControlPane, 0.0);
        AnchorPane.setTopAnchor(mySetControlPane, 0.0);

        AnchorPane.setRightAnchor(myIconTabs, 0.0);
        AnchorPane.setLeftAnchor(myIconTabs, 0.0);
        AnchorPane.setTopAnchor(myIconTabs, 30.0);
        AnchorPane.setBottomAnchor(myIconTabs, 15.0);

        AnchorPane.setRightAnchor(myZoomControlPane, 0.0);
        AnchorPane.setLeftAnchor(myZoomControlPane, 0.0);
        AnchorPane.setBottomAnchor(myZoomControlPane, 0.0);

        setCenter(anchorPane);
        setRight(myDetailPane);

        if (myPanelModel.getSelectedRecord().get() != null)
        {
            String name = myPanelModel.getSelectedRecord().get().collectionNameProperty().get();
            myIconTabs.getSelectionModel().select(myTabs.get(name).getFirstObject());
        }
    }
}
