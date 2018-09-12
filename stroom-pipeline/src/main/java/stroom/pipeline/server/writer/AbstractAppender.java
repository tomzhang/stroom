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

package stroom.pipeline.server.writer;

import stroom.pipeline.destination.Destination;
import stroom.pipeline.server.errorhandler.ErrorReceiverProxy;
import stroom.pipeline.server.factory.PipelineProperty;
import stroom.streamstore.server.fs.serializable.SegmentOutputStream;
import stroom.util.shared.ModelStringUtil;
import stroom.util.shared.Severity;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractAppender extends AbstractDestinationProvider implements Destination {
    private final ErrorReceiverProxy errorReceiverProxy;

    private OutputStream outputStream;
    private SegmentOutputStream segmentOutputStream;
    private byte[] footer;
    private String size;
    private Long sizeBytes = null;

    AbstractAppender(final ErrorReceiverProxy errorReceiverProxy) {
        this.errorReceiverProxy = errorReceiverProxy;
    }

    @Override
    public void endProcessing() {
        closeCurrentOutputStream();
        super.endProcessing();
    }

    @Override
    public Destination borrowDestination() {
        return this;
    }

    @Override
    public void returnDestination(final Destination destination) throws IOException {
        // We assume that the parent will write an entire segment when it borrows a destination so add a segment marker
        // here after a segment is written.

        // Writing a segment marker here ensures there is always a marker written before the footer regardless or
        // whether a footer is actually written. We do this because we always make an allowance for a footer for data
        // display purposes.
        insertSegmentMarker();

        final Long sizeBytes = getSizeBytes();
        if (sizeBytes > 0 && sizeBytes <= getCurrentOutputSize()) {
            closeCurrentOutputStream();
        }
    }

    private void closeCurrentOutputStream() {
        if (outputStream != null) {
            try {
                writeFooter();
            } catch (final IOException e) {
                error(e.getMessage(), e);
            }

            try {
                outputStream.close();
            } catch (final IOException e) {
                error(e.getMessage(), e);
            }

            outputStream = null;
        }
    }

    @Override
    public final OutputStream getByteArrayOutputStream() throws IOException {
        return getOutputStream(null, null);
    }

    @Override
    public OutputStream getOutputStream(final byte[] header, final byte[] footer) throws IOException {
        this.footer = footer;

        if (outputStream == null) {
            outputStream = createOutputStream();
            if (outputStream instanceof SegmentOutputStream) {
                segmentOutputStream = (SegmentOutputStream) outputStream;
            }

            // If we haven't written yet then create the output stream and
            // write a header if we have one.
            if (header != null && header.length > 0) {
                // Write the header.
                write(header);
            }

            // Insert a segment marker before we write the next record regardless of whether the header has actually
            // been written. This is because we always make an allowance for the existence of a header in a segmented
            // stream when viewing data.
            insertSegmentMarker();
        }

        return outputStream;
    }

    private void writeFooter() throws IOException {
        if (footer != null && footer.length > 0) {
            // Write the footer.
            write(footer);
        }
    }

    private void write(final byte[] bytes) throws IOException {
        outputStream.write(bytes, 0, bytes.length);
    }

    private void insertSegmentMarker() throws IOException {
        // Add a segment marker to the output stream if we are segmenting.
        if (segmentOutputStream != null) {
            segmentOutputStream.addSegment();
        }
    }

    protected abstract OutputStream createOutputStream() throws IOException;

    protected void error(final String message, final Exception e) {
        errorReceiverProxy.log(Severity.ERROR, null, getElementId(), message, e);
    }

    abstract long getCurrentOutputSize();

    private Long getSizeBytes() {
        if (sizeBytes == null) {
            sizeBytes = -1L;

            // Set the maximum number of bytes to write before creating a new stream.
            if (size != null && size.trim().length() > 0) {
                try {
                    sizeBytes = ModelStringUtil.parseIECByteSizeString(size);
                } catch (final RuntimeException e) {
                    errorReceiverProxy.log(Severity.ERROR, null, getElementId(), "Unable to parse size: " + size, null);
                }
            }
        }
        return sizeBytes;
    }

    @SuppressWarnings("unused")
    @PipelineProperty(description = "The size of the output stream that will cause a new stream to be created.")
    public void setSplitWhenBiggerThan(final String size) {
        this.size = size;
    }
}
