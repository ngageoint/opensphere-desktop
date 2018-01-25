package io.opensphere.core.util.filesystem;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.predicate.EndsWithPredicate;

/**
 * Class that extends JFileChooser and automatically loads from and saves to
 * preferences the last used directory location. This class is constructed with
 * the preferences registry and a string of what the preferences key should be.
 * The preferences key can be shared, unique, or null to give options as to what
 * the default directory location should be. The same key can be used between
 * different choosers so if a file is saved to a location in one of them, the
 * others that share that key will use that location as their default. All file
 * choosers that use null as their preference key share that preference.
 */
public class MnemonicFileChooser extends JFileChooser
{
    /** The Constant string for the last saved directory preference. */
    public static final String LAST_SAVED_DIRECTORY_PREFERENCE = "LastSavedDirectoryPath";

    /** The cereal version identification number. */
    private static final long serialVersionUID = 1L;

    /** The preferences key. */
    private final String myNameKey;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Gets the last saved directory preference.
     *
     * @param prefsRegistry the prefs registry
     * @param className the class name
     * @return the last saved directory preference
     */
    public static File getLastSavedDirectoryPreference(PreferencesRegistry prefsRegistry, String className)
    {
        return new File(prefsRegistry.getPreferences(MnemonicFileChooser.class).getString(getNameKey(className),
                System.getProperty("user.home")));
    }

    /**
     * Gets the name key.
     *
     * @param className the class name
     * @return the name key
     */
    protected static final String getNameKey(String className)
    {
        StringBuilder key = new StringBuilder(LAST_SAVED_DIRECTORY_PREFERENCE);
        if (!StringUtils.isBlank(className))
        {
            key.append('_');
            key.append(className);
        }
        return key.toString();
    }

    /**
     * Constructor. The MnemonicFileChooser is a JFileChooser that automatically
     * loads and saves preferences for the last selected directory. The second
     * parameter will be used in the preference key. It is possible to use the
     * same key by different file choosers or use a unique value for an
     * individual chooser. And the second parameter can be null as well.
     *
     * @param prefsRegistry The preferences registry.
     * @param className The string name used for preferences key (can be null).
     */
    public MnemonicFileChooser(PreferencesRegistry prefsRegistry, String className)
    {
        myPreferencesRegistry = prefsRegistry;
        myNameKey = getNameKey(className);

        // Add listener to save current directory when the "save" "load" "open"
        // etc. button is pressed from this file chooser.
        if (myPreferencesRegistry != null)
        {
            setCurrentDirectory(getLastSavedDirectoryPreference(prefsRegistry, className));
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getActionCommand().equals("ApproveSelection"))
                    {
                        File aFile = getSelectedFile();
                        if (!aFile.isDirectory())
                        {
                            aFile = getCurrentDirectory();
                        }
                        myPreferencesRegistry.getPreferences(MnemonicFileChooser.class).putString(myNameKey,
                                aFile.getAbsolutePath(), this);
                    }
                }
            });
        }
    }

    /**
     * Show the save dialog. If the user chooses a file, make sure the file ends
     * with one of the acceptible suffixes. If it doesn't append the first
     * suffix to the file. Then check to see if the file exists. If it does, ask
     * the user to approve overwriting the file.
     *
     * @param parent The parent component.
     * @param acceptibleSuffixes The acceptible suffixes for the chosen file.
     * @return the return state of the file chooser on popdown:
     *         <ul>
     *         <li>JFileChooser.CANCEL_OPTION
     *         <li>JFileChooser.APPROVE_OPTION
     *         <li>JFileChooser.ERROR_OPTION if an error occurs or the dialog is
     *         dismissed
     *         </ul>
     */
    public int showSaveDialog(Component parent, Collection<? extends String> acceptibleSuffixes)
    {
        if (!CollectionUtilities.hasContent(acceptibleSuffixes))
        {
            throw new IllegalArgumentException("No acceptible suffixes provided.");
        }

        boolean promptAgain;
        int result;
        do
        {
            result = showSaveDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION)
            {
                String path = getSelectedFile().getAbsolutePath().toLowerCase();
                if (!new EndsWithPredicate(acceptibleSuffixes).test(path))
                {
                    setSelectedFile(new File(getSelectedFile().getAbsolutePath() + acceptibleSuffixes.iterator().next()));
                }

                if (getSelectedFile().exists())
                {
                    int choice = JOptionPane.showConfirmDialog(parent, "Overwrite file?", "File exists",
                            JOptionPane.YES_NO_OPTION);
                    promptAgain = choice == JOptionPane.NO_OPTION;
                }
                else
                {
                    promptAgain = false;
                }
            }
            else
            {
                promptAgain = false;
            }
        }
        while (promptAgain);

        return result;
    }
}
