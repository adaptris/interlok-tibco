/*
 * $RCSfile: RendezvousConsumer.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/03/27 12:19:28 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.tibrv.RendezvousClient;
import com.adaptris.tibrv.StandardRendezvousClient;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * <p>
 * Implementation of <code>AdaptrisMessageConsumer</code> which handles Tibco Rendezvous messages.
 * </p>
 * <p>
 * Requires an Enterprise license
 * </p>
 * 
 * <p>
 * In the adapter configuration file this class is aliased as <b>tibrv-rendezvous-consumer</b> which is the preferred alternative to
 * the fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("tibrv-rendezvous-consumer")
public class RendezvousConsumer extends AdaptrisMessageConsumerImp
  implements TibrvMsgCallback {

  // persistent
  private RendezvousClient rendezvousClient;
  private RendezvousTranslator rendezvousTranslator;

  /**
   * <p>
   * Creates a new instance. Defaults to new
   * <code>StandardRendezvousClient</code> and
   * <code>StandardRendezvousTranslator</code>.
   * </p>
   * @see StandardRendezvousClient
   * @see StandardRendezvousTranslator
   */
  public RendezvousConsumer() {
    setRendezvousClient(new StandardRendezvousClient());
    setRendezvousTranslator(new StandardRendezvousTranslator());
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Enterprise);
  }


  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    try {
      rendezvousClient.init();
      getRendezvousTranslator().registerMessageFactory(AdaptrisMessageFactory.defaultIfNull(getMessageFactory()));
      rendezvousClient.createMessageListener
        (this, getDestination().getDestination());
    }
    catch (TibrvException e) {
      throw new CoreException(e);
    }
  }

  /** @see com.tibco.tibrv.TibrvMsgCallback
   *   #onMsg(com.tibco.tibrv.TibrvListener, com.tibco.tibrv.TibrvMsg) */
  @Override
  public void onMsg(TibrvListener listener, TibrvMsg tibrvMsg) {
    renameThread();

    try {
      long start = System.currentTimeMillis();

      retrieveAdaptrisMessageListener().onAdaptrisMessage
        (getRendezvousTranslator().translate(tibrvMsg));

      log.trace("time to process 1 message ["
        + (System.currentTimeMillis() - start) + "] ms");
    }
    catch (Exception e) {
      log.error("exception receiving message" + e);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    try {
      rendezvousClient.start();
    }
    catch (TibrvException e) {
      throw new CoreException(e);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    getRendezvousClient().stop();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    getRendezvousClient().close();
  }

  /** @see com.adaptris.core.AdaptrisMessageConsumerImp#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());

    result.append(" ");
    result.append(getRendezvousClient());
    result.append(" ");
    result.append(getRendezvousTranslator());

    return result.toString();
  }

  // properties...

  /**
   * <p>
   * Returns the <code>RendezvousTranslator</code> to use.
   * </p>
   * @return the <code>RendezvousTranslator</code> to use
   */
  public RendezvousTranslator getRendezvousTranslator() {
    return rendezvousTranslator;
  }

  /**
   * <p>
   * Sets the the <code>RendezvousTranslator</code> to use.
   * </p>
   * @param r the <code>RendezvousTranslator</code> to use
   */
  public void setRendezvousTranslator(RendezvousTranslator r) {
    if (r == null) {
      throw new IllegalArgumentException("null param");
    }
    rendezvousTranslator = r;
  }

  /**
   * <p>
   * Returns the <code>RendezvousClient</code> to use.
   * </p>
   * @return the <code>RendezvousClient</code> to use
   */
  public RendezvousClient getRendezvousClient() {
    return rendezvousClient;
  }

  /**
   * <p>
   * Sets the <code>RendezvousClient</code> to use.
   * </p>
   * @param r the <code>RendezvousClient</code> to use
   */
  public void setRendezvousClient(RendezvousClient r) {
    if (r == null) {
      throw new IllegalArgumentException("null param");
    }
    rendezvousClient = r;
  }
}
