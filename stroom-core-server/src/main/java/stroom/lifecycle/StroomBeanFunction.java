package stroom.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.task.shared.Task;
import stroom.util.lifecycle.jobmanagement.ScheduledJob;

import java.util.concurrent.atomic.AtomicBoolean;

public class StroomBeanFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomBeanMethodExecutable.class);

    private final AtomicBoolean running;
    private ScheduledJob scheduledJob;

    public StroomBeanFunction(final ScheduledJob scheduledJob) {
        this(scheduledJob, new AtomicBoolean());
        this.scheduledJob = scheduledJob;
    }

    public StroomBeanFunction(final ScheduledJob scheduledJob,  final AtomicBoolean running) {
        this.scheduledJob = scheduledJob;
        this.running = running;
    }

    public void exec(final Task<?> task) {
        try {
            //TODO: debug logging
//            LOGGER.debug(message + " " + methodReference.getClazz().getName() + "." + methodReference.getMethod().getName());

            scheduledJob.getMethod().accept(task);
        } catch (final RuntimeException e) {
            LOGGER.error("Error calling {}", scheduledJob.getName(), e);
        } finally {
            running.set(false);
        }
    }

    @Override
    public String toString() {
        return scheduledJob.toString();
    }
}
