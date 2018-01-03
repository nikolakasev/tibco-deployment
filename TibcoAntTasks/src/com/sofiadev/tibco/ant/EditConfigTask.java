package com.sofiadev.tibco.ant;

import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

public class EditConfigTask extends Task{
	private String configFile;
	private String settingsFile;
	
	public void execute() throws BuildException{
		if(null == configFile || configFile.equals(""))
            throw new BuildException("configFile parameter is required.");
        if(null == settingsFile || settingsFile.equals(""))
            throw new BuildException("settingsFile");
        try
        {
            log((new StringBuilder()).append("Parsing settings file [").append(settingsFile).append("]").toString(), 3);
            DOMParser domparser = new DOMParser();
            domparser.parse(settingsFile);
            Document document = domparser.getDocument();
            NodeList nodelist = document.getElementsByTagName("variable");
            if(null != nodelist && nodelist.getLength() > 0)
            {
                log((new StringBuilder()).append("Found [").append(nodelist.getLength()).append("] variables in the settings file.").toString(), 3);
                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                for(int i = 0; i < nodelist.getLength(); i++)
                {
                    log((new StringBuilder()).append("Read variable [").append(nodelist.item(i).getAttributes().item(0).getTextContent()).append("], value: ").append(nodelist.item(i).getTextContent()).toString(), 3);
                    hashtable.put(nodelist.item(i).getAttributes().item(0).getTextContent().toLowerCase(), nodelist.item(i).getTextContent());
                }

                DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
                documentbuilderfactory.setValidating(false);
                documentbuilderfactory.setNamespaceAware(true);
                Document document1 = documentbuilderfactory.newDocumentBuilder().parse(new File(configFile));
                if(null != document1)
                {
                    NodeList nodelist1 = document1.getElementsByTagNameNS("http://www.tibco.com/xmlns/ApplicationManagement", "name");
                    if(null != nodelist1 && nodelist1.getLength() > 0)
                    {
                        for(int j = 0; j < nodelist1.getLength(); j++)
                        {
                            String s = nodelist1.item(j).getTextContent().toLowerCase();
                            if(hashtable.containsKey(s))
                            {
                                nodelist1.item(j).getNextSibling().getNextSibling().setTextContent((String)hashtable.get(s));
                                log((new StringBuilder()).append("Variable ").append(nodelist1.item(j).getTextContent()).append(" set to ").append((String)hashtable.get(s)).append("").toString());
                            } else
                            {
                                log((new StringBuilder()).append("Variable ").append(s).append(" not found in the config file.").toString(), 3);
                            }
                        }

                        OutputFormat outputformat = new OutputFormat(document1);
                        FileWriter filewriter = new FileWriter(new File(configFile));
                        XMLSerializer xmlserializer = new XMLSerializer(filewriter, outputformat);
                        xmlserializer.serialize(document1);
                        filewriter.flush();
                        filewriter.close();
                    } else
                    {
                        log((new StringBuilder()).append("The [").append(configFile).append("] configuration file is not a TIBCO BusinessWorks EAR config file. Export the configuration file from the EAR file (see exportconfig ant task), then run this task again.").toString());
                    }
                } else
                {
                    log((new StringBuilder()).append("Not able to parse the [").append(configFile).append("] configuration file. Make sure that it is a TIBCO BusinessWorks EAR config file. You can crete one from an existing EAR using the exportconfig ant task.").toString());
                }
            } else
            {
                log((new StringBuilder()).append("No variables found in settings file [").append(settingsFile).append("]. Check XML file format.").toString());
            }
        }
        catch(Exception exception)
        {
            throw new BuildException(exception);
        }
	}
	
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	public void setSettingsFile(String settingsFile) {
		this.settingsFile = settingsFile;
	}
}
