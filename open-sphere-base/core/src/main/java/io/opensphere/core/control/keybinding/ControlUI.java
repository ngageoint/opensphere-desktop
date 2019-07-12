package io.opensphere.core.control.keybinding;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class ControlUI extends HBox
{
    public ControlUI(int width, int height)
    {
        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        getChildren().add(createSubWindows(width / 4, height / 4));
    }

    private HBox createSubWindows(int width, int height)
    {
        HBox theHbox = new HBox();
        theHbox.setMinWidth(width);
        theHbox.setStyle("-fx-background-color : derive(-fx-base, 50%)");

        return theHbox;
    }

    public class IconDispButton extends Button
    {
        public IconDispButton(String key)
        {
            setText(key);
        }
    }
}
