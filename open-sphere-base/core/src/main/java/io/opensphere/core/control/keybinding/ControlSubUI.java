package io.opensphere.core.control.keybinding;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class ControlSubUI {
	
	public HBox topBox() {
		HBox topbar = new HBox();
		
		return topbar;
	}

	public Button custoButton(String theText) {

		Button theButton = new Button(theText);
		theButton.setStyle(" -fx-background-color: \r\n" + "        #3c7fb1,\r\n"
				+ "        linear-gradient(#fafdfe, #e8f5fc),\r\n"
				+ "        linear-gradient(#eaf6fd 0%, #d9f0fc 49%, #bee6fd 50%, #a7d9f5 100%);\r\n"
				+ "    -fx-background-insets: 0,1,2;\r\n" + "    -fx-background-radius: 3,2,1;\r\n"
				+ " " + "    -fx-text-fill: black;\r\n" + "    -fx-font-size: 14px;");

		return theButton;
	}

}
