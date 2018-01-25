package io.opensphere.core.launch;

import java.awt.SplashScreen;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.SplashScreenManager;
import io.opensphere.core.appl.Kernel;
import io.opensphere.core.appl.LookAndFeelInit;
import io.opensphere.core.appl.SplashScreenManagerImpl;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.preferences.PreferencesRegistryImpl;
import io.opensphere.core.security.SecurityManagerImpl;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.SystemPropertyLoader;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Launcher for the OpenSphere Tool Suite. This determines the proper Java
 * arguments and forks a new Java process.
 */
@SuppressWarnings("PMD.GodClass")
public final class Launch
{
    /** Logger reference. */
    private static final Logger LOGGER;

    /** Flag indicating if the app should be restarted. */
    private static volatile boolean ourRestart;

    /** How many milliseconds between re-launches. */
    private static final long RELAUNCH_EXCLUSION_WINDOW_MILLIS = 10000L;

    /**
     * Flag indicating if the application is configured to use a master
     * password.
     */
    private final boolean myMasterPasswordInUse;

    /** The preferences. */
    private final Preferences myPreferences;

    /** Flag indicating if a splash screen is active. */
    private volatile boolean mySplashScreenActive = SplashScreen.getSplashScreen() != null;

    /** The splash screen manager. */
    private final SplashScreenManager mySplashScreenManager = new SplashScreenManagerImpl();

    /** The directory in which the application will launch. */
    private File myCurrentWorkingDirectory;

    static
    {
        try
        {
            new LookAndFeelInit().setLookAndFeel();
        }
        catch (final UnsupportedLookAndFeelException e)
        {
            Logger.getLogger(Launch.class).error(e, e);
        }
        SystemPropertyLoader.validateUserHome();
        SystemPropertyLoader.loadSystemProperties();
        LOGGER = Logger.getLogger(Launch.class);
    }

    /**
     * Main method.
     *
     * @param args The program arguments.
     */
    @SuppressFBWarnings("DM_EXIT")
    public static void main(String[] args)
    {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null)
        {
            splash.createGraphics();
            splash.update();
        }

        int code;
        long lastLaunch = 0L;
        do
        {
            code = new Launch().run();
            if (System.currentTimeMillis() - lastLaunch < RELAUNCH_EXCLUSION_WINDOW_MILLIS)
            {
                LOGGER.info("Encountered a subsequent failure. Giving up.");
                break;
            }
            else if (code == 2)
            {
                LOGGER.info("User requested restart.");
            }
            else
            {
                if (code != 0)
                {
                    logErrorFile();
                }
                if (code != 0 && ourRestart)
                {
                    LOGGER.info("Unexpected exit. Restarting...");
                }
                else
                {
                    LOGGER.info("Exiting with status " + code);
                    break;
                }
            }
            lastLaunch = System.currentTimeMillis();
        }
        while (true);

        System.exit(code);
    }

    /**
     * Read an input stream until its end.
     *
     * @param is The input stream.
     * @throws IOException If there is an IO error.
     */
    private static void evacuateInputStream(InputStream is) throws IOException
    {
        boolean done;
        do
        {
            done = is.read() == -1;
        }
        while (!done);
    }

    /**
     * Get any JVM arguments set in the environment.
     *
     * @return The arguments.
     */
    private static List<String> getJvmArgsFromEnv()
    {
        List<String> args;

        String jvmArgs = System.getenv("OPENSPHERE_JVM_ARGS");
        if (jvmArgs == null)
        {
            // Check for a system property if no environment variable is
            // supplied.
            jvmArgs = System.getProperty("opensphere.jvm.args");
        }
        if (jvmArgs == null)
        {
            args = Collections.<String>emptyList();
        }
        else
        {
            // Split the string on whitespace, ignoring whitespace inside single
            // or double quotes.
            args = new ArrayList<>();
            boolean inParen = false;
            int leadingEdge = -1;
            for (int index = 0; index < jvmArgs.length(); ++index)
            {
                final char ch = jvmArgs.charAt(index);
                if (Character.isWhitespace(ch))
                {
                    if (leadingEdge >= 0 && !inParen)
                    {
                        final String arg = jvmArgs.substring(leadingEdge, index).replace("\"", "");
                        if (!arg.isEmpty())
                        {
                            args.add(StringUtilities.expandProperties(arg, System.getProperties()));
                        }
                        leadingEdge = -1;
                    }
                }
                else
                {
                    if (leadingEdge == -1)
                    {
                        leadingEdge = index;
                    }
                    if (ch == '"' || ch == '\'')
                    {
                        inParen = !inParen;
                    }
                }
            }
            if (leadingEdge >= 0)
            {
                final String arg = jvmArgs.substring(leadingEdge).replace("\"", "");
                if (!arg.isEmpty())
                {
                    args.add(StringUtilities.expandProperties(arg, System.getProperties()));
                }
            }
        }
        return args;
    }

    /**
     * Determine if an error file was written from the process and copy it to
     * the log file.
     */
    private static void logErrorFile()
    {
        File errorFile = null;
        final List<String> jvmArgsFromEnv = getJvmArgsFromEnv();
        for (final String arg : jvmArgsFromEnv)
        {
            final Matcher matcher = Pattern.compile("-XX:ErrorFile=(.+)(?:%p.*)").matcher(arg);
            if (matcher.matches())
            {
                final File file = new File(matcher.group(1));
                final File[] files = file.getParentFile().listFiles(pathname -> pathname.getName().startsWith(file.getName()));

                if (files != null)
                {
                    for (final File file2 : files)
                    {
                        if (errorFile == null || file2.lastModified() > errorFile.lastModified())
                        {
                            errorFile = file2;
                        }
                    }
                }
                else
                {
                    LOGGER.error("Error accessing files in directory " + file.getParentFile());
                }

                break;
            }
        }

        if (errorFile != null && errorFile.canRead())
        {
            for (final String line : FileUtilities.readLines(errorFile))
            {
                LOGGER.error(line);
            }
        }
    }

    /**
     * Attempt to create a JVM.
     *
     * @param input The command.
     * @return {@code true} if the JVM was successfully created.
     */
    private static boolean testJava(Collection<? extends String> input)
    {
        do
        {
            final List<String> command = New.list(input.size() + 1);
            command.addAll(input);
            command.add("-version");
            try
            {
                final Process proc1 = new ProcessBuilder(command).start();
                evacuateInputStream(proc1.getErrorStream());
                evacuateInputStream(proc1.getInputStream());

                return proc1.waitFor() == 0;
            }
            catch (final InterruptedException e)
            {
                LOGGER.warn("Interrupted while testing command [" + command + "]: " + e, e);
            }
            catch (final IOException e)
            {
                LOGGER.warn("Exception while testing command [" + command + "]: " + e, e);
            }
        }
        while (true);
    }

    /**
     * Attempt to create a JVM with a certain max memory setting.
     *
     * @param java The path to the java executable.
     * @param mem The memory setting, in megabytes.
     * @return {@code true} if the JVM was successfully created.
     */
    private static boolean testMemory(String java, int mem)
    {
        final String arg = "-Xmx" + mem + "m";
        return testJava(Arrays.asList(java, arg));
    }

    /** Constructor. */
    private Launch()
    {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final PreferencesRegistry reg = new PreferencesRegistryImpl(executor, executor);
        myPreferences = reg.getPreferences(getClass());
        myMasterPasswordInUse = SecurityManagerImpl.isMasterPasswordInUse(reg);
        myCurrentWorkingDirectory = new File(System.getProperty("user.dir"));
    }

    /**
     * Determine the maxMemory to use for the child application.
     *
     * @param java The java executable.
     * @return The maxMemory in MB.
     */
    private int determineMaxMemory(String java)
    {
        final int prefMaxMemory = myPreferences.getInt("opensphere.launch.maxMemory", -1);
        int maxMemory = prefMaxMemory == -1 ? getUpperLimitForMemoryTesting() : prefMaxMemory;

        final int maxMemoryFloorMB = 512;

        while (maxMemory > maxMemoryFloorMB)
        {
            if (testMemory(java, maxMemory))
            {
                LOGGER.info("Memory test at " + maxMemory + " was successful.");
                break;
            }
            else
            {
                LOGGER.info("Memory test at " + maxMemory + " failed.");
                maxMemory -= 128;
            }
        }

        if (maxMemory < maxMemoryFloorMB)
        {
            maxMemory = maxMemoryFloorMB;
        }

        if (maxMemory != prefMaxMemory)
        {
            if (prefMaxMemory != -1)
            {
                LOGGER.warn("Launching with preferred maxMemory " + prefMaxMemory + " MB failed.");
            }
            myPreferences.putInt("opensphere.launch.maxMemory", maxMemory, this);
        }
        LOGGER.info("Setting maxMemory to " + maxMemory + "MB");
        return maxMemory;
    }

    /**
     * Get the upper limit for memory testing.
     *
     * @return The upper limit in MB.
     */
    @SuppressFBWarnings("DM_EXIT")
    private int getUpperLimitForMemoryTesting()
    {
        int maxMemoryMB;
        final long maxMemTestBytes = Long.getLong("opensphere.launch.maxMemTestBytes", -1L).longValue();
        final long totalPhsicalMemoryBytes = Long.getLong("opensphere.launch.totalPhysicalMemoryBytes", -1L).longValue();
        if (maxMemTestBytes != -1L)
        {
            LOGGER.info("Max test memory is " + maxMemTestBytes + " bytes.");
            maxMemoryMB = (int)((float)maxMemTestBytes / Constants.BYTES_PER_MEGABYTE);
        }
        else if (totalPhsicalMemoryBytes != -1L)
        {
            LOGGER.info("Total Physical memory is " + totalPhsicalMemoryBytes + " bytes.");
            maxMemoryMB = (int)((float)totalPhsicalMemoryBytes / Constants.BYTES_PER_MEGABYTE
                    * Utilities.parseSystemProperty("opensphere.launch.maxMemTestRatio", .5f)) / 128 * 128;
        }
        else
        {
            try
            {
                new LookAndFeelInit().setLookAndFeel();
            }
            catch (final UnsupportedLookAndFeelException e)
            {
                Logger.getLogger(Launch.class).error(e, e);
            }
            final String[] options = new String[] { "256 MB", "512 MB", "768 MB", "1024 MB", "1536 MB", "2048 MB", "4096 MB" };
            final int choice = JOptionPane.showOptionDialog(null,
                    "Maximum memory setting could not be automatically determined. Please select from the following options:",
                    "Select Maximum Memory", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
            if (choice == -1)
            {
                JOptionPane.showMessageDialog(null, "No memory setting made. Application will exit.");
                System.exit(1);
                maxMemoryMB = 0;
            }
            else
            {
                maxMemoryMB = Integer.parseInt(options[choice].split(" ")[0]);
                myPreferences.putInt("opensphere.launch.maxMemory", maxMemoryMB, this);
                myPreferences.waitForPersist();
            }
        }
        return maxMemoryMB;
    }

    /**
     * Get if running in a 64-bit VM.
     *
     * @return {@code true} if the VM is 64-bit.
     */
    private boolean is64bit()
    {
        return System.getProperty("os.arch").contains("64");
    }

    /**
     * Get if running on Linux.
     *
     * @return {@code true} if the operating system is Linux.
     */
    private boolean isLinux()
    {
        return "Linux".equals(System.getProperty("os.name"));
    }

    /**
     * Run the child application.
     *
     * @return The exit code of the child application.
     */
    private int run()
    {
        String java = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";

        File baseDirectory = myCurrentWorkingDirectory.getParentFile();
        File configPropertiesFile = new File(baseDirectory, "config.properties");
        if (configPropertiesFile.canRead())
        {
            Properties configProperties = new Properties();
            try (InputStream in = new FileInputStream(configPropertiesFile))
            {
                configProperties.load(in);

                String preferredVersion = configProperties.getProperty("preferred.version");
                if (!myCurrentWorkingDirectory.getName().equals(preferredVersion))
                {
                    LOGGER.info("Current version (" + myCurrentWorkingDirectory.getName() + ") does not match preferred version ("
                            + preferredVersion + ")");
                    File preferredVersionDirectory = new File(baseDirectory, preferredVersion);
                    if (preferredVersionDirectory.isDirectory())
                    {
                        java = java.replace(myCurrentWorkingDirectory.getName(), preferredVersion);
                        myCurrentWorkingDirectory = preferredVersionDirectory;
                    }
                    else
                    {
                        LOGGER.error("Attempted to use '" + preferredVersionDirectory.getAbsolutePath()
                                + "' for launch location, but it doesn't exist as a directory.");
                    }
                }
            }
            catch (IOException e)
            {
                LOGGER.error("Unable to read configPropertiesFile from '" + configPropertiesFile.getAbsolutePath() + "'", e);
            }
        }

        final List<String> command = new ArrayList<>();
        command.add(java);

        final String os = isLinux() ? "linux" : "win32";
        final String arch = is64bit() ? "x86_64" : "x86";
        command.add("-Djava.library.path=lib" + File.separatorChar + os + File.separatorChar + arch);
        command.add("-Djava.security.policy=java.policy");
        command.add("-Dopensphere.enableRestart=true");
        command.add("-Duser.home=" + System.getProperty("user.home"));

        command.add("-Xmx" + determineMaxMemory(java) + "m");

        final int requiredCommandSize = command.size();

        // On Windows, the master password prompt doesn't get focus if popToBack
        // is used.
        if (isLinux() || !myMasterPasswordInUse)
        {
            command.add("-Dopensphere.enablePopToBack=true");
        }

        command.addAll(getJvmArgsFromEnv());

        while (command.size() > requiredCommandSize && !testJava(command))
        {
            LOGGER.warn("Failed to run with arguments: " + command);
            command.remove(command.size() - 1);
            LOGGER.warn("Attempting to run with arguments: " + command);
        }

        command.add("-cp");
        command.add(constructClasspath());
        command.add("io.opensphere.core.appl.OpenSphere");

        LOGGER.info("Starting Desktop Application: '" + StringUtilities.join("', '", command) + "'");

        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(myCurrentWorkingDirectory);

        final Process proc;
        try
        {
            proc = pb.start();
        }
        catch (final IOException e)
        {
            LOGGER.fatal("Failed to start Desktop Application: " + e, e);
            return -1;
        }

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                proc.destroy();
            }
        });

        startSysoutPipe(proc);
        startSyserrPipe(proc);

        while (true)
        {
            try
            {
                return proc.waitFor();
            }
            catch (final InterruptedException e)
            {
                LOGGER.warn("Interrupted waiting for child process.", e);
            }
        }
    }

    /**
     * Examine the current execution directory, looking for JAR files, and
     * plugins. Construct a full classpath argument from the found items.
     *
     * @return A classpath generated from the set of found items.
     */
    private String constructClasspath()
    {
        String suiteJarName;
        String configJarName;
        final String applicationVersion = System.getProperty("opensphere.launch.version", null);
        final String configJar = System.getProperty("opensphere.launch.config", null);
        if (applicationVersion == null)
        {
            suiteJarName = findSuiteJar();
        }
        else
        {
            suiteJarName = "suite-" + applicationVersion + ".jar";
        }

        if (configJar == null)
        {
            configJarName = findConfigurationJar();
        }
        else
        {
            configJarName = configJar;
        }

        return configJarName + File.pathSeparatorChar + suiteJarName + File.pathSeparatorChar + "plugins/*";
    }

    /**
     * Finds the path of the configuration JAR.
     *
     * @return the path of the configuration jar.
     */
    private String findConfigurationJar()
    {
        String returnValue = null;
        final File file = myCurrentWorkingDirectory;
        if (file.isDirectory())
        {
            final File[] files = file.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    if (name.startsWith("config") && name.endsWith(".jar") || name.contains("-config-") && name.endsWith(".jar"))
                    {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null)
            {
                if (files.length == 1)
                {
                    returnValue = files[0].getName();
                }
                else if (files.length > 1)
                {
                    LOGGER.warn("More than one config JAR found in installation directory, and no property set for "
                            + "'opensphere.launch.config'. Defaulting to newest file.");
                    Arrays.sort(files, new Comparator<File>()
                    {
                        @Override
                        public int compare(File o1, File o2)
                        {
                            return (int)(o2.lastModified() - o1.lastModified());
                        }
                    });
                    returnValue = files[0].getName();
                }
            }
        }

        if (returnValue == null)
        {
            LOGGER.fatal("No property was set for opensphere.launch.version, and no suite jars could be found in '"
                    + file.getAbsolutePath() + "', exiting.");
            throw new IllegalStateException(
                    "No property was set for opensphere.launch.version, and no suite jars could be found in '"
                            + file.getAbsolutePath() + "'");
        }

        return returnValue;
    }

    /**
     * Searches for the suite jar in the launch directory. If only one is
     * present, it will be used. If more than one are present, then the most
     * recently created file will be used.
     *
     * @throws IllegalStateException if no suite jars can be found in the launch
     *             directory.
     * @return the name of the suite jar with which to invoke the application.
     */
    private String findSuiteJar()
    {
        String returnValue = null;
        final File file = myCurrentWorkingDirectory;
        if (file.isDirectory())
        {
            final File[] files = file.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    if (name.startsWith("suite") && name.endsWith(".jar") || name.contains("-suite-") && name.endsWith(".jar"))
                    {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null)
            {
                if (files.length == 1)
                {
                    returnValue = files[0].getName();
                }
                else if (files.length > 1)
                {
                    LOGGER.warn("More than one suite JAR found in installation directory, and no property set for "
                            + "'opensphere.launch.version'. Defaulting to newest file.");
                    Arrays.sort(files, new Comparator<File>()
                    {
                        @Override
                        public int compare(File o1, File o2)
                        {
                            return (int)(o2.lastModified() - o1.lastModified());
                        }
                    });
                    returnValue = files[0].getName();
                }
            }
        }

        if (returnValue == null)
        {
            LOGGER.fatal("No property was set for opensphere.launch.version, and no suite jars could be found in '"
                    + file.getAbsolutePath() + "', exiting.");
            throw new IllegalStateException(
                    "No property was set for opensphere.launch.version, and no suite jars could be found in '"
                            + file.getAbsolutePath() + "'");
        }

        return returnValue;
    }

    /**
     * Start a pipe to send the error output from the child process to my error
     * output.
     *
     * @param proc The child process.
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private void startSyserrPipe(final Process proc)
    {
        final BufferedReader err = new BufferedReader(
                new InputStreamReader(proc.getErrorStream(), StringUtilities.DEFAULT_CHARSET));
        final Thread errThread = new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        for (String line; (line = err.readLine()) != null;)
                        {
                            System.err.println(line);
                        }
                        break;
                    }
                    catch (final IOException e)
                    {
                        LOGGER.warn("IOException piping stderr: " + e, e);
                    }
                }
            }
        };
        errThread.setDaemon(true);
        errThread.start();
    }

    /**
     * Start a pipe to send the standard output from the child process to my
     * standard output.
     *
     * @param proc The child process.
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private void startSysoutPipe(final Process proc)
    {
        final BufferedReader out = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StringUtilities.DEFAULT_CHARSET));
        final Thread outThread = new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        if (mySplashScreenActive)
                        {
                            for (String line; (line = out.readLine()) != null;)
                            {
                                if (line.endsWith(Kernel.DISPLAYING_MAIN_FRAME_MSG))
                                {
                                    final SplashScreen ss = SplashScreen.getSplashScreen();
                                    if (ss != null)
                                    {
                                        ss.close();
                                    }

                                    mySplashScreenActive = false;
                                    break;
                                }
                                else
                                {
                                    final int ix = line.indexOf(SplashScreenManagerImpl.INIT_MESSAGE_PREFIX);
                                    if (ix >= 0)
                                    {
                                        mySplashScreenManager.setInitMessage(
                                                line.substring(ix + SplashScreenManagerImpl.INIT_MESSAGE_PREFIX.length()));
                                    }
                                    else
                                    {
                                        System.out.println(line);
                                    }
                                }
                            }
                        }
                        for (String line; (line = out.readLine()) != null;)
                        {
                            if (line.endsWith(Kernel.SHUTTING_DOWN_MSG))
                            {
                                ourRestart = false;
                            }
                            System.out.println(line);
                        }
                        break;
                    }
                    catch (final IOException e)
                    {
                        LOGGER.warn("IOException piping stdout: " + e, e);
                    }
                }
            }
        };
        outThread.setDaemon(true);
        outThread.start();
    }
}
