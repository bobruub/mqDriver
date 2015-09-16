package com.mqStub.core;

/**
class: mqDriver
Purpose: main method for MQ Driver
Notes: 
Author: Tim Lane
Date: 15/09/2015
Version: 
0.1 15/09/2015 lanet - initial write
**/

import com.ibm.mq.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.FileAppender;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.Security;

import java.util.ArrayList;
import java.util.List;


public class mqDriver {
  
  private MQSeriesProperties mqSeriesProperties;
  private LogFileProperties logFileProperties;

  
  List<String> receiverEventsCntr = new ArrayList<String>();

  private static String mqVersion = "0.1";
  
  public mqDriver(MQSeriesProperties mqSeriesProperties, LogFileProperties logFileProperties)
  {
    this.mqSeriesProperties = mqSeriesProperties;
    this.logFileProperties = logFileProperties;
  }
    
  static Logger logger = Logger.getLogger(httpsStub.class);
  
  public static void main(String[] args) {
    
    /*
     * get config file, need command line option
     */
    String configFileName = null;
    String configMessage = null;
    /*
     * get xml file from the command line
     */
    if (args.length > 0) {
      configFileName = args[0];
    } else { // default for testing purposes.
      configFileName = "C:\\dbox\\Dropbox\\java\\20150821_httpstub\\xml\\vie_test.xml";
    } 
    configMessage = "XML config file: " + configFileName;
    
    try {
      /*
       * open XML config file and from xml config file read HTTP properties
       */
      XMLExtractor extractor = new XMLExtractor(new FileInputStream(new File(configFileName)));
      MQSeriesProperties mqSeriesProperties = new MQSeriesProperties(extractor.getElement("MQServer"));
      mqSeriesProperties.setConfigFileName(configFileName);
     
     /*
     * setup logging
     * TRACE < DEBUG < INFO < WARN < ERROR < FATAL
     */
      LogFileProperties logFileProperties = new LogFileProperties(extractor.getElement("Header")) ;
      PropertyConfigurator.configure(logFileProperties.getLogFileName());
      logger.setLevel(Level.INFO); // for INFO for header information
      logger.info("httpsStub: version " + httpVersion);
      logger.info(configMessage);
      logger.info("log4j config file : " + logFileProperties.getLogFileName());
      httpsStub httpsStub = new httpsStub(httpProperties, logFileProperties);
      httpsStub.RunIsolator();
    } catch (Exception e) {
      logger.error("httpsStub: error extracting XML file " + configFileName + ". " + e);
      e.printStackTrace();
      System.exit(1);
    }
   
  }
    
    
  public void RunIsolator() {
    
    CoreProperties coreProperties = new CoreProperties(mqSeriesProperties.getConfigFileName(),
                                                       logger);
    /*
     * display stub information to log file
     */
    logger.info("Author : " + coreProperties.getAuthor()
                  + " - Name : " + coreProperties.getName()
                  + " - Description : " + coreProperties.getDescription()
                  + " - Date : " + coreProperties.getDate());

  
    /*
     * load the variable configurations
     */
    for (int i = 0; i < coreProperties.getVariables().size(); i++) {
      Variable variable =  (Variable) coreProperties.getVariables().get(i);
    }
    /*
     * load the baseline response message templates
     */
    for (int i = 0; i < coreProperties.getBaselineMessages().size(); i++) {
      BaseLineMessage baseLineMessage =  (BaseLineMessage) coreProperties.getBaselineMessages().get(i);
    }
    /*
     * load the receiver events and the associated messages
     */
    for (int i = 0; i < coreProperties.getReceiverEvents().size(); i++) {
      ReceiverEvent receiverEvent =  (ReceiverEvent) coreProperties.getReceiverEvents().get(i);
      int numberOfMessages = receiverEvent.getMessages().size();
      for (int c = 0; c < numberOfMessages;  c++ ) {
        EventMessage message = (EventMessage) receiverEvent.getMessages().get(c);
      }
    }
    /*
     * setup thread pool
     */
    logger.info("setting up threadpool of size : " + mqSeriesProperties.getThreadCount()); 
    ExecutorService executor = Executors.newFixedThreadPool(mqSeriesProperties.getThreadCount());
    /*
     * now we've written header detail, set log level to that in the xml
     */
    logger.info("logging set to : " + logFileProperties.getLogLevel().toUpperCase()); 
    if (logFileProperties.getLogLevel().toUpperCase().equals("INFO")) {
      logger.setLevel(Level.INFO);
    } else if (logFileProperties.getLogLevel().toUpperCase().equals("DEBUG")) {
      logger.setLevel(Level.DEBUG);
    } else if (logFileProperties.getLogLevel().toUpperCase().equals("WARN")) {
      logger.setLevel(Level.WARN);
    } else if (logFileProperties.getLogLevel().toUpperCase().equals("ERROR")) {
      logger.setLevel(Level.ERROR);
    } else if (logFileProperties.getLogLevel().toUpperCase().equals("FATAL")) {
      logger.setLevel(Level.FATAL);
    } else if (logFileProperties.getLogLevel().toUpperCase().equals("TRACE")) {
      logger.setLevel(Level.TRACE);
    }
    boolean mqSeriesLoop = true;
    boolean connectionLoop = true;
    int connectionLoopCntr = 0;
    while (mqSeriesLoop) { // loop driver until interup code is recived
    
     try { // connect to queue
          
      } catch (Exception e) {
        logger.error("mqDriver: Unable to connect to queue " 
                       + ". " + e);
        e.printStackTrace();
        // exit on fail to connect to MQ server.
        System.exit(1);
      }
      /*
       * loop throiugh baseline message 
       */
      while (connectionLoop){
        try {

          Runnable mqDriverWorker = new mqDriverWorker(clientConnection, 
                                                         httpProperties,
                                                         coreProperties,
                                                         logger,
                                                         receiverEventsCntr);
          executor.execute(mqDriverWorker);
  
          }
     
                    
        } catch (Exception e) {
          logger.error("httpsStub: socket exception. " + e );
          e.printStackTrace();
        } 
            
      }
      /* 
       * shutdown threads
       */
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
                  
    }
    
    
  }
  
  

  
}

