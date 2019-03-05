package io.opensphere.mantle.icon.chooser.view;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.function.Procedure;
import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.javafx.input.tags.Tag;
import io.opensphere.core.util.javafx.input.tags.TagField;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.CustomizationModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
import io.opensphere.mantle.icon.chooser.view.transform.TransformPanel;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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

    /** The label in which the name of the icon is displayed. */
    private final TextField myNameField;

    /** The label in which the source collection of the icon is displayed. */
    private final ComboBox<String> mySourceField;

    /** The label in which the tags applied to the icon are displayed. */
    private final TagField myTagsField;

    private final Button myTagsButton;

    /** The panel on which transformations occur. */
    private final TransformPanel myTransformPanel;

    /** The canvas on which the icon preview is drawn. */
    private final Canvas myCanvas;

    /** The icon chooser model backing the panel. */
    private final IconModel myModel;

    /** the model backing customization operations. */
    private final CustomizationModel myCustomizationModel;

    /**
     * The procedure called when part of the selected icon record's model
     * changes and the parent item needs to be refreshed.
     */
    private final Procedure myRefreshProcedure;

    /** The button to toggle to set favorite state. */
    private final Label myFavoriteToggleButton;

    /** The node to use when the icon is not marked as a favorite. */
    private final Node myUnFavoriteIcon;

    /** The node to use when the icon is marked as a favorite. */
    private final Node myFavoriteIcon;

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

        myTagsField = new TagField();
        myTagsField.tagColorProperty().set(FXUtilities.fromAwtColor(IconUtil.DEFAULT_ICON_FOREGROUND));
        myCustomizationModel.getTags().addListener((ListChangeListener<String>)c ->
        {
            while (c.next())
            {
                if (c.wasAdded())
                {
                    c.getAddedSubList().stream().map(v -> new Tag(v)).forEach(myTagsField::addTag);
                }
                if (c.wasRemoved())
                {
                    final List<? extends String> removedTags = c.getRemoved();
                    final Set<Tag> tagsToRemove = New.set();
                    for (final String removedTagName : removedTags)
                    {
                        myTagsField.getTags().stream().filter(t -> StringUtils.equals(t.textProperty().get(), removedTagName))
                                .forEach(tagsToRemove::add);
                    }
                    myTagsField.getTags().removeAll(tagsToRemove);
                }
            }
        });

        myTagsButton = new Button("Open Tags");
        myTagsButton.setOnAction(e ->
        {
            launchTags();
        });
        myModel.selectedRecordProperty().addListener((obs, o, n) -> 
        {
            if (n != null)
            {
                myTagsButton.setDisable(false);
            }
            else
            {
                myTagsButton.setDisable(true);
            }
        });
        myTagsButton.setDisable(true);

        final VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER);

        final StackPane stackPane = new StackPane();

        myUnFavoriteIcon = FxIcons.createClearIcon(AwesomeIconRegular.STAR, Color.GREY, 16);
        myFavoriteIcon = FxIcons.createClearIcon(AwesomeIconSolid.STAR, Color.GOLD, 16);

        myFavoriteToggleButton = new Label();
        myFavoriteToggleButton.setOnMouseClicked(e ->
        {
            if (myModel.selectedRecordProperty().get() != null && myModel.selectedRecordProperty().get().favoriteProperty().get())
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(false);
                myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
            }
            else
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(true);
                myFavoriteToggleButton.setGraphic(myFavoriteIcon);
            }
            myRefreshProcedure.invoke();
        });

        myFavoriteToggleButton.setPadding(new Insets(5));
        final HBox buttonBox = new HBox(myFavoriteToggleButton);
        buttonBox.setAlignment(Pos.TOP_RIGHT);

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
        grid.add(myTagsButton, 1, 2);

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

    private void launchTags()
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            Window owner = myModel.getToolbox().getUIRegistry().getMainFrameProvider().get();
            ListView<String> listView = new ListView<>();
            listView.setCellFactory((param) ->
            {
                IconTagCell cell = new IconTagCell();

                return cell;
            });
            listView.setItems(myModel.selectedRecordProperty().get().getTags());

            JFXDialog dialog = new JFXDialog(owner, "tags", false);
//            dialog.setFxNode(myTagsField);

            GridPane gridPane = new GridPane();
            TextField textField = new TextField();
            textField.setPromptText("enter new tag");
            textField.setOnAction(a ->
            {
                String tagText = textField.getText();
                ObservableList<String> tags = myModel.selectedRecordProperty().get().getTags();
                if (tags.contains(tagText))
                {
                    Notify.info("Tag \"" + tagText + "\" is already in the list.", Method.POPUP);
                    textField.clear();
                }
                else if (StringUtils.isNotEmpty(tagText))
                {
                    tags.add(textField.getText());
                    textField.clear();
                }
                System.out.println(myModel.selectedRecordProperty().get().getTags());
            });
            gridPane.add(textField, 0, 0);
            gridPane.add(listView, 0, 1);
            gridPane.setVgap(5);
            gridPane.setHgap(5);
            GridPane.setHgrow(textField, Priority.ALWAYS);
            GridPane.setHgrow(listView, Priority.ALWAYS);
            GridPane.setVgrow(listView, Priority.ALWAYS);

            dialog.setFxNode(gridPane);
            dialog.setSize(400, 400);
            dialog.setLocationRelativeTo(owner);
            dialog.setModalityType(ModalityType.APPLICATION_MODAL);
//            dialog.setRejectListener(() -> System.out.println("bye"));
//            dialog.setAcceptListener(() ->
//            {
//                myModel.selectedRecordProperty().get().getTags().addAll(myTagsField.getTagValues());
//                System.out.println(myModel.selectedRecordProperty().get().getTags());
//            });
//            System.out.println(myModel.selectedRecordProperty().get().nameProperty());
            
//            myModel.selectedRecordProperty().get().getTags();
//            System.out.println(this.getParent().getParent());
            dialog.setVisible(true);
        });
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
        }
        else
        {
            myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
        }

        final TransformModel model = myTransformPanel.getModel();
        if (icon == null)
        {
            myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        }
        else
        {
            myTagsField.getTags().clear();
            icon.getTags().stream().map(v -> new Tag(v)).forEach(myTagsField::addTag);
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
