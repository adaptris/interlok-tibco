/*
 * $RCSfile: RendezvousProducer.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/08/13 13:47:27 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
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
 * Implementation of <code>AdaptrisMessageProducee</code> which handles Tibco Rendezvous messages.
 * </p>
 * <p>
 * Implements <code>TibrvMsgCallback</code> to handle confirmation messages which are received if this class is used in conjunction
 * with <code>CertifiedRendezvousClient</code>.
 * </p>
 * 
 * @config tibrv-rendezvous-producer
 * @license ENTERPRISE
 */
@XStreamAlias("tibrv-rendezvous-producer")
public class RendezvousProducer extends ProduceOnlyProducerImp
  implements TibrvMsgCallback {

  // persistent
  @NotNull
  @AutoPopulated
  @Valid
  private RendezvousClient rendezvousClient;
  @NotNull
  @AutoPopulated
  @Valid
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
  public RendezvousProducer() {
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
      rendezvousTranslator.registerMessageFactory(AdaptrisMessageFactory.defaultIfNull(getMessageFactory()));
      rendezvousClient.createConfirmationListener(this);
    }
    catch (TibrvException e) {
      throw new CoreException(e);
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
    rendezvousClient.stop();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    rendezvousClient.close();
  }

  /** @see com.tibco.tibrv.TibrvMsgCallback#onMsg
   *   (com.tibco.tibrv.TibrvListener, com.tibco.tibrv.TibrvMsg) */
  @Override
  public void onMsg(TibrvListener listner, TibrvMsg tibrvMsg) {
    try {
      log.debug("received conf [" + tibrvMsg.get("seqno") + "]");
    }
    catch (NullPointerException e){
      log.warn("", e);
    }
    catch (TibrvException e) {
      log.warn("", e);
    }
  }

  /** @see com.adaptris.core.AdaptrisMessageProducerImp#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" ");
    result.append(getRendezvousClient());
    result.append(" ");
    result.append(getRendezvousTranslator());

    return result.toString();
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer
   *   #produce(com.adaptris.core.AdaptrisMessage,
   *     com.adaptris.core.ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.tibrv.TimingLogger")
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
    throws ProduceException {
    try {
      rendezvousClient.send(getRendezvousTranslator().translate(msg,
          destination.getDestination(msg)));

      log.debug("message [" + msg.getUniqueId() + "] sent");
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
  }



//  public AdaptrisMessage request(AdaptrisMessage msg,
//    ProduceDestination destination, long timeout) throws ProduceException {
//
//    throw new UnsupportedOperationException();
//
//    // create inbox for reply...
//
//    // set on message...
//
//    // block for reply for timeout
//
//    // return reply or null if no reply
//
//    // destroy inbox tba
//
//  }

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
