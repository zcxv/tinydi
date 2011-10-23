package com.googlecode.tinydi.test.beans;

import javax.inject.Inject;
import javax.inject.Named;

public class BeanWithInjections {
  @Inject 
  private ManagedBean bean;
  
  @Inject 
  private SingletonManagedBean singleton;
  
  @Inject 
  private SingletonManagedBean singleton2;
  
  @Inject
  @Named("alias")
  private Object aliasedManagedBean;
  
  public ManagedBean getBean() {
    return bean;
  }

  public void setBean(ManagedBean bean) {
    this.bean = bean;
  }

  public void setSingleton(SingletonManagedBean singleton) {
    this.singleton = singleton;
  }

  public SingletonManagedBean getSingleton() {
    return singleton;
  }

  public SingletonManagedBean getSingleton2() {
    return singleton2;
  }

  public void setSingleton2(SingletonManagedBean singleton2) {
    this.singleton2 = singleton2;
  }

  public Object getAliasedManagedBean() {
    return aliasedManagedBean;
  }

  public void setAliasedManagedBean(Object aliasedManagedBean) {
    this.aliasedManagedBean = aliasedManagedBean;
  }
}
