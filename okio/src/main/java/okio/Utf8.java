/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okio;

/** UTF-16 to UTF-8 transcoder. From AOSP's Charsets.cpp. */
final class Utf8 {
  private static final int SURROGATE_OFFSET = 0x10000 - (0xd800 << 10) - 0xdc00;

  private Utf8() {
  }

  /**
   * Encodes a Java string (UTF-16 chars) as UTF-8, writing the result to {@code
   * sink}. UTF-8 code points are encoded in 1-4 bytes:
   * <ul>
   *   <li>1 char in, 1 byte out. ASCII.
   *   <li>1 char in, 2 bytes out.
   *   <li>1 char in, 3 bytes out.
   *   <li>2 chars in, 4 bytes out. UTF-16 uses two chars to represent a single
   *       code point. The first char is called the lead surrogate; the second
   *       is the trail surrogate.
   * </ul>
   */
  public static void encodeUtf8(String source, OkBuffer sink) {
    Segment tail = sink.writableSegment(4);
    byte[] data = tail.data;
    int limit = tail.limit;

    for (int i = 0, length = source.length(); i < length; i++) {
      int c = source.charAt(i);

      // If the current segment doesn't have room for 4 bytes, append a new empty segment.
      if (limit + 4 > Segment.SIZE) {
        sink.size += (limit - tail.limit);
        tail.limit = limit;
        tail = sink.writableSegment(4);
        data = tail.data;
        limit = tail.limit;
      }

      if (c < 0x80) {
        data[limit++] = (byte) c;

      } else if (c < 0x800) {
        data[limit++] = (byte) (c >> 6   | 0xc0);
        data[limit++] = (byte) (c & 0x3f | 0x80);

      } else if (isSurrogate(c)) {
        int lead = c;
        int trail = (i + 1 != length) ? source.charAt(i + 1) : 0;
        if (!isSurrogateLead(lead) || !isSurrogateTrail(trail)) {
          data[limit++] = '?';
          continue;
        }
        i++; // Only consume the trail when we know we have a valid surrogate pair.
        c = (lead << 10) + trail + SURROGATE_OFFSET;
        data[limit++] = (byte) (c >> 18        | 0xf0);
        data[limit++] = (byte) (c >> 12 & 0x3f | 0x80);
        data[limit++] = (byte) (c >>  6 & 0x3f | 0x80);
        data[limit++] = (byte) (c       & 0x3f | 0x80);

      } else {
        data[limit++] = (byte) (c >> 12        | 0xe0);
        data[limit++] = (byte) (c >>  6 & 0x3f | 0x80);
        data[limit++] = (byte) (c       & 0x3f | 0x80);
      }
    }

    sink.size += (limit - tail.limit);
    tail.limit = limit;
  }

  private static boolean isSurrogate(int c) {
    return c >= 0xd800 && c <= 0xdfff;
  }

  private static boolean isSurrogateLead(int c) {
    return c >= 0xd800 && c <= 0xdbff;
  }

  private static boolean isSurrogateTrail(int c) {
    return c >= 0xdc00 && c <= 0xdfff;
  }
}
