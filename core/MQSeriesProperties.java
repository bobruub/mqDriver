package com.mqDriver.core;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.ibm.mq.*;

public class MQSeriesProperties {
  
    public static final String PARAM_QMGR = "QManager";
    public static final String PARAM_HOST = "ClientHost";
    public static final String PARAM_CHANNEL = "ClientChannel";
    public static final String PARAM_PORT = "ClientPort";
    public static final String PARAM_USERNAME = "ClientUserName";
    public static final String PARAM_PASSWORD = "ClientPassword";
    public static final String PARAM_QUEUENAME = "QueueName";
     public static final String PARAM_THREADCOUNT = "ThreadCount";

    public static final String PARAM_WAIT = "WaitTime";
    // 1.5.6  
    public static String MSG_FORMAT_HEX = "HEX";
    public static String MSG_FORMAT_EBCDIC = "EBCDIC";
    public static String MSG_FORMAT_ASCII = "ASCII";
    public static String MSG_TYPE_REQUEST = "REQUEST";
    public static String MSG_TYPE_REPLY = "REPLY";
    public static String MSG_TYPE_DATAGRAM = "DATAGRAM";

    private int threadCount;
       String configFileName = null;
       
    private String                      qManager;
    private Element                     coreElement;
    private String                      logFile;
    // 1.6.5
    private boolean debugFlag = false;
    // 1.7.1 
    private boolean encFlag = false;

  //  private MQSeriesInputProperties     inputProperties;
  //  private MQSeriesOutputProperties    outputProperties;
    private int                         waitTime = 7200;
    private String                      clientChannel;
    private String                      clientHost;
    // 1.7.2
    private String                      clientUserName;
    private String                      clientPassword;
    private int                         clientPort;
    private String queueName;   

    public MQSeriesProperties(Element mqElement)
    {
      
        setQManager(mqElement.getAttribute(PARAM_QMGR));
        setClientHost(mqElement.getAttribute(PARAM_HOST));
        setClientChannel(mqElement.getAttribute(PARAM_CHANNEL));
        setClientPort(mqElement.getAttribute(PARAM_PORT));
        // 1.7.2
        setClientUserName(mqElement.getAttribute(PARAM_USERNAME));
        setClientPassword(mqElement.getAttribute(PARAM_PASSWORD));
        setQueueName(mqElement.getAttribute(PARAM_QUEUENAME));
        setThreadCount(mqElement.getAttribute(PARAM_THREADCOUNT));
        
        //setWaitTime(mqElement.getAttribute(PARAM_WAIT));
    
        // Now we can setup the message properties by passing the 'MQ' element
        // into the setMessageProperties routine:
   //     setMessageProperties(mqElement);
    }

     public void setQueueName(String queueName){
       
       
    this.queueName = queueName;
  }
  
  public String getQueueName(){
    return queueName;
  }
  
  public void setThreadCount(String threadCount){
    this.threadCount = Integer.parseInt(threadCount);

  }
  
  public int getThreadCount(){
    return threadCount;
  }
  
   public void setConfigFileName(String configFileName){
    this.configFileName = configFileName;
  }
       
  public String getConfigFileName(){
    return configFileName;
  }
  
    public String getQManager() {
        return qManager;
    }

    public void setQManager(String qManager) {

        this.qManager = qManager;
    }
 // 1.6.5
    public boolean getDebugFlag() {
        return debugFlag;
    }
// 1.6.5
    public void setDebugFlag(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }
    
// 1.7.1
    public boolean getEncFlag() {
        return encFlag;
    }
 // 1.7.1
    public void setEncFlag(boolean encFlag) {
        this.encFlag = encFlag;
    }

    public String getLogFileName() {
        return logFile;
    }

    public void setLogFileName(String logFile) {
        this.logFile = logFile;
    }

    public void setCoreElement(Element coreElement)
    {
        this.coreElement = coreElement;
    }

    public Element getCoreElement()
    {
        return coreElement;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        if ((waitTime != null) && (waitTime.length() > 0))
            this.waitTime = Integer.parseInt(waitTime);
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public String getClientChannel() {
        return clientChannel;
    }

    public void setClientChannel(String clientChannel) {
        if ((clientChannel != null) && (clientChannel.length() > 0))
            this.clientChannel = clientChannel;
        if (clientChannel.length() == 0)
         this.clientChannel = null;
        

    }
    
// 1.7.2
    public String getClientUserName() {
        return clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        if ((clientUserName != null) && (clientUserName.length() > 0))
            this.clientUserName = clientUserName;
        if (clientUserName.length() == 0)
         this.clientUserName = null;

    }
    
    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        if ((clientPassword != null) && (clientPassword.length() > 0))
            this.clientPassword = clientPassword;
        

    }
    
    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        if ((clientHost != null) && (clientHost.length() > 0))
            this.clientHost = clientHost;
        

    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(String clientPort) {
        if ((clientPort != null) && (clientPort.length() > 0))
            this.clientPort = Integer.parseInt(clientPort);
        

    }

    public boolean validate()
    {
        return true;
    }


    
    
}
