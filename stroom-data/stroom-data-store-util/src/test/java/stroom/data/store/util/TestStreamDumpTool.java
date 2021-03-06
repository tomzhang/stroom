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

package stroom.data.store.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import stroom.data.shared.StreamTypeNames;
import stroom.data.store.api.Store;
import stroom.data.store.api.Target;
import stroom.data.store.api.TargetUtil;
import stroom.meta.impl.db.MetaDbConnProvider;
import stroom.meta.shared.MetaProperties;
import stroom.test.common.util.db.DbTestUtil;
import stroom.test.common.util.db.DbTestModule;
import stroom.test.common.util.test.FileSystemTestUtil;
import stroom.test.common.util.test.TempDir;
import stroom.test.common.util.test.TempDirExtension;
import stroom.util.io.FileUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

@ExtendWith({TempDirExtension.class, MockitoExtension.class})
class TestStreamDumpTool {

    @Inject
    private MetaDbConnProvider metaDbConnProvider;

    @Inject
    private Store streamStore;

    @Mock
    private ToolInjector toolInjector;

    @BeforeEach
    void setup() {
        final Injector injector = Guice.createInjector(new DbTestModule(), new ToolModule());
        injector.injectMembers(this);

        Mockito.when(toolInjector.getInjector())
                .thenReturn(injector);

        try (final Connection connection = metaDbConnProvider.getConnection()) {
            DbTestUtil.clearAllTables(connection);
        } catch (final SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Test
    void test(@TempDir Path tempDir) {
        final String feedName = FileSystemTestUtil.getUniqueTestString();

        try {
            addData(feedName, "This is some test data to dump1");
            addData(feedName, "This is some test data to dump2");
            addData(feedName, "This is some test data to dump3");

            final StreamDumpTool streamDumpTool = new StreamDumpTool(toolInjector);
            streamDumpTool.setFeed(feedName);
            streamDumpTool.setOutputDir(FileUtil.getCanonicalPath(tempDir));
            streamDumpTool.run();

        } catch (final RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void addData(final String feedName, final String data) {
        final MetaProperties metaProperties = new MetaProperties.Builder()
                .feedName(feedName)
                .typeName(StreamTypeNames.RAW_EVENTS)
                .build();
        try (final Target streamTarget = streamStore.openTarget(metaProperties)) {
            TargetUtil.write(streamTarget, data);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
