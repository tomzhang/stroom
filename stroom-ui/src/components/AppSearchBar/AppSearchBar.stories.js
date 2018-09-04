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
import React from 'react';
import { connect } from 'react-redux';
import { compose, withState } from 'recompose';
import { Field, reduxForm } from 'redux-form';
import { Form } from 'semantic-ui-react';

import { storiesOf, addDecorator } from '@storybook/react';
import StoryRouter from 'storybook-react-router';
import { ReduxDecorator } from 'lib/storybook/ReduxDecorator';
import { ThemedDecorator } from 'lib/storybook/ThemedDecorator';
import { KeyIsDownDecorator } from 'lib/storybook/KeyIsDownDecorator';
import { PollyDecoratorWithTestData } from 'lib/storybook/PollyDecoratorWithTestData';

import AppSearchBar from './AppSearchBar';

import 'styles/main.css';
import 'semantic/dist/semantic.min.css';

const enhanceForm = compose(
  connect(
    ({ form }) => ({
      thisForm: form.appSearchBarTest,
    }),
    {},
  ),
  reduxForm({
    form: 'appSearchBarTest',
  }),
);

const RawAppSearchAsPickerForm = ({ pickerId, typeFilters, thisForm }) => (
  <Form>
    <Form.Field>
      <label>Chosen Doc Ref</label>
      <Field
        name="chosenDocRef"
        component={({ input: { onChange, value } }) => (
          <AppSearchBar
            pickerId={pickerId}
            typeFilters={typeFilters}
            onChange={onChange}
            value={value}
          />
        )}
      />
    </Form.Field>
    {thisForm &&
      thisForm.values && (
        <div>
          Chosen Doc Ref: {thisForm.values.chosenDocRef && thisForm.values.chosenDocRef.name}
        </div>
      )}
  </Form>
);

const AppSearchAsPickerForm = enhanceForm(RawAppSearchAsPickerForm);

class AppSearchAsNavigator extends React.Component {
  constructor(props) {
    super(props);

    this.displayRef = React.createRef();
    this.state = {
      chosenDocRef: undefined,
    };
  }
  render() {
    const { pickerId, chosenDocRef, setChosenDocRef } = this.props;

    return (
      <div>
        <AppSearchBar
          pickerId={pickerId}
          onChange={(d) => {
            this.setState({ chosenDocRef: d });
            this.displayRef.current.focus();
          }}
          value={this.state.chosenDocRef}
        />
        <div tabIndex={0} ref={this.displayRef}>
          {this.state.chosenDocRef
            ? `Would be opening ${this.state.chosenDocRef.name}...`
            : 'no doc ref chosen'}
        </div>
      </div>
    );
  }
}

storiesOf('App Search Bar', module)
  .addDecorator(PollyDecoratorWithTestData)
  .addDecorator(ThemedDecorator)
  .addDecorator(KeyIsDownDecorator())
  .addDecorator(ReduxDecorator)
  .addDecorator(StoryRouter())
  .add('Search Bar (global)', () => <AppSearchAsNavigator pickerId="global-search" />)
  .add('Doc Ref Picker', () => <AppSearchAsPickerForm pickerId="docRefPicker1" />)
  .add('Doc Ref Picker (filter to pipeline)', () => (
    <AppSearchAsPickerForm pickerId="docRefPicker2" typeFilters={['Pipeline']} />
  ))
  .add('Doc Ref Picker (filter to feed AND dictionary)', () => (
    <AppSearchAsPickerForm pickerId="docRefPicker3" typeFilters={['Feed', 'Dictionary']} />
  ))
  .add('Doc Ref Picker (filter to Folders)', () => (
    <AppSearchAsPickerForm pickerId="docRefPicker4" typeFilters={['Folder']} />
  ));
