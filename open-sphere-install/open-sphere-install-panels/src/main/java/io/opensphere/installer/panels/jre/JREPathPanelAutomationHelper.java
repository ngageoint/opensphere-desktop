package io.opensphere.installer.panels.jre;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;

/**
 * An automation helper used to configure and locate the JRE.
 */
public class JREPathPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation
{
    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.automation.PanelAutomation#createInstallationRecord(com.izforge.izpack.api.data.InstallData,
     *      com.izforge.izpack.api.adaptator.IXMLElement)
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        String jdkVarName = installData.getVariable("jreVarName");
        String jdkPathName = installData.getVariable(jdkVarName);

        IXMLElement jrePath = new XMLElementImpl("jrePath", rootElement);
        jrePath.setContent(jdkPathName);
        rootElement.addChild(jrePath);

        IXMLElement jreVar = new XMLElementImpl("jreVarName", rootElement);
        jreVar.setContent(jdkVarName);
        rootElement.addChild(jreVar);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.automation.PanelAutomation#runAutomated(com.izforge.izpack.api.data.InstallData,
     *      com.izforge.izpack.api.adaptator.IXMLElement)
     */
    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement jrePathElement = panelRoot.getFirstChildNamed("jrePath");
        String jrePath = jrePathElement.getContent();

        IXMLElement jreVarNameElement = panelRoot.getFirstChildNamed("jreVarName");
        String jreVarName = jreVarNameElement.getContent();

        installData.setVariable(jreVarName, jrePath);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.installer.automation.PanelAutomation#processOptions(com.izforge.izpack.api.data.InstallData, com.izforge.izpack.api.data.Overrides)
     */
    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
        /* intentionally blank */
    }
}