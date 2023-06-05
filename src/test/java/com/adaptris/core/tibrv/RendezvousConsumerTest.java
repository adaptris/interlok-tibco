package com.adaptris.core.tibrv;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.ExampleConsumerCase;
import com.adaptris.interlok.util.Closer;
import com.adaptris.tibrv.RendezvousClient;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;

public class RendezvousConsumerTest extends ExampleConsumerCase {

  private static final String BASE_DIR_KEY = "TibrvConsumerExamples.baseDir";
  private static String CHAR_ENC_KEY = "char-enc";
  private static String CHAR_ENC_VAL = Charset.defaultCharset().name();
  private static String PAYLOAD_KEY = "payload";
  private static String PAYLOAD_VAL = "payload-val";
  private static String UNIQUE_ID_KEY = "unique-id";
  private static String UNIQUE_ID_VAL = "unique-id-val";
  private static String METADATA_KEY = "metadata";
  private static String DESTINATION = "over-there";

  private TibrvMsg tibrvMsg;
  @Mock
  private TibrvListener listener;
  @Mock
  private TibrvException exception;
  @Mock
  private AdaptrisMessageListener adaptrisListener;
  @Mock
  private AdaptrisMessage adaptrisMsg;

  private RendezvousTranslator translatorSpy;
  private RendezvousClient clientSpy;
  private RendezvousConsumer consumer;

  private AutoCloseable openMocks;

  public RendezvousConsumerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    consumer = new RendezvousConsumer();

    consumer.registerAdaptrisMessageListener(adaptrisListener);
    consumer.setSubject(DESTINATION);
    translatorSpy = spy(consumer.getRendezvousTranslator());
    consumer.setRendezvousTranslator(translatorSpy);
    clientSpy = spy(consumer.getRendezvousClient());
    consumer.setRendezvousClient(clientSpy);

    // Some things are easier NOT mocked...
    tibrvMsg = new TibrvMsg();
    tibrvMsg.setSendSubject("subject");
    tibrvMsg.add(PAYLOAD_KEY, PAYLOAD_VAL.getBytes(), TibrvMsg.OPAQUE);
    tibrvMsg.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL, TibrvMsg.STRING);
    tibrvMsg.add(CHAR_ENC_KEY, CHAR_ENC_VAL, TibrvMsg.STRING);

    TibrvMsg metadata = new TibrvMsg();
    metadata.add("key", "val", TibrvMsg.STRING);
    metadata.add("key2", "val", TibrvMsg.STRING);

    tibrvMsg.add(METADATA_KEY, metadata, TibrvMsg.MSG);
  }

  @AfterEach
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testInit() throws Exception {
    doNothing().when(clientSpy).init();
    doNothing().when(clientSpy).createMessageListener(consumer, DESTINATION);

    LifecycleHelper.init(consumer);

    verify(clientSpy).init();
    verify(clientSpy).createMessageListener(consumer, DESTINATION);

    doThrow(exception).when(clientSpy).init();
    consumer.changeState(ClosedState.getInstance()); // reset the state to allow for init to fire again.
    try {
      LifecycleHelper.init(consumer);
      fail("No CoreException thrown for .init() fail");
    } catch (CoreException e) {
    }
  }

  /**
   * tibrv message should be translated and then appropriate adaptrisListener
   * called
   *
   * @throws Exception
   */
  @Test
  public void testOnMsg() throws Exception {
    when(translatorSpy.translate(tibrvMsg)).thenReturn(adaptrisMsg);

    consumer.onMsg(listener, tibrvMsg);

    verify(translatorSpy, times(2)).translate(tibrvMsg);
    verify(adaptrisListener).onAdaptrisMessage(adaptrisMsg);
  }

  /**
   * Process should complete without exception
   *
   * @throws Exception
   */
  @Test
  public void testOnMsgNull() throws Exception {
    consumer.onMsg(listener, null);
  }

  @Test
  public void testStartStopClose() throws Exception {
    doNothing().when(clientSpy).start();
    doNothing().when(clientSpy).stop();
    doNothing().when(clientSpy).close();

    consumer.changeState(InitialisedState.getInstance());

    LifecycleHelper.start(consumer);
    verify(clientSpy).start();

    LifecycleHelper.stop(consumer);
    verify(clientSpy).stop();

    LifecycleHelper.close(consumer);
    verify(clientSpy).close();

    // Also need to test when TibrvException raised on .start()
    // However StandardRendezvousClient doesn't throw that exception
  }

  @Test
  public void testSetNull() throws Exception {
    try {
      consumer.setRendezvousClient(null);
      fail("no error for null RendezvousClient");
    } catch (Exception e) {
    }
    try {
      consumer.setRendezvousTranslator(null);
      fail("no error for null RendezvousTranslator");
    } catch (Exception e) {
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    RendezvousConsumer consumer = new RendezvousConsumer();

    consumer.setSubject("subject");

    StandaloneConsumer result = new StandaloneConsumer();
    result.setConsumer(consumer);
    return result;
  }

}
