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

package stroom.util.shared;

import java.io.Serializable;
import java.util.Objects;

public class PageRequest implements Serializable, Copyable<PageRequest> {
    public static final int DEFAULT_PAGE_SIZE = 100;
    private static final long serialVersionUID = 6838082084157676358L;
    /**
     * Offset from the start 0 is no offset.
     */
    private Long offset;
    /**
     * Page size to use, e.g. 10 is 10 records
     */
    private Integer length;

    public PageRequest() {
        this(0L, null);
    }

    public PageRequest(final Long offset) {
        this(offset, DEFAULT_PAGE_SIZE);
    }

    public PageRequest(final Long offset, final Integer length) {
        this.offset = offset;
        this.length = length;
    }

    public static PageRequest createBoundedPageRequest(final long offset, final int length) {
        final PageRequest request = new PageRequest();
        request.setOffset(offset);
        request.setLength(length);
        return request;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(final Long offset) {
        this.offset = offset;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(final Integer maxLength) {
        this.length = maxLength;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PageRequest that = (PageRequest) o;
        return Objects.equals(offset, that.offset) &&
                Objects.equals(length, that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "offset=" + offset +
                ", length=" + length +
                '}';
    }

    @Override
    public void copyFrom(final PageRequest other) {
        this.offset = other.offset;
        this.length = other.length;
    }
}
