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

package stroom.cluster.task.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import stroom.cluster.api.ClusterCallService;
import stroom.cluster.api.ClusterCallServiceRemote;
import stroom.cluster.api.ServiceName;
import stroom.cluster.task.api.ClusterDispatchAsync;
import stroom.cluster.task.api.ClusterResultCollector;
import stroom.cluster.task.api.ClusterTask;
import stroom.cluster.task.api.CollectorId;
import stroom.docref.SharedObject;
import stroom.task.api.GenericServerTask;
import stroom.task.api.TaskManager;
import stroom.task.impl.CurrentTaskState;
import stroom.task.shared.SimpleThreadPool;
import stroom.task.shared.Task;
import stroom.task.shared.TaskId;
import stroom.task.shared.ThreadPool;
import stroom.util.logging.LogExecutionTime;
import stroom.util.shared.ModelStringUtil;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Entry to point to distribute cluster tasks in system.
 */
public class ClusterDispatchAsyncImpl implements ClusterDispatchAsync {
    static final ServiceName SERVICE_NAME = new ServiceName("clusterDispatchAsync");
    static final String RECEIVE_RESULT_METHOD = "receiveResult";
    private static final ThreadPool THREAD_POOL = new SimpleThreadPool(5);
    static final Class<?>[] RECEIVE_RESULT_METHOD_ARGS = {ClusterTask.class, String.class, TaskId.class,
            CollectorId.class, SharedObject.class, Throwable.class, Boolean.class};
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterDispatchAsyncImpl.class);
    private static final String RECEIVE_RESULT = "receiveResult";

    private final TaskManager taskManager;
    private final ClusterResultCollectorCacheImpl collectorCache;
    private final ClusterCallService clusterCallService;

    @Inject
    ClusterDispatchAsyncImpl(final TaskManager taskManager,
                             final ClusterResultCollectorCacheImpl collectorCache,
                             final ClusterCallServiceRemote clusterCallService) {
        this.taskManager = taskManager;
        this.collectorCache = collectorCache;
        this.clusterCallService = clusterCallService;
    }

    @Override
    public <R extends SharedObject> void execAsync(final ClusterTask<R> task,
                                                   final ClusterResultCollector<R> collector,
                                                   final String sourceNode,
                                                   final Set<String> targetNodes) {
//        // Try and discover the parent task for this task as one hasn't been
//        // supplied.
//        if (!TaskScopeContextHolder.contextExists()) {
//            throw new IllegalStateException("Task scope context does not exist!");
//        }

        final Task<?> parentTask = CurrentTaskState.currentTask();
        execAsync(parentTask, task, collector, sourceNode, targetNodes);
    }

    private <R extends SharedObject> void execAsync(final Task<?> sourceTask,
                                                    final ClusterTask<R> clusterTask,
                                                    final ClusterResultCollector<R> collector,
                                                    final String sourceNode,
                                                    final Set<String> targetNodes) {
        if (sourceTask == null) {
            throw new NullPointerException("A source task must be provided");
        }
        if (taskManager.isTerminated(sourceTask.getId())) {
            throw new RuntimeException("Task has been terminated");
        }
        if (sourceTask.getId() == null) {
            throw new NullPointerException("Null source task id");
        }
        if (clusterTask == null) {
            throw new NullPointerException("A cluster task must be provided");
        }
        if (collector == null) {
            throw new NullPointerException("A collector must be provided");
        }
        if (collector.getId() == null) {
            throw new NullPointerException("Null collector id");
        }
        if (sourceNode == null) {
            throw new NullPointerException("Null source node");
        }
        if (targetNodes == null || targetNodes.size() == 0) {
            throw new NullPointerException("No target nodes");
        }

        if (clusterTask.getTaskName() == null) {
            // Can't be done in the UI as GWT knows nothing about class etc.
            clusterTask.setTaskName("Cluster Task: " + ModelStringUtil.toDisplayValue(clusterTask.getClass().getSimpleName()));
        }

        final TaskId sourceTaskId = sourceTask.getId();
        final CollectorId collectorId = collector.getId();
        for (final String targetNode : targetNodes) {
            if (targetNode == null) {
                throw new NullPointerException("Null target node?");
            }

            // Create a task to make the cluster call.
            final String message = "Calling node '" +
                    targetNode +
                    "' for task '" +
                    clusterTask.getTaskName() +
                    "'";

            final GenericServerTask clusterCallTask = GenericServerTask.create(sourceTask, clusterTask.getUserToken(), "Cluster call", message);
            // Create a runnable so we can execute the remote call
            // asynchronously.
            clusterCallTask.setRunnable(() -> {
                try {
                    clusterCallService.call(sourceNode, targetNode, ClusterWorkerImpl.SERVICE_NAME,
                            ClusterWorkerImpl.EXEC_ASYNC_METHOD, ClusterWorkerImpl.EXEC_ASYNC_METHOD_ARGS,
                            new Object[]{clusterTask, sourceNode, sourceTaskId, collectorId});
                } catch (final RuntimeException e) {
                    LOGGER.debug(e.getMessage(), e);
                    collector.onFailure(targetNode, e);
                }
            });

            // Execute the cluster call asynchronously so we don't block calls
            // to other nodes.
            LOGGER.trace(message);
            taskManager.execAsync(clusterCallTask, THREAD_POOL);
        }
    }

    /**
     * This method receives results from worker nodes that have executed tasks
     * using the <code>execAsync()</code> method above. Received results are
     * processed by the named collector in a new task thread so that result
     * consumption does not hold on to the HTTP connection.
     *
     * @param task         The task that was executed on the target worker node.
     * @param targetNode   The worker node that is returning the result.
     * @param sourceTaskId The id of the parent task that owns this worker cluster task.
     * @param collectorId  The id of the collector to send results back to.
     * @param result       The result of the remote task execution.
     * @param throwable    An exception that may have been thrown during remote task
     *                     execution in the result of task failure.
     * @param success      Whether or not the remote task executed successfully.
     */
    //This method is * executed by Guice using a named bean/method, hence 'unused' suppression
    @SuppressWarnings({"unchecked", "unused"})
    public <R extends SharedObject> Boolean receiveResult(final ClusterTask<R> task,
                                                          final String targetNode,
                                                          final TaskId sourceTaskId,
                                                          final CollectorId collectorId,
                                                          final R result,
                                                          final Throwable throwable,
                                                          final Boolean success) {
        final AtomicBoolean successfullyReceived = new AtomicBoolean();

        DebugTrace.debugTraceIn(task, RECEIVE_RESULT, success);
        try {
            LOGGER.debug("{}() - {} {}", RECEIVE_RESULT, task, targetNode);

            // Get the source id and check it is valid.
            if (sourceTaskId == null) {
                throw new NullPointerException("No source id");
            }

            // Try and get an active task for this source task id.
            final Task<?> sourceTask = taskManager.getTaskById(sourceTaskId);
            if (sourceTask == null || taskManager.isTerminated(sourceTaskId)) {
                // If we can't get an active source task then ignore the result
                // as we don't want to keep using the collector as it might not
                // have gone from the cache for some reason and we will just end
                // up keeping it alive.
                LOGGER.warn("Source task has terminated. Ignoring result...");

            } else {
                // See if we can get a collector for this result.
                final ClusterResultCollector<R> collector = (ClusterResultCollector<R>) collectorCache.get(collectorId);
                if (collector == null) {
                    // There is no collector to receive this result.
                    LOGGER.error("{}() - collector gone away - {} {}", RECEIVE_RESULT, task.getTaskName(), sourceTask);

                } else {
                    // Make sure the collector is happy to receive this result.
                    // It might not be if it was told to wait and it's wait time
                    // elapsed.
                    if (collector.onReceive()) {
                        // Build a task description.
                        final StringBuilder sb = new StringBuilder();
                        if (success) {
                            sb.append("Receiving success from ");
                        } else {
                            sb.append("Receiving failure from ");
                        }
                        sb.append(" '");
                        sb.append(targetNode);
                        sb.append("' for task '");
                        sb.append(task.getTaskName());
                        sb.append("'");
                        final String message = sb.toString();

                        final GenericServerTask genericServerTask = GenericServerTask.create(sourceTask, task.getUserToken(), "Cluster result", message);
                        genericServerTask.setRunnable(() -> {
                            final LogExecutionTime logExecutionTime = new LogExecutionTime();
                            try {
                                if (success) {
                                    collector.onSuccess(targetNode, result);
                                } else {
                                    collector.onFailure(targetNode, throwable);
                                }
                            } finally {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("{}() - collector {} {} took {}", RECEIVE_RESULT, task.getTaskName(), sourceTask, logExecutionTime);
                                }
                                if (logExecutionTime.getDuration() > 1000) {
                                    LOGGER.warn("{}() - collector {} {} took {}", RECEIVE_RESULT, task.getTaskName(), sourceTask, logExecutionTime);
                                }
                            }
                        });

                        // Execute the task asynchronously so that we do not
                        // block the receipt of data which would hold on to the
                        // HTTP connection longer than necessary.
                        LOGGER.trace(message);
                        taskManager.execAsync(genericServerTask, THREAD_POOL);
                        successfullyReceived.set(true);
                    }
                }
            }
        } catch (final RuntimeException e) {
            LOGGER.error(MarkerFactory.getMarker("FATAL"), e.getMessage(), e);

        } finally {
            DebugTrace.debugTraceOut(task, RECEIVE_RESULT, success);
        }

        return successfullyReceived.get();
    }
}
