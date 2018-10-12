package io.opensphere.core.importer;

import java.awt.Component;
import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import io.opensphere.core.util.Utilities;

/** Imports URLs through the importer framework. */
public class URLImporterImpl implements URLImporter
{
    /** The importer registry. */
    private final ImporterRegistry myImporterRegistry;

    /** The main frame provider. */
    private final Supplier<? extends JFrame> myMainFrameProvider;

    /**
     * Constructor.
     *
     * @param importerRegistry The importer registry.
     * @param mainFrameProvider The main frame provider.
     */
    public URLImporterImpl(ImporterRegistry importerRegistry, Supplier<? extends JFrame> mainFrameProvider)
    {
        myImporterRegistry = Utilities.checkNull(importerRegistry, "importerRegistry");
        myMainFrameProvider = mainFrameProvider == null ? () -> null : mainFrameProvider;
    }

    @Override
    public void importURL(String urlString)
    {
        URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            EventQueue.invokeLater(
                    () -> JOptionPane.showMessageDialog(myMainFrameProvider.get(), "URL is malformed: " + urlString));
            return;
        }
        List<FileOrURLImporter> importers = myImporterRegistry.getImporters(
                input -> input.importsURLs() && input.canImport(url, null), FileOrURLImporter.PREC_ORDER);
        if (importers.isEmpty())
        {
            EventQueue.invokeLater(
                    () -> JOptionPane.showMessageDialog(myMainFrameProvider.get(), "Cannot import URL: " + urlString));
        }
        else
        {
            EventQueue.invokeLater(() -> importers.get(0).importURL(url, (Component)null));
        }
    }
}
