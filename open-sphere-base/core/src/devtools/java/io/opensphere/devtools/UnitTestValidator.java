package io.opensphere.devtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/** Validates unit test package structure. */
@SuppressWarnings("PMD.SystemPrintln")
public class UnitTestValidator implements Runnable
{
    /** The workspace directory. */
    private final String myWorkspace;

    /**
     * Main.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        String workspace = args[0];
        UnitTestValidator validator = new UnitTestValidator(workspace);
        validator.run();
    }

    /**
     * Constructor.
     *
     * @param workspace The workspace directory
     */
    public UnitTestValidator(String workspace)
    {
        myWorkspace = workspace;
    }

    @Override
    public void run()
    {
        try
        {
            final String[] skips = { hug(".metadata"), hug(".svn"), hug("bin" + File.separatorChar + "com"), hug("lib"), hug("target") };
            Files.walkFileTree(Paths.get(myWorkspace), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                {
                    String fileString = file.toString();
                    boolean skip = containsAny(fileString, skips);
                    if (!skip && fileString.endsWith("Test.java") && !fileString.endsWith("JDependTest.java"))
                    {
                        String testFile = fileString;
                        String srcFile = fileString.replace(hug("test"), hug("main")).replace("Test.java", ".java");
                        if (!Files.exists(Paths.get(srcFile)))
                        {
                            System.out.println(testFile);
                        }
                    }
                    return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    /**
     * Determines whether the string s contains any of the strings in the array.
     *
     * @param s the string to check
     * @param strings the array
     * @return whether the string s contains any of the strings in the array
     */
    private static boolean containsAny(String s, String[] strings)
    {
        for (String string : strings)
        {
            if (s.contains(string))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Hugs the string with file separators.
     *
     * @param s the string
     * @return the new string
     */
    private static String hug(String s)
    {
        return File.separatorChar + s + File.separatorChar;
    }
}
