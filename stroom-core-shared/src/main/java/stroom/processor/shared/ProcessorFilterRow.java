/*
 * Copyright 2016 Crown Copyright
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

package stroom.processor.shared;

import stroom.docref.SharedObject;
import stroom.util.shared.Expander;
import stroom.util.shared.TreeRow;

import java.util.Objects;

public class ProcessorFilterRow implements SharedObject, TreeRow {
    private static final long serialVersionUID = 3306590492924959915L;

    private static final Expander EXPANDER = new Expander(1, false, true);
    private ProcessorFilter processorFilter;

    public ProcessorFilterRow() {
        // Default constructor necessary for GWT serialisation.
    }

    public ProcessorFilterRow(final ProcessorFilter processorFilter) {
        this.processorFilter = processorFilter;
    }

    public ProcessorFilter getProcessorFilter() {
        return processorFilter;
    }

    @Override
    public Expander getExpander() {
        return EXPANDER;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ProcessorFilterRow that = (ProcessorFilterRow) o;
        return Objects.equals(processorFilter, that.processorFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processorFilter);
    }

    @Override
    public String toString() {
        return "ProcessorFilterRow{" +
                "processorFilter=" + processorFilter +
                '}';
    }
}
