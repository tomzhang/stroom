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

package stroom.importexport.impl;

import stroom.importexport.shared.ImportConfigConfirmationAction;
import stroom.importexport.shared.ImportState;
import stroom.resource.api.ResourceStore;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.shared.SharedList;

import javax.inject.Inject;


class ImportConfigConfirmationHandler
        extends AbstractTaskHandler<ImportConfigConfirmationAction, SharedList<ImportState>> {
    private final ImportExportService importExportService;
    private final ResourceStore resourceStore;

    @Inject
    ImportConfigConfirmationHandler(final ImportExportService importExportService,
                                    final ResourceStore resourceStore) {
        this.importExportService = importExportService;
        this.resourceStore = resourceStore;
    }

    @Override
    public SharedList<ImportState> exec(final ImportConfigConfirmationAction task) {
        try {
            return importExportService.createImportConfirmationList(resourceStore.getTempFile(task.getKey()));
        } catch (final RuntimeException rex) {
            // In case of error delete the temp file
            resourceStore.deleteTempFile(task.getKey());
            throw rex;
        }
    }
}
