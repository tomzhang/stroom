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

package stroom.dashboard.impl;

import stroom.dashboard.shared.ValidateExpressionAction;
import stroom.dashboard.shared.ValidateExpressionResult;
import stroom.task.api.AbstractTaskHandler;

import javax.inject.Inject;

class ValidateExpressionHandler extends AbstractTaskHandler<ValidateExpressionAction, ValidateExpressionResult> {
    private final SearchService searchService;

    @Inject
    ValidateExpressionHandler(final SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public ValidateExpressionResult exec(final ValidateExpressionAction action) {
        return searchService.validateExpression(action.getExpression());
    }
}
