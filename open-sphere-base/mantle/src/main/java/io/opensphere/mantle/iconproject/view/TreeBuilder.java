package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.TreeItem;

import io.opensphere.mantle.icon.IconRegistry;

public class TreeBuilder extends TreeItem<String>
{
    @SuppressWarnings("unchecked")
    public TreeBuilder(IconRegistry iconReg)
    {
        TreeItem<String> Test = new TreeItem<String>("Planes");
        TreeItem<String> Test2 = new TreeItem<String>("Cats");

        getChildren().addAll(Test, Test2);
        setExpanded(true);
    }
}