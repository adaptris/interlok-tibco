/*
 * $RCSfile$
 * $Revision$
 * $Date$
 * $Author$
 */
package com.adaptris.tibrv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvDispatcher;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvdTransport;

/**
 * <p>
 * Partial implementation of <code>RendezvousClient</code>.
 * </p>
 */
public abstract class RendezvousClientImp implements RendezvousClient {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  
  // persistent
  private String service;
  private String network;
  private String daemon;
  private String queueName;
  // queue limit policy / amount / max events / priority not exposed
  
  // transient
  protected transient TibrvRvdTransport transport;
  protected transient TibrvQueue queue;
  private transient TibrvDispatcher dispatcher;
  
  /** @see com.adaptris.tibrv.RendezvousClient#init() */
  public void init() throws TibrvException {
    Tibrv.open(Tibrv.IMPL_NATIVE);

    transport = new TibrvRvdTransport
      (this.getService(), this.getNetwork(), this.getDaemon());

    this.initEventQueue();
  }
  
  /**
   * <p>
   * Initialise the event queue.  If an <code>eventQueueName</code> is set then
   * a queue of this name is created, otherwise the default queue is used.
   * </p>
   * @throws TibrvException if any occur
   */
  private void initEventQueue() throws TibrvException {
    if (this.getQueueName() != null || "".equals(this.getQueueName())) {
      queue = new TibrvQueue();
      queue.setName(this.getQueueName());
    }
    else {
      queue = Tibrv.defaultQueue();
    }
  }
  
  /** @see com.adaptris.tibrv.RendezvousClient#start() */
  public void start() {
    dispatcher = new TibrvDispatcher(this.queue);
  }
  
  /** @see com.adaptris.tibrv.RendezvousClient#stop() */
  public void stop() {
    if (this.dispatcher != null) {
      this.dispatcher.destroy();
    }
  }
  
  /** @see com.adaptris.tibrv.RendezvousClient#close() */
  public void close() {
    if (this.transport != null) {
      this.transport.destroy();
    }
    if (this.queueName != null) { // only close if not default
      if (this.queue != null) {
        this.queue.destroy();
      }
    }
  }
  
  /** @see com.adaptris.tibrv.RendezvousClient#send(com.tibco.tibrv.TibrvMsg) */
  public void send(TibrvMsg tibrvMsg) throws TibrvException {
    this.transport.send(tibrvMsg);
  }
  
  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    
    result.append(" service [");
    result.append(this.getService());
    result.append("] network [");
    result.append(this.getNetwork());
    result.append("] daemon [");
    result.append(this.getDaemon());
    result.append("] queue name [");
    result.append(this.getQueueName());
    result.append("]");
    
    return result.toString();
  }
  
  // properties...

  /**
   * <p>
   * Returns the <code>rvd</code> daemon to connect to.  If null a local 
   * <code>rvd</code> daemon will be used.
   * </p>
   * @return the <code>rvd</code> daemon to connect to
   */
  public String getDaemon() {
    return this.daemon;
  }

  /**
   * <p>
   * Sets the <code>rvd</code> daemon to connect to.  If null a local 
   * <code>rvd</code> daemon will be used.
   * </p>
   * @param s the dameon to connect to
   */
  public void setDaemon(String s) {
    this.daemon = s;
  }

  /**
   * <p>
   * Returns the network interface to use.  If null  the default network 
   * interface will be used.
   * </p>
   * @return the network interface to use
   */
  public String getNetwork() {
    return this.network;
  }

  /**
   * <p>
   * Sets the network interface to use.  If null  the default network 
   * interface will be used.
   * </p>
   * @param s the network interface to use
   */
  public void setNetwork(String s) {
    this.network = s;
  }

  /**
   * <p>
   * Returns the service to use.  If null the default service will be used.
   * </p>
   * @return the service to use
   */
  public String getService() {
    return this.service;
  }

  /**
   * <p>
   * Sets the service to use.  If null the default service will be used.
   * </p>
   * @param s the service to use
   */
  public void setService(String s) {
    this.service = s;
  }

  /**
   * <p>
   * Returns the name of the <code>TibrvQueue</code> to use for events.  If null
   * the default queue will be used.  NB this is a system queue for events and 
   * not related to message destinations.
   * </p>
   * @return the name of the <code>TibrvQueue</code> to use for events
   */
  public String getQueueName() {
    return this.queueName;
  }

  /**
   * <p>
   * Sets the name of the <code>TibrvQueue</code> to use for events.  If null
   * the default queue will be used.  NB this is a system queue for events and 
   * not related to message destinations.
   * </p>
   * @param s the name of the <code>TibrvQueue</code> to use for events
   */
  public void setQueueName(String s) {
    this.queueName = s;
  }
}
