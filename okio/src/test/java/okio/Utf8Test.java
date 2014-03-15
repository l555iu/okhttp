/*
 * Copyright (C) 2014 Square, Inc.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class Utf8Test {
  @Test public void encodeEveryCodepoint() throws Exception {
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
      byte[] utf32 = new byte[4];
      utf32[0] = (byte) ((i >>> 24) & 0xff);
      utf32[1] = (byte) ((i >>> 16) & 0xff);
      utf32[2] = (byte) ((i >>>  8) & 0xff);
      utf32[3] = (byte)  (i         & 0xff);

      String s = new String(utf32);
      ByteString expected = ByteString.of(s.getBytes("utf-8"));

      OkBuffer buffer = new OkBuffer();
      buffer.writeUtf8(s);
      ByteString actual = buffer.readByteString(buffer.size());

      assertEquals(expected, actual);
    }
  }
}
