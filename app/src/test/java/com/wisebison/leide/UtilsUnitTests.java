package com.wisebison.leide;

import com.wisebison.leide.util.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsUnitTests {
  /**
   * Test formatting a float to 2 decimal places, both with and without a + sign for positive
   * numbers.
   */
  @Test
  public void testFormatFloat() {
    // WHEN
    final String test1 = Utils.formatFloat(5.25367782351f, false);

    // THEN
    assertEquals("5.25", test1);

    // WHEN
    final String test2 = Utils.formatFloat(5.25367782351f, true);

    // THEN
    assertEquals("+5.25", test2);

    // WHEN
    final String test3 = Utils.formatFloat(-3.2345253252f, false);

    // THEN
    assertEquals("-3.23", test3);

    // WHEN
    final String test4 = Utils.formatFloat(-9.23634534534f, true);

    // THEN
    assertEquals("-9.24", test4);
  }

  /**
   * Test formatting epoch millis to a human-readable date.
   */
  @Test
  public void testFormatDate() {
    assertEquals("Thu, Dec 03 2020 6:58 AM", Utils.formatDate(1606996718548L));
  }
}
