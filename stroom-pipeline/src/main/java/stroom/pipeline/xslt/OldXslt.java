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

package stroom.pipeline.xslt;

import stroom.importexport.migration.DocumentEntity;
import stroom.importexport.shared.ExternalFile;

/**
 * Used for legacy migration
 **/
@Deprecated
public class OldXslt extends DocumentEntity {
    private String description;
    private String data;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @ExternalFile("xsl")
    public String getData() {
        return data;
    }


    public void setData(final String data) {
        this.data = data;
    }
}
