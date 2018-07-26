package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;

/** Crates the Icon Display Grid. */
public class GridBuilder extends TilePane
{
    /** The width used for icon buttons. */
    private final int myTileWidth;

    /** The icon registry used for the pane. */
    private IconRegistry myIconRegistry;

    /** the selected icon to be used for the builder. */
    private IconRecord mySelectedIcon;

    List<IconRecord> myRecordList;

    /**
     * The GridBuilder constructor. sets up the rows and columns for the icon
     * grid.
     *
     * @param tileWidth the width of each tile(button).
     * @param iconRegistry the icon registry
     * @param category the category the icons belong to on the tree.
     */

    public GridBuilder(int tileWidth, List<IconRecord> recList, IconRegistry iconRegistry)//, String category)
    {
        myTileWidth = tileWidth;
        myIconRegistry = iconRegistry;
        myRecordList = recList;
        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");

        for (IconRecord record : myRecordList)
        {
            Button sample  = buttonBuilder(record);
            setMargin(sample,new Insets(5., 5., 5., 5.));
            getChildren().add(sample);
        }
    }

    private Button buttonBuilder(IconRecord record)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setPadding(new Insets(5, 5, 5, 5));

        String text = record.getName();
        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(record.getImageURL().toString());
        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 25);
            iconView.setFitWidth(myTileWidth - 25);
        }
        generic.setGraphic(iconView);

        generic.setOnAction(e ->
        {
            mySelectedIcon = record;
        });

        return generic;
    }

    /**
     * Shows the icon customizer.
     *
     * @param tb the toolbox.
     * @param owner the current window pane.
     */
    public void showIconCustomizer(Toolbox tb, Window owner)
    {
        IconCustomizerDialog builderPane = new IconCustomizerDialog(owner, myIconRegistry, mySelectedIcon);
        builderPane.setVisible(true);
    }

    /**
     * Clears the gridPane
     */
    public void refresh()
    {
        getChildren().clear();
        System.out.println("clearing yo");
        //new GridBuilder(myTileWidth, myIconRegistry);
    }

    public IconRegistry getMyIconRegistry()
    {
        return myIconRegistry;
    }

    public void setMyIconRegistry(IconRegistry myIconRegistry)
    {
        this.myIconRegistry = myIconRegistry;
    }

    public List<IconRecord> getMyRecordList()
    {
        return myRecordList;
    }

    public void setMyRecordList(List<IconRecord> myRecordList)
    {
        this.myRecordList = myRecordList;
    }

}