package com.mqDriver.core;

/**
 class: MqDriverWorker
 Purpose: new thread for HTTP stub.
 Notes:
 Author: Tim Lane
 Date: 24/03/2014
 
 **/

import com.sharkysoft.printf.Printf;
import com.sharkysoft.printf.PrintfTemplate;
//import java.io.*;
//import java.io.IOException;
//import java.net.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.List;
//import java.util.Vector;
//import java.util.Iterator;
import java.util.Random;
//import java.util.Formatter;
import java.util.Date;
import java.util.Locale;
//import java.net.URL; 
//import java.sql.Timestamp;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.UUID;
//import java.util.ArrayList;
//import java.util.List;
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.commons.lang3.StringUtils;
// 2.2
//import java.util.Collection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.ibm.mq.*;

public class MQDriverWorker implements Runnable {
  
  public static final String FILE_TYPE = "File";
  public static final String FILE_READ_TYPE = "FileRead";
  
  public static final String NUMBER_TYPE = "Number";
  public static final String RANDOM_DOUBLE_TYPE = "RandomDouble";
  public static final String RANDOM_LONG_TYPE = "RandomLong";
  public static final String STRING_TYPE = "String";
  public static final String THREAD_TYPE = "Thread";
  public static final String TIMESTAMP_TYPE = "Timestamp";
  public static final String HEX_TYPE = "HEX";
  public static final String GUID_TYPE = "Guid";
  public static final String SESSION_TYPE = "SessionId";
  
  private RandomNumberGenerator randomGenerator;
  
  private BaseLineMessage baseLineMessage;
  private MQSeriesProperties mqSeriesProperties;
  private CoreProperties coreProperties;
  private Logger logger;
  private Variable variable;
  String threadName = null;
  
  
  public MQDriverWorker(BaseLineMessage baseLineMessage, 
                        MQSeriesProperties mqSeriesProperties,
                        CoreProperties coreProperties,
                        Logger logger){
    this.baseLineMessage = baseLineMessage;
    this.mqSeriesProperties = mqSeriesProperties;
    this.coreProperties = coreProperties;
    this.logger = logger;  
    this.variable=variable;
  }
  
  @Override
  public void run() {
    
    threadName = Printf.format("%.8d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
    logger = Logger.getLogger(mqDriver.class);
    if (logger.isInfoEnabled()){
      logger.debug("mqSeries Client Channel " + mqSeriesProperties.getClientChannel());
      logger.debug("mqSeries Client host " + mqSeriesProperties.getClientHost());
      logger.debug("mqSeries Client port " + mqSeriesProperties.getClientPort());
      logger.debug("mqSeries Client user " + mqSeriesProperties.getClientUserName());
      logger.debug("mqSeries Client password " + mqSeriesProperties.getClientPassword());
      logger.debug("mqSeries queue manager " + mqSeriesProperties.getQManager());
      logger.debug("mqSeries queue " + mqSeriesProperties.getQueueName());
    }
    
    String responseMsg = null;
    String variableValue = null;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
    String formattedDate;
    String formattedStartDate;
    Date date;
    //Date startDate;
    //Date endDate;
    String queueName;
        
    
    String waitDistribution = baseLineMessage.getWaitDistribution();
    double minWait = Double.parseDouble(baseLineMessage.getMinWait());
    double maxWait = Double.parseDouble(baseLineMessage.getMaxWait());
    double waitTime;
    int maxVolume = Integer.valueOf(baseLineMessage.getVolume());
    // loop for the volume set in the xml
    for (int loopCounter=0; loopCounter<maxVolume; loopCounter++){
      try {
        // if client connections details are set then use them
        if (mqSeriesProperties.getClientChannel() != null) {
          MQEnvironment.channel = mqSeriesProperties.getClientChannel();
          MQEnvironment.hostname = mqSeriesProperties.getClientHost();
          MQEnvironment.port = mqSeriesProperties.getClientPort();
          MQEnvironment.userID = mqSeriesProperties.getClientUserName();
          MQEnvironment.password = mqSeriesProperties.getClientPassword(); 
        }
        // connect to the queue manage and queue
        MQQueueManager qMgr = new MQQueueManager(mqSeriesProperties.getQManager());
        int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
        // if the message has an QueueName defined use that otherwise MQSERIES default.
        if (baseLineMessage.getQueueName().length() > 0){
          queueName = baseLineMessage.getQueueName();
        } else {
          queueName = mqSeriesProperties.getQueueName();
        } 
        MQQueue outQueue = qMgr.accessQueue(queueName, openOptions);
        // extract the baselinemessage data to a variable
        responseMsg = baseLineMessage.getCdata();
        // loop through all variables
        for (int vCount = 0; vCount < coreProperties.getVariables().size(); vCount++) {
          Variable variable =  (Variable) coreProperties.getVariables().get(vCount);
          CharSequence seq = "%" + variable.getName() + "%";
          // if the variable exists in the baselinemessage then process it. 
          if (responseMsg.contains(seq)){
            variableValue = processMessage(variable,responseMsg);
            responseMsg = responseMsg.replaceAll(seq.toString(), variableValue); 
          }
        }
        // write the message
        MQMessage msg = new MQMessage();
        // generate a guid to use as the correlation ID
        String corrId = processSessionType("12");
        msg.correlationId = corrId.getBytes();
        msg.writeString(responseMsg);
        // Specify the default put message options 
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        outQueue.put(msg, pmo);
        if (logger.isInfoEnabled()){
          logger.info(sdf.format(new Date()) 
                        + " SEND: T" + threadName + " -"
                        + " CORR: " + corrId + " -"
                        + " MSG: " + baseLineMessage.getName() + " -"
                        + " Queue: " + queueName + " - " 
                        + responseMsg.replaceAll("[\r\n]", ""));
        }
        // Close the queue 
        outQueue.close();
        
        // delay an amount of time
        /* we have all the required details to write a response, so it is here that we 
         * insert the delay in responding as per  baselinemessage config.
         */
        RandomNumberGenerator generator = new RandomNumberGenerator(waitDistribution, minWait, maxWait);
        waitTime = generator.randomDouble();
        long longWaitTime;
        try {
          longWaitTime = Double.valueOf(waitTime * 1000).longValue();
          Thread.sleep(longWaitTime);
        } catch (InterruptedException e) {
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("mqDriverWorker: " + formattedDate + " error in thread sleep : " + e);
          e.printStackTrace();
        }
      } catch (Exception e) {
        logger.error("mqDriver: Unable to connect to queue " 
                       + ". " + e);
        e.printStackTrace();
        // exit on fail to connect to MQ server.
        System.exit(1);
      }
    }
  }
  
  private String processMessage(Variable variable, String responseMsg) {
    
    String variableValue = "variable value not found for " + variable.getName() + ":" + variable.getType(); 
    if (variable.getType().equals(TIMESTAMP_TYPE)){
      variableValue = processTimestampType(variable);
    } else if (variable.getType().equals(RANDOM_LONG_TYPE)){
      variableValue = processRandomLongType(variable);
    } else if (variable.getType().equals(GUID_TYPE)){
      variableValue = processGuidType();
    } else if (variable.getType().equals(SESSION_TYPE)){
      variableValue = processSessionType(variable);
    } else if (variable.getType().equals(NUMBER_TYPE)){
      variableValue = processNumberType(variable);
    } else if (variable.getType().equals(STRING_TYPE)){
      variableValue = processStringType(variable);
    } else if (variable.getType().equals(FILE_READ_TYPE)){
      variableValue = processFileReadType(variable);
    }  
    return variableValue;
  }
  
//  public String processDelimitedType(String httpLine, Variable variable){
//    
//    String variableValue = null;
//    try {
//      String[] rightOf = httpLine.split(variable.getRightOf());
//      String[] leftOf = rightOf[1].split(variable.getLeftOf());
//      variableValue = leftOf[0];
//    } catch(Exception e){
//      if (variable.getDefaultValue().length() > 0){
//        logger.info("httpStubWorker : delimited variable : " + variable.getName() + " not found with: " 
//                      + variable.getRightOf() + " : " + variable.getLeftOf() 
//                      + " default value : " + variable.getDefaultValue() + " used.");
//        variableValue = variable.getDefaultValue();
//      } else {
//        logger.error("httpStubWorker : delimited variable : " + variable.getName() + " not found with: " 
//                       + variable.getRightOf() + " : " + variable.getLeftOf() + ". " + e); 
//        variableValue = variable.getName() + " not found"; // avoid null pointer errors
//      }
//    }
//    return variableValue;
//  }
  
  public String processTimestampType(Variable variable){
    
    String variableValue = null;
    Calendar cal = Calendar.getInstance();
    /*
     * * apply a time offset if required
     */
    if (variable.getOffset() > 0){
      cal.add(Calendar.SECOND, variable.getOffset());
    } 
    SimpleDateFormat sdf = new SimpleDateFormat(variable.getFormat());
    variableValue = sdf.format(cal.getTime());
    return variableValue;
  } 
  
  public String processRandomLongType(Variable variable){
    
    String variableValue = null;
    Random random = new Random();
    long randMin = (long) variable.getRandMin(); 
    long randMax = (long) variable.getRandMax();
    long range = (long)randMax - (long)randMin + 1;
    long fraction = (long)(range * random.nextDouble());
    int randomNumber =  (int)(fraction + randMin);
    /*
     * apply the printf format if any
     */
    if (variable.getFormat().length() == 0) {
      variableValue = Double.valueOf(randomNumber).toString();
    } else {
      variableValue = Printf.format(variable.getFormat(), new Object[] {Double.valueOf(randomNumber)});
    }
    return variableValue;
  } 
  
  public String processNumberType(Variable variable)
  {
    
    String variableValue = variable.getNumberValue();
    double numberValue = Double.parseDouble(variableValue);
    
    if (variable.getFormat().length() == 0) {
      variableValue = Double.valueOf(numberValue).toString();
    } else {
      variableValue = Printf.format(variable.getFormat(), new Object[] {Double.valueOf(numberValue)});
    }
    /*
     * update the variable counter file
     */
    variable.updateNumberValue(variable.getFileName(),variableValue, variable.getIncrement());
    
    return variableValue;
  } 
  
//  public String processPositionalType(String httpLine, Variable variable){
//    
//    String variableValue = null;
//    
//    try {
//      int startPos = variable.getStartPosition();
//      int strLength = startPos + variable.getLength();
//      variableValue = httpLine.substring(startPos, strLength);
//      
//    } catch( Exception e ) {
//      if (variable.getDefaultValue().length() > 0){
//        variableValue = variable.getDefaultValue();
//        logger.info("httpStubWorker: positional variable " + variable.getName() 
//                      + " not found at: " + variable.getStartPosition() + " default value used.");
//      } else {
//        logger.error("httpStubWorker: positional variable " + variable.getName() 
//                       + " not found at: " + variable.getStartPosition() + ". " + e);
//        variableValue = variable.getName() + " not found"; // avoid null pointer errors
//      }
//    } 
//    return variableValue;
//  }
  
  public String processGuidType(){
    String variableValue = null;
    UUID idOne = UUID.randomUUID();
    variableValue = String.valueOf(idOne);
    return variableValue;
  }
  
  // 2.2
  public String processSessionType(Variable variable){
    String variableValue = null;
    int sessionLength = Integer.parseInt(variable.getSessionLength());
    
    try {
      SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
      String randomNum = new Integer(prng.nextInt()).toString();
      
      MessageDigest sha = MessageDigest.getInstance("SHA-1");
      byte[] result =  sha.digest(randomNum.getBytes());
      
      StringBuilder sessionResult = new StringBuilder();
      
      if (sessionLength == 0 ) {
        sessionLength = result.length;
      } else if (result.length < sessionLength){
        sessionLength = result.length;
      }
      
      char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
      for (int idx = 0; idx < sessionLength; ++idx) {
        byte b = result[idx];
        sessionResult.append(digits[ (b&0xf0) >> 4 ]);
        sessionResult.append(digits[ b&0x0f]);
      }
      
      variableValue = sessionResult.toString();
      
    }
    catch (NoSuchAlgorithmException ex) {
      System.err.println(ex);
    }    
    return variableValue;
  }
  
  public String processSessionType(String length){
    String variableValue = null;
    int sessionLength = Integer.parseInt(length);
 
    try {
      SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
      String randomNum = new Integer(prng.nextInt()).toString();
      
      MessageDigest sha = MessageDigest.getInstance("SHA-1");
      byte[] result =  sha.digest(randomNum.getBytes());
      
      StringBuilder sessionResult = new StringBuilder();
        
      if (sessionLength == 0 ) {
        sessionLength = result.length;
      } else if (result.length < sessionLength){
        sessionLength = result.length;
      }
      
      char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
      for (int idx = 0; idx < sessionLength; ++idx) {
        byte b = result[idx];
        sessionResult.append(digits[ (b&0xf0) >> 4 ]);
        sessionResult.append(digits[ b&0x0f]);
      }
      
      variableValue = sessionResult.toString();
      
    }
    catch (NoSuchAlgorithmException ex) {
      System.err.println(ex);
    }    
    return variableValue;
  }
  
  
  
  public String processStringType(Variable variable){
    String variableValue = variable.getValue();
    return variableValue;
  }
  
  public String processFileReadType(Variable variable){
    String variableValue = "not found.";
    String arrayValue="";
    /*
     * if the config calls for a random entry then call the random function
     */
    if (variable.getAccessType().toUpperCase().equals("RANDOM")){
      variableValue = variable.getVariableFileArray().getRandomValue();
    } else { // next in file
      variableValue = variable.getVariableFileArray().getNextValue();
    }
    return variableValue;
  }
  
  
}
