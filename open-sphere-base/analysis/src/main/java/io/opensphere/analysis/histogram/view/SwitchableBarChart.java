package io.opensphere.analysis.histogram.view;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.analysis.base.model.BinType;
import io.opensphere.analysis.base.model.DataType;
import io.opensphere.analysis.base.model.LabelModel;
import io.opensphere.analysis.base.model.LabelType;
import io.opensphere.analysis.base.model.Orientation;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.ToolModels;
import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.core.dialog.alertviewer.event.Message;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

/**
 * BarChart that can have the orientation changed. Currently this is not
 * reusable outside the analysis tools.
 */
public class SwitchableBarChart extends BorderPane
{
//    /** Logger reference. */
//    private static final Logger LOGGER = Logger.getLogger(SwitchableBarChart.class);

    /** The model. */
    private final ToolModels myModel;

    /** The vertical UI model. */
    private Series<String, Number> myVerticalSeries;

    /** The vertical chart. */
    private BarChart<String, Number> myVerticalChart;

    /** The horizontal UI model. */
    private Series<Number, String> myHorizontalSeries;

    /** The horizontal chart. */
    private BarChart<Number, String> myHorizontalChart;

    /** Whether the current chart is vertical. */
    private boolean myIsVertical;

    /** Whether the data has been changed. */
    private boolean myDataChanged;

    /** The maximum count of any bar. */
    private int myMaxCount;

    /**
     * Constructor.
     *
     * @param model The model
     */
    public SwitchableBarChart(ToolModels model)
    {
        super();
        myModel = model;
        FXUtilities.runOnFXThreadAndWait(this::initFx);
    }

    /** Initializes the JavaFX stuff. */
    private void initFx()
    {
        FXUtilities.addListenerAndInit(myModel.getSettingsModel().orientationProperty(), this::handleOrientationChange);
        FXUtilities.addListenerAndInit(myModel.getSettingsModel().binTypeProperty(), this::handleBinTypeChange);
        myModel.getSettingsModel().backgroundColorProperty()
                .addListener((obs, old, newValue) -> handleBackgroundColorChange(newValue));
        myModel.getSettingsModel().foregroundColorProperty()
                .addListener((obs, old, newValue) -> handleForegroundColorChange(newValue));
        myModel.getSettingsModel().showTitleProperty()
                .addListener((obs, old, newValue) -> getCharts().stream().forEach(chart -> updateShowTitle(chart, newValue)));
        myModel.getSettingsModel().titleTextProperty()
                .addListener((obs, old, newValue) -> getCharts().stream().forEach(chart -> updateTitleText(chart, newValue)));
        myModel.getSettingsModel().selectedColumnProperty().addListener((obs, old, newValue) -> getCharts().stream()
                .forEach(chart -> updateCategoryLabel((CategoryAxis)chart.lookup("#category"))));
        myModel.getSettingsModel().categoryAxisTextProperty().addListener(
                (obs, old, newValue) -> getCharts().stream().forEach(chart -> updateCategoryAxisText(chart, newValue)));
        myModel.getSettingsModel().countAxisTextProperty()
                .addListener((obs, old, newValue) -> getCharts().stream().forEach(chart -> updateCountAxisText(chart, newValue)));

        myModel.getSettingsModel().getLabelModels().stream().forEach(labelModel ->
        {
            labelModel.colorProperty().addListener((obs, old, newValue) -> getCharts().stream()
                    .forEach(chart -> handleLabelColorChange(chart, labelModel, newValue)));
            labelModel.fontProperty().addListener((obs, old, newValue) -> getCharts().stream()
                    .forEach(chart -> handleLabelFontChange(chart, labelModel, newValue)));
            labelModel.sizeProperty().addListener((obs, old, newValue) -> getCharts().stream()
                    .forEach(chart -> handleLabelSizeChange(chart, labelModel, newValue)));
        });

        myModel.getDataModel().getBins().addListener(this::handleModelChange);
        myModel.getDataModel().layerColorProperty().addListener((obs, old, color) ->
        {
            String styleString = toStyleString("fx-bar-fill", color);
            Series<?, ?> series = myIsVertical ? myVerticalSeries : myHorizontalSeries;
            for (XYChart.Data<?, ?> data : series.getData())
            {
                data.getNode().setStyle(styleString);
            }
        });
    }

    /**
     * Handles a change in the orientation.
     *
     * @param orientation the orientation
     */
    private void handleOrientationChange(Orientation orientation)
    {
        myIsVertical = orientation == Orientation.VERTICAL;
        if (myIsVertical)
        {
            if (myVerticalSeries == null || myDataChanged)
            {
                myVerticalSeries = new XYChart.Series<>();
                myVerticalSeries.getData().setAll(
                        myModel.getDataModel().getBins().stream().map(this::newVerticalData).collect(Collectors.toList()));
                myVerticalChart = newVerticalChart();
            }
            setCenter(myVerticalChart);
        }
        else
        {
            if (myHorizontalSeries == null || myDataChanged)
            {
                myHorizontalSeries = new XYChart.Series<>();
                myHorizontalSeries.getData().setAll(
                        myModel.getDataModel().getBins().stream().map(this::newHorizontalData).collect(Collectors.toList()));
                myHorizontalChart = newHorizontalChart();
            }
            setCenter(myHorizontalChart);
        }
        myDataChanged = false;
    }

    /**
     * Handles a change in the bin type.
     *
     * @param binType the bin type
     */
    private void handleBinTypeChange(BinType binType)
    {
        for (BarChart<?, ?> chart : getCharts())
        {
            chart.setCategoryGap(binType == BinType.UNIQUE ? 4 : 1);
        }
    }

    /**
     * Handles a change in the background color.
     *
     * @param color the color
     */
    private void handleBackgroundColorChange(Color color)
    {
        for (XYChart<?, ?> chart : getCharts())
        {
            updateChartColor(chart, color, myModel.getSettingsModel().foregroundColorProperty().get());
        }
    }

    /**
     * Handles a change in the foreground color.
     *
     * @param color the color
     */
    private void handleForegroundColorChange(Color color)
    {
        for (XYChart<?, ?> chart : getCharts())
        {
            updateChartColor(chart, myModel.getSettingsModel().backgroundColorProperty().get(), color);
        }
    }

    /**
     * Updates the chart colors.
     *
     * @param chart the chart to update
     * @param bgColor the background color
     * @param fgColor the foreground color
     */
    private void updateChartColor(XYChart<?, ?> chart, Color bgColor, Color fgColor)
    {
        StringBuilder sb = new StringBuilder();
        if (bgColor != null)
        {
            sb.append(toStyleString("fx-background", bgColor));
            sb.append(toStyleString("fx-background-color", bgColor));
        }
        if (fgColor != null)
        {
            sb.append(toStyleString("fx-text-background-color", fgColor));
        }
        chart.setStyle(sb.toString());
    }

    /**
     * Handles a change in showing the title.
     *
     * @param chart the chart to update
     * @param showTitle whether to show the title
     */
    private void updateShowTitle(XYChart<?, ?> chart, Boolean showTitle)
    {
        if (showTitle.booleanValue())
        {
            chart.setTitle(myModel.getSettingsModel().titleTextProperty().get());
        }
        else
        {
            chart.setTitle(null);
        }
    }

    /**
     * Handles a change in the title text.
     *
     * @param chart the chart to update
     * @param text the text
     */
    private void updateTitleText(XYChart<?, ?> chart, String text)
    {
        if (myModel.getSettingsModel().showTitleProperty().get())
        {
            chart.setTitle(text);
        }
    }

    /**
     * Handles a change in the category axis label text.
     *
     * @param chart the chart to update
     * @param text the text
     */
    private void updateCategoryAxisText(XYChart<?, ?> chart, String text)
    {
        if (text != null)
        {
            ((CategoryAxis)chart.lookup("#" + LabelType.AXIS_CATEGORY)).setLabel(text);
        }
    }

    /**
     * Handles a change in the count axis label text.
     *
     * @param chart the chart to update
     * @param text the text
     */
    private void updateCountAxisText(XYChart<?, ?> chart, String text)
    {
        if (text != null)
        {
            ((NumberAxis)chart.lookup("#" + LabelType.AXIS_COUNT)).setLabel(text);
        }
    }

    /**
     * Handles a change in label color.
     *
     * @param chart the chart
     * @param labelModel the label model
     * @param color the color
     */
    private void handleLabelColorChange(XYChart<?, ?> chart, LabelModel labelModel, Color color)
    {
        if (color == null)
        {
            labelModel.colorProperty().set(Color.WHITE);
        }
        else
        {
            labelModel.colorProperty().set(color);
        }
        updateLabelStyle(chart, labelModel);
    }

    /**
     * Handles a change in label font family.
     *
     * @param chart the chart
     * @param labelModel the label model
     * @param fontFamily the font family
     */
    private void handleLabelFontChange(XYChart<?, ?> chart, LabelModel labelModel, String fontFamily)
    {
        labelModel.fontProperty().set(fontFamily);
        updateLabelStyle(chart, labelModel);
    }

    /**
     * Handles a change in label font size.
     *
     * @param chart the chart
     * @param labelModel the label model
     * @param size the font size
     */
    private void handleLabelSizeChange(XYChart<?, ?> chart, LabelModel labelModel, Number size)
    {
        if (size.doubleValue() == 0.0)
        {
            labelModel.sizeProperty().set(14);
        }
        else
        {
            labelModel.sizeProperty().set(size.intValue());
        }
        updateLabelStyle(chart, labelModel);
    }

    /**
     * Updates label color and font on a given chart, depending on label model.
     *
     * @param chart the chart
     * @param labelModel the label model
     */
    private void updateLabelStyle(XYChart<?, ?> chart, LabelModel labelModel)
    {
        Color color = labelModel.colorProperty().get();
        Font font = Font.font(labelModel.fontProperty().get(), labelModel.sizeProperty().get());

        switch (labelModel.getType())
        {
            case TITLE:
                Label title = (Label)chart.lookup(".chart-title");
                title.setTextFill(color);
                title.setFont(font);
                break;
            default:
                Label categoryLabel = (Label)chart.lookup("#" + LabelType.AXIS_CATEGORY).lookup(".axis-label");
                categoryLabel.setTextFill(color);
                categoryLabel.setFont(font);
                Label countLabel = (Label)chart.lookup("#" + LabelType.AXIS_COUNT).lookup(".axis-label");
                countLabel.setTextFill(color);
                countLabel.setFont(font);
        }
    }

    /**
     * Handles a change in the model.
     *
     * @param c the change
     */
    private void handleModelChange(ListChangeListener.Change<? extends UIBin> c)
    {
        myDataChanged = true;
        while (c.next())
        {
            if (c.wasUpdated())
            {
                myMaxCount = myModel.getDataModel().getBins().stream().mapToInt(b -> b.getCount()).max().orElse(0);
//                long start = System.currentTimeMillis();

                for (int i = c.getFrom(); i < c.getTo(); ++i)
                {
                    UIBin bin = c.getList().get(i);
                    Integer count = Integer.valueOf(bin.getCount());
                    if (myIsVertical)
                    {
                        myVerticalSeries.getData().get(i).setYValue(count);
                    }
                    else
                    {
                        myHorizontalSeries.getData().get(i).setXValue(count);
                    }
                }

//                long delta = System.currentTimeMillis() - start;
//                int size = c.getTo() - c.getFrom();
//                LOGGER.info("Updated " + size + " in " + delta);
            }
            else
            {
                attachOrDetachModel(c);

                if (c.wasRemoved())
                {
                    myMaxCount = myModel.getDataModel().getBins().stream().mapToInt(b -> b.getCount()).max().orElse(0);
//                    long start = System.currentTimeMillis();

                    Series<?, ?> series = myIsVertical ? myVerticalSeries : myHorizontalSeries;
                    series.getData().remove(c.getFrom(), c.getFrom() + c.getRemovedSize());

//                    long delta = System.currentTimeMillis() - start;
//                    LOGGER.info("Removed " + c.getRemovedSize() + " in " + delta);
                }
                if (c.wasAdded())
                {
//                    long start = System.currentTimeMillis();

                    int max = c.getAddedSubList().stream().mapToInt(b -> b.getCount()).max().orElse(0);
                    if (max > myMaxCount)
                    {
                        myMaxCount = max;
                    }

                    if (myIsVertical)
                    {
                        myVerticalSeries.getData().addAll(c.getFrom(),
                                c.getAddedSubList().stream().map(this::newVerticalData).collect(Collectors.toList()));
                    }
                    else
                    {
                        myHorizontalSeries.getData().addAll(c.getFrom(),
                                c.getAddedSubList().stream().map(this::newHorizontalData).collect(Collectors.toList()));
                    }

//                    long delta = System.currentTimeMillis() - start;
//                    LOGGER.info("Added " + c.getAddedSize() + " in " + delta);
                }
            }
        }
    }

    /**
     * Attaches/detaches the model from the UI as appropriate.
     *
     * @param change the change
     */
    private void attachOrDetachModel(ListChangeListener.Change<? extends UIBin> change)
    {
        Series<?, ?> series = myIsVertical ? myVerticalSeries : myHorizontalSeries;
        XYChart<?, ?> chart = myIsVertical ? myVerticalChart : myHorizontalChart;
        int newBinCount = series.getData().size() - change.getRemovedSize() + change.getAddedSize();
        if (newBinCount <= SettingsModel.MAX_BIN_COUNT)
        {
            // Attach the UI model if necessary
            if (chart.getData().isEmpty())
            {
                if (myIsVertical)
                {
                    myVerticalChart.getData().add(myVerticalSeries);
                }
                else
                {
                    myHorizontalChart.getData().add(myHorizontalSeries);
                }
            }
            // clear the message, so that the bottom pane is removed in
            // HistogramPanel
            myModel.userMessageProperty().set(null);
        }
        else if (!chart.getData().isEmpty())
        {
            // Detach the UI model (ignore updates)
            chart.getData().clear();

            String message = "This would create " + newBinCount + " bins, exceeding the limit of " + SettingsModel.MAX_BIN_COUNT
                    + ".\nYou can choose a different column, change the bin width, or reduce the amount of data, and try again.";
            myModel.userMessageProperty().set(new Message(message, Type.WARNING, "Warning: Too Many Bins"));
        }
    }

    /**
     * Creates a new vertical bar chart.
     *
     * @return the bar chart
     */
    private BarChart<String, Number> newVerticalChart()
    {
        BarChart<String, Number> chart = new BarChart<>(createCategoryAxis(), createCountAxis());
        chart.setVerticalGridLinesVisible(false);
        chart.getData().add(myVerticalSeries);
        setChartSettings(chart);
        return chart;
    }

    /**
     * Creates a new horizontal bar chart.
     *
     * @return the bar chart
     */
    private BarChart<Number, String> newHorizontalChart()
    {
        BarChart<Number, String> chart = new BarChart<>(createCountAxis(), createCategoryAxis());
        chart.setHorizontalGridLinesVisible(false);
        chart.getData().add(myHorizontalSeries);
        setChartSettings(chart);
        return chart;
    }

    /**
     * Creates a category axis.
     *
     * @return the axis
     */
    private CategoryAxis createCategoryAxis()
    {
        CategoryAxis axis = new CategoryAxis();
        axis.setId(LabelType.AXIS_CATEGORY.toString());
        if (myModel.getSettingsModel().categoryAxisTextProperty().getValueSafe().isEmpty())
        {
            updateCategoryLabel(axis);
        }
        ChangeListener<Object> listener = (obs, o, n) -> updateCategoryLabel(axis);
        myModel.getSettingsModel().selectedColumnProperty().addListener(listener);
        myModel.getSettingsModel().timeBinTypeProperty().addListener(listener);
        return axis;
    }

    /**
     * Creates a count axis.
     *
     * @return the axis
     */
    private NumberAxis createCountAxis()
    {
        NumberAxis axis = new NumberAxis();
        if (myModel.getSettingsModel().countAxisTextProperty().getValueSafe().isEmpty())
        {
            myModel.getSettingsModel().countAxisTextProperty().set(LabelType.AXIS_COUNT.toString());
        }
        axis.setId(LabelType.AXIS_COUNT.toString());
        axis.setTickLabelFormatter(new StringConverter<Number>()
        {
            @Override
            public String toString(Number object)
            {
                final double threshhold = 0.000001;
                double doubleValue = object.doubleValue();
                int intValue = (int)Math.round(doubleValue);
                boolean isInteger = Math.abs(doubleValue - intValue) < threshhold;
                return isInteger ? String.valueOf(intValue) : null;
            }

            @Override
            public Number fromString(String string)
            {
                return null;
            }
        });
        return axis;
    }

    /**
     * Sets common chart settings.
     *
     * @param chart the chart
     */
    private void setChartSettings(BarChart<?, ?> chart)
    {
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setBarGap(0);
        chart.setCategoryGap(0);
        updateChartColor(chart, myModel.getSettingsModel().backgroundColorProperty().get(),
                myModel.getSettingsModel().foregroundColorProperty().get());

        updateShowTitle(chart, myModel.getSettingsModel().showTitleProperty().getValue());
        updateTitleText(chart, myModel.getSettingsModel().titleTextProperty().get());
        updateCategoryAxisText(chart, myModel.getSettingsModel().categoryAxisTextProperty().get());
        updateCountAxisText(chart, myModel.getSettingsModel().countAxisTextProperty().get());

        myModel.getSettingsModel().getLabelModels().stream().forEach(labelModel ->
        {
            handleLabelColorChange(chart, labelModel, labelModel.colorProperty().getValue());
            handleLabelFontChange(chart, labelModel, labelModel.fontProperty().getValueSafe());
            handleLabelSizeChange(chart, labelModel, labelModel.sizeProperty().getValue());
        });
    }

    /**
     * Creates a new vertical chart data from the bin model.
     *
     * @param bin the bin model
     * @return the chart data
     */
    private XYChart.Data<String, Number> newVerticalData(UIBin bin)
    {
        XYChart.Data<String, Number> datum = new XYChart.Data<>();
        datum.setXValue(bin.getLabel());
        datum.setYValue(Integer.valueOf(bin.getCount()));
        addDataListener(bin, datum);
        return datum;
    }

    /**
     * Creates a new horizontal chart data from the bin model.
     *
     * @param bin the bin model
     * @return the chart data
     */
    private XYChart.Data<Number, String> newHorizontalData(UIBin bin)
    {
        XYChart.Data<Number, String> datum = new XYChart.Data<>();
        datum.setXValue(Integer.valueOf(bin.getCount()));
        datum.setYValue(bin.getLabel());
        addDataListener(bin, datum);
        return datum;
    }

    /**
     * Adds listener(s) to chart data.
     *
     * @param bin the bin
     * @param datum the datum
     */
    private void addDataListener(UIBin bin, XYChart.Data<?, ?> datum)
    {
        final Label label = new Label();
        Color labelColor = ColorUtilities.getBrightness(FXUtilities.toAwtColor(bin.getColor())) < 130 ? Color.WHITE : Color.BLACK;
        label.setTextFill(labelColor);
        final Tooltip tooltip = new Tooltip();
        Object count = myIsVertical ? datum.getYValue() : datum.getXValue();
        Object category = myIsVertical ? datum.getXValue() : datum.getYValue();
        updateLabelAndTooltip(label, tooltip, count, category);
        datum.nodeProperty().addListener((obs, old, node) ->
        {
            if (node != null)
            {
                node.setStyle(toStyleString("fx-bar-fill", bin.getColor()));
                ((Pane)node).getChildren().add(label);
                Tooltip.install(node, tooltip);
                node.setOnMouseClicked(event -> myModel.getActionModel().binSelected(bin));
            }
        });
        ObjectProperty<?> valueProperty = myIsVertical ? datum.YValueProperty() : datum.XValueProperty();
        valueProperty.addListener((obs, old, newValue) -> updateLabelAndTooltip(label, tooltip, newValue, category));
    }

    /**
     * Updates the label and tooltip.
     *
     * @param label the label
     * @param tooltip the tooltip
     * @param count the count
     * @param category the category value
     */
    private void updateLabelAndTooltip(Label label, Tooltip tooltip, Object count, Object category)
    {
        int countValue = ((Number)count).intValue();
        int threshhold = Math.max(myMaxCount / 20, 1);
        label.setText(countValue >= threshhold ? String.valueOf(countValue) : null);
        tooltip.setText(new StringBuilder("Value: ").append(category).append("  Count: ").append(countValue).toString());
    }

    /**
     * Updates the category label.
     *
     * @param axis the category axis to update
     */
    private void updateCategoryLabel(CategoryAxis axis)
    {
        StringBuilder label = new StringBuilder();
        String selectedColumn = myModel.getSettingsModel().selectedColumnProperty().get();
        if (selectedColumn != null)
        {
            label.append(selectedColumn);
            if (myModel.getSettingsModel().dataTypeProperty().get() == DataType.DATE)
            {
                TimeBinType timeBinType = myModel.getSettingsModel().timeBinTypeProperty().get();
                label.append(" (").append(timeBinType);
                if (timeBinType.getDuration() == null || timeBinType.getDuration().isLessThan(Days.ONE))
                {
                    label.append(", in GMT");
                }
                label.append(")");
            }
            myModel.getSettingsModel().categoryAxisTextProperty().set(label.toString());
        }
    }

    /**
     * Gets the charts.
     *
     * @return the charts
     */
    private Collection<BarChart<?, ?>> getCharts()
    {
        List<BarChart<?, ?>> charts = New.list(2);
        if (myVerticalChart != null)
        {
            charts.add(myVerticalChart);
        }
        if (myHorizontalChart != null)
        {
            charts.add(myHorizontalChart);
        }
        return charts;
    }

    /**
     * Creates a style string.
     *
     * @param style the style
     * @param color the color
     * @return the style string
     */
    private static String toStyleString(String style, Color color)
    {
        return new StringBuilder().append('-').append(style).append(": ").append(toStyleString(color)).append(';').toString();
    }

    /**
     * Converts a color into a style string.
     *
     * @param c the color
     * @return the style string
     */
    private static String toStyleString(Color c)
    {
        final double maxColor = 255.0;
        int r = (int)Math.round(c.getRed() * maxColor);
        int g = (int)Math.round(c.getGreen() * maxColor);
        int b = (int)Math.round(c.getBlue() * maxColor);
        int a = (int)Math.round(c.getOpacity() * maxColor);
        return String.format("rgba(%d,%d,%d,%d)", r, g, b, a).toString();
    }
}
