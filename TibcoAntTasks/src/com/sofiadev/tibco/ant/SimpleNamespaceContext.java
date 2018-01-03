package com.sofiadev.tibco.ant;

import java.util.*;
import javax.xml.namespace.NamespaceContext;

class SimpleNamespaceContext
    implements NamespaceContext
{

    public SimpleNamespaceContext()
    {
        urisByPrefix = new HashMap<String, String>();
        prefixesByURI = new HashMap<String, HashSet<String>>();
        addNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        addNamespace("xmlns", "http://www.w3.org/2000/xmlns/");
    }

    public synchronized void addNamespace(String s, String s1)
    {
        urisByPrefix.put(s, s1);
        if(prefixesByURI.containsKey(s1))
        {
            ((Set<String>)prefixesByURI.get(s1)).add(s);
        } else
        {
            HashSet<String> hashset = new HashSet<String>();
            hashset.add(s);
            prefixesByURI.put(s1, hashset);
        }
    }

    public String getNamespaceURI(String s)
    {
        if(s == null)
            throw new IllegalArgumentException("prefix cannot be null");
        if(urisByPrefix.containsKey(s))
            return (String)urisByPrefix.get(s);
        else
            return "";
    }

    public String getPrefix(String s)
    {
        return (String)getPrefixes(s).next();
    }

    public Iterator<String> getPrefixes(String s)
    {
        if(s == null)
            throw new IllegalArgumentException("namespaceURI cannot be null");
        if(prefixesByURI.containsKey(s))
            return ((Set<String>)prefixesByURI.get(s)).iterator();
        else
            //return Collections.EMPTY_SET.iterator();
        	return null;
    }

    private Map<String, String> urisByPrefix;
    private Map<String, HashSet<String>> prefixesByURI;
}
