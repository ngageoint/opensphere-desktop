package io.opensphere.search.view;

import java.io.Closeable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.units.angle.Angle;
import io.opensphere.core.units.angle.DecimalDegrees;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.Utilities;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The basic UI to represent a {@link SearchResult}.
 */
public class SearchResultBasic extends GridPane implements Closeable
{
    /** The transparency of the border. */
    private static final double BORDER_ALPHA = .25;

    /** The color applied to the title when the item is selected. */
    private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;

    /** The color applied to the title when the item is selected. */
    private static final Color SELECTED_TITLE_COLOR = new Color(0.050980392, 0.694117647, 0.694117647, 1.0);

    /** The color applied to the title when the item is hovered. */
    private static final Color HOVERED_TITLE_COLOR = Color.SPRINGGREEN;

    /** The selected background color (a mask to make it a little darker). */
    private static final Background SELECTED_BACKGROUND = new Background(
            new BackgroundFill(new Color(0, 0, 0, .35), new CornerRadii(5), null));

    /** The hovered background color (a mask to make it a little darker). */
    private static final Background HOVERED_BACKGROUND = new Background(
            new BackgroundFill(new Color(0, 0, 0, .35), new CornerRadii(5), null));

    /** The default background color (completely transparent). */
    private static final Background DEFAULT_BACKGROUND = new Background(
            new BackgroundFill(new Color(0, 0, 0, 0), new CornerRadii(5), null));

    /** The label in which the description is rendered. */
    private Label myDescription;

    /** The search result to display. */
    private final SearchResult myModel;

    /** The label in which the title is rendered. */
    private Label myTitle;

    /** The property change listener. */
    private final ChangeListener<Boolean> myPropertyListener = this::updateDecoration;

    /**
     * Constructs a new basic UI that displays a search result.
     *
     * @param searchResult The search result to display.
     * @param detailed whether to create the detailed version
     */
    public SearchResultBasic(SearchResult searchResult, boolean detailed)
    {
        myModel = searchResult;
        if (!detailed)
        {
            myModel.hoveredProperty().addListener(myPropertyListener);
            myModel.selectedProperty().addListener(myPropertyListener);
        }
        createUI(detailed);
    }

    /**
     * Cleans up resources.
     */
    @Override
    public void close()
    {
        myModel.hoveredProperty().removeListener(myPropertyListener);
        myModel.selectedProperty().removeListener(myPropertyListener);
    }

    /**
     * Create the ui components.
     *
     * @param detailed whether to create the detailed version
     */
    private void createUI(boolean detailed)
    {
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column2.setPercentWidth(50);
        column2.halignmentProperty().set(HPos.RIGHT);
        getColumnConstraints().addAll(column1, column2);

        setPadding(new Insets(10));
        setBorder(new Border(
                new BorderStroke(new Color(0, 0, 0, BORDER_ALPHA), BorderStrokeStyle.SOLID, new CornerRadii(5), null)));
        if (detailed)
        {
            setVgap(5);
        }

        int row = 0;

        myTitle = new Label();
        myTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        myTitle.textProperty().set(myModel.getText());
        myTitle.setTextFill(Color.WHITE);
        if (detailed)
        {
            add(myTitle, 0, row, 1, 1);
            add(new Label("Provider: " + myModel.getSearchType()), 1, row, 1, 1);
        }
        else
        {
            add(myTitle, 0, row, 2, 1);
        }
        row++;

        myDescription = new Label();
        myDescription.setWrapText(true);
        String description = detailed ? Utilities.getValue(myModel.getFullDescription(), myModel.getDescription())
                : myModel.getDescription();
        myDescription.textProperty().set(description);
        myDescription.setTextFill(DEFAULT_TEXT_COLOR);
        add(myDescription, 0, row++, 2, 1);

        if (detailed)
        {
            String locationText = myModel.getLocations().stream().map(this::toString)
                    .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), ""));
            if (!StringUtils.isBlank(locationText))
            {
                String label = myModel.getLocations().size() > 1 ? "Locations: " : "Location: ";
                add(new Label(label + locationText), 0, row++, 2, 1);
            }
        }
    }

    /**
     * Reacts to selection and focus changes reported through the model.
     *
     * @param observable the observable that triggered the event.
     * @param oldValue the previous value of the field.
     * @param newValue the new value of the field.
     */
    private void updateDecoration(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
    {
        // Always set the description text color to work around ListView's
        // stupidity.
        myDescription.setTextFill(DEFAULT_TEXT_COLOR);
        if (myModel.selectedProperty().get())
        {
            myTitle.setTextFill(SELECTED_TITLE_COLOR);
            backgroundProperty().set(SELECTED_BACKGROUND);
        }
        else if (myModel.hoveredProperty().get())
        {
            backgroundProperty().set(HOVERED_BACKGROUND);
            myTitle.setTextFill(HOVERED_TITLE_COLOR);
        }
        else
        {
            backgroundProperty().set(DEFAULT_BACKGROUND);
            myTitle.setTextFill(DEFAULT_TEXT_COLOR);
        }
    }

    /**
     * Formats the location to a string.
     *
     * @param location the location
     * @return the formatted string
     */
    private String toString(LatLonAlt location)
    {
        StringBuilder builder = new StringBuilder();
        Angle lat = Angle.create(DecimalDegrees.class, location.getLatD());
        Angle lon = Angle.create(DecimalDegrees.class, location.getLonD());
        Length alt = Length.create(Meters.class, location.getAltitude().getMagnitude());
        builder.append(lat.toShortLabelString(12, 6, 'N', 'S')).append(' ');
        builder.append(lon.toShortLabelString(12, 6, 'E', 'W'));
        if (alt.getMagnitude() != 0)
        {
            builder.append(' ').append(alt.toShortLabelString());
        }
        return builder.toString();
    }
}
