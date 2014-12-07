/*
 * Lintools: tools by @lintool
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package tl.lin.data.pair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.JUnit4TestAdapter;

import org.apache.hadoop.io.WritableComparator;
import org.junit.Test;

import tl.lin.data.WritableComparatorTestHarness;

public class PairOfLongStringTest {

  @Test
  public void testBasic() throws IOException {
    PairOfLongString pair = new PairOfLongString(1L, "hi");

    assertEquals("hi", pair.getRightElement());
    assertEquals(1L, pair.getLeftElement());
  }

  @Test
  public void testSerialize() throws IOException {
    PairOfLongString origPair = new PairOfLongString(2L, "hi");

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(bytesOut);

    origPair.write(dataOut);

    PairOfLongString pair = new PairOfLongString();

    pair.readFields(new DataInputStream(new ByteArrayInputStream(bytesOut.toByteArray())));

    assertEquals("hi", pair.getRightElement());
    assertTrue(pair.getLeftElement() == 2L);
  }

  @Test
  public void testComparison1() throws IOException {
    PairOfLongString pair1 = new PairOfLongString(1L, "hi");
    PairOfLongString pair2 = new PairOfLongString(1L, "hi");
    PairOfLongString pair3 = new PairOfLongString(0L, "hi");
    PairOfLongString pair4 = new PairOfLongString(0L, "a");
    PairOfLongString pair5 = new PairOfLongString(2L, "hi");
    
    assertTrue(pair1.equals(pair2));
    assertFalse(pair1.equals(pair3));

    assertTrue(pair1.compareTo(pair2) == 0);
    assertTrue(pair1.compareTo(pair3) > 0);
    assertTrue(pair1.compareTo(pair4) > 0);
    assertTrue(pair1.compareTo(pair5) < 0);
    assertTrue(pair3.compareTo(pair4) > 0);
    assertTrue(pair4.compareTo(pair5) < 0);   
  }

  @Test
  public void testComparison2() throws IOException {
    WritableComparator comparator = new PairOfLongString.Comparator();

    PairOfLongString pair1 = new PairOfLongString(1L, "hi");
    PairOfLongString pair2 = new PairOfLongString(1L, "hi");
    PairOfLongString pair3 = new PairOfLongString(0L, "hi");
    PairOfLongString pair4 = new PairOfLongString(0L, "a");
    PairOfLongString pair5 = new PairOfLongString(2L, "hi");
    
    assertTrue(pair1.equals(pair2));
    assertFalse(pair1.equals(pair3));

    assertTrue(WritableComparatorTestHarness.compare(comparator, pair1, pair2) == 0);
    assertTrue(WritableComparatorTestHarness.compare(comparator, pair1, pair3) > 0);
    assertTrue(WritableComparatorTestHarness.compare(comparator, pair1, pair4) > 0);
    assertTrue(WritableComparatorTestHarness.compare(comparator, pair1, pair5) < 0);
    assertTrue(WritableComparatorTestHarness.compare(comparator, pair3, pair4) > 0);
    assertTrue(WritableComparatorTestHarness.compare(comparator, pair4, pair5) < 0);    
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(PairOfLongStringTest.class);
  }
}
