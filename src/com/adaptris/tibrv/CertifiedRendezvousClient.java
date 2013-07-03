/*
 * $RCSfile$
 * $Revision$
 * $Date$
 * $Author$
 */
package com.adaptris.tibrv;

import com.tibco.tibrv.TibrvCmListener;
import com.tibco.tibrv.TibrvCmMsg;
import com.tibco.tibrv.TibrvCmTransport;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * <p>
 * Implementation of <code>RendezvousClient</code> which provides certified
 * message delivery.  Persistent messaging requires a ledger file name and 
 * request old to be set to true.
 * </p>
 */
public class CertifiedRendezvousClient extends RendezvousClientImp {
  
  // persistent
  private String uniqueName;
  private String confirmationSubject;
  private String ledgerFileName;
  private long deliveryTimeLimit;
  private boolean requestOld; // i.e. durable
  
  // transient
  private TibrvCmTransport cmTransport;
  private TibrvCmListener messageListener;
  private TibrvListener confirmListener;
  
  /**
   * <p>
   * Creates a new instance. Default delivery time limit is 0 (no limit).
   * </p>
   */
  public CertifiedRendezvousClient() {
    this.setDeliveryTimeLimit(0L);
  }
      
  /** @see com.adaptris.tibrv.RendezvousClient#createMessageListener
   *   (com.tibco.tibrv.TibrvMsgCallback, java.lang.String) */
  public void createMessageListener
    (TibrvMsgCallback callback, String sendSubject) throws TibrvException {
  
     messageListener = new TibrvCmListener
       (this.queue, callback, this.cmTransport, sendSubject, null);
  }

  /** @see com.adaptris.tibrv.RendezvousClient
   *   #createConfirmationListener(com.tibco.tibrv.TibrvMsgCallback) */
  public void createConfirmationListener(TibrvMsgCallback callback) 
    throws TibrvException {
    
    if (this.getConfirmationSubject() != null) {
      confirmListener = new TibrvListener
        (this.queue, callback, transport, this.getConfirmationSubject(), null);
    }
  }
  
  /** @see com.adaptris.tibrv.RendezvousClientImp
   *  #send(com.tibco.tibrv.TibrvMsg) */
  public void send(TibrvMsg tibrvMsg) throws TibrvException {
    TibrvCmMsg.setTimeLimit(tibrvMsg, this.getDeliveryTimeLimit());
    this.cmTransport.send(tibrvMsg);
  }
  
  /** @see com.adaptris.tibrv.RendezvousClientImp#init() */
  public void init() throws TibrvException {
    super.init();
    
    if (this.getUniqueName() == null || "".equals(this.getUniqueName())) {
      throw new IllegalArgumentException
        ("invalid cmUniqueName [" + this.getUniqueName() + "]");
    }
    
    cmTransport = new TibrvCmTransport(this.transport, this.getUniqueName(), 
      requestOld, this.getLedgerFileName(), true);
  }
  
  /** @see com.adaptris.tibrv.StandardRendezvousClient#close() */
  public void close() {
    if (this.messageListener != null) {
      this.messageListener.destroy();
    }
    if (this.confirmListener != null) {
      this.confirmListener.destroy();
    }
    if (this.cmTransport != null) {
      this.cmTransport.destroy();
    }
    
    super.close();
  }

  /** @see com.adaptris.tibrv.RendezvousClientImp#toString() */
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    
    result.append(" unique name [");
    result.append(this.getUniqueName());
    result.append("] confirmation subject [");
    result.append(this.getConfirmationSubject());
    result.append("] ledger file [");
    result.append(this.getLedgerFileName());
    result.append("] request old [");
    result.append(this.getRequestOld());
    result.append("] delivery time limit [");
    result.append(this.getDeliveryTimeLimit());
    result.append("]");
    
    return result.toString();
  }

  // properties

  /**
   * <p>
   * Returns the name of the underlying certified transport, which must be 
   * unique in the context of all certified transports in the system.
   * </p>
   * @return the name of the underlying certified transport
   */
  public String getUniqueName() {
    return this.uniqueName;
  }

  /**
   * <p>
   * Sets the name of the underlying certified transport, which must be 
   * unique in the context of all certified transports in the system.
   * </p>
   * @param s the name of the underlying certified transport
   */
  public void setUniqueName(String s) {
    this.uniqueName = s;
  }

  /**
   * <p>
   * If request old is true, when consumers which use this client start up
   * they will request messages which were delivered while they were not 
   * running.  This is analgous to durable subscriptions in JMS.
   * </p>
   * @return whether to request old messages or not
   */
  public boolean getRequestOld() {
    return this.requestOld;
  }

  /**
   * <p>
   * If request old is true, when consumers which use this client start up
   * they will request messages which were delivered while they were not 
   * running.  This is analgous to durable subscriptions in JMS.
   * </p>
   * @param b whether to request old messages or not
   */
  public void setRequestOld(boolean b) {
    this.requestOld = b;
  }

  /**
   * <p>
   * Returns the optional confirmation subject.  This can be used to obtain and
   * log confirmation messages which may be useful for debugging or 
   * troubleshooting.
   * </p>
   * @return the optional confirmation subject
   */
  public String getConfirmationSubject() {
    return this.confirmationSubject;
  }

  /**
   * <p>
   * Sets the optional confirmation subject.  This can be used to obtain and
   * log confirmation messages which may be useful for debugging or 
   * troubleshooting.
   * </p>
   * @param s the optional confirmation subject
   */
  public void setConfirmationSubject(String s) {
    this.confirmationSubject = s;
  }

  /**
   * <p>
   * Return the delivery time limit in seconds.  After the delivery time limit
   * the message will not be delivered.  This is analgous to time to live in
   * JMS.
   * </p>
   * @return the delivery time limit in seconds
   */
  public long getDeliveryTimeLimit() {
    return this.deliveryTimeLimit;
  }

  /**
   * <p>
   * Sets the delivery time limit in seconds.  After the delivery time limit
   * the message will not be delivered.  This is analgous to time to live in
   * JMS.
   * </p>
   * @param l delivery time limit in seconds
   */
  public void setDeliveryTimeLimit(long l) {
    if (l < 0) {
      throw new IllegalArgumentException("negative time limit");
    }
    this.deliveryTimeLimit = l;
  }

  /**
   * <p>
   * Returns the name of the file to use as a ledger file.  If no file name is 
   * specified a transient process ledger will be used.
   * </p>
   * @return the name of the file to use as a ledger file
   */
  public String getLedgerFileName() {
    return this.ledgerFileName;
  }

  /**
   * <p>
   * Sets the name of the file to use as a ledger file.  If no file name is 
   * specified a transient process ledger will be used.
   * </p>
   * @param s the name of the file to use as a ledger file
   */
  public void setLedgerFileName(String s) {
    this.ledgerFileName = s;
  }
}
