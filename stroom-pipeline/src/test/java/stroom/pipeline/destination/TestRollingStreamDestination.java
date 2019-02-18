/*
 * Copyright 2018 Crown Copyright
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

package stroom.pipeline.destination;


import org.junit.jupiter.api.Test;
import stroom.meta.impl.mock.MockMetaService;
import stroom.meta.shared.MetaProperties;
import stroom.data.store.api.Target;
import stroom.data.store.impl.mock.MockStreamStore;
import stroom.streamstore.shared.StreamTypeNames;
import stroom.util.date.DateUtil;
import stroom.util.scheduler.SimpleCron;

import javax.inject.Inject;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TestRollingStreamDestination {
    private MockStreamStore streamStore = new MockStreamStore(new MockMetaService());

    @Test
    void testFrequency() throws IOException {
        final long time = DateUtil.parseNormalDateTimeString("2010-01-01T00:00:00.000Z");
        final MetaProperties dataProperties = new MetaProperties.Builder().typeName(StreamTypeNames.EVENTS).build();
        final Target streamTarget = streamStore.openStreamTarget(dataProperties);
        final StreamKey streamKey = new StreamKey("test", StreamTypeNames.EVENTS, false);
        final RollingStreamDestination rollingStreamDestination = new RollingStreamDestination(streamKey,
                60000L,
                null,
                100,
                time,
                streamStore,
                streamTarget,
                "test");

        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time)).isFalse();
        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time + 60000)).isFalse();
        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time + 60001)).isTrue();
    }

    @Test
    void testSchedule() throws IOException {
        final long time = DateUtil.parseNormalDateTimeString("2010-01-01T00:00:00.000Z");
        final MetaProperties dataProperties = new MetaProperties.Builder().typeName(StreamTypeNames.EVENTS).build();
        final Target streamTarget = streamStore.openStreamTarget(dataProperties);
        final StreamKey streamKey = new StreamKey("test", StreamTypeNames.EVENTS, false);
        final RollingStreamDestination rollingStreamDestination = new RollingStreamDestination(streamKey,
                null,
                SimpleCron.compile("* * *"),
                100,
                time,
                streamStore,
                streamTarget,
                "test");

        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time)).isFalse();
        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time + 60000)).isFalse();
        assertThat(rollingStreamDestination.tryFlushAndRoll(false, time + 60001)).isTrue();
    }
}