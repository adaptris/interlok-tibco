package com.adaptris.core.jms.tibco;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.core.jms.UrlVendorImplementation;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibjms.TibjmsQueueConnectionFactory;

/**
 * <p>
 * <code>BasicTibcoEmsImplementation</code>
 * </p>
 *
 * @author Aaron - 5 Oct 2012
 */
@XStreamAlias("basic-tibco-ems-implementation")
public class BasicTibcoEmsImplementation extends UrlVendorImplementation {

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    return createConnectionFactory(getBrokerUrl());
  }

  protected ConnectionFactory createConnectionFactory(String serverUrl) {
    return new TibjmsQueueConnectionFactory(serverUrl);
  }

}
