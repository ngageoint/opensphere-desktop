package io.opensphere.mantle.iconproject.view;

import java.util.ArrayList;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class TreeBuilder extends TreeItem<String>
{
    public TreeBuilder()
    {
        TreeItem Test = new TreeItem("Planes");
        TreeItem Test2 = new TreeItem("Cats");

        getChildren().addAll(Test, Test2);
        setExpanded(true);
    }
}