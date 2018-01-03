package com.sofiadev.tibco.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BuildEarTask extends Task{	
	public void execute() throws BuildException{
		
		if(null == archiveName || archiveName.equals("")){
			throw new BuildException("archiveName parameter is required.");
		}
		
		if(null == traPath || traPath.equals("")){
			throw new BuildException("traPath parameter is required.");
		}
		
		if(null == destFile || destFile.equals("")){
			throw new BuildException("destFile parameter is required.");
		}
		
		if(null == src || src.equals("")){
			throw new BuildException("src parameter is required.");
		}
		
		try{
			String line;
			Process p = Runtime.getRuntime().exec(traPath + "\\buildear -s -ear /Deployment/" + archiveName + ".archive -o " + destFile + " -p " + src, null, new File(traPath));
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
	
	private String archiveName;
	
	public void setArchivename(String archiveName){
		this.archiveName = archiveName;
	}
	
	private String traPath;
	
	public void setTrapath(String traPath){
		this.traPath = traPath;
	}
	
	private String destFile;
	
	public void setDestfile(String destFile){
		this.destFile = destFile;
	}
	
	private String src;
	
	public void setSrc(String src){
		this.src = src;
	}
}