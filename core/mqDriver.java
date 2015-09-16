package com.mqDriver.core;

/**
 class: mqDriver
 Purpose: main method for MQ Driver
 Notes: 
 Author: Tim Lane
 Date: 15/09/2015
 Version: 
 0.1 15/09/2015 lanet - initial write
 **/
//import com.sharkysoft.printf.Printf;
//import com.sharkysoft.printf.PrintfTemplate;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//import java.util.ArrayList;
//import java.util.List;
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.commons.lang3.StringUtils;
// 2.2
//import java.util.Collection;

//import com.ibm.mq.*;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import java.util.ArrayList;
//import java.util.List;
//import java.lang.*;

//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.InputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.FileAppender;


import java.util.ArrayList;
import java.util.List;


public class mqDriver {
  
  private MQSeriesProperties mqSeriesProperties;
  private LogFileProperties logFileProperties;
  String responseMsg = null;
  String variableValue = null;
  

  public static final String FILE_TYPE = "File";
  public static final String NUMBER_TYPE = "Number";
  public static final String RANDOM_DOUBLE_TYPE = "RandomDouble";
  public static final String RANDOM_LONG_TYPE = "RandomLong";
  public static final String STRING_TYPE = "String";
  public static final String TIMESTAMP_TYPE = "Timestamp";
  public static final String LOOKUP_TYPE = "Lookup";
  public static final String GUID_TYPE = "Guid";
  public static final String SESSION_TYPE = "SessionId";
  
  List<String> receiverEventsCntr = new ArrayList<String>();
  
  private static String mqVersion = "0.2";
  
  public mqDriver(MQSeriesProperties mqSeriesProperties, LogFileProperties logFileProperties)
  {
    this.mqSeriesProperties = mqSeriesProperties;
    this.logFileProperties = logFileProperties;
  }
  
  static Logger logger = Logger.getLogger(mqDriver.class);
  
  public static void main(String[] args) {
    
    /*
     * get config file, need command line option
     */
    String configFileName = null;
    String logFileName = null;
    String configMessage = null;
    boolean testConfig = false;
    /*
     * get xml file from the command line
     */
    if (args.length > 0) {
      configFileName = args[0];
    } else { // default for testing purposes.
      testConfig=true;
      configFileName = "C:\\dbox\\Dropbox\\java\\mq_driver\\xml\\mqdriver.xml";
      logFileName = "C:\\dbox\\Dropbox\\java\\mq_driver\\xml\\log4j.properties";
    } 
    configMessage = "XML config file: " + configFileName;
        
    try {
      /*
       * open XML config file and from xml config file read HTTP properties
       */
      XMLExtractor extractor = new XMLExtractor(new FileInputStream(new File(configFileName)));
      MQSeriesProperties mqSeriesProperties = new MQSeriesProperties(extractor.getElement("MQSeries"));
      mqSeriesProperties.setConfigFileName(configFileName);
      /*
       * setup logging
       * TRACE < DEBUG < INFO < WARN < ERROR < FATAL
       */
      LogFileProperties logFileProperties = new LogFileProperties(extractor.getElement("Header")) ;
      if (!testConfig) {
        logFileName = logFileProperties.getLogFileName();
      } 
      PropertyConfigurator.configure(logFileName);
      logger.setLevel(Level.INFO); // for INFO for header information
      logger.info("mqDriver: version " + mqVersion);
      logger.info(configMessage);
      logger.info("log4j config file : " + logFileName);
      mqDriver mqDriver = new mqDriver(mqSeriesProperties, logFileProperties);
      mqDriver.RunIsolator();
    } catch (Exception e) {
      logger.error("mqDriver: error extracting XML file " + configFileName + ". " + e);
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
    
    /*
     * loop through all the baseline messages and create a thread for each one processing.
     */ 
    for (int i = 0; i < coreProperties.getBaselineMessages().size(); i++) {
      BaseLineMessage baseLineMessage =  (BaseLineMessage) coreProperties.getBaselineMessages().get(i);
      Runnable MQDriverWorker = new MQDriverWorker(baseLineMessage, 
                                                   mqSeriesProperties,
                                                   coreProperties,
                                                   logger);
      executor.execute(MQDriverWorker);
       
      
    }
  
    /* 
     * shutdown threads
     */
    executor.shutdown();
    while (!executor.isTerminated()) {
    }
    
  }
  
  
  
  
}

