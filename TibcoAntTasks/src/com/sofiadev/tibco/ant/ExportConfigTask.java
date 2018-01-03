package com.sofiadev.tibco.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ExportConfigTask extends Task{
	public void execute() throws BuildException{

		if(null == earFile || earFile.equals("")){
			throw new BuildException("earFile parameter is required.");
		}
		
		if(null == traPath || traPath.equals("")){
			throw new BuildException("traPath parameter is required.");
		}
		
		if(null == destFile || destFile.equals("")){
			throw new BuildException("destFile parameter is required.");
		}
		
		try{
			String line;
			Process p = Runtime.getRuntime().exec(traPath + "\\AppManage -export -ear " + earFile + " -out " + destFile, null, new File(traPath));
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null){
				log(line);
			}
			input.close();

			//check the exit value, 0 means a normal execution without errors
			if(p.exitValue() != 0){
				throw new BuildException("Missing parameters, check buildear task configuration.");
			}
		}catch(Exception err){
			throw new BuildException(err);
		}
	}

	private String traPath;
	private String earFile;
	private String destFile;
	
	public void setDestFile(String destFile) {
		this.destFile = destFile;
	}
	
	public void setEarFile(String earFile) {
		this.earFile = earFile;
	}
	
	public void setTrapath(String traPath){
		this.traPath = traPath;
	}
}
