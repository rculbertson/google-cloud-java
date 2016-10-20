/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.dns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RecordSetTest {

  private static final String NAME = "example.com.";
  private static final Integer TTL = 3600;
  private static final TimeUnit UNIT = TimeUnit.HOURS;
  private static final Integer UNIT_TTL = 1;
  private static final RecordSet.Type TYPE = RecordSet.Type.AAAA;
  private static final RecordSet RECORD_SET = RecordSet.newBuilder(NAME, TYPE)
      .setTtl(UNIT_TTL, UNIT)
      .build();
  private static final RecordSet DEPRECATED_RECORD_SET = RecordSet.builder(NAME, TYPE)
      .ttl(UNIT_TTL, UNIT)
      .build();

  @Test
  public void testDefaultDnsRecord() {
    RecordSet recordSet = RecordSet.newBuilder(NAME, TYPE).build();
    assertEquals(0, recordSet.getRecords().size());
    assertEquals(TYPE, recordSet.getType());
    assertEquals(NAME, recordSet.getName());
  }

  @Test
  public void testDefaultDnsRecordDeprecated() {
    RecordSet recordSet = RecordSet.builder(NAME, TYPE).build();
    assertEquals(0, recordSet.records().size());
    assertEquals(TYPE, recordSet.type());
    assertEquals(NAME, recordSet.name());
  }

  @Test
  public void testBuilder() {
    assertEquals(NAME, RECORD_SET.getName());
    assertEquals(TTL, RECORD_SET.getTtl());
    assertEquals(TYPE, RECORD_SET.getType());
    assertEquals(0, RECORD_SET.getRecords().size());
    // verify that one can add records to the record set
    String testingRecord = "Testing recordSet";
    String anotherTestingRecord = "Another recordSet 123";
    RecordSet anotherRecord = RECORD_SET.toBuilder()
        .addRecord(testingRecord)
        .addRecord(anotherTestingRecord)
        .build();
    assertEquals(2, anotherRecord.getRecords().size());
    assertTrue(anotherRecord.getRecords().contains(testingRecord));
    assertTrue(anotherRecord.getRecords().contains(anotherTestingRecord));
  }

  @Test
  public void testBuilderDeprecated() {
    assertEquals(NAME, DEPRECATED_RECORD_SET.name());
    assertEquals(TTL, DEPRECATED_RECORD_SET.ttl());
    assertEquals(TYPE, DEPRECATED_RECORD_SET.type());
    assertEquals(0, DEPRECATED_RECORD_SET.records().size());
    // verify that one can add records to the record set
    String testingRecord = "Testing recordSet";
    String anotherTestingRecord = "Another recordSet 123";
    RecordSet anotherRecord = RECORD_SET.toBuilder()
        .addRecord(testingRecord)
        .addRecord(anotherTestingRecord)
        .build();
    assertEquals(2, anotherRecord.records().size());
    assertTrue(anotherRecord.records().contains(testingRecord));
    assertTrue(anotherRecord.records().contains(anotherTestingRecord));
  }

  @Test
  public void testValidTtl() {
    try {
      RecordSet.newBuilder(NAME, TYPE).setTtl(-1, TimeUnit.SECONDS);
      fail("A negative value is not acceptable for ttl.");
    } catch (IllegalArgumentException e) {
      // expected
    }
    RecordSet.newBuilder(NAME, TYPE).setTtl(0, TimeUnit.SECONDS);
    RecordSet.newBuilder(NAME, TYPE).setTtl(Integer.MAX_VALUE, TimeUnit.SECONDS);
    try {
      RecordSet.newBuilder(NAME, TYPE).setTtl(Integer.MAX_VALUE, TimeUnit.HOURS);
      fail("This value is too large for int.");
    } catch (IllegalArgumentException e) {
      // expected
    }
    RecordSet record = RecordSet.newBuilder(NAME, TYPE).setTtl(UNIT_TTL, UNIT).build();
    assertEquals(TTL, record.getTtl());
  }

  @Test
  public void testValidTtlDeprecated() {
    try {
      RecordSet.builder(NAME, TYPE).ttl(-1, TimeUnit.SECONDS);
      fail("A negative value is not acceptable for ttl.");
    } catch (IllegalArgumentException e) {
      // expected
    }
    RecordSet.builder(NAME, TYPE).ttl(0, TimeUnit.SECONDS);
    RecordSet.builder(NAME, TYPE).ttl(Integer.MAX_VALUE, TimeUnit.SECONDS);
    try {
      RecordSet.builder(NAME, TYPE).ttl(Integer.MAX_VALUE, TimeUnit.HOURS);
      fail("This value is too large for int.");
    } catch (IllegalArgumentException e) {
      // expected
    }
    RecordSet record = RecordSet.builder(NAME, TYPE).ttl(UNIT_TTL, UNIT).build();
    assertEquals(TTL, record.ttl());
  }

  @Test
  public void testEqualsAndNotEquals() {
    RecordSet clone = RECORD_SET.toBuilder().build();
    assertEquals(RECORD_SET, clone);
    clone = RECORD_SET.toBuilder().addRecord("another recordSet").build();
    assertNotEquals(RECORD_SET, clone);
    String differentName = "totally different name";
    clone = RECORD_SET.toBuilder().setName(differentName).build();
    assertNotEquals(RECORD_SET, clone);
    clone = RECORD_SET.toBuilder().setTtl(RECORD_SET.getTtl() + 1, TimeUnit.SECONDS).build();
    assertNotEquals(RECORD_SET, clone);
    clone = RECORD_SET.toBuilder().setType(RecordSet.Type.TXT).build();
    assertNotEquals(RECORD_SET, clone);
  }

  @Test
  public void testSameHashCodeOnEquals() {
    int hash = RECORD_SET.hashCode();
    RecordSet clone = RECORD_SET.toBuilder().build();
    assertEquals(clone.hashCode(), hash);
  }

  @Test
  public void testToAndFromPb() {
    assertEquals(RECORD_SET, RecordSet.fromPb(RECORD_SET.toPb()));
    RecordSet partial = RecordSet.newBuilder(NAME, TYPE).build();
    assertEquals(partial, RecordSet.fromPb(partial.toPb()));
    partial = RecordSet.newBuilder(NAME, TYPE).addRecord("test").build();
    assertEquals(partial, RecordSet.fromPb(partial.toPb()));
    partial = RecordSet.newBuilder(NAME, TYPE).setTtl(15, TimeUnit.SECONDS).build();
    assertEquals(partial, RecordSet.fromPb(partial.toPb()));
  }

  @Test
  public void testToBuilder() {
    assertEquals(RECORD_SET, RECORD_SET.toBuilder().build());
    RecordSet partial = RecordSet.newBuilder(NAME, TYPE).build();
    assertEquals(partial, partial.toBuilder().build());
    partial = RecordSet.newBuilder(NAME, TYPE).addRecord("test").build();
    assertEquals(partial, partial.toBuilder().build());
    partial = RecordSet.newBuilder(NAME, TYPE).setTtl(15, TimeUnit.SECONDS).build();
    assertEquals(partial, partial.toBuilder().build());
  }

  @Test
  public void clearRecordSet() {
    // make sure that we are starting not empty
    RecordSet clone =
        RECORD_SET.toBuilder().addRecord("record").addRecord("another").build();
    assertNotEquals(0, clone.getRecords().size());
    clone = clone.toBuilder().clearRecords().build();
    assertEquals(0, clone.getRecords().size());
    clone.toPb(); // verify that pb allows it
  }

  @Test
  public void removeFromRecordSet() {
    String recordString = "record";
    // make sure that we are starting not empty
    RecordSet clone = RECORD_SET.toBuilder().addRecord(recordString).build();
    assertNotEquals(0, clone.getRecords().size());
    clone = clone.toBuilder().removeRecord(recordString).build();
    assertEquals(0, clone.getRecords().size());
  }
}
