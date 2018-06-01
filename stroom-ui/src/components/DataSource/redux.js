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
import { createAction, handleActions } from 'redux-actions';

// Data Sources
const receiveDataSource = createAction('RECEIVE_DATA_SOURCE', (uuid, dataSource) => ({
  uuid,
  dataSource,
}));

// data source definitions, keyed on doc ref UUID
const defaultDataSourceState = {};

const dataSourceReducer = handleActions(
  {
    [receiveDataSource]: (state, action) => ({
      ...state,
      [action.payload.uuid]: action.payload.dataSource,
    }),
  },
  defaultDataSourceState,
);

export { receiveDataSource, dataSourceReducer };
