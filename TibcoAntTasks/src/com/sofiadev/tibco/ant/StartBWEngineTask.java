package com.sofiadev.tibco.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import COM.TIBCO.hawk.console.hawkeye.AgentManager;
import COM.TIBCO.hawk.console.hawkeye.TIBHawkConsole;
import COM.TIBCO.hawk.talon.DataElement;
import COM.TIBCO.hawk.talon.MethodInvocation;
import COM.TIBCO.hawk.talon.MicroAgentData;
import COM.TIBCO.hawk.talon.MicroAgentException;
import COM.TIBCO.hawk.talon.MicroAgentID;

public class StartBWEngineTask extends Task{
	public void execute() throws BuildException{

		if(null == rvService || rvService.equals("")){
			throw new BuildException("rvService parameter is required.");
		}
		
		if(null == rvNetwork || rvNetwork.equals("")){
			throw new BuildException("rvNetwork parameter is required.");
		}
		
		if(null == rvDaemon || rvDaemon.equals("")){
			throw new BuildException("rvDaemon parameter is required.");
		}
		
		try{
			TIBHawkConsole console = new TIBHawkConsole(hawkDomain, rvService, rvNetwork, rvDaemon);
			AgentManager manager = console.getAgentManager();
			
			manager.initialize();
			log((new StringBuilder()).append("Initialized AgentManager for ").append(hawkDomain).append(" Hawk domain, rv service [").append(rvService).append("], rv network [").append(rvNetwork).append("], rv daemon [").append(rvDaemon).append("]").toString());
			
			log((new StringBuilder()).append("Retrieving microagents (this may take a while)...").toString());
			MicroAgentID[] mAgents = manager.getMicroAgentIDs("COM.TIBCO.hawk.microagent.RuleBaseEngine");
			
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
				throw new Exception("No microagent was found for COM.TIBCO.hawk.microagent.RuleBaseEngine");
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
			throw new BuildException(err);
		}
	}

	//the only parameter that is not mandatory, "" is used if a Hawk domain is not specified
	private String hawkDomain = "";
	private String rvService;
	private String rvNetwork;
	private String rvDaemon;
	private String rulebaseFile;
	
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