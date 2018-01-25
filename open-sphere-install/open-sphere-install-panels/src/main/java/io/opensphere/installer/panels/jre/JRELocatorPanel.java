package io.opensphere.installer.panels.jre;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.jdkpath.JDKPathPanelAutomationHelper;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.panels.path.PathSelectionPanel;
import com.izforge.izpack.util.Platform;

/**
 * A panel used to locate the JRE/JDK during the installation process. Allows
 * the user to specify the minimum and maximum versions, as well as an
 * environment variable from which to locate the JRE.
 */
public class JRELocatorPanel extends IzPanel implements ActionListener, HyperlinkListener
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -3230419123578694401L;

    /**
     * The logger instance used to capture output during execution.
     */
    private static final Logger LOG = Logger.getLogger(JRELocatorPanel.class.getName());

    /**
     * The handler used to interact with the registry.
     */
    private final RegistryDefaultHandler myHandler;

    /**
     * Flag whether the choosen path must exist or not
     */
    protected boolean mustExist = false;

    /**
     * Files which should be exist
     */
    protected String[] existFiles = null;

    /**
     * The path selection sub panel
     */
    protected final PathSelectionPanel pathSelectionPanel;

    protected final String emptyTargetMsg;

    protected final String warnMsg;

    /**
     * Creates a new panel, accepting the supplied parameters for configuration.
     *
     * @param panel
     * @param parent
     * @param installData
     * @param resources
     * @param log
     * @param handler
     * @param replacer
     */
    public JRELocatorPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log,
            RegistryDefaultHandler handler, VariableSubstitutor replacer)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        emptyTargetMsg = getI18nStringForClass("empty_target", "TargetPanel");
        warnMsg = getI18nStringForClass("warn", "TargetPanel");

        add(IzPanelLayout.createParagraphGap());

        // Label for input
        // row 1 column 0.
        add(createLabel("intro", "JRELocatorPanel", "open", LEFT, true), NEXT_LINE);
        // Create path selection components and add they to this panel.
        pathSelectionPanel = new PathSelectionPanel(this, installData, log);
        add(pathSelectionPanel, NEXT_LINE);
        createLayoutBottom();
        getLayoutHelper().completeLayout();

        myHandler = handler;

        setMustExist(true);
        if (!installData.getPlatform().isA(Platform.Name.MAC_OSX))
        {
            setExistFiles(JRELocatorHelper.testFiles);
        }

        String msg = getString("JRELocatorPanel.jreDownload");
        if (msg != null && !msg.isEmpty())
        {
            add(IzPanelLayout.createParagraphGap());
            JEditorPane textArea = new JEditorPane("text/html; charset=utf-8", replacer.substitute(msg, null));
            textArea.setCaretPosition(0);
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            textArea.setBackground(getBackground());

            JScrollPane scroller = new JScrollPane(textArea);
            scroller.setAlignmentX(LEFT_ALIGNMENT);
            add(scroller, NEXT_LINE);
        }
        JRELocatorHelper.initialize(installData);
    }

    /**
     * Returns the selected path.
     *
     * @return the selected path
     */
    public String getPath()
    {
        String chosenPath = pathSelectionPanel.getPath();
        return PathInputBase.normalizePath(chosenPath);
    }

    /**
     * This method does nothing. It is called from ctor of PathInputPanel, to
     * give in a derived class the possibility to add more components under the
     * path input components.
     */
    public void createLayoutBottom()
    {
        // Derived classes implements additional elements.
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == pathSelectionPanel.getPathInputField())
        {
            parent.navigateNext();
        }

    }

    /**
     * Returns the must exist state.
     *
     * @return the must exist state
     */
    public boolean isMustExist()
    {
        return mustExist;
    }

    /**
     * Sets the must exist state. If it is true, the path must exist.
     *
     * @param mustExist must exist state
     */
    public void setMustExist(boolean mustExist)
    {
        this.mustExist = mustExist;
    }

    /**
     * Returns the array of strings which are described the files which must
     * exist.
     *
     * @return paths of files which must exist
     */
    public String[] getExistFiles()
    {
        return existFiles;
    }

    /**
     * Sets the paths of files which must exist under the chosen path.
     *
     * @param strings paths of files which must exist under the chosen path
     */
    public void setExistFiles(String[] strings)
    {
        existFiles = strings;
    }

    /**
     * Verifies that the specified file exists.
     *
     * @param file the file to check
     * @return {@code true} if the file exists, otherwise {@code false}
     */
    protected boolean checkExists(File file)
    {
        if (!file.exists())
        {
            emitError(getString("installer.error"), getString(getI18nStringForClass("required", "PathInputPanel")));
            return false;
        }
        return true;
    }

    /**
     * Determines if an empty path is allowed.
     *
     * @return {@code true} if an empty path is allowed, otherwise {@code false}
     */
    protected boolean checkEmptyPath()
    {
        if (isMustExist())
        {
            emitError(getString("installer.error"), getI18nStringForClass("required", "PathInputPanel"));
            return false;
        }
        return emitWarning(getString("installer.warning"), emptyTargetMsg);
    }

    /**
     * Verifies that installation information exists in the specified path.
     *
     * @param path the path
     * @return {@code true} if installation information exists, otherwise
     *         {@code false}
     */
    protected boolean checkInstallationInformation(File path)
    {
        File info = new File(path, InstallData.INSTALLATION_INFORMATION);
        if (!info.exists())
        {
            emitError(getString("installer.error"), getString("PathInputPanel.required.forModificationInstallation"));

            return false;
        }
        return true;
    }

    /**
     * Determines if required files exist relative to the specified path
     *
     * @return {@code true} if no files are required, or they exist
     */
    protected boolean checkRequiredFilesExist(String path)
    {
        if (existFiles == null || path == null || path.isEmpty())
        {
            return true;
        }
        for (String existFile : existFiles)
        {
            File file = new File(path, existFile).getAbsoluteFile();
            if (!file.exists())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the specified directory can be created.
     *
     * @param dir the directory
     * @return {@code true} if the directory may be created, otherwise
     *         {@code false}
     */
    protected boolean checkCreateDirectory(File dir)
    {
        boolean result = true;
        // if 'ShowCreateDirectoryMessage' configuration option set 'false' then
        // don't show
        // then don't show "directory will be created" dialog:
        String show = getMetadata().getConfigurationOptionValue(PathInputBase.SHOWCREATEDIRECTORYMESSAGE, installData.getRules());
        if (show == null || Boolean.getBoolean(show))
        {
            result = emitNotificationFeedback(getI18nStringForClass("createdir", "TargetPanel") + "\n" + dir);
        }
        return result;
    }

    /**
     * Determines if an existing directory can be written to.
     *
     * @param dir the directory
     * @return {@code true} if the directory can be written to, otherwise
     *         {@code false}
     */
    protected boolean checkOverwrite(File dir)
    {
        boolean result = true;
        // if 'ShowExistingDirectoryWarning' configuration option set 'false'
        // then don't show
        // "The directory already exists! Are you sure you want to install here
        // and possibly overwrite existing files?"
        // warning dialog:
        String show = getMetadata().getConfigurationOptionValue(PathInputBase.SHOWEXISTINGDIRECTORYWARNING,
                installData.getRules());
        if ((show == null || Boolean.getBoolean(show)) && dir.isDirectory() && dir.list().length > 0)
        {
            result = askWarningQuestion(getString("installer.warning"), warnMsg, AbstractUIHandler.CHOICES_YES_NO,
                    AbstractUIHandler.ANSWER_YES) == AbstractUIHandler.ANSWER_YES;
        }
        return result;
    }

    /**
     * Determines if an existing installation is being modified.
     *
     * @return {@code true} if an installation is being modified, otherwise
     *         {@code false}
     */
    protected boolean modifyInstallation()
    {
        return Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Same as calling {@link #pathIsValid(boolean) pathIsValid(false)}.
     */
    protected boolean pathIsValid()
    {
        return pathIsValid(false);
    }

    /**
     * Returns whether the chosen path is valid or not. If existFiles are not
     * null, the existence of it under the chosen path are detected. This method
     * can be also implemented in derived classes to handle special verification
     * of the path.
     *
     * @return true if existFiles are exist or not defined, else false
     */
    protected boolean pathIsValid(boolean notifyUserIfInvalid)
    {
        String pathToBeChecked = getPath();
        boolean isValid = checkRequiredFilesExist(pathToBeChecked);
        if (!isValid && notifyUserIfInvalid)
        {
            String errMsg = getString(getI18nStringForClass("notValid", "PathInputPanel"));
            LOG.log(Level.WARNING, String.format("%s: '%s'", errMsg, pathToBeChecked));
            emitError(getString("installer.error"), errMsg);
        }
        return isValid;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                String urls = e.getURL().toExternalForm();
                if (Desktop.isDesktopSupported())
                {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(urls));
                }
            }
        }
        catch (Exception err)
        {
            LOG.log(Level.WARNING, err.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.panels.path.PathInputPanel#isValidated()
     */
    @Override
    public boolean isValidated()
    {
        if (testValidPath())
        {
            String detectedJavaVersion;
            String strPath = pathSelectionPanel.getPath();

            detectedJavaVersion = JRELocatorHelper.getCurrentJavaVersion(strPath, installData.getPlatform());
            String errorMessage = JRELocatorHelper.validate(strPath, detectedJavaVersion, installData.getMessages());
            if (!errorMessage.isEmpty())
            {
                if (errorMessage.endsWith("?"))
                {
                    if (askQuestion(getString("installer.warning"), errorMessage, AbstractUIHandler.CHOICES_YES_NO,
                            AbstractUIHandler.ANSWER_NO) == AbstractUIHandler.ANSWER_YES)
                    {
                        installData.setVariable(JRELocatorHelper.JRE_PATH, pathSelectionPanel.getPath());
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    protected boolean testValidPath()
    {
        String path = getPath();
        String normalizedPath = PathInputBase.normalizePath(path);
        File file = new File(normalizedPath).getAbsoluteFile();

        if (normalizedPath.length() == 0 && !checkEmptyPath())
        {
            // Empty path disallowed
            return false;
        }

        pathSelectionPanel.setPath(normalizedPath);

        if (isMustExist())
        {
            if (!checkExists(file) || !pathIsValid(true) || modifyInstallation() && !checkInstallationInformation(file))
            {
                return false;
            }
        }
        else
        {
            if (!PathInputBase.isWritable(file))
            {
                emitError(getString("installer.error"), getI18nStringForClass("notwritable", "TargetPanel"));
                return false;
            }

            // We put a warning if the directory exists else we warn that it
            // will be created
            if (file.exists())
            {
                if (!checkOverwrite(file))
                {
                    return false;
                }
            }
            else if (!checkCreateDirectory(file))
            {
                return false;
            }
        }

        if (!installData.getPlatform().isValidDirectoryPath(file))
        {
            emitError(getString("installer.error"), getI18nStringForClass("syntax.error", "TargetPanel"));
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.panels.path.PathInputPanel#panelActivate()
     */
    @Override
    public void panelActivate()
    {
        super.panelActivate();
        if (modifyInstallation())
        {
            // installation directory has to exist if an installation is being
            // modified
            mustExist = true;
        }
        PathInputBase.setInstallData(installData);
        String defaultValue = JRELocatorHelper.getDefaultJavaPath(installData, myHandler);
        pathSelectionPanel.setPath(defaultValue);

        // Should we skip this panel?
        if (JRELocatorHelper.skipPanel(installData, defaultValue))
        {
            parent.skipPanel();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.gui.IzPanel#getSummaryBody()
     */
    @Override
    public String getSummaryBody()
    {
        return installData.getVariable(JRELocatorHelper.JRE_PATH);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.gui.IzPanel#createInstallationRecord(com.izforge.izpack.api.adaptator.IXMLElement)
     */
    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new JDKPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.gui.IzPanel#saveData()
     */
    @Override
    public void saveData()
    {
        installData.setVariable(JRELocatorHelper.JRE_PATH, pathSelectionPanel.getPath());
    }
}
