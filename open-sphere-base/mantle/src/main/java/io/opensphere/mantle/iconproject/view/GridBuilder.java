package io.opensphere.mantle.iconproject.view;

import java.net.URL;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import io.opensphere.mantle.icon.IconRegistry;

/** Crates the Icon Display Grid. */
public class GridBuilder extends GridPane
{
    /** the width used for icon buttons. */
    private final int myTileWidth;

    /** the icon registry used for the pane. */
    private final IconRegistry myIconRegistry;

    /** the selected icon URL to be used for the builder. */
    private URL selectedIconURL;

    /** The GridBuilder constructor.
     * sets up the rows and columns for the icon grid
     *
     * @param tileWidth the width of each tile(button)
     * @param iconRegistry the icon registry
     */
    public GridBuilder(int tileWidth, IconRegistry iconRegistry)
    {
        myTileWidth = tileWidth;
        myIconRegistry = iconRegistry;
        //selectedIcon = null;

        //System.out.println(myIconRegistry.getSubCategoiresForCollection("User Added"));

        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        int counter = 2;
        int numcols = 4;
        for (int row = 0; row <= 50; row++)
        {
            for (int col = 0; col <= numcols; col++)
            {
                Button sample = gridButtonBuilder(counter);
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

        /*
        int numR = 5;
        int numC = 4;
        for (int row = 0; row <= numR; row++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            getRowConstraints().add(rc);
        }
        for (int col = 0; col <= numC; col++)
        {
            ColumnConstraints cc = new ColumnConstraints();
         //   cc.setFillWidth(true);
            cc.setPercentWidth(100/5);
            getColumnConstraints().add(cc);
        }
        int topIndex = numR * numC;
        for (int i = 0; i <= 9; i++) {
            Button sample = GridButtonBuilder(i + 1);
            add(sample, i % numC, i / numC);
        }
         */
    }

    /** Build buttons with Images for the grid.
     *
     * @param count the index of the icon
     * @return the built button
     */
    public Button gridButtonBuilder(int count)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setPadding(new Insets(5, 5, 5, 5));
        String text = myIconRegistry.getIconRecordByIconId(count).getName();
        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(myIconRegistry.getIconRecordByIconId(count).getImageURL().toString());

        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 25);
            iconView.setFitWidth(myTileWidth - 25);
        }

        generic.setGraphic(iconView);
        generic.setOnAction(e ->
        {
            selectedIconURL = myIconRegistry.getIconRecordByIconId(count).getImageURL();
            //selectedIcon = iconView;
            System.out.println("button label: " + text);
            System.out.println("Button url:   " + selectedIconURL);
            System.out.println("the reseult of the getter: " + getSelectedIconURL());
        });

        return generic;
    }

    /** The getter for getSelectedIconURL.
     *
     * @return selectedIconURL the URL of the icon selected
     */
    public URL getSelectedIconURL()
    {
        return selectedIconURL;
    }
}