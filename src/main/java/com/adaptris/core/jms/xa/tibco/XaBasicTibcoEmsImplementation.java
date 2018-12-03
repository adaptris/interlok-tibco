package com.adaptris.core.jms.xa.tibco;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;

import com.adaptris.core.jms.tibco.BasicTibcoEmsImplementation;
import com.adaptris.xa.jms.XAVendorImplementation;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibjms.TibjmsXAConnectionFactory;

/**
 * <p>
 * <code>XaBasicTibcoEmsImplementation</code>
 * </p>
 * 
 * @config xa-basic-tibco-ems-implementation
 * @license BASIC
 * 
 */
@XStreamAlias("xa-basic-tibco-ems-implementation")
public class XaBasicTibcoEmsImplementation extends BasicTibcoEmsImplementation implements XAVendorImplementation {

  @Override
  public XAConnectionFactory createXAConnectionFactory() throws JMSException {
    return new TibjmsXAConnectionFactory(getBrokerUrl());
  }

  @Override
  public XASession createXASession(XAConnection connection) throws JMSException {
    log.info("Creating a new XA session.");
    XASession session = ((XAConnection) connection).createXASession();
    applyVendorSessionProperties(session);
    return session;
  }
}