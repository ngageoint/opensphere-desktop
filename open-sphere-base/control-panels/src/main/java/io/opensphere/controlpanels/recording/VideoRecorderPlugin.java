package io.opensphere.controlpanels.recording;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import io.opensphere.controlpanels.recording.VideoRecorderController.RecordingCompleteListener;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.IconButton;

/**
 * The plugin for managing the video recording capability.
 */
public class VideoRecorderPlugin extends PluginAdapter
{
    /** Tool tip to use prior to any user interaction. */
    private static final String ORIGINAL_TOOLTIP_TEXT = "Record a video";

    /**
     * Tool tip to use after the file is selected but before recording has
     * started.
     */
    private static final String START_RECORDING_TOOLTIP_TEXT = "Start recording";

    /** Tool tip to use when recording. */
    private static final String STOP_RECORDING_TOOPTIP_TEXT = "Stop recording";

    /** The default icon. */
    private ImageIcon myDefaultIcon;

    /** The provider of the component to use as the dialog's parent. */
    private Supplier<? extends Component> myDialogParentProvider;

    /** The preferences registry. */
    private PreferencesRegistry myPreferencesRegistry;

    /** The flash icon. */
    private ImageIcon mySelectedIcon;

    /** Timer used to flash the button while recording. */
    private Timer myTimer;

    /** The Activation button. */
    private JButton myVideoActivationButton;

    /** The transformer for displaying videos on the canvas. */
    private VideoPlayerTransformer myVideoPlayerTransformer;

    /** The Video recorder controller. */
    private VideoRecorderController myVideoRecorderController;

    @Override
    public void close()
    {
    }

    /**
     * Creates the toolbar activation button.
     *
     * @return the j button
     */
    public JButton createToolbarActivationButton()
    {
        IconButton activationButton = new IconButton("Record");

        IconUtil.setIcons(activationButton, "/images/video.png", IconUtil.DEFAULT_ICON_FOREGROUND, new Color(255, 154, 154));

        activationButton.setToolTipText(ORIGINAL_TOOLTIP_TEXT);

        myDefaultIcon = (ImageIcon)activationButton.getIcon();
        mySelectedIcon = (ImageIcon)activationButton.getSelectedIcon();

        return activationButton;
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singletonList(myVideoPlayerTransformer);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        myDialogParentProvider = toolbox.getUIRegistry().getMainFrameProvider();
        myPreferencesRegistry = toolbox.getPreferencesRegistry();
        myVideoPlayerTransformer = new VideoPlayerTransformer(toolbox);
        myVideoRecorderController = new VideoRecorderController(toolbox);
        myVideoRecorderController.addListener(new RecordingCompleteListener()
        {
            @Override
            public void recordingComplete()
            {
                myVideoActivationButton.setSelected(false);
                myVideoActivationButton.setToolTipText(ORIGINAL_TOOLTIP_TEXT);
            }
        });
        myVideoActivationButton = createToolbarActivationButton();
        myVideoActivationButton.addMouseListener(new MouseAdapter()
        {
            /** The selected output file. */
            private File myOutputFile;

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == 1)
                {
                    Quantify.collectMetric("mist3d.recording.record-a-video");
                    if (myVideoActivationButton.isSelected())
                    {
                        myVideoRecorderController.stopRecording();
                    }
                    else if (myOutputFile == null)
                    {
                        myOutputFile = selectOutputFile();

                        if (myOutputFile != null)
                        {
                            myVideoActivationButton.setToolTipText(START_RECORDING_TOOLTIP_TEXT);
                            myTimer = new Timer(500, new ActionListener()
                            {
                                /** Flag indicating which color to switch to. */
                                private boolean myToggle;

                                @Override
                                public void actionPerformed(ActionEvent ev)
                                {
                                    myVideoActivationButton.setIcon(myToggle ? myDefaultIcon : mySelectedIcon);
                                    myToggle = !myToggle;
                                }
                            });
                            myTimer.start();
                        }
                    }
                    else
                    {
                        File outputFile = myOutputFile;

                        // ensure this is set to null:
                        myOutputFile = null;
                        myVideoActivationButton.setIcon(myDefaultIcon);
                        myTimer.stop();
                        myTimer = null;
                        if (myVideoRecorderController.startRecording(outputFile))
                        {
                            myVideoActivationButton.setSelected(true);
                            myVideoActivationButton.setToolTipText(STOP_RECORDING_TOOPTIP_TEXT);
                        }
                        else
                        {
                            myVideoActivationButton.setToolTipText(ORIGINAL_TOOLTIP_TEXT);
                        }
                    }
                }
            }
        });

        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "VideoRecorder",
                myVideoActivationButton, 456, SeparatorLocation.RIGHT, new Insets(0, 2, 0, 2));

        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon("/images/video.png"),
                "Video Recorder",
                "Opens the 'Record a video' dialog. Enter a file name and press the 'Ok' button. "
                        + "The 'Video Recorder' button begins to flash. Press it to start your recording. "
                        + "Press it a second time to end your recording.");
    }

    /**
     * Determine the file from which to load data.
     *
     * @return The file containing LiDAR data.
     */
    private File selectOutputFile()
    {
        MnemonicFileChooser chooser = new MnemonicFileChooser(myPreferencesRegistry, "VideoRecorderController");
        chooser.setDialogTitle("Record a video");
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File aFile)
            {
                return aFile != null && (aFile.isDirectory() || StringUtilities.endsWith(aFile.getAbsolutePath().toLowerCase(),
                        myVideoRecorderController.getAcceptableFileExtensions()));
            }

            @Override
            public String getDescription()
            {
                StringBuilder sb = new StringBuilder(64).append("Video Formats [*");
                for (String extension : myVideoRecorderController.getAcceptableFileExtensions())
                {
                    sb.append(extension).append(",*");
                }
                sb.setLength(sb.length() - 2);
                return sb.append(']').toString();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        JTextArea accessoryText = new JTextArea();
        accessoryText.setEditable(false);
        accessoryText.setBackground(panel.getBackground());
        accessoryText.setBorder(BorderFactory.createEmptyBorder());
        accessoryText.setWrapStyleWord(true);
        accessoryText.setLineWrap(true);
        StringBuilder sb = new StringBuilder(256).append("Supported file types are:\n\n  ");
        StringUtilities.join(sb, ", ", myVideoRecorderController.getAcceptableFileExtensions());
        sb.append("\n\nPlease note that older versions of Windows Media Player"
                + " may have difficulty with formats other than wmv.\n"
                + "\nIf no valid extension is provided recorder will default to ");
        String defaultExtension = myVideoRecorderController.getAcceptableFileExtensions().get(0);
        sb.append(defaultExtension);
        accessoryText.setText(sb.toString());
        panel.add(accessoryText, BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(200, 250));
        panel.setPreferredSize(new Dimension(200, 250));
        chooser.setAccessory(panel);

        File file = null;
        do
        {
            if (chooser.showDialog(myDialogParentProvider.get(), "OK") == JFileChooser.APPROVE_OPTION)
            {
                file = chooser.getSelectedFile();
                if (file == null)
                {
                    break;
                }
                else
                {
                    if (!StringUtilities.endsWith(file.getAbsolutePath().toLowerCase(),
                            myVideoRecorderController.getAcceptableFileExtensions()))
                    {
                        file = new File(file.getAbsolutePath() + defaultExtension);
                    }

                    if (file.exists())
                    {
                        int result = JOptionPane.showConfirmDialog(myDialogParentProvider.get(),
                                "The file \"" + file.getAbsolutePath() + "\" already exists.\n\nDo you want to overwrite it?",
                                "File Overwrite Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result == JOptionPane.NO_OPTION)
                        {
                            file = null;
                        }
                    }
                }
            }
            else
            {
                break;
            }
        }
        while (file == null);
        return file;
    }
}
