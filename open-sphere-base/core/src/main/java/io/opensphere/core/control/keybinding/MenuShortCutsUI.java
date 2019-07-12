package io.opensphere.core.control.keybinding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class MenuShortCutsUI extends HBox
{
    public MenuShortCutsUI(int width, int height)
    {
        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        getChildren().add(createTableView(width, height));
    }

    @SuppressWarnings("unchecked")
    public TableView<MenuShortCut> createTableView(int width, int height)
    {
        TableView<MenuShortCut> theTable = new TableView<>();
        theTable.setSelectionModel(null);
        theTable.setMaxWidth(width);
        theTable.setMaxHeight(height);

        TableColumn<MenuShortCut, String> topicCol = customColumn("Topic", "topic", width);
        TableColumn<MenuShortCut, String> controlCol = customColumn("Control", "control", width);
        TableColumn<MenuShortCut, String> keybindCol = customColumn("Key", "key", width);
        theTable.getColumns().addAll(topicCol, controlCol, keybindCol);

        ObservableList<MenuShortCut> data = FXCollections.observableArrayList(new MenuShortCut("File", "", ""));
        ObservableList<MenuShortCut> finalData = populateMenu(data);

        theTable.setItems(finalData);
        return theTable;
    }

    public TableColumn<MenuShortCut, String> customColumn(String name, String propname, int width)
    {
        TableColumn<MenuShortCut, String> theColumn = new TableColumn<MenuShortCut, String>(name);
        theColumn.setMinWidth(width / 3);
        theColumn.setCellValueFactory(new PropertyValueFactory<>(propname));
        theColumn.setSortable(false);

        return theColumn;
    }

    private ObservableList<MenuShortCut> populateMenu(ObservableList<MenuShortCut> data)
    {
        data.add(new MenuShortCut("", "Open", "Ctrl - O "));
        data.add(new MenuShortCut("", "Quit", "Ctrl - Q "));
        data.add(new MenuShortCut("Edit", "", ""));
        data.add(new MenuShortCut("", "Set Logger Levels", "Ctrl - L"));
        data.add(new MenuShortCut("", "Settings", "F8"));
        data.add(new MenuShortCut("View", "", ""));
        data.add(new MenuShortCut("", "Alert Viewer", "Ctrl - M"));
        data.add(new MenuShortCut("Tools", "", ""));
        data.add(new MenuShortCut("", "Analyze", "Ctrl + Shift - X"));
        data.add(new MenuShortCut("", "ArcLength", "m"));
        data.add(new MenuShortCut("", "Icon Manager", "Ctrl + Shift - I"));
        data.add(new MenuShortCut("", "Styles", "Ctrl + Shift - S"));

        return data;
    }

}