// Copyright (c) 2024 The MathWorks, Inc.
// All Rights Reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.mathworks.polyspace.jenkins.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.mathworks.polyspace.jenkins.utils.PolyspaceUtils;

import org.junit.jupiter.api.Test;

class PolyspaceUtilsTest {

  private static Path noEndingNewLine = Paths.get(System.getProperty("user.dir"), "src", "test", "data", "simpleFileWithNoEndingNewLine.txt");
  private static Path withEndingNewLine = Paths.get(System.getProperty("user.dir"), "src", "test", "data", "simpleFileWithEndingNewLine.txt");

  @Test
  void testGetFileContent() throws Exception
  {
    assertEquals(
      "line1" + System.lineSeparator() +
      "line2" + System.lineSeparator() +
      "line3 with ending new line",
      PolyspaceUtils.getFileContent(noEndingNewLine));
    assertEquals(
      "line1" + System.lineSeparator() +
      "line2" + System.lineSeparator() +
      "line3 with ending new line" + System.lineSeparator(),
      PolyspaceUtils.getFileContent(withEndingNewLine));
  }

  @Test
  void testGetFileLineCount() throws Exception
  {
    assertEquals(3, PolyspaceUtils.getFileLineCount(noEndingNewLine));
    assertEquals(3, PolyspaceUtils.getFileLineCount(withEndingNewLine));
  }

}
