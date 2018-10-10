package io.opensphere.mantle.data.analysis.impl;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.CipherChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.data.analysis.ColumnAnalysis;
import io.opensphere.mantle.data.analysis.DataAnalysisReporter;
import io.opensphere.mantle.util.TextViewDialog;
import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData;
import io.opensphere.mantle.util.columnanalyzer.DataAnalyzerRepository;
import io.opensphere.mantle.util.columnanalyzer.DataTypeColumnAnalyzerDataSet;

/**
 * The Class DataAnalysisReporterImpl.
 */
public class DataAnalysisReporterImpl implements DataAnalysisReporter
{
    /** The Constant DATA_ANALYSIS_REPOSITORY_PREF_KEY. */
    private static final String DATA_ANALYSIS_REPOSITORY_PREF_KEY = "repository";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataAnalysisReporterImpl.class);

    /** The Advanced options provider. */
    private final AdvancedDataAnalysisReporterOptionsProvider myAdvancedOptionsProvider;

    /**
     * Listener for cipher changes so that the preferences can be re-encrypted.
     */
    private final CipherChangeListener myCipherChangeListener;

    /** The Elements added event listener. */
    private final EventListener<DataElementsAddedEvent> myElementsAddedEventListener;

    /** Executor used to publish events. */
    private final ThreadPoolExecutor myExecutor;

    /** The Options provider. */
    private final DataAnalysisReporterOptionsProvider myOptionsProvider;

    /** The Repo rw lock. */
    private final ReentrantReadWriteLock myRepoRWLock;

    /** The Repository. */
    private DataAnalyzerRepository myRepository;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new data analysis reporter impl.
     *
     * @param tb the {@link Toolbox}
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DataAnalysisReporterImpl(Toolbox tb)
    {
        myToolbox = tb;
        myOptionsProvider = new DataAnalysisReporterOptionsProvider(myToolbox.getPreferencesRegistry());
        myAdvancedOptionsProvider = new AdvancedDataAnalysisReporterOptionsProvider(this);
        myOptionsProvider.addSubTopic(myAdvancedOptionsProvider);
        myToolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);
        myExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("DataAnalysisReporter:Analysis"));

        myRepoRWLock = new ReentrantReadWriteLock();
        tb.getPreferencesRegistry().setPreferencesCompression(DataAnalysisReporterImpl.class, false);
        tb.getPreferencesRegistry().setPreferencesCipherFactory(DataAnalysisReporterImpl.class,
                tb.getSecurityManager().getCipherFactory());
        myCipherChangeListener = new CipherChangeListener()
        {
            @Override
            public void cipherChanged()
            {
                // Force the preferences to save with the new cipher.
                myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class).waitForPersist();
            }
        };
        tb.getSecurityManager().addCipherChangeListener(myCipherChangeListener);
        myRepository = tb.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                .getJAXBObject(DataAnalyzerRepository.class, DATA_ANALYSIS_REPOSITORY_PREF_KEY, new DataAnalyzerRepository());
        myElementsAddedEventListener = createElementsAddedEventListener();
        myToolbox.getEventManager().subscribe(DataElementsAddedEvent.class, myElementsAddedEventListener);
    }

    @Override
    public void clearAllColumnAnalysisData()
    {
        myExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                DataAnalyzerRepository repositoryCopy = new DataAnalyzerRepository();
                myRepoRWLock.writeLock().lock();
                try
                {
                    myRepository = new DataAnalyzerRepository();
                }
                finally
                {
                    myRepoRWLock.writeLock().unlock();
                }
                myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                        .putJAXBObject(DATA_ANALYSIS_REPOSITORY_PREF_KEY, repositoryCopy, false, this);
            }
        });
    }

    @Override
    public void clearColumnAnalysis(final String dtiKey)
    {
        myExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                DataAnalyzerRepository repositoryCopy = null;
                myRepoRWLock.writeLock().lock();
                try
                {
                    if (myRepository.removeAnalyzerData(dtiKey) != null)
                    {
                        repositoryCopy = new DataAnalyzerRepository(myRepository);
                    }
                }
                finally
                {
                    myRepoRWLock.writeLock().unlock();
                }

                if (repositoryCopy != null)
                {
                    myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                            .putJAXBObject(DATA_ANALYSIS_REPOSITORY_PREF_KEY, repositoryCopy, false, this);
                }
            }
        });
    }

    @Override
    public void clearColumnAnalysis(final String dtiKey, final String columnKey)
    {
        myExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                DataAnalyzerRepository repositoryCopy = null;
                myRepoRWLock.writeLock().lock();
                try
                {
                    DataTypeColumnAnalyzerDataSet set = myRepository.getAnalyzerData(dtiKey);
                    if (set != null && set.removeAnalyzerDataForColumnKey(columnKey) != null)
                    {
                        repositoryCopy = new DataAnalyzerRepository(myRepository);
                    }
                }
                finally
                {
                    myRepoRWLock.writeLock().unlock();
                }

                if (repositoryCopy != null)
                {
                    myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                            .putJAXBObject(DATA_ANALYSIS_REPOSITORY_PREF_KEY, repositoryCopy, false, this);
                }
            }
        });
    }

    /**
     * Execute analysis task.
     *
     * @param task the task
     * @return the future
     */
    public Future<?> executeAnalysisTask(AnalysisTask task)
    {
        return myExecutor.submit(task);
    }

    /**
     * Gets the analyzer data set.
     *
     * @param dtiKey the dti key
     * @return the analyzer data set
     */
    public DataTypeColumnAnalyzerDataSet getAnalyzerDataSet(String dtiKey)
    {
        DataTypeColumnAnalyzerDataSet dataSet = null;
        myRepoRWLock.readLock().lock();
        try
        {
            dataSet = myRepository.getColumnAnalyzerDataForType(dtiKey);
        }
        finally
        {
            myRepoRWLock.readLock().unlock();
        }
        return dataSet;
    }

    @Override
    public ColumnAnalysis getColumnAnalysis(String dtiKey, String columnKey)
    {
        ColumnAnalyzerData data = null;
        myRepoRWLock.readLock().lock();
        try
        {
            data = myRepository.getColumnAnalyzerData(dtiKey, columnKey);
        }
        finally
        {
            myRepoRWLock.readLock().unlock();
        }
        return data == null ? null : new ColumnAnalysisImpl(data);
    }

    @Override
    public boolean isColumnDataAnalysisEnabled()
    {
        return myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporter.class)
                .getBoolean("DataAnalsysisReporterEnabled", true);
    }

    @Override
    public void setAnalyzeStringsOnly(final String dtiKey, final boolean analyzeStringsOnly)
    {
        myExecutor.execute(new ModifyDataTypeColumnAnalyzerDataSetWorker(dtiKey)
        {
            @Override
            public void modifySet(DataTypeColumnAnalyzerDataSet set)
            {
                if (set != null)
                {
                    set.setAnalyzeStringsOnly(analyzeStringsOnly);
                }
            }
        });
    }

    @Override
    public void setDoNotTrackForType(final String dtiKey, final boolean doNotTrack)
    {
        myExecutor.execute(new ModifyDataTypeColumnAnalyzerDataSetWorker(dtiKey)
        {
            @Override
            public void modifySet(DataTypeColumnAnalyzerDataSet set)
            {
                if (set != null)
                {
                    set.setDoNotTrack(doNotTrack);
                }
            }
        });
    }

    @Override
    public void setFinalizedForType(final String dtiKey, final boolean finalized)
    {
        myExecutor.execute(new ModifyDataTypeColumnAnalyzerDataSetWorker(dtiKey)
        {
            @Override
            public void modifySet(DataTypeColumnAnalyzerDataSet set)
            {
                if (set != null)
                {
                    set.setIsFinalized(finalized);
                }
            }
        });
    }

    /**
     * Show registry report text.
     */
    public void showRegistryReportText()
    {
        String report = "ERROR";
        myRepoRWLock.readLock().lock();
        try
        {
            report = myRepository.toString();
        }
        finally
        {
            myRepoRWLock.readLock().unlock();
        }
        Component parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        TextViewDialog dvd = new TextViewDialog(parent, "Data Analysis Reporter Registry Summary (TEXT)", report, false,
                myToolbox.getPreferencesRegistry());
        dvd.setLocationRelativeTo(parent);
        dvd.setVisible(true);
    }

    /**
     * Show xml registry report.
     */
    public void showXMLRegistryReport()
    {
        String report = "ERROR";
        myRepoRWLock.readLock().lock();
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                XMLUtilities.writeXMLObject(myRepository, baos, DataAnalyzerRepository.class);
                report = new String(baos.toByteArray(), "UTF-8");
            }
            catch (JAXBException e1)
            {
                LOGGER.error("Error marshaling data analysis repository to XML");
            }
            catch (IOException e2)
            {
                LOGGER.error("Error reading data analysis repository to XML");
            }
        }
        finally
        {
            myRepoRWLock.readLock().unlock();
        }
        Component parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        TextViewDialog dvd = new TextViewDialog(parent, "Data Analysis Reporter Registry Summary (XML)", report, false,
                myToolbox.getPreferencesRegistry());
        dvd.setLocationRelativeTo(parent);
        dvd.setVisible(true);
    }

    /**
     * Update analyzer data set.
     *
     * @param dataSet the data set
     */
    public void updateAnalyzerDataSet(DataTypeColumnAnalyzerDataSet dataSet)
    {
        DataAnalyzerRepository repositoryCopy = null;
        myRepoRWLock.writeLock().lock();
        try
        {
            myRepository.setAnalyzerData(dataSet);
            repositoryCopy = new DataAnalyzerRepository(myRepository);
        }
        finally
        {
            myRepoRWLock.writeLock().unlock();
        }
        myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                .putJAXBObject(DATA_ANALYSIS_REPOSITORY_PREF_KEY, repositoryCopy, false, this);
    }

    /**
     * Creates the elements added event listener.
     *
     * @return the event listener
     */
    private EventListener<DataElementsAddedEvent> createElementsAddedEventListener()
    {
        EventListener<DataElementsAddedEvent> listener = new EventListener<>()
        {
            @Override
            public void notify(DataElementsAddedEvent event)
            {
                handleDataElementsAddedEvent(event);
            }
        };
        return listener;
    }

    /**
     * Handle data elements added event.
     *
     * @param event the event
     */
    private void handleDataElementsAddedEvent(DataElementsAddedEvent event)
    {
        if (isColumnDataAnalysisEnabled())
        {
            myExecutor.execute(new AnalysisTask(myToolbox, this, event));
        }
    }

    /**
     * The Class ModifyDataTypeColumnAnalyzerDataSetWorkers.
     */
    public abstract class ModifyDataTypeColumnAnalyzerDataSetWorker implements Runnable
    {
        /** The DTI key. */
        private final String myDTIKey;

        /**
         * Instantiates a new modify data type column analyzer data set.
         *
         * @param dtiKey the dti key
         */
        public ModifyDataTypeColumnAnalyzerDataSetWorker(String dtiKey)
        {
            myDTIKey = dtiKey;
        }

        /**
         * Modify set.
         *
         * @param set the set
         */
        public abstract void modifySet(DataTypeColumnAnalyzerDataSet set);

        @Override
        public void run()
        {
            DataAnalyzerRepository repositoryCopy = null;
            myRepoRWLock.writeLock().lock();
            try
            {
                DataTypeColumnAnalyzerDataSet set = myRepository.getAnalyzerData(myDTIKey);
                if (set == null)
                {
                    set = new DataTypeColumnAnalyzerDataSet(myDTIKey);
                    myRepository.setAnalyzerData(set);
                }
                modifySet(set);
                repositoryCopy = new DataAnalyzerRepository(myRepository);
            }
            finally
            {
                myRepoRWLock.writeLock().unlock();
            }

            myToolbox.getPreferencesRegistry().getPreferences(DataAnalysisReporterImpl.class)
                    .putJAXBObject(DATA_ANALYSIS_REPOSITORY_PREF_KEY, repositoryCopy, false, this);
        }
    }
}
