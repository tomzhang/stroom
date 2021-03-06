/*
 * Copyright 2019 Crown Copyright
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

import stroom.cluster.lock.api.ClusterLockService;
import stroom.data.store.impl.fs.DataVolumeDao.DataVolume;
import stroom.meta.api.PhysicalDelete;
import stroom.meta.shared.FindMetaCriteria;
import stroom.meta.shared.Meta;
import stroom.meta.shared.MetaFields;
import stroom.meta.shared.MetaService;
import stroom.meta.shared.Status;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.ExpressionTerm.Condition;
import stroom.task.api.TaskContext;
import stroom.util.date.DateUtil;
import stroom.util.io.FileUtil;
import stroom.util.logging.LambdaLogUtil;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;
import stroom.util.logging.LogExecutionTime;
import stroom.util.shared.ModelStringUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PhysicalDeleteExecutor {
    private static final LambdaLogger LOGGER = LambdaLoggerFactory.getLogger(PhysicalDeleteExecutor.class);

    private static final String TASK_NAME = "Fs Delete Executor";
    private static final String LOCK_NAME = "FsDeleteExecutor";

    private final ClusterLockService clusterLockService;
    private final DataStoreServiceConfig dataStoreServiceConfig;
    private final FsPathHelper fileSystemStreamPathHelper;
    private final MetaService metaService;
    private final PhysicalDelete physicalDelete;
    private final DataVolumeDao dataVolumeDao;
    private final TaskContext taskContext;

    @Inject
    PhysicalDeleteExecutor(
            final ClusterLockService clusterLockService,
            final DataStoreServiceConfig dataStoreServiceConfig,
            final FsPathHelper fileSystemStreamPathHelper,
            final MetaService metaService,
            final PhysicalDelete physicalDelete,
            final DataVolumeDao dataVolumeDao,
            final TaskContext taskContext) {
        this.clusterLockService = clusterLockService;
        this.dataStoreServiceConfig = dataStoreServiceConfig;
        this.fileSystemStreamPathHelper = fileSystemStreamPathHelper;
        this.metaService = metaService;
        this.physicalDelete = physicalDelete;
        this.dataVolumeDao = dataVolumeDao;
        this.taskContext = taskContext;
    }

    public void exec() {
        lockAndDelete();
    }

    final void lockAndDelete() {
        LOGGER.info(() -> TASK_NAME + " - start");
        clusterLockService.tryLock(LOCK_NAME, () -> {
            try {
                if (!Thread.currentThread().isInterrupted()) {
                    final LogExecutionTime logExecutionTime = new LogExecutionTime();
                    final long age = getDeleteAge(dataStoreServiceConfig);
                    if (age > 0) {
                        delete(age);
                    }
                    LOGGER.info(() -> TASK_NAME + " - finished in " + logExecutionTime);
                }
            } catch (final RuntimeException e) {
                LOGGER.error(e::getMessage, e);
            }
        });
    }

    public void delete(final long age) {
        if (!Thread.currentThread().isInterrupted()) {
            long count = 0;
            long total = 0;

            final LogExecutionTime logExecutionTime = new LogExecutionTime();

            final int deleteBatchSize = dataStoreServiceConfig.getDeleteBatchSize();

            if (!Thread.currentThread().isInterrupted()) {
                do {
                    // Insert a batch of ids into the temp id table and find out
                    // how many were inserted.
                    final List<Meta> idList = getDeleteIdList(age, deleteBatchSize);
                    count = idList.size();

                    // If we inserted some ids then try and delete this batch.
                    if (count > 0) {
                        total += count;
                        deleteCurrentBatch(idList);
                    }
                } while (!Thread.currentThread().isInterrupted() && count >= deleteBatchSize);
            }

            LOGGER.debug(LambdaLogUtil.message("Deleted {} streams in {}.", total, logExecutionTime));
        }
    }

    private void deleteCurrentBatch(final List<Meta> metaList) {
        try {
            // Delete all matching files.
            for (final Meta meta : metaList) {
                info(() -> "Deleting everything associated with " + meta);

                final DataVolume dataVolume = dataVolumeDao.findDataVolume(meta.getId());
                if (dataVolume == null) {
                    LOGGER.warn(() -> "Unable to find any volume for " + meta);
                } else {
                    final Path file = fileSystemStreamPathHelper.getRootPath(dataVolume.getVolumePath(), meta, meta.getTypeName());
                    final Path dir = file.getParent();
                    String baseName = file.getFileName().toString();
                    baseName = baseName.substring(0, baseName.indexOf("."));

                    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir, baseName + ".*")) {
                        stream.forEach(f -> {
                            try {
                                info(() -> "Deleting file: " + FileUtil.getCanonicalPath(f));
                                Files.deleteIfExists(f);

                            } catch (final InterruptedException e) {
                                LOGGER.debug(e::getMessage, e);

                                // Continue to interrupt.
                                Thread.currentThread().interrupt();

                                throw new RuntimeException(e);

                            } catch (final IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            final List<Long> metaIdList = metaList.stream().map(Meta::getId).collect(Collectors.toList());

            // Delete meta volumes.
            info(() -> "Deleting data volumes");
            dataVolumeDao.delete(metaIdList);

            // Physically delete meta data.
            info(() -> "Deleting meta data");
            physicalDelete.cleanup(metaIdList);

        } catch (final InterruptedException e) {
            LOGGER.debug(e::getMessage, e);

            // Continue to interrupt.
            Thread.currentThread().interrupt();
        }
    }

    private void info(final Supplier<String> message) throws InterruptedException {
        try {
            taskContext.info(message);
            LOGGER.debug(message);
        } catch (final RuntimeException e) {
            LOGGER.error(e::getMessage, e);
        }
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private List<Meta> getDeleteIdList(final long age, final int batchSize) {
        final ExpressionOperator expression = new ExpressionOperator.Builder()
                .addTerm(MetaFields.STATUS, Condition.EQUALS, Status.DELETED.getDisplayValue())
                .addTerm(MetaFields.STATUS_TIME, Condition.LESS_THAN, DateUtil.createNormalDateTimeString(age))
                .build();

        final FindMetaCriteria criteria = new FindMetaCriteria(expression);
        criteria.setSort(MetaFields.ID.getDisplayValue());
        criteria.obtainPageRequest().setLength(batchSize);

        return metaService.find(criteria);
    }

    private Long getDeleteAge(final DataStoreServiceConfig config) {
        Long age = null;
        final String durationString = config.getDeletePurgeAge();
        if (durationString != null && !durationString.isEmpty()) {
            try {
                final long duration = ModelStringUtil.parseDurationString(durationString);
                age = System.currentTimeMillis() - duration;
            } catch (final RuntimeException e) {
                LOGGER.error(() -> "Error reading config");
            }
        }
        return age;
    }
}
