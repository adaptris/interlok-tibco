/*
 * $RCSfile$
 * $Revision$
 * $Date$
 * $Author$
 */
package com.adaptris.tibrv;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * <p>
 * Interface to <i>Tibco Rendezvous</i>.  Implementations of this class may not
 * be thread safe.
 * </p>
 */
public interface RendezvousClient {
  
  /**
   * <p>
   * Creates a <code>TibrvListener</code> using the passed <code>callback</code>
   * and <code>sendSubject</code>.  Not part of <code>init</code> as only 
   * required by client code which consumes messages.
   * </p>
   * @param callback the <code>TibrvMsgCallback</code> (some sort of 
   * consumer)
   * @param sendSubject the send subject, conforming to TibcoRendezvous rules
   * @throws TibrvException if any occur
   */
  void createMessageListener(TibrvMsgCallback callback, String sendSubject)
    throws TibrvException;
  
  /**
   * <p>
   * Creates a <code>TibrvListener</code> for confirmation messages, which
   * may be useful for debugging / troubleshooting.
   * </p>
   * @param callback the <code>TibrvMsgCallback</code> to use
   * @throws TibrvException if any occur
   */
  void createConfirmationListener(TibrvMsgCallback callback) 
    throws TibrvException;
  
  /**
   * <p>
   * Sends the passed <code>TibrvMsg</code>.
   * </p>
   * @param tibrvMsg the message to send
   * @throws TibrvException if any occur
   */
  void send(TibrvMsg tibrvMsg) throws TibrvException;
  
  /**
   * <p>
   * Initialise this instance.
   * </p>
   * @throws TibrvException if any occur
   */
  void init() throws TibrvException;

  /**
   * <p>
   * Start message delivery.
   * </p>
   * @throws TibrvException if any occur
   */
  void start() throws TibrvException;

  /**
   * <p>
   * Stop message delivery.
   * </p>
   */
  void stop();

  /**
   * <p>
   * Close this instance.
   * </p>
   */
  void close();
}
