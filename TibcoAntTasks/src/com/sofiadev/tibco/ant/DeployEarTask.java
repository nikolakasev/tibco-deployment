package com.sofiadev.tibco.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class DeployEarTask extends Task{	
	public void execute() throws BuildException{
		
		if(null == traPath || traPath.equals("")){
			throw new BuildException("traPath parameter is required.");
		}
		
		if(null == earFile || earFile.equals("")){
			throw new BuildException("earFile parameter is required.");
		}
		
		if(null == configFile || configFile.equals("")){
			throw new BuildException("configFile parameter is required.");
		}
		
		if(null == appName || appName.equals("")){
			throw new BuildException("appName parameter is required.");
		}
		
		if(null == domain || domain.equals("")){
			throw new BuildException("domain parameter is required.");
		}
		
		if(null == adminUser || adminUser.equals("")){
			throw new BuildException("adminUser parameter is required.");
		}
		
		if(null == password || password.equals("")){
			throw new BuildException("password parameter is required.");
		}
		
		try{
			String line;
			Process p = Runtime.getRuntime().exec(traPath + "\\AppManage -deploy -ear " + earFile + " -deployConfig " + configFile + " -app " + appName + " -domain " + domain + " -user " + adminUser + " -pw " + password, null, new File(traPath));
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null){
				log(line);
			}
			input.close();

			//check the exit value, 0 means a normal execution without errors
			if(p.exitValue() != 0){
				throw new BuildException("Missing parameters, check deployear task configuration.");
			}
		}catch(Exception err){
			throw new BuildException(err);
		}
	}

	private String traPath;
	private String earFile;
	private String configFile;
	private String appName;
	private String domain;
	private String adminUser;
	private String password;
	
	public void setEarFile(String earFile) {
		this.earFile = earFile;
	}
	
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	public void setTrapath(String traPath){
		this.traPath = traPath;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setPassword(String password) {
		this.password = password;
	}	
}
