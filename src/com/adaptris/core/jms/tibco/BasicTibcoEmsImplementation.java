package com.adaptris.core.jms.tibco;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.core.jms.JmsConnectionConfig;
import com.adaptris.core.jms.VendorImplementationImp;
import com.tibco.tibjms.TibjmsQueueConnectionFactory;

/**
 * <p>
 * <code>BasicTibcoEmsImplementation</code>
 * </p>
 *
 * @author Aaron - 5 Oct 2012
 * @version
 *
 * <p>
 * In the adapter configuration file this class is aliased as <b>BasicTibcoEmsImplementation</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
public class BasicTibcoEmsImplementation extends VendorImplementationImp {

	@Override
	public ConnectionFactory createConnectionFactory(JmsConnectionConfig cfg) throws JMSException {
    return createConnectionFactory(cfg.configuredBrokerUrl());
	}

	protected ConnectionFactory createConnectionFactory(String serverUrl) {
		return new TibjmsQueueConnectionFactory(serverUrl);
	}
}
