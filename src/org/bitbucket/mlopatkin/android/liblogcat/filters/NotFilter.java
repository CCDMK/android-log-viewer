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
package org.bitbucket.mlopatkin.android.liblogcat.filters;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;

public class NotFilter implements LogRecordFilter {

    private LogRecordFilter base;

    public NotFilter(LogRecordFilter base) {
        this.base = base;
    }

    @Override
    public boolean include(LogRecord record) {
        return !base.include(record);
    }

    @Override
    public String toString() {
        return "HIDING<br>" + base.toString();
    }
}
