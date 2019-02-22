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

package stroom.streamtask;

import com.google.inject.AbstractModule;
import stroom.task.api.TaskHandlerBinder;
import stroom.util.GuiceUtil;
import stroom.util.shared.Clearable;

public class MockStreamTaskModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StreamTaskCreator.class).to(MockStreamTaskCreator.class);
        bind(StreamProcessorService.class).to(MockStreamProcessorService.class);
        bind(StreamProcessorFilterService.class).to(MockStreamProcessorFilterService.class);
        bind(StreamTaskService.class).to(MockStreamTaskService.class);

        bind(CachedStreamProcessorService.class).to(MockStreamProcessorService.class);
        bind(CachedStreamProcessorFilterService.class).to(MockStreamProcessorFilterService.class);

        GuiceUtil.buildMultiBinder(binder(), Clearable.class)
                .addBinding(MockStreamProcessorService.class)
                .addBinding(MockStreamProcessorFilterService.class)
                .addBinding(MockStreamTaskService.class);

        TaskHandlerBinder.create(binder())
                .bind(StreamProcessorTask.class, StreamProcessorTaskHandler.class);
    }
}