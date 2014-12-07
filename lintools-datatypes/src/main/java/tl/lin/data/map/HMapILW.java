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

package tl.lin.data.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * <p>
 * Writable representing a map where both keys and values are ints.
 * </p>
 *
 * <p>
 * One notable feature of this class is the ability to support <i>lazy decoding</i>,
 * controlled by the {@link #setLazyDecodeFlag(boolean)} method. In lazy
 * decoding mode, when an object of this type is deserialized, key-value pairs
 * are not inserted into the map, but rather held in arrays. The reduces memory
 * used in cases where random access to values is not required. In lazy decoding
 * mode, the raw keys and values may be fetched by the {@link #getKeys()} and
 * {@link #getValues()} methods, respectively. The map can be subsequently
 * populated with the {@link #decode()} method.
 * </p>
 */
public class HMapILW extends HMapIL implements Writable {
  private static boolean LazyDecode = false;
  private static final long serialVersionUID = 2794563945L;

  private int numEntries = 0;
  private int[] keys = null;
  private long[] values = null;

  /**
   * Creates a <code>HMapILW</code> object.
   */
  public HMapILW() {
    super();
  }

  /**
   * Deserializes the map.
   *
   * @param in source for raw byte representation
   */
  public void readFields(DataInput in) throws IOException {
    this.clear();

    numEntries = in.readInt();
    if (numEntries == 0)
      return;

    if (LazyDecode) {
      // Lazy initialization: read into arrays.
      keys = new int[numEntries];
      values = new long[numEntries];

      for (int i = 0; i < numEntries; i++) {
        keys[i] = in.readInt();
        values[i] = in.readLong();
      }
    } else {
      // Normal initialization: populate the map.
      for (int i = 0; i < numEntries; i++) {
        put(in.readInt(), in.readLong());
      }
    }
  }

  /**
   * In lazy decoding mode, populates the map with deserialized data.
   * Otherwise, does nothing.
   *
   * @throws IOException
   */
  public void decode() throws IOException {
    if (keys == null)
      return;

    for (int i = 0; i < keys.length; i++) {
      put(keys[i], values[i]);
    }

    keys = null;
    values = null;
  }

  /**
   * Returns whether or not this map has been decoded. If not in lazy decoding
   * mode, this method always return <i>true</i>.
   */
  public boolean isDecoded() {
    if (getLazyDecodeFlag() == false)
      return true;

    return keys == null;
  }

  /**
   * Serializes the map.
   *
   * @param out where to write the raw byte representation
   */
  public void write(DataOutput out) throws IOException {
    // Write out the number of entries in the map.
    out.writeInt(size());
    if (size() == 0)
      return;

    for (MapIL.Entry e : entrySet()) {
      out.writeInt(e.getKey());
      out.writeLong(e.getValue());
    }
  }

  /**
   * Returns the serialized representation of this object as a byte array.
   *
   * @return byte array representing the serialized representation of this object
   * @throws IOException
   */
  public byte[] serialize() throws IOException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(bytesOut);
    write(dataOut);

    return bytesOut.toByteArray();
  }

  /**
   * Creates a <code>HMapILW</code> object from a <code>DataInput</code>.
   *
   * @param in source for reading the serialized representation
   * @return a newly-created <code>HMapILW</code> object
   * @throws IOException
   */
  public static HMapILW create(DataInput in) throws IOException {
    HMapILW m = new HMapILW();
    m.readFields(in);

    return m;
  }

  /**
   * Creates a <code>HMapILW</code> object from a byte array.
   *
   * @param bytes raw serialized representation
   * @return a newly-created <code>HMapILW</code> object
   * @throws IOException
   */
  public static HMapILW create(byte[] bytes) throws IOException {
    return create(new DataInputStream(new ByteArrayInputStream(bytes)));
  }

  /**
   * Sets the lazy decoding flag.
   */
  public static void setLazyDecodeFlag(boolean b) {
    LazyDecode = b;
  }

  /**
   * Returns the value of the lazy decoding flag
   */
  public static boolean getLazyDecodeFlag() {
    return LazyDecode;
  }

  /**
   * In lazy decoding mode, returns an array of all the keys if the map hasn't
   * been decoded yet. Otherwise, returns null.
   *
   * @return an array of all the keys
   */
  public int[] getKeys() {
    return keys;
  }

  /**
   * In lazy decoding mode, returns an array of all the values if the map
   * hasn't been decoded yet. Otherwise, returns null.
   *
   * @return an array of all the values
   */
  public long[] getValues() {
    return values;
  }

  /**
   * In lazy decoding mode, adds values from keys of another map to this map.
   * This map must have already been decoded, but the other map must not have
   * been already decoded.
   *
   * @param m the other map
   */
  public void lazyplus(HMapILW m) {
    int[] k = m.getKeys();
    long[] v = m.getValues();

    for (int i = 0; i < k.length; i++) {
      if (this.containsKey(k[i])) {
        this.put(k[i], this.get(k[i]) + v[i]);
      } else {
        this.put(k[i], v[i]);
      }
    }
  }


  @Override
  public int size() {
    if (!isDecoded()) {
      return keys.length;
    }

    return super.size();
  }
}
