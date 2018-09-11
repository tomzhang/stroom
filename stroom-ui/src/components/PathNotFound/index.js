/*
 * Copyright 2017 Crown Copyright
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

import React from 'react';
import PropTypes from 'prop-types';
import { Grid, Header, Icon } from 'semantic-ui-react';

const PathNotFound = ({ message }) => (
  <React.Fragment>
    <Grid className="content-tabs__grid">
      <Grid.Column width={12}>
        <Header as="h3">
          <Icon name="exclamation triangle" />
          <Header.Content>Not Found</Header.Content>
        </Header>
      </Grid.Column>
    </Grid>
    <div className="content-floating-without-appbar">
      <div className="PathNotFound-card">
        <h3>Page not found!</h3>
        <p>{message}</p>
      </div>
    </div>
  </React.Fragment>
);

PathNotFound.propTypes = {
  message: PropTypes.string.isRequired,
};

PathNotFound.defaultProps = {
  message: "There's nothing here I'm afraid.",
};

export default PathNotFound;
