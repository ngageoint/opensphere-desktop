package io.opensphere.mantle.icon.chooser.view;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.controller.IconRemover;
import io.opensphere.mantle.icon.chooser.model.CustomizationModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
import io.opensphere.mantle.icon.chooser.view.transform.TransformPanel;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/** A panel in which icon details are displayed. */
public class IconDetail extends AnchorPane
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(IconDetail.class);

    /** The canvas on which the icon preview is drawn. */
    private final Canvas myCanvas;

    /** The model backing customization operations. */
    private final CustomizationModel myCustomizationModel;

    /** The node to use when the icon is marked as a favorite. */
    private final Node myFavoriteIcon;

    /** The button to toggle to set favorite state. */
    private final Label myFavoriteToggleButton;

    /** Removes the selected icon from the icon manager. */
    private final IconRemover myIconRemover;

    /** The icon chooser model backing the panel. */
    private final IconModel myModel;

    /** The label in which the name of the icon is displayed. */
    private final TextField myNameField;

    /**
     * The procedure called when part of the selected icon record's model
     * changes and the parent item needs to be refreshed.
     */
    private final Procedure myRefreshProcedure;

    /** The remove icon button. */
    private final Button myRemoveButton;

    /** The label in which the source collection of the icon is displayed. */
    private final ComboBox<String> mySourceField;

    /** The menu button for creating, displaying, and removing tags. */
    private final MenuButton myTagMenuButton;

    /** The panel on which transformations occur. */
    private final TransformPanel myTransformPanel;

    /** The node to use when the icon is not marked as a favorite. */
    private final Node myUnFavoriteIcon;

    /**
     * Creates a new detail panel bound to the supplied model.
     *
     * @param model the model to which to bind the panel.
     * @param refreshProcedure The procedure called when part of the selected
     *            icon record's model changes and the parent item needs to be
     *            refreshed.
     */
    public IconDetail(final IconModel model, final Procedure refreshProcedure)
    {
        myModel = model;
        myRefreshProcedure = refreshProcedure;
        myCustomizationModel = model.getCustomizationModel();
        myIconRemover = new IconRemover(model);
        setMaxWidth(250);
        setMinWidth(250);

        myCanvas = new Canvas(246, 250);
        myCanvas.getGraphicsContext2D().drawImage(null, USE_COMPUTED_SIZE, BASELINE_OFFSET_SAME_AS_HEIGHT);

        myNameField = new TextField();
        myNameField.textProperty().bindBidirectional(myCustomizationModel.nameProperty());

        mySourceField = new ComboBox<>(myModel.getModel().getEditableCollectionNames());
        mySourceField.valueProperty().bindBidirectional(myCustomizationModel.sourceProperty());
        myCustomizationModel.sourceProperty().addListener((obs, oldV, newV) ->
        {
            if (!newV.equals(oldV))
            {
                IconRecord myRecord = myModel.selectedRecordProperty().get();
                myRecord.collectionNameProperty().set(newV);
            }
        });

        myTagMenuButton = createTagMenuButton();

        final VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER);

        final StackPane stackPane = new StackPane();

        myUnFavoriteIcon = FxIcons.createClearIcon(AwesomeIconRegular.STAR, Color.GREY, 16);
        myFavoriteIcon = FxIcons.createClearIcon(AwesomeIconSolid.STAR, Color.GOLD, 16);

        myRemoveButton = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        myRemoveButton.setOnAction(e -> myIconRemover.deleteIcons());
        myRemoveButton.setTooltip(new Tooltip("Remove selected icons."));
        myRemoveButton.setStyle("-fx-background-color: transparent");
        myRemoveButton.setPadding(new Insets(5, 5, 5, 3));

        myFavoriteToggleButton = new Label();
        myFavoriteToggleButton.setOnMouseClicked(e ->
        {
            if (myModel.selectedRecordProperty().get() != null && myModel.selectedRecordProperty().get().favoriteProperty().get())
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(false);
                myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
                setFavoriteTooltip(true);
            }
            else
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(true);
                myFavoriteToggleButton.setGraphic(myFavoriteIcon);
                setFavoriteTooltip(false);
            }
            myRefreshProcedure.invoke();
        });

        myFavoriteToggleButton.setPadding(new Insets(5, 3, 5, 5));
        final HBox buttonBox = new HBox(myFavoriteToggleButton, myRemoveButton);
        buttonBox.setAlignment(Pos.TOP_RIGHT);

        setDeleteButtonState();
        myModel.selectedRecordProperty().addListener((e) ->
        {
            setDeleteButtonState();
        });

        final HBox canvasBox = new HBox(myCanvas);
        final Border border = new Border(new BorderStroke(new Color(0.247058824, 0.247058824, 0.305882353, 1),
                BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT, new Insets(0, 0, 10, 2)));
        canvasBox.setBorder(border);
        stackPane.getChildren().addAll(canvasBox, buttonBox);

        getChildren().add(stackPane);
        setTopAnchor(stackPane, 0.0);
        setLeftAnchor(stackPane, 0.0);
        setRightAnchor(stackPane, 0.0);

        final GridPane grid = new GridPane();
        final Label nameLabel = new Label("Name:");
        nameLabel.setMinWidth(USE_PREF_SIZE);
        grid.add(nameLabel, 0, 0);
        grid.add(myNameField, 1, 0);
        final Label sourceLabel = new Label("Source:");
        sourceLabel.setMinWidth(USE_PREF_SIZE);
        grid.add(sourceLabel, 0, 1);
        grid.add(mySourceField, 1, 1);
        final Label tagsLabel = new Label("Tags:");
        tagsLabel.setMinWidth(USE_PREF_SIZE);
        grid.add(tagsLabel, 0, 2);
        grid.add(myTagMenuButton, 1, 2);

        box.getChildren().add(grid);

        myTransformPanel = new TransformPanel(this::saveCanvas);

        final TransformModel transformModel = myTransformPanel.getModel();
        transformModel.horizontalMoveProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.verticalMoveProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.rotationProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.horizontalScaleProperty()
                .addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.verticalScaleProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));

        final HBox spacer = new HBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        box.getChildren().add(spacer);

        box.getChildren().add(myTransformPanel);

        getChildren().add(box);

        setTopAnchor(box, 275.0);
        setLeftAnchor(box, 2.0);
        setRightAnchor(box, 0.0);
        setBottomAnchor(box, 0.0);

        redrawPreview(model.selectedRecordProperty().get());
    }

    /**
     * Creates the menu button that handles icon tags.
     *
     * @return the new menu button
     */
    private MenuButton createTagMenuButton()
    {
        MenuButton menuButton = new MenuButton();

        TextField textField = new TextField();
        textField.setPromptText("Enter new tags");
        textField.setOnMouseClicked(e -> menuButton.hide());
        textField.setOnAction(e ->
        {
            String text = textField.getText();
            ObservableList<String> tagList = myModel.selectedRecordProperty().get().getTags();

            if (StringUtils.isNotBlank(text) && !tagList.contains(text))
            {
                createTagMenuItem(text, tagList);
                tagList.add(text);
            }
            textField.setText("");
        });

        myModel.selectedRecordProperty().addListener((observable, oldValue, newValue) ->
        {
            if (menuButton.isDisabled())
            {
                menuButton.setDisable(false);
            }
            if (oldValue == null || !oldValue.equals(newValue))
            {
                menuButton.getItems().clear();
                newValue.getTags().forEach(tag -> createTagMenuItem(tag, newValue.getTags()));
            }
        });

        menuButton.setGraphic(textField);
        menuButton.setDisable(true);

        return menuButton;
    }

    /**
     * Creates a custom menu item with the tag name, and attaches it to the
     * tags menu button.
     *
     * @param tagName the name of the tag
     * @param tagList the list of tags the new tag is a part of
     */
    private void createTagMenuItem(String tagName, ObservableList<String> tagList)
    {
        CustomMenuItem customMenuItem = new CustomMenuItem();
        customMenuItem.setHideOnClick(false);

        Button deleteButton = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        deleteButton.setTooltip(new Tooltip("Delete this tag"));
        deleteButton.setOnAction(e ->
        {
            myTagMenuButton.getItems().remove(customMenuItem);
            tagList.remove(tagName);
        });

        HBox menuItemBox = new HBox(8);
        Label tagLabel = new Label(tagName);
        menuItemBox.getChildren().addAll(deleteButton, tagLabel);
        menuItemBox.setAlignment(Pos.CENTER_LEFT);
        customMenuItem.setContent(menuItemBox);

        myTagMenuButton.getItems().add(customMenuItem);
    }

    /**
     * Enables or disables the remove button depending on if the icon selected
     * is a user imported icon.
     */
    private void setDeleteButtonState()
    {
        if (myModel.selectedRecordProperty().get() == null
                || myModel.selectedRecordProperty().get().imageURLProperty().get() == null
                || myModel.selectedRecordProperty().get().imageURLProperty().get().toString().startsWith("jar:file:")
                || myModel.selectedRecordProperty().get().imageURLProperty().get().toString().contains("/target/classes/images/"))
        {
            // This is not a user imported icon, it is a system provided icon do
            // not delete.
            myRemoveButton.setDisable(true);
        }
        else
        {
            myRemoveButton.setDisable(false);
        }
    }
    /**
     * Saves the current content of the canvas to a new icon record.
     */
    private void saveCanvas()
    {
        final SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        final WritableImage image = myCanvas.snapshot(parameters, null);

        final BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        final BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
                Transparency.TRANSLUCENT);

        final Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        try
        {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufImageRGB, "png", outputStream);

            final URL imageURL = myModel.getIconRegistry().getIconCache().cacheIcon(outputStream.toByteArray(),
                    myNameField.textProperty().get(), true);
            final IconProvider provider = new DefaultIconProvider(imageURL, mySourceField.getSelectionModel().getSelectedItem(), "User");
            myModel.getIconRegistry().addIcon(provider, this);
        }
        catch (final IOException e)
        {
            LOG.error("Failed to write image.", e);
        }
    }

    /**
     * Sets the tooltip text of the favorite button to either "Add icon to
     * favorites" or "Remove icon from favorites"
     *
     * @param isFavorite true for add, false for remove
     */
    private void setFavoriteTooltip(boolean isFavorite)
    {
        if (isFavorite)
        {
            myFavoriteToggleButton.setTooltip(new Tooltip("Add icon to favorites"));
        }
        else
        {
            myFavoriteToggleButton.setTooltip(new Tooltip("Remove icon from favorites"));
        }
    }

    /**
     * Draws the preview of the supplied icon on the supplied canvas, applying
     * any transforms specified by the user.
     *
     * @param icon the icon from which to extract the image for the preview.
     */
    public void redrawPreview(final IconRecord icon)
    {
        if (icon != null && icon.favoriteProperty().get())
        {
            myFavoriteToggleButton.setGraphic(myFavoriteIcon);
            setFavoriteTooltip(false);
        }
        else
        {
            myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
            setFavoriteTooltip(true);
        }

        final TransformModel model = myTransformPanel.getModel();
        if (icon == null)
        {
            myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        }
        else
        {
            final Image image = icon.imageProperty().get();
            final double xOrigin = myCanvas.getWidth() / 2;
            final double yOrigin = myCanvas.getHeight() / 2;

            myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
            final double drawX = 0 - image.getWidth() * model.horizontalScaleProperty().get() / 2;
            final double drawY = 0 - image.getHeight() * model.verticalScaleProperty().get() / 2;

            final GraphicsContext graphicsContext2D = myCanvas.getGraphicsContext2D();

            final double horizontalTranslate = xOrigin + drawX + model.horizontalMoveProperty().get();
            final double mxx = model.horizontalScaleProperty().get();
            final double mxy = 0;
            final double tx = horizontalTranslate;

            final double verticalTranslate = yOrigin + drawY + model.verticalMoveProperty().get();
            final double myx = 0;
            final double myy = model.verticalScaleProperty().get();
            final double ty = verticalTranslate;

            final Affine affine = new Affine(mxx, mxy, tx, myx, myy, ty);
            affine.appendRotation(model.rotationProperty().get(), image.getWidth() / 2, image.getHeight() / 2);

            if (LOG.isTraceEnabled())
            {
                LOG.trace("Affine: " + affine.toString());
            }

            try
            {
                graphicsContext2D.setTransform(affine);
                graphicsContext2D.drawImage(image, 0, 0);
                final Affine inverseAffine = affine.createInverse();
                graphicsContext2D.transform(inverseAffine);
            }
            catch (final NonInvertibleTransformException e)
            {
                LOG.error("Unable to invert scale transform", e);
            }
        }
    }
}
