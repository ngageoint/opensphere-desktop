package io.opensphere.mantle.iconproject.view;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

/** Crates the Icon Display Grid */
public class GridBuilder extends GridPane
{
    /** the width used for icon buttons. */
    private int myTileWidth;

    /** the icon registry used for the pane */
    private IconRegistry myIconRegistry;

    // /** the record of icons to be used */
    // private IconRecord myIconRecord;

    public GridBuilder(int TileWidth, IconRegistry iconRegistry)
    {
        myTileWidth = TileWidth;
        myIconRegistry = iconRegistry;
        System.out.println(myIconRegistry.getSubCategoiresForCollection("User Added"));

        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        int counter = 700;
        int numcols = 4;
        for (int row = 0; row <= 50; row++)
        {
            for (int col = 0; col <= numcols; col++)
            {
                Button sample = GridButtonBuilder(counter);
                add(sample, col, row);
                counter = counter + 1;
            }
        }
        for (int col = 0; col <= numcols; col++)
        {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setFillWidth(true);
            cc.setPercentWidth(100 / numcols);
            getColumnConstraints().add(cc);
        }
    }

    public Button GridButtonBuilder(int count)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setPadding(new Insets(5.,5.,5.,5.));
        generic.setText(myIconRegistry.getIconRecordByIconId(count).getName());
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);
     
        ImageView iconView = new ImageView(myIconRegistry.getIconRecordByIconId(count).getImageURL().toString());

        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 25);
            iconView.setFitWidth(myTileWidth - 25);
        }

        generic.setGraphic(iconView);
        return generic;
    }

}
