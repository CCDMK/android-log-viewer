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

public class PriorityFilter extends AbstractFilter implements LogRecordFilter {

    private LogRecord.Priority priority;

    public PriorityFilter(LogRecord.Priority priority) {
        this.priority = priority;
    }

    @Override
    public boolean include(LogRecord record) {
        LogRecord.Priority p = record.getPriority();
        return p.ordinal() >= priority.ordinal();
    }

    @Override
    public String toString() {
        return "Priority: " + priority;
    }

    @Override
    protected void dumpFilter(FilterData data) {
        data.priority = priority;
    }
}
