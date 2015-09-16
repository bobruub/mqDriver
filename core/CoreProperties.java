package com.mqDriver.core;

/**
class: coreProperties
Purpose: extracts all the core details for the request/response/variables, etc from xml file.
Notes:
Author: Tim Lane
Date: 24/03/2014
**/

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CoreProperties {
 
    public static final String CORE_TAG = "Core";
    public static final String HEADER_TAG = "Header";
    public static final String VARIABLE_TAG = "Variable";
    public static final String BASELINE_MESSAGE_TAG = "BaselineMessage";

    private String logFileName;
    private String logLevel;
    private String author;
    private String name;
    private String description;
    private String date;
    private int numberValue;
    
    private List<Variable>  variables = new ArrayList<Variable>();
    private List<BaseLineMessage>  baselineMessages = new ArrayList<BaseLineMessage>();
    
    public CoreProperties(){
    }
    
    public CoreProperties(String configFileName, Logger logger){
      
      try {
        FileInputStream inFile = new FileInputStream(new File(configFileName));
                                                     
        XMLExtractor extractor = new XMLExtractor(inFile);
        /*
         * get core details
         * <Core Author="Tim Lane" Name="ESG KISS V0.5" Description="ESG KISS Stub Port 9001" Date="07/02/2014">
         */
         Element coreElement = extractor.getElement(CORE_TAG);
         setAuthor(coreElement.getAttribute("Author"));
         setName(coreElement.getAttribute("Name"));
         setDescription(coreElement.getAttribute("Description"));
         setDate(coreElement.getAttribute("Date"));
          
        /* 
         * get variables
         * <Variable Name="TIMESTAMP" Type="Timestamp" Format="HH:mm:ss"/>
        */
        NodeList variableNodes = extractor.getNodeList(VARIABLE_TAG);
        for (int s = 0; s < variableNodes.getLength(); s++)
        {
            Element variableElement = (Element)variableNodes.item(s);
            addVariable(getVariable(variableElement,logger));
        }
        
        /* 
         * get baseline nodes
         * <BaselineMessage Name="TestPage"><![CDATA[respopnse message]]</BaselineMessage> 
        */        
        NodeList baselineMsgNodes = extractor.getNodeList(BASELINE_MESSAGE_TAG);
        for (int s = 0; s < baselineMsgNodes.getLength(); s++) {
            Element baselineMsgElement = (Element)baselineMsgNodes.item(s);
            addBaselineMessage(getBaselineMessage(baselineMsgElement));
        }
        
        inFile.close();
      } catch (Exception e) {
        logger.error("CoreProperties: error processing xml config file. " + configFileName 
                    + " " + e);
        e.printStackTrace();
      } 
    }
    /*
     * core elements
     */
    
    public void setName(String name) {
      this.name = name;
    }
        
    public String getName(){
      return this.name;
    }
    
    public void setAuthor(String author) {
      this.author = author;
    }
        
    public String getAuthor(){
      return this.author;
    }
    
    public void setDescription(String description) {
      this.description = description;
    }
        
    public String getDescription(){
      return this.description;
    }
    
    public void setDate(String date) {
      this.date = date;
    }
        
    public String getDate(){
      return this.date;
    }
   
    public void setNumberValue(String numberValue) {
       this.numberValue=Integer.valueOf(numberValue);
    }
    
    public int getNumberValue() {
      return this.numberValue;
    }
    
    public void updateNumberValue(String numberIncrement) {
      if (numberIncrement.length() == 0){
        int increment = Integer.parseInt(numberIncrement);
        this.numberValue += increment;
      } else {
          this.numberValue++;
      }
    }
    
    public void setLogFileName(String logFileName) {
      this.logFileName = logFileName;
    }
        
    public String getLogFileName(){
      return this.logFileName;
    }
      
    public String getProgramVersion(){
      return this.logLevel;
    }
    
    public void setLogLevel(String logLevel) {
      this.logLevel = "Level." + logLevel.toUpperCase();
    }
        
    public String getLogLevel(){
      return this.logLevel;
    }
    /*
     *  Populate a Variable element: 
    */
    public void addVariable(Variable variable){
        variables.add(variable);
    }
    
    public List getVariables(){
        return variables;
    }
    
    private static Variable getVariable(Element variableElement, Logger logger){
        try {
            Variable variable = new Variable(logger);
            variable.setName(variableElement.getAttribute("Name"));
            variable.setType(variableElement.getAttribute("Type"));
            variable.setValue(variableElement.getAttribute("Value"));
            variable.setStartPosition(variableElement.getAttribute("StartPosition"));
            variable.setLength(variableElement.getAttribute("Length"));
            variable.setColumn(variableElement.getAttribute("Column"));
            variable.setDelimiter(variableElement.getAttribute("Delimiter"));
            variable.setFormat(variableElement.getAttribute("Format"));
            variable.setInitialValue(variableElement.getAttribute("InitialValue"));
            variable.setIncrement(variableElement.getAttribute("Increment"));
            variable.setOffset(variableElement.getAttribute("Offset"));
            variable.setOccurrence(variableElement.getAttribute("Occurrence"));
            variable.setFileName(variableElement.getAttribute("Filename"));
            variable.setNumberValue(variableElement.getAttribute("Filename"), 
                                    variableElement.getAttribute("InitialValue"));
            variable.setRandMin(variableElement.getAttribute("RandMin"));
            variable.setRandMax(variableElement.getAttribute("RandMax"));
            variable.setDefaultValue(variableElement.getAttribute("DefaultValue"));
            variable.setConvert(variableElement.getAttribute("Convert"));
            variable.setTrim(variableElement.getAttribute("Trim"));
            variable.setDelimiter(variableElement.getAttribute("Delimiter")); 
            variable.setSessionLength(variableElement.getAttribute("SessionLength")); 
            variable.setAccessType(variableElement.getAttribute("AccessType"));
            variable.loadDataFile(variableElement.getAttribute("Filename"));
            return variable;
        } catch (Exception e) {
            logger.error("CoreProperties: Error setting variables." + e);
            e.printStackTrace();
            return null;
        }
    }
    
    /*
     *  Populate a BaselineMessage element: 
    */
    public void addBaselineMessage(BaseLineMessage baselineMessage)
    {
        baselineMessages.add(baselineMessage);
    }

    public List getBaselineMessages()
    {
        return baselineMessages;
    }
      
    private static BaseLineMessage getBaselineMessage(Element baselineMsgElement)
    {
        BaseLineMessage baselineMsg = new BaseLineMessage();
        baselineMsg.setName(baselineMsgElement.getAttribute("Name"));
        baselineMsg.setVolume(baselineMsgElement.getAttribute("Volume"));
        baselineMsg.setWaitDistribution(baselineMsgElement.getAttribute("WaitDistribution"));
        baselineMsg.setMinWait(baselineMsgElement.getAttribute("MinWait"));
        baselineMsg.setMaxWait(baselineMsgElement.getAttribute("MaxWait"));
        baselineMsg.setQueueName(baselineMsgElement.getAttribute("QueueName"));
        Text baselineMsgText = (Text)baselineMsgElement.getFirstChild();
        baselineMsg.setCdata(baselineMsgText.getData());
        return baselineMsg;
    }
     
   

        
}

