/*
 * $RCSfile: StandardTranslatorTest.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/11/06 18:13:13 $
 * $Author: lchan $
 */
package com.adaptris.core.tibrv;

import java.util.Arrays;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvMsg;

public class StandardTranslatorTest extends TestCase {

  private static String CHAR_ENC_KEY = "char-enc";
  private static String CHAR_ENC_VAL = "char-enc-val";
  private static String PAYLOAD_KEY = "payload";
  private static String PAYLOAD_VAL = "payload-val";
  private static String UNIQUE_ID_KEY = "unique-id";
  private static String UNIQUE_ID_VAL = "unique-id-val";
  private static String METADATA_KEY = "metadata";

  private StandardRendezvousTranslator translator;

  public StandardTranslatorTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    translator = new StandardRendezvousTranslator();
    translator.registerMessageFactory(new DefaultMessageFactory());
    Tibrv.open(Tibrv.IMPL_JAVA); // doesn't need rvd...
  }

  public void testAdaptrisToTibrvWithoutMetadata() throws Exception {
    TibrvMsg expected = new TibrvMsg();
    expected.add(PAYLOAD_KEY, PAYLOAD_VAL.getBytes());
    expected.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL);
    expected.add(CHAR_ENC_KEY, CHAR_ENC_VAL);
    expected.setSendSubject("subject");

    AdaptrisMessage input = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(PAYLOAD_VAL);
    input.setUniqueId(UNIQUE_ID_VAL);
    input.setContentEncoding(CHAR_ENC_VAL);

    TibrvMsg result = translator.translate(input, "subject");

    assertEquals(3, result.getNumFields());

    assertTrue(Arrays.equals((byte[]) expected.get(PAYLOAD_KEY),
        (byte[]) result.get(PAYLOAD_KEY)));

    assertEquals(expected.get(UNIQUE_ID_KEY), result.get(UNIQUE_ID_KEY));
    assertEquals(expected.get(CHAR_ENC_KEY), result.get(CHAR_ENC_KEY));
  }

  public void testAdaptrisToTibrvWithMetadata() throws Exception {
    TibrvMsg expected = new TibrvMsg();
    expected.setSendSubject("subject");
    expected.add(PAYLOAD_KEY, PAYLOAD_VAL.getBytes(), TibrvMsg.OPAQUE);
    expected.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL, TibrvMsg.STRING);
    expected.add(CHAR_ENC_KEY, CHAR_ENC_VAL, TibrvMsg.STRING);

    TibrvMsg metadata = new TibrvMsg();
    metadata.add("key", "val", TibrvMsg.STRING);
    metadata.add("key2", "val", TibrvMsg.STRING);

    AdaptrisMessage input = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(PAYLOAD_VAL);
    input.setUniqueId(UNIQUE_ID_VAL);
    input.setContentEncoding(CHAR_ENC_VAL);
    input.addMetadata("key", "val");
    input.addMetadata("key2", "val");

    TibrvMsg result = translator.translate(input, "subject");

    assertEquals(4, result.getNumFields());

    assertTrue(Arrays.equals((byte[]) expected.get(PAYLOAD_KEY),
        (byte[]) result.get(PAYLOAD_KEY)));

    assertEquals(expected.get(UNIQUE_ID_KEY), result.get(UNIQUE_ID_KEY));
    assertEquals(expected.get(CHAR_ENC_KEY), result.get(CHAR_ENC_KEY));

    assertEquals("val", ((TibrvMsg) result.get(METADATA_KEY)).get("key"));
    assertEquals("val", ((TibrvMsg) result.get(METADATA_KEY)).get("key2"));

    assertEquals(2, ((TibrvMsg) result.get(METADATA_KEY)).getNumFields());
  }

  public void testTibrvToAdaptrisWithoutMetadata() throws Exception {
    AdaptrisMessage expected = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(PAYLOAD_VAL);
    expected.setUniqueId(UNIQUE_ID_VAL);
    expected.setContentEncoding(CHAR_ENC_VAL);

    TibrvMsg input = new TibrvMsg();
    input.add(PAYLOAD_KEY, PAYLOAD_VAL.getBytes());
    input.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL);
    input.add(CHAR_ENC_KEY, CHAR_ENC_VAL);
    input.setSendSubject("subject");

    AdaptrisMessage result = translator.translate(input);

    assertTrue(Arrays.equals(expected.getPayload(), result.getPayload()));
    assertEquals(expected.getUniqueId(), result.getUniqueId());
    assertEquals(expected.getContentEncoding(), result.getContentEncoding());
  }

  public void testTibrvToAdaptrisWithMetadata() throws Exception {
    AdaptrisMessage expected = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(PAYLOAD_VAL);
    expected.setUniqueId(UNIQUE_ID_VAL);
    expected.setContentEncoding(CHAR_ENC_VAL);
    expected.addMetadata("key", "val");
    expected.addMetadata("key2", "val");

    TibrvMsg input = new TibrvMsg();
    input.setSendSubject("subject");

    input.add(PAYLOAD_KEY, PAYLOAD_VAL.getBytes(), TibrvMsg.OPAQUE);
    input.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL, TibrvMsg.STRING);
    input.add(CHAR_ENC_KEY, CHAR_ENC_VAL, TibrvMsg.STRING);

    TibrvMsg metadata = new TibrvMsg();
    metadata.add("key", "val", TibrvMsg.STRING);
    metadata.add("key2", "val", TibrvMsg.STRING);

    input.add(METADATA_KEY, metadata, TibrvMsg.MSG);

    AdaptrisMessage result = translator.translate(input);

    assertTrue(Arrays.equals(expected.getPayload(), result.getPayload()));
    assertEquals(expected.getUniqueId(), result.getUniqueId());
    assertEquals(expected.getContentEncoding(), result.getContentEncoding());

    assertEquals(expected.getMetadataValue("key"), result
        .getMetadataValue("key"));

    assertEquals(expected.getMetadataValue("key2"), result
        .getMetadataValue("key2"));

    assertEquals(2, result.getMetadata().size());
  }

  public void testAdaptrisToTibrvWithNoPayloadOrCharEnc() throws Exception {
    TibrvMsg expected = new TibrvMsg();
    expected.setSendSubject("subject");
    expected.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL, TibrvMsg.STRING);

    TibrvMsg metadata = new TibrvMsg();
    metadata.add("key", "val", TibrvMsg.STRING);
    metadata.add("key2", "val", TibrvMsg.STRING);

    AdaptrisMessage input = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();
    input.setContentEncoding(null);
    input.setUniqueId(UNIQUE_ID_VAL);
    input.addMetadata("key", "val");
    input.addMetadata("key2", "val");

    TibrvMsg result = translator.translate(input, "subject");

    assertEquals(2, result.getNumFields());

    assertEquals(expected.get(UNIQUE_ID_KEY), result.get(UNIQUE_ID_KEY));

    assertNull(result.get(PAYLOAD_KEY));
    assertNull(result.get(CHAR_ENC_KEY));

    assertEquals("val", ((TibrvMsg) result.get(METADATA_KEY)).get("key"));
    assertEquals("val", ((TibrvMsg) result.get(METADATA_KEY)).get("key2"));

    assertEquals(2, ((TibrvMsg) result.get(METADATA_KEY)).getNumFields());
  }

  public void testTibrvToAdaptrisWithNoPayloadOrCharEnc() throws Exception {
    AdaptrisMessage expected = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();
    expected.setUniqueId(UNIQUE_ID_VAL);
    expected.addMetadata("key", "val");
    expected.addMetadata("key2", "val");

    TibrvMsg input = new TibrvMsg();
    input.setSendSubject("subject");

    input.add(UNIQUE_ID_KEY, UNIQUE_ID_VAL, TibrvMsg.STRING);

    TibrvMsg metadata = new TibrvMsg();
    metadata.add("key", "val", TibrvMsg.STRING);
    metadata.add("key2", "val", TibrvMsg.STRING);

    input.add(METADATA_KEY, metadata, TibrvMsg.MSG);

    AdaptrisMessage result = translator.translate(input);

    assertEquals(expected.getUniqueId(), result.getUniqueId());

    assertEquals(0, result.getPayload().length);
    assertNull(result.getContentEncoding());

    assertEquals(expected.getMetadataValue("key"), result
        .getMetadataValue("key"));

    assertEquals(expected.getMetadataValue("key2"), result
        .getMetadataValue("key2"));

    assertEquals(2, result.getMetadata().size());
  }
  public void testTranslateNull() throws Exception{
    try{
      translator.translate(null, "Null Test");
      fail("No IllegalArgumentException for .translate(null) message parameter");
    }
	catch(IllegalArgumentException e){}
    
    AdaptrisMessage input = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    input.setContentEncoding(null);
    input.setUniqueId(UNIQUE_ID_VAL);
    input.addMetadata("key", "val");
    input.addMetadata("key2", "val");

    try{
      translator.translate(input, null);
      fail("No IllegalArgumentException for .translate(null) subject parameter");
	}
	catch(IllegalArgumentException e){}
    
    try{
        translator.translate(input, "");
        fail("No IllegalArgumentException for .translate(\"\") subject parameter");
  	}
  	catch(IllegalArgumentException e){}
  }
  public void testSetNull() throws Exception{
    try{
      translator.setCharEncName(null);
      fail("No IllegalArgumentException for .setCharEncName(null)");
  	}
  	catch(IllegalArgumentException e){}
      
    try{
      translator.setMetadataName(null);
      fail("No IllegalArgumentException for .setMetadataName(null)");
    }
   	catch(IllegalArgumentException e){}
    
    try{
      translator.setPayloadName(null);
      fail("No IllegalArgumentException for .setPayloadName(null)");
    }
    catch(IllegalArgumentException e){}
    
    try{
      translator.setUniqueIdName(null);
      fail("No IllegalArgumentException for .setUniqueIdName(null)");
    }
    catch(IllegalArgumentException e){}
      
  }
}
