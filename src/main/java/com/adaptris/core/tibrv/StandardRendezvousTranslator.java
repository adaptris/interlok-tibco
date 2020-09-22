/*
 * $RCSfile: StandardRendezvousTranslator.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/03/20 10:49:56 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import java.util.Iterator;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageTranslator;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * <p>
 * Implementation of <code>RendezvousTranslator</code> which stores <code>AdaptrisMessage</code>'s payload against a
 * <code>TibrvMsg</code> field of type <code>TibrvMsg.OPAQUE</code>, and unique ID and char encoding against fields of type
 * <code>TibrvMsg.STRING</code>. <code>AdaptrisMessage</code> metadata is store in a child <code>TibrvMsg</code> as
 * <code>Tibrv.STRING</code>s using the metadata key as the 'name.
 * </p>
 * <p>
 * The names against which these elements are stored / expected to be found in <code>TibrvMsg</code> are configurable and defaulted
 * in the constructor.
 * </p>
 *
 * @config tibrv-standard-translator
 */
@XStreamAlias("tibrv-standard-translator")
public class StandardRendezvousTranslator implements RendezvousTranslator {

  @NotBlank
  @Valid
  private String uniqueIdName;
  @NotBlank
  @Valid
  private String payloadName;
  @NotBlank
  @Valid
  private String charEncName;
  @NotBlank
  @Valid
  private String metadataName;
  private transient AdaptrisMessageFactory messageFactoryToUse;

  /**
   * <p>
   * Creates a new instance. Default names are <code>char-enc</code>,
   * <code>metadata</code>, <code>payoad</code>, <code>unique-id</code>.
   * </p>
   */
  public StandardRendezvousTranslator() {
    setCharEncName("char-enc");
    setMetadataName("metadata");
    setPayloadName("payload");
    setUniqueIdName("unique-id");
    registerMessageFactory(new DefaultMessageFactory());
  }

  /**
   *
   * @see AdaptrisMessageTranslator#registerMessageFactory(AdaptrisMessageFactory)
   */
  @Override
  public void registerMessageFactory(AdaptrisMessageFactory f) {
    messageFactoryToUse = f;
  }

  /**
   *
   * @see AdaptrisMessageTranslator#currentMessageFactory()
   */
  @Override
  public AdaptrisMessageFactory currentMessageFactory() {
    return messageFactoryToUse;
  }

  /**
   * @see com.adaptris.core.tibrv.RendezvousTranslator
   *      #translate(com.adaptris.core.AdaptrisMessage, String)
   */
  @Override
  public TibrvMsg translate(AdaptrisMessage msg, String sendSubject)
      throws Exception {
    Args.notNull(msg, "msg");
    Args.notBlank(sendSubject, "sendSubject");
    TibrvMsg result = new TibrvMsg();
    result.setSendSubject(sendSubject);
    result.add(getUniqueIdName(), msg.getUniqueId(), TibrvMsg.STRING);

    if (msg.getPayload() != null && msg.getSize() > 0) {
      result.add(getPayloadName(), msg.getPayload(), TibrvMsg.OPAQUE);
    }

    if (msg.getContentEncoding() != null) {
      result.add(getCharEncName(), msg.getContentEncoding(), TibrvMsg.STRING);
    }

    if (msg.getMetadata().size() > 0) {
      TibrvMsg metadata = new TibrvMsg();
      Iterator<MetadataElement> itr = msg.getMetadata().iterator();

      while (itr.hasNext()) {
        MetadataElement m = itr.next();
        metadata.add(m.getKey(), m.getValue(), TibrvMsg.STRING);
      }

      result.add(getMetadataName(), metadata, TibrvMsg.MSG);
    }

    return result;
  }

  /**
   * @see com.adaptris.core.tibrv.RendezvousTranslator
   *      #translate(com.tibco.tibrv.TibrvMsg)
   */
  @Override
  public AdaptrisMessage translate(TibrvMsg tibrvMsg) throws Exception {
    if (tibrvMsg == null) {
      throw new IllegalArgumentException("null param");
    }

    AdaptrisMessage result = currentMessageFactory().newMessage();

    result.setUniqueId((String) tibrvMsg.get(getUniqueIdName()));
    result.setContentEncoding((String) tibrvMsg.get(getCharEncName()));
    result.setPayload((byte[]) tibrvMsg.get(getPayloadName()));

    if (tibrvMsg.get(getMetadataName()) != null) {
      TibrvMsg metadata = (TibrvMsg) tibrvMsg.get("metadata");

      for (int i = 0; i < metadata.getNumFields(); i++) {
        TibrvMsgField field = metadata.getFieldByIndex(i);
        result.addMetadata(field.name, (String) field.data);
      }
    }

    return result;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer(this.getClass().getName());
    result.append(" unique ID name [");
    result.append(getUniqueIdName());
    result.append("] payload name [");
    result.append(getPayloadName());
    result.append("] metadata name [");
    result.append(getMetadataName());
    result.append("] char enc name [");
    result.append(getCharEncName());
    result.append("]");

    return result.toString();
  }

  // properties...

  /**
   * <p>
   * Returns the name that (optional) character encoding details are stored
   * against in <code>TibrvMsg</code>.
   * </p>
   *
   * @return the name that (optional) character encoding details are stored
   *         against in <code>TibrvMsg</code>
   */
  public String getCharEncName() {
    return charEncName;
  }

  /**
   * <p>
   * Sets the name that (optional) character encoding details are stored against
   * in <code>TibrvMsg</code>. May not be null or empty.
   * </p>
   *
   * @param s the name that (optional) character encoding details are stored
   *          against in <code>TibrvMsg</code>
   */
  public void setCharEncName(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    charEncName = s;
  }

  /**
   * <p>
   * Returns the name that the payload is stored against in
   * <code>TibrvMsg</code>.
   * </p>
   *
   * @return the name that the payload is stored against in
   *         <code>TibrvMsg</code>
   */
  public String getPayloadName() {
    return payloadName;
  }

  /**
   * <p>
   * Sets the name that the payload is stored against in <code>TibrvMsg</code>.
   * May not be null or empty.
   * </p>
   *
   * @param s the name that the payload is stored against in
   *          <code>TibrvMsg</code>
   */
  public void setPayloadName(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    payloadName = s;
  }

  /**
   * <p>
   * Returns the name that the message unique ID is stored against in
   * <code>TibrvMsg</code>.
   * </p>
   *
   * @return the name that the message unique ID is stored against in
   *         <code>TibrvMsg</code>
   */
  public String getUniqueIdName() {
    return uniqueIdName;
  }

  /**
   * <p>
   * Sets the name that the message unique ID is stored against in
   * <code>TibrvMsg</code>. May not be null or empty.
   * </p>
   *
   * @param s the name that the message unique ID is stored against in
   *          <code>TibrvMsg</code>
   */
  public void setUniqueIdName(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    uniqueIdName = s;
  }

  /**
   * <p>
   * Returns the name that the child <code>TibrvMsg</code> containing metadata
   * is stored against in the parent <code>TibrvMsg</code>.
   * </p>
   *
   * @return the name that the child <code>TibrvMsg</code> containing metadata
   *         is stored against in the parent <code>TibrvMsg</code>
   */
  public String getMetadataName() {
    return metadataName;
  }

  /**
   * <p>
   * Sets the name that the child <code>TibrvMsg</code> containing metadata is
   * stored against in the parent <code>TibrvMsg</code>. May not be null or
   * empty.
   * </p>
   *
   * @param s the name that the child <code>TibrvMsg</code> containing metadata
   *          is stored against in the parent <code>TibrvMsg</code>
   */
  public void setMetadataName(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    metadataName = s;
  }
}
