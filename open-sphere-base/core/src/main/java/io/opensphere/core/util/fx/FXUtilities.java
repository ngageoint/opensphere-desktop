package io.opensphere.core.util.fx;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Nanoseconds;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.Nulls;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;

/** JavaFX utilities. */
@SuppressWarnings("PMD.GodClass")
public final class FXUtilities
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(FXUtilities.class);

    /**
     * Adds some style to the parent.
     *
     * @param parent the parent
     * @return the parent
     */
    public static Parent addDesktopStyle(Parent parent)
    {
        addDesktopStyle(parent.getStylesheets());
        return parent;
    }

    /**
     * Adds some style to the scene.
     *
     * @param scene the scene
     * @return the scene
     */
    public static Scene addDesktopStyle(Scene scene)
    {
        addDesktopStyle(scene.getStylesheets());
        return scene;
    }

    /**
     * Adds the listener to the property and calls the listener with the initial
     * value.
     *
     * @param <T> the type of the value
     * @param property the property
     * @param listener the listener
     */
    public static <T> void addListenerAndInit(ObjectProperty<T> property, Consumer<T> listener)
    {
        listener.accept(property.get());
        property.addListener((obs, old, newValue) -> listener.accept(newValue));
    }

    /**
     * Given a table column and a row number, determine the width of the cell at
     * that location.
     *
     * @param <S> The type of the TableView generic type (i.e. S ==
     *            TableView&lt;S&gt;)
     * @param <T> The type of the content in all cells in this TableColumn.
     * @param tc The table column.
     * @param from The beginning row number (inclusive).
     * @param to The ending row number (exclusive).
     * @return The width.
     */
    public static <S, T> double getMaxTableCellWidth(TableColumn<S, T> tc, int from, int to)
    {
        final Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory = tc.getCellFactory();
        final TableCell<S, T> cell = cellFactory == null ? null : cellFactory.call(tc);
        if (cell == null)
        {
            return 0.;
        }

        // set this property to tell the TableCell we want to know its actual
        // preferred width, not the width of the associated TableColumnBase
        cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);

        // determine cell padding
        double padding = 10;
        final Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
        if (n instanceof Region)
        {
            final Region r = (Region)n;
            padding = r.snappedLeftInset() + r.snappedRightInset();
        }

        double maxWidth = 0;
        for (int row = from; row < to; ++row)
        {
            cell.updateTableColumn(tc);
            cell.updateTableView(tc.getTableView());
            cell.updateIndex(row);

            if (cell.getText() != null && !cell.getText().isEmpty() || cell.getGraphic() != null)
            {
                @SuppressWarnings("unchecked")
                final SkinBase<TableView<T>> skinBase = (SkinBase<TableView<T>>)tc.getTableView().getSkin();
                skinBase.getChildren().add(cell);
                cell.applyCss();
                maxWidth = Math.max(maxWidth, cell.prefWidth(-1));
                skinBase.getChildren().remove(cell);
            }
        }

        return maxWidth + padding;
    }

    /**
     * Load a web page into a {@link WebEngine} and call the
     * {@code engineConsumer} once the page is successfully loaded. This will
     * block until the page is loaded, so this must not be called from the FX
     * thread.
     *
     * @param url The URL to load.
     * @param timeout The amount of time to wait for the page to load before
     *            timing out.
     * @param engineConsumer A consumer for the web engine.
     * @throws ExecutionException If an error is thrown by the WebEngine.
     * @throws InterruptedException If the thread is interrupted.
     * @throws TimeoutException If the request runs out of time.
     */
    public static void loadAndProcess(String url, Duration timeout, Consumer<? super WebEngine> engineConsumer)
            throws ExecutionException, InterruptedException, TimeoutException
    {
        loadAndProcess(timeout, engine -> engine.load(url), engineConsumer);
    }

    /**
     * Load content into a {@link WebEngine} and call the {@code engineConsumer}
     * once the page is successfully loaded. This will block until the page is
     * loaded, so this must not be called from the FX thread.
     *
     * @param content The content to load.
     * @param timeout The amount of time to wait for the page to load before
     *            timing out.
     * @param engineConsumer A consumer for the web engine.
     * @throws ExecutionException If an error is thrown by the WebEngine.
     * @throws InterruptedException If the thread is interrupted.
     * @throws TimeoutException If the request runs out of time.
     */
    public static void loadContentAndProcess(String content, Duration timeout, Consumer<? super WebEngine> engineConsumer)
            throws ExecutionException, InterruptedException, TimeoutException
    {
        loadAndProcess(timeout, engine -> engine.loadContent(content), engineConsumer);
    }

    /**
     * Creates a new horizontal spacer, suitable for use in an HBox.
     *
     * @return the spacer
     */
    public static Region newHSpacer()
    {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Creates a new horizontal spacer, suitable for use in an HBox.
     *
     * @param width the width
     * @return the spacer
     */
    public static Region newHSpacer(double width)
    {
        final Region spacer = new Region();
        spacer.setPrefWidth(width);
        return spacer;
    }

    /**
     * Creates a default border.
     *
     * @return the border
     */
    public static Border newBorder()
    {
        return new Border(
                new BorderStroke(Color.web("#464656"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    }

    /**
     * Creates a BorderPane.
     *
     * @param top The node to set as the top of the BorderPane.
     * @param center The node to set as the center of the BorderPane.
     * @return the border pane
     */
    public static BorderPane newBorderPane(Node top, Node center)
    {
        final BorderPane pane = new BorderPane();
        pane.setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 5, 0));
        pane.setCenter(center);
        return pane;
    }

    /**
     * Utility method to create a new HBox.
     *
     * @param elements the elements
     * @return the HBox
     */
    public static HBox newHBox(Node... elements)
    {
        final HBox box = new HBox(5, elements);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Utility method to create a new VBox.
     *
     * @param elements the elements
     * @return the VBox
     */
    public static VBox newVBox(Node... elements)
    {
        final VBox box = new VBox(5, elements);
        return box;
    }

    /**
     * Creates a new button for the given icon type.
     *
     * @param type The icon type.
     * @return the button
     */
    public static Button newIconButton(IconType type)
    {
        return newIconButton(type, null);
    }

    /**
     * Creates a new button for the given icon type.
     *
     * @param type The icon type.
     * @param color optional color
     * @return the button
     */
    public static Button newIconButton(IconType type, Color color)
    {
        return newIconButton(Nulls.STRING, type, color);
    }

    /**
     * Creates a new button for the given resource name, and optional color.
     *
     * @param resourceName the resource name (relative path to the image)
     * @param color optional color
     * @return the button
     */
    public static Button newIconButton(String resourceName, Color color)
    {
        Image image;
        if (color != null)
        {
            final ImageIcon imageIcon = new ImageIcon(FXUtilities.class.getResource(resourceName));
            final BufferedImage colorizedImage = IconUtil.getColorizedImage(imageIcon, IconStyle.NORMAL, toAwtColor(color));
            image = SwingFXUtils.toFXImage(colorizedImage, null);
        }
        else
        {
            image = new Image(resourceName);
        }
        final Button button = new Button(null, new ImageView(image));
        button.setPadding(new Insets(0));
        button.setPrefSize(24, 24);
        return button;
    }

    /**
     * Creates a new button for the given icon type.
     *
     * @param text The text for the button.
     * @param type The icon type.
     * @param color optional color
     * @return the button
     */
    public static Button newIconButton(String text, IconType type, Color color)
    {
        return newIconButton(text, type, color, -1);
    }

    /**
     * Creates a new button for the given icon type.
     *
     * @param text The text for the button.
     * @param type The icon type.
     * @param color optional color
     * @param size The icon size, or -1 to leave it unchanged
     * @return the button
     */
    public static Button newIconButton(String text, IconType type, Color color, int size)
    {
        final ImageIcon imageIcon = IconUtil.getIcon(type);
        final java.awt.Color awtColor = color == null ? IconUtil.DEFAULT_ICON_FOREGROUND : toAwtColor(color);
        final BufferedImage colorizedImage = IconUtil.getColorizedImage(imageIcon, IconStyle.NORMAL, awtColor);
        final Image image = SwingFXUtils.toFXImage(colorizedImage, null);

        final ImageView imageView = new ImageView(image);
        if (size != -1)
        {
            imageView.setFitWidth(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
        }

        final Button button = new Button(text, imageView);
        if (text == null)
        {
            button.setPrefSize(24, 24);
            button.setPadding(new Insets(0));
        }
        else
        {
            button.setPadding(new Insets(2, 6, 2, 6));
        }
        return button;
    }

    /**
     * Creates a text field that formats the text for numbers.
     *
     * @param numberOfDecimals The number of decimals to format. This can be
     *            zero to represent whole numbers.
     * @return The new text field.
     */
    public static TextField newNumericText(int numberOfDecimals)
    {
        TextField text = new TextField();
        StringBuilder formatString = new StringBuilder("0");
        if (numberOfDecimals > 0)
        {
            formatString.append('.');
            for (int i = 0; i < numberOfDecimals; i++)
            {
                formatString.append('0');
            }
        }

        if (numberOfDecimals == 0)
        {
            formatString.append(';');
        }

        DecimalFormat format = new DecimalFormat(formatString.toString());
        format.setMaximumFractionDigits(0);
        text.setTextFormatter(new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty())
            {
                return c;
            }

            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parse(c.getControlNewText(), parsePosition);

            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()
                    || numberOfDecimals == 0 && c.getControlNewText().contains("."))
            {
                return null;
            }
            return c;
        }));

        return text;
    }

    /**
     * Converts an AWT color to a JavaFX color. If no color is provided, default
     * to White.
     *
     * @param awtColor the AWT color
     * @return the JavaFX color
     */
    public static Color fromAwtColor(java.awt.Color awtColor)
    {
        final double maxOpacity = 255.;
        Color color;

        if (awtColor != null)
        {
            color = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(), awtColor.getAlpha() / maxOpacity);
        }
        else
        {
            color = Color.WHITE;
        }

        return color;
    }

    /**
     * Converts a JavaFX color to an AWT color. If no color is provided, default
     * to White.
     *
     * @param color the JavaFX color
     * @return the AWT color
     */
    public static java.awt.Color toAwtColor(Color color)
    {
        java.awt.Color awtColor;

        if (color != null)
        {
            awtColor = new java.awt.Color((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(),
                    (float)color.getOpacity());
        }
        else
        {
            awtColor = java.awt.Color.WHITE;
        }

        return awtColor;
    }

    /**
     * Runs the runnable on the fx application thread if the calling thread is
     * not already a the fx thread.
     *
     * @param runnable The code to execute on the fx thread.
     */
    public static void runOnFXThread(Runnable runnable)
    {
        if (Platform.isFxApplicationThread())
        {
            runnable.run();
        }
        else
        {
            Platform.runLater(runnable);
        }
    }

    /**
     * Runs the runnable on the fx application thread if the calling thread is
     * not already a the fx thread.
     *
     * @param runnable The code to execute on the fx thread.
     */
    public static void runOnFXThreadAndWait(Runnable runnable)
    {
        if (Platform.isFxApplicationThread())
        {
            runnable.run();
        }
        else
        {
            runAndWait(runnable);
        }
    }

    /**
     * Extracted implementation of PlatformImpl.runAndWait(Runnable r).
     *
     * @param runnable the runnable to execute
     */
    public static void runAndWait(Runnable runnable)
    {
        if (Platform.isFxApplicationThread())
        {
            try
            {
                runnable.run();
            }
            catch (Throwable t)
            {
                LOG.error("Exception in Runnable : runAndWait", t);
            }
        }
        else
        {
            final CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(() ->
            {
                try
                {
                    runnable.run();
                }
                finally
                {
                    doneLatch.countDown();
                }
            });

            // The original method checked if the JavaFX toolkit had already
            // exited; our Kernal class ensures that will not occur until the
            // entire application exits.

            try
            {
                doneLatch.await();
            }
            catch (InterruptedException ex)
            {
                LOG.error("Exception in CountDownLatch : runAndWait", ex);
            }
        }
    }

    /**
     * Shows an alert in a dialog subordinate to the specified <i>parent</i>
     * Window, if one is given. Otherwise, it is the same as its namesake.
     *
     * @param parent the parent ("owner") Window
     * @param message alert message
     * @param title dialog title String
     * @param buttons the buttons to display on the alert.
     * @return the user selection, if any, or null
     */
    public static ButtonType showAlert(java.awt.Window parent, String message, String title, ButtonType... buttons)
    {
        return showAlert(parent, message, title, JFXAlert.DEFAULT_WIDTH, JFXAlert.DEFAULT_HEIGHT, buttons);
    }

    /**
     * Shows an alert in a dialog subordinate to the specified <i>parent</i>
     * Window, if one is given. Otherwise, it is the same as its namesake.
     *
     * @param parent the parent ("owner") Window
     * @param message alert message
     * @param title dialog title String
     * @param buttons the buttons to display on the alert.
     * @param width the width of the dialog to display.
     * @param height the height of the dialog to display.
     * @return the user selection, if any, or null
     */
    public static ButtonType showAlert(java.awt.Window parent, String message, String title, int width, int height,
            ButtonType... buttons)
    {
        JFXAlert alert = new JFXAlert(parent, JFXAlertType.CONFIRMATION, title, message, buttons);
        return alert.showAndWait().orElse(null);
    }

    /**
     * Adds some style to the stylesheets.
     *
     * @param stylesheets the stylesheets
     */
    private static void addDesktopStyle(ObservableList<String> stylesheets)
    {
        loadFont("FontAwesome Regular", "/fonts/fa-regular-400.ttf");
        loadFont("FontAwesome Solid", "/fonts/fa-solid-900.ttf");
        loadFont("FontAwesome Brands", "/fonts/fa-brands-400.ttf");

        stylesheets.add(FXUtilities.class.getResource("/styles/opensphere.css").toExternalForm());
    }

    /**
     * Loads the supplied font package for use in glyphs.
     *
     * @param packageName the human readable name of the font package.
     * @param fontPath the relative path of the font TTF file.
     */
    private static void loadFont(String packageName, String fontPath)
    {
        final URL fontUrl = FXUtilities.class.getResource(fontPath);
        if (fontUrl != null)
        {
            Font font = Font.loadFont(fontUrl.toExternalForm(), 12);
            LOG.info("Loaded font " + packageName + " family: '" + font.getFamily() + "' name: '" + font.getName() + "'");
        }
        else
        {
            LOG.warn("Unable to load " + packageName + " package.");
        }
    }

    /**
     * Create a {@link WebEngine} on the FX thread and give it to the
     * {@code engineLoader}. Then wait for the content to load and then supply
     * the engine to the {@code engineConsumer}.
     *
     * @param timeout The amount of time to wait for the page to load before
     *            timing out.
     * @param engineLoader A consumer responsible for loading the web engine.
     * @param engineConsumer A consumer for the web engine once it's loaded.
     * @throws ExecutionException If an error is thrown by the WebEngine.
     * @throws InterruptedException If the thread is interrupted.
     * @throws TimeoutException If the request runs out of time.
     */
    private static void loadAndProcess(Duration timeout, Consumer<? super WebEngine> engineLoader,
            Consumer<? super WebEngine> engineConsumer)
                    throws InterruptedException, TimeoutException, ExecutionException
    {
        assert !Platform.isFxApplicationThread();

        // Prevent the web engine from being garbage-collected.
        final AtomicReference<WebEngine> engineSaver = new AtomicReference<>();

        final AtomicReference<Throwable> errorProp = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final Runnable webengRunner = () ->
        {
            final WebEngine eng = setupWebEngine(engineConsumer, errorProp, latch);
            engineSaver.set(eng);
            engineLoader.accept(eng);
        };
        try
        {
            Platform.startup(webengRunner);
        }
        catch (IllegalStateException e)
        {
            // Platform already started; ignore
            Platform.runLater(webengRunner);
        }

        if (!latch.await(Nanoseconds.get(timeout).longValue(), TimeUnit.NANOSECONDS))
        {
            throw new TimeoutException("Request exceeded timeout of " + timeout);
        }

        if (errorProp.get() != null)
        {
            throw new ExecutionException(errorProp.get());
        }
    }

    /**
     * Set up the web engine.
     *
     * @param engineConsumer The consumer that gets the engine once it reaches
     *            the {@code SUCCEEDED} state.
     * @param error Error receiver.
     * @param latch The latch to count down when processing is complete.
     * @return The engine.
     */
    private static WebEngine setupWebEngine(Consumer<? super WebEngine> engineConsumer, AtomicReference<Throwable> error,
            CountDownLatch latch)
    {
        assert Platform.isFxApplicationThread();
        final WebEngine eng = new WebEngine();
        eng.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
        {
            switch (newValue)
            {
                case SUCCEEDED:
                    try
                    {
                        engineConsumer.accept(eng);
                    }
                    catch (RuntimeException | Error e)
                    {
                        error.set(e);
                    }
                    latch.countDown();
                    break;
                case CANCELLED:
                    latch.countDown();
                    break;
                case FAILED:
                    error.set(eng.getLoadWorker().getException());
                    latch.countDown();
                    break;
                case SCHEDULED:
                default:
                    break;
            }
        });
        // eng.getLoadWorker().exceptionProperty().addListener((v, o, n) ->
        // System.err.println("exc: " + n));
        // eng.getLoadWorker().messageProperty().addListener((v, o, n) ->
        // System.err.println("msg: " + n));
        // eng.getLoadWorker().progressProperty().addListener((v, o, n) ->
        // System.err.println("prog: " + n));
        // eng.onErrorProperty().addListener((v, o, n) ->
        // System.err.println("error: " + n));
        // eng.setConfirmHandler((param) ->
        // {
        // System.err.println("Confirm: " + param);
        // return Boolean.TRUE;
        // });
        // eng.setOnAlert(event -> System.err.println(event));
        return eng;
    }

    /** Disallow instantiation. */
    private FXUtilities()
    {
    }
}
