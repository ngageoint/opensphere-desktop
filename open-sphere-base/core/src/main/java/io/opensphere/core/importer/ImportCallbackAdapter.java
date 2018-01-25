package io.opensphere.core.importer;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Adapter for an {@link ImportCallback}.
 */
public class ImportCallbackAdapter implements ImportCallback
{
    @Override
    public void fileGroupImportComplete(boolean success, List<File> files, Object responseObject)
    {
    }

    @Override
    public void fileImportComplete(boolean success, File aFile, Object responseObject)
    {
    }

    @Override
    public void urlImportComplete(boolean success, URL aURL, Object responseObject)
    {
    }
}
