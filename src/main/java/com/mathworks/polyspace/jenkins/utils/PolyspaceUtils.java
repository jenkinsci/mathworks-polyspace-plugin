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

package com.mathworks.polyspace.jenkins.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class PolyspaceUtils {

  public static Path copyToTempFile(Path path) throws IOException {
    if (!path.toFile().isDirectory())
    {
      Path tempDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "polyspace-");
      Path filename = path.getFileName();
      if (filename == null)
      {
        throw new RuntimeException("Cannot work on a path with 0 elements: '" + path + "'");
      }
      Path tempFile = Paths.get(tempDir.toString(), filename.toString());
      Files.copy(path, tempFile);
      return tempFile;
    } else {
      throw new RuntimeException("Should not be a directory: '" + path + "'");
    }
  }

  public static String getFileContent(Path path) throws IOException
  {
    return Files.readString(path);
  }

  public static void writeContent(Path path, ArrayList<String> content) throws IOException
  {
    Files.write(path, content, StandardCharsets.UTF_8);
  }

  public static long getFileLineCount(Path path) throws IOException
  {
      long nb;
      try (Stream<String> lines = Files.lines(path)) {
        nb = lines.count();
      }
      return nb;
  }
}
