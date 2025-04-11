// Copyright (c) 2019-2025 The MathWorks, Inc.
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

package com.mathworks.polyspace.jenkins;

public class PolyspaceConstants {
    // Polyspace General variables
    public static final String POLYSPACE_BIN = "POLYSPACE_BIN";                             // Path to polyspace

    // Polyspace Access oriented variables
    public static final String POLYSPACE_ACCESS           = "ps_helper_access";             // polyspace-access -protocol ... -host ... -port ... -login ... -encrypted-password ...
    public static final String POLYSPACE_ACCESS_PROTOCOL  = "POLYSPACE_ACCESS_PROTOCOL";    // protocol of Access
    public static final String POLYSPACE_ACCESS_HOST      = "POLYSPACE_ACCESS_HOST";        // host of Access
    public static final String POLYSPACE_ACCESS_PORT      = "POLYSPACE_ACCESS_PORT";        // port of Access
    public static final String POLYSPACE_ACCESS_URL       = "POLYSPACE_ACCESS_URL";         // <protocol>://<host>:<port>
}
