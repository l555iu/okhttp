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
package com.squareup.okhttp.benchmarks;

import com.google.caliper.Param;
import com.google.caliper.model.ArbitraryMeasurement;
import com.google.caliper.runner.CaliperMain;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import okio.OkBuffer;

public class Utf8Benchmark extends com.google.caliper.Benchmark {
  public static final Charset UTF_8 = Charset.forName("UTF-8");

  @Param({ "10", "1000" })
  int length;

  @Param
  Alphabet alphabet;

  private String data;

  @Override protected void setUp() throws Exception {
    data = alphabet.randomString(length);
  }

  @ArbitraryMeasurement(description = "Mreps")
  public double writer() throws IOException {
    long start = System.nanoTime();
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(bytesOut, UTF_8);
    for (int i = 0; i < 1000000; i++) {
      writer.write(data);
      bytesOut.reset();
    }
    long end = System.nanoTime();
    return (end - start) / 1000000000d;
  }

  @ArbitraryMeasurement(description = "Mreps")
  public double okBufferOriginal() {
    long start = System.nanoTime();
    OkBuffer okBuffer = new OkBuffer();
    for (int i = 0; i < 1000000; i++) {
      okBuffer.writeUtf8(data);
      okBuffer.clear();
    }
    long end = System.nanoTime();
    return (end - start) / 1000000000d;
  }

  @ArbitraryMeasurement(description = "Mreps")
  public double okBufferInline() {
    long start = System.nanoTime();
    OkBuffer okBuffer = new OkBuffer();
    for (int i = 0; i < 1000000; i++) {
      okBuffer.writeUtf8embedded(data);
      okBuffer.clear();
    }
    long end = System.nanoTime();
    return (end - start) / 1000000000d;
  }

  enum Alphabet {
    ENGLISH,
    RUSSIAN;

    private String randomString(int length) {
      String alphabet = this == ENGLISH
          ? "I can eat glass and it doesn't hurt me."
          : "Я могу есть стекло, оно мне не вредит.";
      char[] chars = new char[length];
      Random random = new Random(0);
      for (int i = 0; i < length; i++) {
        chars[i] = alphabet.charAt(random.nextInt(alphabet.length()));
      }
      return new String(chars);
    }
  }

  public static void main(String[] args) {
    List<String> allArgs = new ArrayList<String>();
    allArgs.add("--instrument");
    allArgs.add("arbitrary");
    allArgs.addAll(Arrays.asList(args));
    CaliperMain.main(Utf8Benchmark.class, allArgs.toArray(new String[allArgs.size()]));
  }
}
