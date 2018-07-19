package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.TreeItem;

public class TreeBuilder extends TreeItem<String>
{
    @SuppressWarnings("unchecked")
    public TreeBuilder()
    {
        TreeItem<String> Test = new TreeItem<String>("Planes");
        TreeItem<String> Test2 = new TreeItem<String>("Cats");

        getChildren().addAll(Test, Test2);
        setExpanded(true);
    }
}