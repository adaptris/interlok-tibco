package com.adaptris.core.tibrv;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.tibrv.RendezvousClient;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;

public class RendezvousProducerTest extends ProducerCase {

	private static String CHAR_ENC_VAL = "char-enc-val";
	private static String PAYLOAD_VAL = "payload-val";
	private static String UNIQUE_ID_VAL = "unique-id-val";
	private static String DESTINATION = "over-there";

	@Mock private TibrvListener listener;
	@Mock private TibrvException exception;
	@Mock private TibrvMsg msg;
	@Mock private ProduceDestination destination;
	@Mock private AdaptrisMessage adaptrisMsg;

	RendezvousTranslator translatorSpy;
	RendezvousClient clientSpy;
	private RendezvousProducer producer;

  private static final String BASE_DIR_KEY = "TibrvProducerExamples.baseDir";

  public RendezvousProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

	@Override
  protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		producer = new RendezvousProducer();
		translatorSpy = spy(producer.getRendezvousTranslator());
		producer.setRendezvousTranslator(translatorSpy);
		clientSpy = spy(producer.getRendezvousClient());
		producer.setRendezvousClient(clientSpy);

		adaptrisMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_VAL);
		adaptrisMsg.setUniqueId(UNIQUE_ID_VAL);
		adaptrisMsg.setCharEncoding(CHAR_ENC_VAL);
		adaptrisMsg.addMetadata("key", "val");
		adaptrisMsg.addMetadata("key2", "val");

		when(destination.getDestination(adaptrisMsg)).thenReturn(DESTINATION);
	}

	public void testInit() throws Exception{
		doNothing().when(clientSpy).init();
		doNothing().when(clientSpy).createConfirmationListener(producer);

    LifecycleHelper.init(producer);

		verify(clientSpy).init();
		verify(clientSpy).createConfirmationListener(producer);

		doThrow(exception).when(clientSpy).init();
		try{
      LifecycleHelper.init(producer);
			fail("No CoreException thrown for .init() fail");
		}
		catch(CoreException e){}
	}
	public void testStartStopClose() throws Exception{
		doNothing().when(clientSpy).start();
		doNothing().when(clientSpy).stop();
		doNothing().when(clientSpy).close();

    LifecycleHelper.start(producer);
		verify(clientSpy).start();

    LifecycleHelper.stop(producer);
		verify(clientSpy).stop();

    LifecycleHelper.close(producer);
		verify(clientSpy).close();

		//Also need to test when TibrvException raised on .start()
		//However StandardRendezvousClient doesn't throw that exception
	}
	public void testSetNull() throws Exception{
		try{
			producer.setRendezvousClient(null);
			fail("no error for null RendezvousClient");
		}
		catch(Exception e){}
		try{
			producer.setRendezvousTranslator(null);
			fail("no error for null RendezvousTranslator");
		}
		catch(Exception e){}
	}
	/**
	 * Only logs!
	 *
	 * @throws Exception
	 */
	public void testOnMsg() throws Exception{
		producer.onMsg(listener, msg);
	}
	/**
	 * Process should function without errors
	 *
	 * @throws Exception
	 */
	public void testOnMsgNull() throws Exception{
		producer.onMsg(listener, null);
	}
	/**
	 * Process should function without errors
	 *
	 * @throws Exception
	 */
	public void testOnMsgError() throws Exception{
		doThrow(exception).when(msg).get("seqno");

		producer.onMsg(listener, msg);
	}
	public void testProduce() throws Exception{
		when(translatorSpy.translate(adaptrisMsg, DESTINATION)).thenReturn(msg);
		doNothing().when(clientSpy).send(msg);

		producer.produce(adaptrisMsg, destination);

		verify(translatorSpy, times(2)).translate(adaptrisMsg, destination.getDestination(adaptrisMsg));
		verify(clientSpy).send(msg);
	}
	public void testProduceWithError() throws Exception{
		when(translatorSpy.translate(adaptrisMsg, DESTINATION)).thenReturn(msg);
		doThrow(exception).when(clientSpy).send(msg);

		try{
			producer.produce(adaptrisMsg, destination);
			fail("No exception produced from .produce() fail");
		}
		catch(ProduceException e){}
	}
	@Override
  protected Object retrieveObjectForSampleConfig() {
		RendezvousProducer producer = new RendezvousProducer();
		producer.setDestination(new ConfiguredProduceDestination("produce"));

		StandaloneProducer result = new StandaloneProducer();
		result.setProducer(producer);

		return result;
	}
}
