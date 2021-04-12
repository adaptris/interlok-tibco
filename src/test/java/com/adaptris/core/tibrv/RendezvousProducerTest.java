package com.adaptris.core.tibrv;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.ExampleProducerCase;
import com.adaptris.interlok.util.Closer;
import com.adaptris.tibrv.RendezvousClient;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;

public class RendezvousProducerTest extends ExampleProducerCase {

  private static String CHAR_ENC_VAL = Charset.defaultCharset().name();
  private static String PAYLOAD_VAL = "payload-val";
  private static String UNIQUE_ID_VAL = "unique-id-val";
  private static String DESTINATION = "over-there";

  @Mock
  private TibrvListener listener;
  @Mock
  private TibrvException exception;
  @Mock
  private TibrvMsg msg;
  @Mock
  private AdaptrisMessage adaptrisMsg;

  RendezvousTranslator translatorSpy;
  RendezvousClient clientSpy;
  private RendezvousProducer producer;

  private AutoCloseable openMocks;

  private static final String BASE_DIR_KEY = "TibrvProducerExamples.baseDir";

  public RendezvousProducerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    producer = new RendezvousProducer();
    translatorSpy = spy(producer.getRendezvousTranslator());
    producer.setRendezvousTranslator(translatorSpy);
    clientSpy = spy(producer.getRendezvousClient());
    producer.setRendezvousClient(clientSpy);
    producer.setSubject(DESTINATION);

    adaptrisMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_VAL);
    adaptrisMsg.setUniqueId(UNIQUE_ID_VAL);
    adaptrisMsg.setContentEncoding(CHAR_ENC_VAL);
    adaptrisMsg.addMetadata("key", "val");
    adaptrisMsg.addMetadata("key2", "val");
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testInit() throws Exception {
    doNothing().when(clientSpy).init();
    doNothing().when(clientSpy).createConfirmationListener(producer);

    LifecycleHelper.init(producer);

    verify(clientSpy).init();
    verify(clientSpy).createConfirmationListener(producer);

    doThrow(exception).when(clientSpy).init();
    try {
      LifecycleHelper.init(producer);
      fail("No CoreException thrown for .init() fail");
    } catch (CoreException e) {
    }
  }

  @Test
  public void testStartStopClose() throws Exception {
    doNothing().when(clientSpy).start();
    doNothing().when(clientSpy).stop();
    doNothing().when(clientSpy).close();

    LifecycleHelper.start(producer);
    verify(clientSpy).start();

    LifecycleHelper.stop(producer);
    verify(clientSpy).stop();

    LifecycleHelper.close(producer);
    verify(clientSpy).close();

    // Also need to test when TibrvException raised on .start()
    // However StandardRendezvousClient doesn't throw that exception
  }

  @Test
  public void testSetNull() throws Exception {
    try {
      producer.setRendezvousClient(null);
      fail("no error for null RendezvousClient");
    } catch (Exception e) {
    }
    try {
      producer.setRendezvousTranslator(null);
      fail("no error for null RendezvousTranslator");
    } catch (Exception e) {
    }
  }

  /**
   * Only logs!
   *
   * @throws Exception
   */
  @Test
  public void testOnMsg() throws Exception {
    producer.onMsg(listener, msg);
  }

  /**
   * Process should function without errors
   *
   * @throws Exception
   */
  @Test
  public void testOnMsgNull() throws Exception {
    producer.onMsg(listener, null);
  }

  /**
   * Process should function without errors
   *
   * @throws Exception
   */
  @Test
  public void testOnMsgError() throws Exception {
    doThrow(exception).when(msg).get("seqno");

    producer.onMsg(listener, msg);
  }

  @Test
  public void testProduce() throws Exception {
    when(translatorSpy.translate(adaptrisMsg, DESTINATION)).thenReturn(msg);
    doNothing().when(clientSpy).send(msg);

    producer.produce(adaptrisMsg);

    verify(translatorSpy, times(2)).translate(adaptrisMsg, DESTINATION);
    verify(clientSpy).send(msg);
  }

  @Test
  public void testProduceWithError() throws Exception {
    when(translatorSpy.translate(adaptrisMsg, DESTINATION)).thenReturn(msg);
    doThrow(exception).when(clientSpy).send(msg);

    try {
      producer.produce(adaptrisMsg);
      fail("No exception produced from .produce() fail");
    } catch (ProduceException e) {
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    RendezvousProducer producer = new RendezvousProducer();
    producer.setSubject("produce");

    StandaloneProducer result = new StandaloneProducer();
    result.setProducer(producer);

    return result;
  }
}
