package com.bitsys.common.http.proxy;

import java.util.Collection;
import javax.script.ScriptException;
import org.junit.Assert;
import org.junit.Test;

public class ProxyScriptRunnerTest
{
	@Test
	public void testScriptsExist()
	{
		boolean initialized = true;
		try
		{
			ProxyScriptRunner runner = new ProxyScriptRunner();
			runner.loadSystemScripts();
			
			Collection<String> scripts = ProxyScriptRunner.getSystemScripts();
			Assert.assertEquals(15, scripts.size());
		}
		catch (IllegalStateException | ScriptException e)
		{
			initialized = false;
		}
		Assert.assertTrue(initialized);
	}
}
