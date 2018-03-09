package io.opensphere.analysis.histogram.view;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.opensphere.analysis.base.model.ToolModels;
import io.opensphere.analysis.base.view.SettingsPane;
import io.opensphere.analysis.histogram.controller.HistogramController;
import io.opensphere.analysis.toolbox.AnalysisToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.Message;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.util.ClipBoardTool;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Histogram Swing panel. */
public class HistogramPanel extends JFXPanel implements Closeable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The model. */
    private final ToolModels myModel;

    /** The histogram controller. */
    private final HistogramController myController;

    /** The border pane. */
    private BorderPane myPane;

    /** The settings pane. */
    private SettingsPane mySettingsPane;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public HistogramPanel(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        AnalysisToolbox analysisToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(AnalysisToolbox.class);
        myController = new HistogramController(toolbox, analysisToolbox.getSettingsModel());
        myModel = myController.getModel();
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                handleMouseClick(e);
            }
        });
        Platform.runLater(this::initFx);
    }

    @Override
    public void close()
    {
        myController.close();
        mySettingsPane.closeDialog();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        myController.open();
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();
        myController.close();
    }

    /**
     * Handles a mouse click.
     *
     * @param e the mouse event
     */
    void handleMouseClick(MouseEvent e)
    {
        boolean isRightClick = (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0;
        if (isRightClick)
        {
            JPopupMenu menu = new JPopupMenu();
            menu.add(SwingUtilities.newMenuItem("Copy chart to clipboard", menuEvent -> copyToClipboard()));
            menu.add(SwingUtilities.newMenuItem("Save as...", menuEvent -> saveAs()));
            menu.show(HistogramPanel.this, e.getX(), e.getY());
        }
    }

    /** Initializes the JavaFX stuff. */
    private void initFx()
    {
        myPane = new BorderPane();
        mySettingsPane = new SettingsPane(this, myModel.getSettingsModel());
        myPane.setTop(mySettingsPane);
        myPane.setCenter(new SwitchableBarChart(myModel));
        setScene(FXUtilities.addDesktopStyle(new Scene(myPane)));
        myModel.userMessageProperty().addListener((obs, o, n) -> notifyUser(n));
    }

    /**
     * Displays or clears a message.
     *
     * @param m the message
     */
    private void notifyUser(Message m)
    {
        if (m != null)
        {
            myPane.setBottom(createMessageNode(m));
        }
        else
        {
            myPane.setBottom(null);
        }
    }

    /**
     * Creates a display message for the user.
     *
     * @param m the message
     * @return the message node
     */
    private Node createMessageNode(Message m)
    {
        HBox box = FXUtilities.newHBox(createMessage(m), createMessageIcon(m));
        box.setAlignment(Pos.BASELINE_CENTER);
        box.setPadding(new Insets(0, 15, 10, 15));
        return box;
    }

    /**
     * Creates the display text for the message.
     *
     * @param m the message
     * @return the text to be displayed
     */
    private Label createMessage(Message m)
    {
        Label message = new Label(m.getTitle());
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFont(Font.font(null, FontWeight.BOLD, 14));
        message.setTextFill(getTypeColor(m));
        return message;
    }

    /**
     * Creates the icon with a tooltip for the message.
     *
     * @param m the message
     * @return the icon with further information
     */
    private Label createMessageIcon(Message m)
    {
        Label icon = new Label();
        icon.setGraphic(createInfoImage());
        Tooltip messageInfo = new Tooltip(m.getMessage());
        messageInfo.setFont(new Font(null, 12));
        icon.setTooltip(messageInfo);
        return icon;
    }

    /**
     * Creates the image view for the message info icon.
     *
     * @return the image
     */
    private ImageView createInfoImage()
    {
        ImageIcon imageIcon = IconUtil.getIcon(IconType.QUESTION);
        java.awt.Color awtColor = FXUtilities.toAwtColor(Colors.INFO);
        BufferedImage colorizedImage = IconUtil.getColorizedImage(imageIcon, IconStyle.NORMAL, awtColor);
        Image image = SwingFXUtils.toFXImage(colorizedImage, null);
        return new ImageView(image);
    }

    /**
     * Gets the correct color for a message depending on message type.
     *
     * @param m the message
     * @return the color for the message
     */
    private Color getTypeColor(Message m)
    {
        Color color;
        switch (m.getSeverity())
        {
            case ERROR:
                color = Colors.ERROR;
                break;
            case WARNING:
                color = Colors.WARNING;
                break;
            default:
                color = Colors.INFO;
        }
        return color;
    }

    /**
     * Copies the panel to the clipboard.
     */
    private void copyToClipboard()
    {
        BufferedImage image = createBufferedImage();
        ClipBoardTool.writeToClipboard(image);
    }

    /**
     * Performs the save as action.
     */
    private void saveAs()
    {
        final String extension = "png";
        MnemonicFileChooser chooser = new MnemonicFileChooser(myToolbox.getPreferencesRegistry(), "AnalysisTools");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Images", extension));
        int result = chooser.showSaveDialog(getParent());
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File file = FileUtilities.ensureSuffix(chooser.getSelectedFile(), extension);
            BufferedImage image = createBufferedImage();
            try
            {
                ImageIO.write(image, extension, file);
            }
            catch (IOException e)
            {
                notifyUser(new Message(e.getMessage(), Type.ERROR, "Error"));
            }
        }
    }

    /**
     * Creates a buffered image of the panel.
     *
     * @return the buffered image
     */
    private BufferedImage createBufferedImage()
    {
        Component component = this;
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        component.paint(image.getGraphics());
        return image;
    }
}
