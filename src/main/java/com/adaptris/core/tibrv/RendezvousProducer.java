/*
 * $RCSfile: RendezvousProducer.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/08/13 13:47:27 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.Args;
import com.adaptris.tibrv.RendezvousClient;
import com.adaptris.tibrv.StandardRendezvousClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
@AdapterComponent
@ComponentProfile(summary = "Send messages to Tibco Rendezvous", tag = "producer,tibco", recommended = {NullConnection.class})
@DisplayOrder(order = {"subject", "rendezvousClient", "rendezvousTranslator"})
public class RendezvousProducer extends ProduceOnlyProducerImp
 implements TibrvMsgCallback, LicensedComponent {

  @NotNull
  @Valid
  @AutoPopulated
  @NonNull
  @Getter
  @Setter
  private RendezvousClient rendezvousClient;
  @NotNull
  @AutoPopulated
  @Valid
  @NonNull
  @Getter
  @Setter
  private RendezvousTranslator rendezvousTranslator;

  /**
   * The destination is the Tibrv Subject
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'subject' instead")
  private ProduceDestination destination;

  /**
   * The Subject
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String subject;

  private transient boolean destWarning;

  public RendezvousProducer() {
    setRendezvousClient(new StandardRendezvousClient());
    setRendezvousTranslator(new StandardRendezvousTranslator());
  }

  @Override
  public final void prepare() throws CoreException {
    DestinationHelper.logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'subject' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getSubject(), getDestination());
    Args.notNull(getRendezvousClient(), "rendezvousClient");
    Args.notNull(getRendezvousTranslator(), "rendezvousTranslator");
    LicenseChecker.newChecker().checkLicense(this);
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Enterprise);
  }


  @Override
  public void init() throws CoreException {
    try {
      getRendezvousClient().init();
      getRendezvousTranslator()
          .registerMessageFactory(AdaptrisMessageFactory.defaultIfNull(getMessageFactory()));
      getRendezvousClient().createConfirmationListener(this);
    }
    catch (TibrvException e) {
      throw new CoreException(e);
    }
  }


  @Override
  public void start() throws CoreException {
    try {
      getRendezvousClient().start();
    }
    catch (TibrvException e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void stop() {
    getRendezvousClient().stop();
  }

  @Override
  public void close() {
    getRendezvousClient().close();
  }

  @Override
  public void onMsg(TibrvListener listner, TibrvMsg tibrvMsg) {
    try {
      log.debug("received conf [{}]", tibrvMsg.get("seqno"));
    }
    catch (Exception e) {
      log.warn("", e);
    }
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    try {
      rendezvousClient.send(getRendezvousTranslator().translate(msg, endpoint));
      log.debug("message [{}] sent", msg.getUniqueId());
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getSubject(), getDestination(), msg);

  }
}
