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

import stroom.meta.shared.Meta;
import stroom.util.shared.BaseCriteria;
import stroom.util.shared.CriteriaSet;

public class FindDataVolumeCriteria extends BaseCriteria {
    private static final long serialVersionUID = 3528656425356870590L;

//    private StreamRange streamRange;
//
//    private CriteriaSet<StreamStatus> streamStatusSet = null;
//    private CriteriaSet<Long> nodeIdSet = null;
    private CriteriaSet<Integer> volumeIdSet = null;
    private CriteriaSet<Long> metaIdSet = null;

    public static FindDataVolumeCriteria create(final Meta meta) {
        FindDataVolumeCriteria rtn = new FindDataVolumeCriteria();
        rtn.obtainMetaIdSet().add(meta.getId());
        return rtn;
    }

    public boolean isValidCriteria() {
        if (metaIdSet != null && metaIdSet.isConstrained()) {
            return true;
        }
        return false;
//        return streamRange != null && streamRange.isFileLocation();
    }

//    public StreamRange getStreamRange() {
//        return streamRange;
//    }
//
//    public void setStreamRange(StreamRange streamRange) {
//        this.streamRange = streamRange;
//    }
//
//    public CriteriaSet<Long> getNodeIdSet() {
//        return nodeIdSet;
//    }
//
//    public CriteriaSet<Long> obtainNodeIdSet() {
//        if (nodeIdSet == null) {
//            nodeIdSet = new CriteriaSet<>();
//        }
//        return nodeIdSet;
//    }

    public CriteriaSet<Long> getMetaIdSet() {
        return metaIdSet;
    }

    public CriteriaSet<Long> obtainMetaIdSet() {
        if (metaIdSet == null) {
            metaIdSet = new CriteriaSet<>();
        }
        return metaIdSet;
    }

    public CriteriaSet<Integer> getVolumeIdSet() {
        return volumeIdSet;
    }

    public CriteriaSet<Integer> obtainVolumeIdSet() {
        if (volumeIdSet == null) {
            volumeIdSet = new CriteriaSet<>();
        }
        return volumeIdSet;
    }

//    public CriteriaSet<StreamStatus> getStreamStatusSet() {
//        return streamStatusSet;
//    }
//
//    public CriteriaSet<StreamStatus> obtainStreamStatusSet() {
//        if (streamStatusSet == null) {
//            streamStatusSet = new CriteriaSet<>();
//        }
//        return streamStatusSet;
//    }
}
