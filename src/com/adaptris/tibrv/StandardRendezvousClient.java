/*
 * $RCSfile$
 * $Revision$
 * $Date$
 * $Author$
 */
package com.adaptris.tibrv;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * <p>
 * Implementation of <code>RendezvousClient</code> which provides reliable
 * message delivery.
 * </p>
 */
public class StandardRendezvousClient extends RendezvousClientImp {
  
  private TibrvListener listener;

  /** 
   * <p><i>
   * Note - this does look a bit odd - creating the listener but not either 
   * returning it or retaining a reference to it.  The 
   * <code>TibrvTransport</code> retains a reference to it, as it (the 
   * listener) is passed in to <code>onTibrvMsg</code>. Also note closure is not
   * exposed for now. 
   * </i></p>
   * @see com.adaptris.tibrv.RendezvousClient#createMessageListener
   *   (com.tibco.tibrv.TibrvMsgCallback, java.lang.String) */
  public void createMessageListener
    (TibrvMsgCallback callback, String sendSubject) throws TibrvException {
    
    listener = new TibrvListener
      (this.queue, callback, this.transport, sendSubject, null);
  }
  
  /** @see com.adaptris.tibrv.RendezvousClient
   *   #createConfirmationListener(com.tibco.tibrv.TibrvMsgCallback) */
  public void createConfirmationListener
  (TibrvMsgCallback callback) throws TibrvException {
    
    // do nothing
  }
  
  /** @see com.adaptris.tibrv.RendezvousClientImp#close() */
  public void close() {
    super.close();
    
    if (this.listener != null) {
      this.listener.destroy();
    }
  }
}
