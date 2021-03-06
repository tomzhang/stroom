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

package stroom.pipeline.xmlschema;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import stroom.docstore.api.DocumentActionHandlerBinder;
import stroom.importexport.api.ImportExportActionHandler;
import stroom.xmlschema.shared.XmlSchemaDoc;

// TODO: What's this doing in main? I think it should be in test in stroom-app.
public class MockXmlSchemaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(XmlSchemaStore.class).to(XmlSchemaStoreImpl.class);

//        final Multibinder<Handler> entityEventHandlerBinder = Multibinder.newSetBinder(binder(), Handler.class);
//        entityEventHandlerBinder.addBinding().to(XMLSchemaCache.class);
//
//        final Multibinder<ExplorerActionHandler> explorerActionHandlerBinder = Multibinder.newSetBinder(binder(), ExplorerActionHandler.class);
//        explorerActionHandlerBinder.addBinding().to(XmlSchemaStoreImpl.class);

//        final Multibinder<Clearable> clearableBinder = Multibinder.newSetBinder(binder(), Clearable.class);
//        clearableBinder.addBinding().to(XmlSchemaStoreImpl.class);

        final Multibinder<ImportExportActionHandler> importExportActionHandlerBinder = Multibinder.newSetBinder(binder(), ImportExportActionHandler.class);
        importExportActionHandlerBinder.addBinding().to(XmlSchemaStoreImpl.class);

        DocumentActionHandlerBinder.create(binder())
                .bind(XmlSchemaDoc.DOCUMENT_TYPE, XmlSchemaStoreImpl.class);

//        final Multibinder<FindService> findServiceBinder = Multibinder.newSetBinder(binder(), FindService.class);
//        findServiceBinder.addBinding().to(XmlSchemaStoreImpl.class);
    }
}