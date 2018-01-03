package com.sofiadev.tibco.ant;

import java.net.InetAddress;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import COM.TIBCO.hawk.console.hawkeye.AgentManager;
import COM.TIBCO.hawk.console.hawkeye.TIBHawkConsole;
import COM.TIBCO.hawk.talon.DataElement;
import COM.TIBCO.hawk.talon.MethodInvocation;
import COM.TIBCO.hawk.talon.MicroAgentData;
import COM.TIBCO.hawk.talon.MicroAgentException;
import COM.TIBCO.hawk.talon.MicroAgentID;

public class ReloadHawkRulebaseTask extends Task{
	
	final String RULEBASE_MICROAGENT_NAME = "COM.TIBCO.hawk.microagent.RuleBaseEngine";
	
	public void execute() throws BuildException{
		
		if(null == rulebaseFile || rulebaseFile.equals("")){
			throw new BuildException("rulebaseFile parameter is required.");
		}
		
		if(null == hostName || hostName.equals("")){
			if((null == rvService || rvService.equals("")) 
					|| (null == rvNetwork || rvNetwork.equals("")) 
					|| (null == rvDaemon || rvDaemon.equals("")))
				throw new BuildException("all three rvService, rvNetwork and rvDaemon parameters are required when hostName is not set.");
		}
		
		AgentManager manager = null;
		
		try
		{
			//the host name is given, retrieve the IP and determine the rvService, rvNetwork and rvDaemon parameters
			if(null != hostName && !hostName.equals(""))
			{
				java.net.InetAddress addr = InetAddress.getByName(hostName);
				if(null != addr && !addr.getHostAddress().equals(""))
				{
					log((new StringBuilder()).append("Got IP address ").append(addr.getHostAddress()).append(" for the ").append(hostName).append(" host name.").toString());
					
					rvService = "7474";
					rvNetwork = addr.getHostAddress() + ";";
					rvDaemon = "tcp:" + addr.getHostAddress() + ":7474";
				}
				else
					throw new Exception("Unable to retrieve the IP address for host " + hostName + "! Please specify the rvService, rvNetwork and rvDaemon parameters.");
			}
			
			TIBHawkConsole console = new TIBHawkConsole(hawkDomain, rvService, rvNetwork, rvDaemon);
			manager = console.getAgentManager();
			
			manager.initialize();
			log((new StringBuilder()).append("Initialized AgentManager for [").append(hawkDomain).append("] Hawk domain, rv service [").append(rvService).append("], rv network [").append(rvNetwork).append("], rv daemon [").append(rvDaemon).append("]").toString());
			
			log((new StringBuilder()).append("Retrieving microagents (this may take a while)...").toString());
			MicroAgentID[] mAgents = manager.getMicroAgentIDs(RULEBASE_MICROAGENT_NAME);
			
			Object result = null;
			if(mAgents.length > 0)
			{
				MethodInvocation loadBaseMethod = new MethodInvocation("loadRuleBaseFromFile", new DataElement[]{
						 new DataElement("File", rulebaseFile)
			     });
				result = manager.invoke(mAgents[0], loadBaseMethod);
			}
			else
			{
				throw new Exception("No microagent was found for " + RULEBASE_MICROAGENT_NAME);
			}
			
			if (result instanceof MicroAgentData && !(((MicroAgentData)result).getData() instanceof MicroAgentException)) {
				log((new StringBuilder()).append("Rulebase ").append(rulebaseFile).append(" reloaded successfully").toString());
			} else if (result instanceof MicroAgentData && ((MicroAgentData)result).getData() instanceof MicroAgentException) {
				throw (MicroAgentException)((MicroAgentData)result).getData();
			} else if (result instanceof MicroAgentException) {
			    throw (MicroAgentException)result;
			} else if (result == null) {
			    throw new MicroAgentException("loadRuleBaseFromFile invocation returned null");
			} else {
			    throw new MicroAgentException("loadRuleBaseFromFile returned an unhandled type: " + result.getClass());
			}
			
			manager.shutdown();
		}catch(Exception err){
			if (manager != null)
				manager.shutdown();
				
			throw new BuildException(err);
		}
	}

	private String hostName;
	private String hawkDomain = "";
	private String rvService;
	private String rvNetwork;
	private String rvDaemon;
	private String rulebaseFile;
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public void setHawkDomain(String hawkDomain) {
		this.hawkDomain = hawkDomain;
	}
	
	public void setRvService(String rvService) {
		this.rvService = rvService;
	}
	
	public void setRvNetwork(String rvNetwork){
		this.rvNetwork = rvNetwork;
	}
	
	public void setRvDaemon(String rvDaemon) {
		this.rvDaemon = rvDaemon;
	}
	
	public void setRulebaseFile(String rulebaseFile) {
		this.rulebaseFile = rulebaseFile;
	}
}