package io.opensphere.mantle.util.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class DynamicCompiler.
 */
public class DynamicCompiler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DynamicCompiler.class);

    /** The collector. */
    private final DiagnosticCollector<JavaFileObject> myCollector;

    /** The my compile lock. */
    private final ReentrantLock myCompileLock;

    /** The compiler. */
    private final JavaCompiler myCompiler;

    /** The manager. */
    private final JavaFileManager myManager;

    /**
     * Instantiates a new dynamic compiler.
     *
     * @throws DynamicCompilerUnavailableException the dynamic compiler
     *             unavailable exception
     */
    public DynamicCompiler() throws DynamicCompilerUnavailableException
    {
        myCompileLock = new ReentrantLock();

        myCompiler = getCompiler();
        if (myCompiler == null)
        {
            throw new DynamicCompilerUnavailableException("Could not retrieve compiler from ToolProvider");
        }
        myCollector = new DiagnosticCollector<>();
        myManager = new DynamicClassFileManager(myCompiler.getStandardFileManager(null, null, null));
    }

    /**
     * Compile to class.
     *
     * @param fullName the full name
     * @param javaCode the java code
     * @return the class
     * @throws ClassNotFoundException the class not found exception
     */
    public Class<?> compileToClass(String fullName, String javaCode) throws ClassNotFoundException
    {
        Class<?> cl = null;
        myCompileLock.lock();
        try
        {
            StringJavaFileObject strFile = new StringJavaFileObject(fullName, javaCode);
            Iterable<? extends JavaFileObject> units = Arrays.asList(strFile);

            ArrayList<String> moduleNames = new ArrayList<>();
            moduleNames.add("java.base");
            moduleNames.add("ALL-UNNAMED");

            CompilationTask task = myCompiler.getTask(null, myManager, myCollector, null, null, units);
            task.addModules(moduleNames);

            long start = System.nanoTime();
            boolean status = task.call().booleanValue();
            if (status)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Compilation successful for type : " + fullName + " in ",
                            System.nanoTime() - start));
                }
                cl = myManager.getClassLoader(null).loadClass(fullName);
            }
            else
            {
                StringBuilder sb = new StringBuilder(64);
                sb.append("Compile Failure for type \"").append(fullName).append("\" Message:\n");
                for (Diagnostic<?> d : myCollector.getDiagnostics())
                {
                    sb.append(String.format("%s%n", d.getMessage(null)));
                }
                sb.append("***** Compilation failed!!! ******");
                LOGGER.error(sb.toString());

                BufferedReader reader = new BufferedReader(new StringReader(javaCode));
                try
                {
                    String str;
                    for (int lineNumber = 1; (str = reader.readLine()) != null;)
                    {
                        LOGGER.error(Integer.toString(lineNumber++) + ":\t" + str);
                    }
                }
                catch (IOException e)
                {
                    throw new ImpossibleException(e);
                }
            }
        }
        finally
        {
            myCompileLock.unlock();
        }
        return cl;
    }

    /**
     * Get the Java compiler.
     *
     * @return The compiler.
     */
    private JavaCompiler getCompiler()
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null)
        {
            try
            {
                Iterator<JavaCompiler> iter = ServiceLoader.load(JavaCompiler.class).iterator();
                do
                {
                    compiler = iter.next();
                }
                while (iter.hasNext() && compiler == null);
            }
            catch (NoSuchElementException ex)
            {
                // Try loading the compiler from the context class loader.
                // This is almost guaranteed to not work, as
                // com.sun.tools.javac.api.JavacTool is not accessible. Plus
                // we've already tried the serviceloader, so this shouldn't
                // exist.
                try
                {
                    Class<JavaCompiler> clazz = (Class<JavaCompiler>)Class
                            .forName("com.sun.tools.javac.api.JavacTool", false, Thread.currentThread().getContextClassLoader())
                            .asSubclass(JavaCompiler.class);

                    Constructor<JavaCompiler>[] constructors = (Constructor<JavaCompiler>[])clazz.getConstructors();

                    if (constructors.length > 0)
                    {
                        compiler = constructors[0].newInstance();
                    }
                }
                catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoClassDefFoundError
                        | IllegalArgumentException | InvocationTargetException | SecurityException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                }
            }
        }
        return compiler;
    }
}
