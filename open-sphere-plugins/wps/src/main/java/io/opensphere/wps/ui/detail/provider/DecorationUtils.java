package io.opensphere.wps.ui.detail.provider;

import java.util.List;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import org.apache.commons.lang.StringUtils;

import jidefx.scene.control.validation.ValidationEvent;

/**
 * A utility class in which node-decoration methods are provided.
 */
public final class DecorationUtils
{
    /** The error style. */
    public static final String ERROR_STYLE = "-fx-focus-color: red; -fx-faint-focus-color: #d3524422; "
            + "-fx-highlight-fill: -fx-accent; -fx-highlight-text-fill: white; "
            + "-fx-background-color: -fx-focus-color, -fx-control-inner-background, "
            + "-fx-faint-focus-color, linear-gradient(from 0px 0px to 0px 5px, "
            + "derive(-fx-control-inner-background, -9%), -fx-control-inner-background); "
            + "-fx-background-insets: -0.2, 1, -1.4, 3; -fx-background-radius: 3, 2, 4, 0; "
            + "-fx-prompt-text-fill: transparent;";

    /**
     * Private constructor, hidden from use.
     */
    private DecorationUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Updates the border of the supplied node, changing it based on the
     * contents of the supplied event.
     *
     * @param pNode the node for which the border will be updated.
     * @param pEvent the event on which the update operation is based.
     */
    public static void update(Node pNode, EventType<ValidationEvent> pEvent)
    {
        if (pEvent.equals(ValidationEvent.VALIDATION_ERROR))
        {
            pNode.setStyle(ERROR_STYLE);
        }
        else
        {
            pNode.setStyle("-fx-border-style: none;");
        }
    }

    /**
     * Sets the value of the combo box.
     *
     * @param comboBox the combo box
     * @param values the values in the combo box
     * @param defaultValue the default value
     */
    public static void setValue(ComboBox<String> comboBox, List<String> values, String defaultValue)
    {
        String value = null;
        if (values.size() == 1)
        {
            value = values.get(0);
        }
        else if (StringUtils.isNotBlank(defaultValue) && values.contains(defaultValue))
        {
            value = defaultValue;
        }
        else if (values.contains(comboBox.getValue()))
        {
            value = comboBox.getValue();
        }
        else if (values.contains(""))
        {
            value = "";
        }
        comboBox.getSelectionModel().select(value);
    }
}
