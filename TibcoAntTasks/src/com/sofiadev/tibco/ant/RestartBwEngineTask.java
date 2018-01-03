package com.sofiadev.tibco.ant;

import java.net.InetAddress;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import COM.TIBCO.hawk.console.hawkeye.AgentManager;
import COM.TIBCO.hawk.console.hawkeye.TIBHawkConsole;
import COM.TIBCO.hawk.talon.CompositeData;
import COM.TIBCO.hawk.talon.DataElement;
import COM.TIBCO.hawk.talon.MethodInvocation;
import COM.TIBCO.hawk.talon.MicroAgentData;
import COM.TIBCO.hawk.talon.MicroAgentException;
import COM.TIBCO.hawk.talon.MicroAgentID;

public class RestartBwEngineTask extends Task{
	
	final String TRA_MICROAGENT_NAME = "COM.TIBCO.admin.TRA";
	
	public void execute() throws BuildException{
		
		if(null == hawkDomain || hawkDomain.equals("")){
			throw new BuildException("hawkDomain parameter is required.");
		}
		
		if(null == deployment || deployment.equals("")){
			throw new BuildException("deployment parameter is required.");
		}
		
		if(null == componentInstance || componentInstance.equals("")){
			throw new BuildException("componentInstance parameter is required.");
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

			MicroAgentID microAgentID = null;
			MicroAgentID[] mAgents = manager.getMicroAgentIDs(TRA_MICROAGENT_NAME);
			if(mAgents.length > 0)
			{
				//look for the microagent that matches the host name given
				for (MicroAgentID m : mAgents) {
					if(m.getAgent().getName().equals(hostName))
						microAgentID = m;
				}
				
				if(null == microAgentID)
					log((new StringBuilder()).append("No microagent name matches the ").append(hostName).append(" host name.").toString());
			}
			else
				log((new StringBuilder()).append("Unable to retrieve any microagents for ").append(TRA_MICROAGENT_NAME).toString());
			
			String status = getBWEngineStatus(manager, microAgentID, deployment, componentInstance);
			
			if(status.equals("RUNNING"))
			{
				log((new StringBuilder()).append("Engine is RUNNING, stopping it...").toString());
				stopBWEngine(manager, microAgentID, deployment, componentInstance, enableKill, killDelay);
			}
			
			status = getBWEngineStatus(manager, microAgentID, deployment, componentInstance);
			
			Boolean firstTimeAlert = true;
			//wait while the engine status is 
			if(status != "STOPPED")
			{
				while(status == "STOPPED")
				{
					if(firstTimeAlert){
						log((new StringBuilder()).append("Engine is not yet STOPPED, waiting for 10 seconds...").toString());
						log((new StringBuilder()).append("NOTE: Engine will be killed in ").append(killDelay).append(" seconds.").toString());
						firstTimeAlert = false;
					}
					else
						log((new StringBuilder()).append("Engine is not yet STOPPED, waiting for 10 seconds...").toString());
					
					Thread.currentThread().sleep(10000);
					status = getBWEngineStatus(manager, microAgentID, deployment, componentInstance);
				}
				
				log((new StringBuilder()).append("Sleeping for ").append(waitDelay).append(" seconds before initiating a start...").toString());
				Thread.currentThread().sleep(waitDelay*1000);
			}
			
			log((new StringBuilder()).append("Starting the engine...").toString());
			startBWEngine(manager, microAgentID, deployment, componentInstance);
			
			manager.shutdown();
		}catch(Exception err){
			if (manager != null)
				manager.shutdown();
				
			throw new BuildException(err);
		}
	}

	private String hostName;
	private String deployment;
	private String componentInstance;
	
	private String hawkDomain = "";
	private String rvService;
	private String rvNetwork;
	private String rvDaemon;
	
	private int waitDelay = 20;
	private Boolean enableKill = true;
	private int killDelay = 60;
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public void setHawkDomain(String hawkDomain) {
		this.hawkDomain = hawkDomain;
	}
	
	public void setDeployment(String deployment){
		this.deployment = deployment;
	}
	
	public void setComponentInstance(String componentInstance) {
		this.componentInstance = componentInstance;
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
	
	public void setEnableKill(Boolean enableKill){
		this.enableKill = enableKill;
	}
	
	public void setKillDelay(int killDelay) {
		this.killDelay = killDelay;
	}
	
	public void setWaitDelay(int waitDelay) {
		this.waitDelay = waitDelay;
	}
	
	private String getBWEngineStatus(AgentManager manager, MicroAgentID microAgentID, String deployment, String componentInstance) throws MicroAgentException
	{
		String status = null;
		
		MethodInvocation loadBaseMethod = new MethodInvocation("getComponentInstanceStatus", new DataElement[]{
				 new DataElement("Deployment", deployment), new DataElement("Component Instance", componentInstance)
	     });
		MicroAgentData result = (MicroAgentData)manager.invoke(microAgentID, loadBaseMethod);
		
		if(null != result)
		{
			if(null != result.getData() && !(result.getData() instanceof MicroAgentException))
			{
				status = ((CompositeData)result.getData()).getDataElements()[0].getValue().toString();
			} else 
				throw (MicroAgentException)result.getData();
		}
		else
			throw new MicroAgentException("getComponentInstanceStatus invocation returned null");
		
		return status;
	}

	private void stopBWEngine(AgentManager manager, MicroAgentID microAgentID, String deployment, String componentInstance, Boolean enableKill, int killDelay) throws MicroAgentException
	{
		MethodInvocation loadBaseMethod = new MethodInvocation("stopComponentInstance", new DataElement[]{
				 new DataElement("Deployment", deployment), new DataElement("Component Instance", componentInstance),
				 new DataElement("EnableKill", enableKill), new DataElement("KillDelay", killDelay),
				 new DataElement("MaxDelay", (killDelay > 5)?killDelay-5:0), new DataElement("WaitForCheckpoints", true)
	     });
		MicroAgentData result = (MicroAgentData)manager.invoke(microAgentID, loadBaseMethod);
		
		if(null != result)
		{
			if(null != result.getData() && (result.getData() instanceof MicroAgentException))
			{
				throw (MicroAgentException)result.getData();
			}
		}
		else
			throw new MicroAgentException("stopComponentInstance invocation returned null");
	}

	private void startBWEngine(AgentManager manager, MicroAgentID microAgentID, String deployment, String componentInstance) throws MicroAgentException
	{
		MethodInvocation loadBaseMethod = new MethodInvocation("startComponentInstance", new DataElement[]{
				 new DataElement("Deployment", deployment), new DataElement("Component Instance", componentInstance)
	     });
		MicroAgentData result = (MicroAgentData)manager.invoke(microAgentID, loadBaseMethod);
		
		if(null != result)
		{
			if(null != result.getData() && (result.getData() instanceof MicroAgentException))
			{
				throw (MicroAgentException)result.getData();
			}
		}
		else
		    throw new MicroAgentException("startComponentInstance invocation returned null");
	}
}

