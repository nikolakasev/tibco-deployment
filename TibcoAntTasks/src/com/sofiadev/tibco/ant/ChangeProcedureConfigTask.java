package com.sofiadev.tibco.ant;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChangeProcedureConfigTask extends Task {
    public void execute()
        throws BuildException
    {
        if(null == activityName || activityName.equals(""))
            throw new BuildException("activityName parameter is required.");
        if(null == processFileName || processFileName.equals(""))
            throw new BuildException("processFileName parameter is required.");
        if(null == procedureName || procedureName.equals(""))
            throw new BuildException("procedureName parameter is required.");
        if(!overwrite)
        {
            if(null == outputFileName || outputFileName.equals(""))
                throw new BuildException("outputFileName parameter is required, as overwrite parameter is set to false.");
        } else
        {
            outputFileName = processFileName;
            log((new StringBuilder()).append("Overwriting [").append(outputFileName).append("] BW process definition file.").toString());
        }
        try
        {
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            SimpleNamespaceContext simplenamespacecontext = new SimpleNamespaceContext();
            xpath.setNamespaceContext(simplenamespacecontext);
            simplenamespacecontext.addNamespace("pd", "http://xmlns.tibco.com/bw/process/2003");
            NodeList nodelist = (NodeList)xpath.evaluate((new StringBuilder()).append("//pd:activity[@name='").append(activityName).append("']/config").toString(), new InputSource(new FileReader(processFileName)), XPathConstants.NODESET);
            if(null != nodelist && nodelist.getLength() == 1)
            {
                Element element = (Element)nodelist.item(0);
                NodeList nodelist1 = element.getElementsByTagName("StaffwareProcedureName");
                if(null != nodelist1 && nodelist1.getLength() == 1)
                {
                    nodelist1.item(0).setTextContent(procedureName);
                    log((new StringBuilder()).append("Setting procedure name to [").append(procedureName).append("]").toString());
                } else
                {
                    log((new StringBuilder()).append("Child node [StaffwareProcedureName] not found for activity [").append(activityName).append("].").toString(), 0);
                }
                NodeList nodelist2 = element.getElementsByTagName("StaffwareProcedureStatus");
                if(null != nodelist2 && nodelist2.getLength() == 1)
                {
                    nodelist2.item(0).setTextContent((new StringBuilder()).append("").append(procedureStatus).toString());
                    log((new StringBuilder()).append("Setting procedure status to [").append(procedureStatus).append("]").toString());
                } else
                {
                    log((new StringBuilder()).append("Child node [StaffwareProcedureStatus] not found for activity [").append(activityName).append("].").toString(), 0);
                }
                org.w3c.dom.Document document = element.getOwnerDocument();
                OutputFormat outputformat = new OutputFormat(document);
                FileWriter filewriter = new FileWriter(new File(outputFileName));
                XMLSerializer xmlserializer = new XMLSerializer(filewriter, outputformat);
                xmlserializer.serialize(document);
                filewriter.flush();
                filewriter.close();
            } else
            {
                log((new StringBuilder()).append("Activity [").append(activityName).append("] not found.").toString(), 0);
            }
        }
        catch(Exception exception)
        {
            throw new BuildException(exception);
        }
    }

    public void setActivityname(String s)
    {
        activityName = s;
    }

    public void setProcessfilename(String s)
    {
        processFileName = s;
    }

    public void setOutputfilename(String s)
    {
        outputFileName = s;
    }

    public void setProcedurename(String s)
    {
        procedureName = s;
    }

    public void setProcedurestatus(int i)
    {
        procedureStatus = i;
    }

    public void setOverwrite(boolean flag)
    {
        overwrite = flag;
    }

    private String activityName;
    private String processFileName;
    private String outputFileName;
    private String procedureName;
    private int procedureStatus;
    private boolean overwrite;
}
