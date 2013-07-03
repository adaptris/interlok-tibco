/*
 * $RCSfile: RendezvousTranslator.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/08/13 13:47:27 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageTranslator;
import com.tibco.tibrv.TibrvMsg;

/**
 * <p>
 * Translate between <code>AdaptrisMessage</code>s and <code>TibrvMsg</code>s.
 * </p>
 */
public interface RendezvousTranslator extends AdaptrisMessageTranslator {

  /**
   * <p>
   * Create a new <code>TibrvMsg</code> based on the passed
   * <code>AdaptrisMessage</code> and <code>sendSubject</code>.
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @param sendSubject the send subject to use
   * @return a new <code>TibrvMsg</code>
   * @throws Exception if any occur
   */
  TibrvMsg translate(AdaptrisMessage msg, String sendSubject) throws Exception;

  /**
   * <p>
   * Create a new <code>AdaptrisMessage</code> based on the passed
   * <code>TibrvMsg</code>.
   * </p>
   * 
   * @param tibrvMsg the <code>TibrvMsg</code> to translate
   * @return a new <code>AdaptrisMessage</code>
   * @throws Exception if any occur
   */
  AdaptrisMessage translate(TibrvMsg tibrvMsg) throws Exception;
}
