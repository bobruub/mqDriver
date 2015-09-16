package com.mqDriver.core;

/**
 class: BaseLineMessage
 Purpose: setup the baseline respopnse message.
 Notes:
 Author: Tim Lane
 Date: 25/03/2014
 
 **/

import java.util.List;

public class BaseLineMessage {
  
  private String  Name;
  private String  Cdata;
  private String maxWait;
  private String minWait;
  private String volume;
  private String waitDistribution;
  private String queueName;
  
  
  public static BaseLineMessage findInList(List templateList, String templateName)
  {
    for (Object aTemplateList : templateList)
    {
      BaseLineMessage tmp = (BaseLineMessage) aTemplateList;
      if (tmp.getName().equals(templateName))
      {
        return tmp;
      }
    }
    return null;
  }
  
  public String getMaxWait() {
    return this.maxWait;
  }
  
  public void setMaxWait(String maxWait) {
    this.maxWait = maxWait;
  }
  
  public String getMinWait() {
    return this.minWait;
  }
  
  public void setMinWait(String minWait) {
    this.minWait = minWait;
  }
  
  public String getVolume() {
    return this.volume;
  }
  
  public void setVolume(String volume) {
    this.volume = volume;
  }
  
  public String getWaitDistribution() {
    return this.waitDistribution;
  }
  
  public void setWaitDistribution(String waitDistribution) {
    this.waitDistribution = waitDistribution;
  }
  public String getName() {
    return this.Name;
  }
  
  public void setName(String name) {
    this.Name = name;
  }
  
  public String getCdata() {
    return this.Cdata;
  }
  
  public void setCdata(String cdata) {
    this.Cdata = cdata;
  }
  
   public void setQueueName(String queueName) {
    this.queueName = queueName;
  }
  
  public String getQueueName() {
    return this.queueName;
  }
}
