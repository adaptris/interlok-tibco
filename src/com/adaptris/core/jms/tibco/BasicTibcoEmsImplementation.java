package com.adaptris.core.jms.tibco;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.core.jms.UrlVendorImplementation;
import com.tibco.tibjms.TibjmsQueueConnectionFactory;

/**
 * <p>
 * <code>BasicTibcoEmsImplementation</code>
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>BasicTibcoEmsImplementation</b> which is the preferred alternative
 * to the fully qualified classname when building your configuration.
 * </p>
 * 
 * @author Aaron - 5 Oct 2012
 */
public class BasicTibcoEmsImplementation extends UrlVendorImplementation {

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    return createConnectionFactory(getBrokerUrl());
  }

  protected ConnectionFactory createConnectionFactory(String serverUrl) {
    return new TibjmsQueueConnectionFactory(serverUrl);
  }
}
