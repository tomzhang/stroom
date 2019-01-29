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

package stroom.data.store.impl.fs;

import stroom.data.store.api.NestedInputStream;
import stroom.io.BasicStreamCloser;
import stroom.io.SeekableInputStream;
import stroom.io.StreamCloser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for a nested input stream.
 * <p>
 * You must call getNextEntry and closeEntry like the ZIP API.
 */
class RANestedInputStream extends NestedInputStream {
    protected final InputStream data;
    protected final SupplierWithIO<InputStream> indexInputStreamSupplier;
    private final StreamCloser streamCloser;
    private long currentEntry = -1;
    private boolean currentEntryClosed = true;
    private boolean closed = false;
    private Long segmentCount = null;
    private RASegmentInputStream segmentInputStream;

    RANestedInputStream(final InputStream data, final InputStream inputStream) {
        this(data, () -> inputStream);
    }

    RANestedInputStream(final InputStream data, final SupplierWithIO<InputStream> indexInputStreamSupplier) {
        this.data = data;
        this.indexInputStreamSupplier = indexInputStreamSupplier;

        streamCloser = new BasicStreamCloser();
        streamCloser.add(data);
    }

    public void closeEntry() throws IOException {
        checkNotClosed();
        if (currentEntryClosed) {
            throw new IOException("Current nested stream is not open");
        }
        currentEntryClosed = true;
        segmentInputStream = null;
    }

    public long getEntryCount() throws IOException {
        return getSegmentCount();
    }

    @Override
    public boolean getNextEntry() throws IOException {
        return getNextEntry(0);
    }

    boolean getNextEntry(long skipCount) throws IOException {
        currentEntry = currentEntry + skipCount + 1;
        return getEntry(currentEntry);
    }

    @Override
    public boolean getEntry(long entryNo) throws IOException {
        // Check that this stream is open.
        checkNotClosed();

        // Check bounds.
        final long segmentCount = getSegmentCount();
        if (entryNo < 0 || entryNo >= segmentCount) {
            return false;
        }

        // Check that any stream segment that was previously used has been
        // closed.
        if (!currentEntryClosed) {
            throw new IOException("Current nested stream is not closed");
        }

        // Record the entry we are going to open and create an input stream for
        // the entry.
        currentEntry = entryNo;
        segmentInputStream = new RASegmentInputStream(data, indexInputStreamSupplier);

        // If this stream has segments, include the requested segment
        // otherwise we will use the whole stream.
        if (segmentCount > 0) {
            segmentInputStream.include(currentEntry);
        }

        // Record that the entry has been opened.
        currentEntryClosed = false;
        return true;
    }

    long entryByteOffsetStart() throws IOException {
        checkOpenEntry();
        return segmentInputStream.byteOffset(currentEntry);
    }

    long entryByteOffsetEnd() throws IOException {
        checkOpenEntry();
        long offset = segmentInputStream.byteOffset(currentEntry + 1);
        if (offset == -1) {
            offset = ((SeekableInputStream) data).getSize();
        }
        return offset;
    }

    private long getSegmentCount() throws IOException {
        if (segmentCount == null) {
            if (segmentInputStream == null) {
                segmentInputStream = new RASegmentInputStream(data, indexInputStreamSupplier);
            }
            segmentCount = segmentInputStream.count();
        }
        return segmentCount;
    }

    private void checkNotClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream has been closed and no more entries allowed");
        }
    }

    private void checkOpenEntry() throws IOException {
        if (currentEntryClosed) {
            throw new IOException("Nested stream is not open");
        }
    }

    @Override
    public int read() throws IOException {
        checkOpenEntry();
        return segmentInputStream.read();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int read(byte[] b) throws IOException {
        checkOpenEntry();
        return segmentInputStream.read(b);
    }

    @Override
    public void close() throws IOException {
        closed = true;
        try {
            streamCloser.close();
        } finally {
            super.close();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkOpenEntry();
        return segmentInputStream.read(b, off, len);
    }

    @Override
    public void mark(int readlimit) {
        if (segmentInputStream != null) {
            segmentInputStream.mark(readlimit);
        }
    }

    @Override
    public int available() throws IOException {
        checkOpenEntry();
        return segmentInputStream.available();
    }

    @Override
    public boolean markSupported() {
        if (segmentInputStream != null) {
            return segmentInputStream.markSupported();
        }
        return false;
    }

    @Override
    public void reset() throws IOException {
        checkOpenEntry();
        segmentInputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        checkOpenEntry();
        return segmentInputStream.skip(n);
    }
}
