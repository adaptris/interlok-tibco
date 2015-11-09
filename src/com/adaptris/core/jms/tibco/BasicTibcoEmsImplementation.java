package com.adaptris.core.jms.tibco;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.core.CoreException;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibjms.TibjmsQueueConnectionFactory;

/**
 * <p>
 * <code>BasicTibcoEmsImplementation</code>
 * </p>
 * 
 * @config basic-tibco-ems-implementation
 * @license BASIC
 * 
 * @author Aaron - 5 Oct 2012
 */
@XStreamAlias("basic-tibco-ems-implementation")
public class BasicTibcoEmsImplementation extends UrlVendorImplementation implements LicensedComponent {

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    return createConnectionFactory(getBrokerUrl());
  }

  protected ConnectionFactory createConnectionFactory(String serverUrl) {
    return new TibjmsQueueConnectionFactory(serverUrl);
  }

  @Override
  public void prepare() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
    super.prepare();
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Basic);
  }
}
