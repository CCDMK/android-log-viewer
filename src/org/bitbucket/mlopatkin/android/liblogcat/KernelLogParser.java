/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.android.liblogcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods to check kernel log lines to be
 * well-formed.
 */
public class KernelLogParser {
    private KernelLogParser() {
    }

    private static final Pattern KERNEL_LOG_RECORD_PATTTERN = Pattern
            .compile("^<\\d+>(\\[\\s*\\d+\\.\\d+\\] )?.*$");

    public static KernelLogRecord parseRecord(String line) {
        Matcher m = KERNEL_LOG_RECORD_PATTTERN.matcher(line);
        if (m.matches()) {
            return new KernelLogRecord(line);
        } else {
            return null;
        }
    }
}
