package stroom.config.global.impl;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.config.app.AppConfig;
import stroom.config.app.ConfigLocation;
import stroom.config.app.YamlUtil;
import stroom.util.HasHealthCheck;
import stroom.util.config.FieldMapper;
import stroom.util.logging.LogUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@Singleton
public class AppConfigMonitor implements Managed, HasHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigMonitor.class);

    private final AppConfig appConfig;
    private final Path configFile;
    private final Path dirToWatch;
    private final ExecutorService executorService;
    private WatchService watchService = null;
    private Future<?> watcherFuture = null;
    private final ConfigMapper configMapper;
    private boolean isRunning = false;
    private final boolean isValidFile;

    @Inject
    public AppConfigMonitor(final AppConfig appConfig,
                            final ConfigLocation configLocation,
                            final ConfigMapper configMapper) {
        this.appConfig = appConfig;
        this.configFile = configLocation.getConfigFilePath();
        this.configMapper = configMapper;

        if (Files.isRegularFile(configFile)) {
            isValidFile = true;

            dirToWatch = configFile.getParent();
            if (!Files.isDirectory(dirToWatch)) {
                throw new RuntimeException(LogUtil.message("{} is not a directory", dirToWatch));
            }
            executorService = Executors.newSingleThreadExecutor();
        } else {
            isValidFile = false;
            dirToWatch = null;
            executorService = null;
        }
    }


    /**
     * Starts the object. Called <i>before</i> the application becomes available.
     *
     * @throws Exception if something goes wrong; this will halt the application startup.
     */
    @Override
    public void start() throws Exception {
        if (isValidFile) {
            try {
                startWatcher();
            } catch (Exception e) {
                LOGGER.error("Unable to start config file monitor due to [{}]. Changes to {} will not be monitored.",
                        e.getMessage(), configFile.toAbsolutePath().normalize(), e);
            }
        } else {
            LOGGER.error("Unable to start watcher as {} is not a valid file", configFile.toAbsolutePath().normalize());
        }
    }

    private void startWatcher() throws IOException {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(LogUtil.message("Error creating watch new service, {}", e.getMessage()), e);
        }

        dirToWatch.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        // run the watcher in its own thread else it will block app startup
        watcherFuture = executorService.submit(() -> {
            WatchKey watchKey = null;

            LOGGER.info("Starting config file modification watcher for {}", configFile.toAbsolutePath().normalize());
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    LOGGER.debug("Thread interrupted, stopping watching directory {}", dirToWatch.toAbsolutePath());
                    break;
                }

                try {
                    // block until the watch service spots a change
                    watchKey = watchService.take();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    // continue to re-use the if block above
                    continue;
                }

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind().equals(OVERFLOW)) {
                        break;
                    }
                    handleWatchEvent((WatchEvent<Path>) event);
                }
                boolean isValid = watchKey.reset();
                if (!isValid) {
                    LOGGER.warn("Watch key is no longer valid, the watch service may have been stopped");
                    break;
                }
            }
        });
        isRunning = true;
    }

    private void handleWatchEvent(final WatchEvent<Path> event) {
        final WatchEvent<Path> pathEvent = event;
        final WatchEvent.Kind<Path> kind = pathEvent.kind();
        LOGGER.debug("Dir watch event {}, {}, {}", kind.name(), kind.type(), pathEvent.context());

        if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            final Path modifiedFile = dirToWatch.resolve(pathEvent.context());

            try {
                // we don't care about changes to other files
                if (Files.isRegularFile(modifiedFile) && Files.isSameFile(configFile, modifiedFile)) {
                    updateAppConfigFromFile();
                }
            } catch (IOException e) {
                // Swallow error so future changes can be monitored.
                LOGGER.error("Error comparing paths {} and {}", configFile, modifiedFile, e);
            }
        }
    }

    private void updateAppConfigFromFile() {
        final AppConfig newAppConfig;
        try {
            LOGGER.info("Change detected to file {}, updating application config.",
                    configFile.toAbsolutePath().normalize());
            newAppConfig = YamlUtil.readAppConfig(configFile);

            try {
                // TODO this needs to take into account the CommonDbConfig copying that goes on in AppConfigModule,
                // i.e. read the yaml then do the db config copying then copy the values into the appConfig

                // Copy changed values from the newly modified appConfig into the guice bound one
                FieldMapper.copy(newAppConfig, this.appConfig);

            } catch (Throwable e) {
                // Swallow error as we don't want to break the app because the new config is bad
                // The admins can fix the problem and let it have another go.
                LOGGER.error("Error updating runtime configuration from file {}", configFile.toAbsolutePath(), e);
            }
        } catch (Throwable e) {
            // Swallow error as we don't want to break the app because the file is bad.
            LOGGER.error("Error parsing configuration from file {}", configFile.toAbsolutePath(), e);
        }
    }


    /**
     * Stops the object. Called <i>after</i> the application is no longer accepting requests.
     *
     * @throws Exception if something goes wrong.
     */
    @Override
    public void stop() throws Exception {
        if (isValidFile) {
            LOGGER.info("Stopping file modification watcher for {}", configFile.toAbsolutePath());

            if (watchService != null) {
                watchService.close();
            }
            if (executorService != null) {
                watchService.close();
                if (watcherFuture != null && !watcherFuture.isCancelled() && !watcherFuture.isDone()) {
                    watcherFuture.cancel(true);
                }
                executorService.shutdown();
            }
        }
        isRunning = false;
    }

    @Override
    public HealthCheck.Result getHealth() {
        HealthCheck.ResultBuilder resultBuilder = HealthCheck.Result.builder();

        if (isRunning) {
            resultBuilder.healthy();
        } else {
            resultBuilder.unhealthy();
        }

        return resultBuilder
                .withDetail("configFilePath", configFile)
                .withDetail("isRunning", isRunning)
                .withDetail("isValidFile", isValidFile)
                .build();
    }
}
