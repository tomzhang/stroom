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
import { createActions, handleActions } from 'redux-actions';

import {
  assignRandomUuids,
  updateItemInTree,
  addItemsToTree,
  deleteItemFromTree,
  moveItemsInTree,
} from 'lib/treeUtils';
import { createActionHandlerPerId } from 'lib/reduxFormUtils';

// Expression Editors
const actionCreators = createActions({
  EXPRESSION_EDITOR_CREATED: expressionId => ({
    expressionId,
  }),
  EXPRESSION_EDITOR_DESTROYED: expressionId => ({
    expressionId,
  }),
  EXPRESSION_SET_EDITABLE_BY_USER: (expressionId, isEditableUserSet) => ({
    expressionId,
    isEditableUserSet,
  }),
  EXPRESSION_CHANGED: (expressionId, expression) => ({
    expressionId,
    expression,
  }),
  EXPRESSION_TERM_ADDED: (expressionId, operatorId) => ({
    expressionId,
    operatorId,
  }),
  EXPRESSION_OPERATOR_ADDED: (expressionId, operatorId) => ({ expressionId, operatorId }),
  EXPRESSION_ITEM_UPDATED: (expressionId, itemId, updates) => ({ expressionId, itemId, updates }),
  EXPRESSION_ITEM_DELETED: (expressionId, itemId) => ({ expressionId, itemId }),
  EXPRESSION_ITEM_MOVED: (expressionId, itemToMove, destination) => ({
    expressionId,
    itemToMove,
    destination,
  }),
});

const NEW_TERM = {
  type: 'term',
  condition: 'EQUALS',
  enabled: true,
};

const NEW_OPERATOR = {
  type: 'operator',
  op: 'AND',
  enabled: true,
  children: [],
};

const defaultStatePerExpression = {
  pendingDeletionOperatorId: undefined,
  expression: NEW_OPERATOR,
};

// expressions, keyed on ID, there may be several expressions on a page
const defaultState = {};

/**
 * These constants and functions are used to generate Doc Ref pickerId values
 * that can carry the Dictionary choices for expression terms.
 * The Expression ID and the Term UUID must be carried inside the picker ID.
 */
const EXPRESSION_PREFIX = 'EXP';
const PICKER_DELIM = '_';

/**
 * Given the value of a docRef pickerId. Attempts to parse out the expression ID and term UUID.
 * Looks for the format composed in the join function below.
 * @param {string} value May or may not be a value generated by the joinDictionaryTermId function below
 * @return {object}
 *  {
 *      isExpressionBased : {boolean} to indicate that the input value is a dictionary term value
 *      expressionId : the parsed expression ID. Will be populated if isExpressionBased === true
 *      termUuid : the parsed term UUID. Will be populated if isExpressionBased === true
 *  }
 * TODO - do we need this?
 */
export const splitDictionaryTermId = (value) => {
  const p = value.split(PICKER_DELIM);
  let isExpressionBased = false;
  let expressionId = null;
  let termUuid = null;
  if (p.length === 3 && p[0] === EXPRESSION_PREFIX) {
    isExpressionBased = true;
    expressionId = p[1];
    termUuid = p[2];
  }
  return {
    isExpressionBased,
    expressionId,
    termUuid,
  };
};

/**
 * Create the combined picker ID with the appropriate PREFIX and DELIMTER
 * that allow for unambiguous detection in the splitter function.
 *
 * @param {string} expressionId The ID of the expression to which this picker belong
 * @param {string} termUuid The UUID of the term that the picker is being used to pick a value for.
 */
const joinDictionaryTermId = (expressionId, termUuid) =>
  EXPRESSION_PREFIX + PICKER_DELIM + expressionId + PICKER_DELIM + termUuid;

const getCurrentExpression = (state, action) =>
  (state[action.payload.expressionId] ? state[action.payload.expressionId].expression : {});

const byExpressionId = createActionHandlerPerId(
  ({ payload }) => payload.expressionId,
  defaultStatePerExpression,
);

const reducer = handleActions(
  {
    // Expression Changed
    EXPRESSION_CHANGED: byExpressionId((state, action) => ({
      expression: assignRandomUuids(action.payload.expression),
    })),

    // Expression Term Added
    EXPRESSION_TERM_ADDED: byExpressionId((state, action) => ({
      expression: addItemsToTree(getCurrentExpression(state, action), action.payload.operatorId, [
        NEW_TERM,
      ]),
    })),

    // Expression Operator Added
    EXPRESSION_OPERATOR_ADDED: byExpressionId((state, action) => ({
      expression: addItemsToTree(getCurrentExpression(state, action), action.payload.operatorId, [
        NEW_OPERATOR,
      ]),
    })),

    // Expression Term Updated
    EXPRESSION_ITEM_UPDATED: byExpressionId((state, action) => ({
      expression: updateItemInTree(
        getCurrentExpression(state, action),
        action.payload.itemId,
        action.payload.updates,
      ),
    })),

    // Expression Item Deleted
    EXPRESSION_ITEM_DELETED: byExpressionId((state, action) => ({
      expression: deleteItemFromTree(getCurrentExpression(state, action), action.payload.itemId),
    })),

    // Expression Item Moved
    EXPRESSION_ITEM_MOVED: byExpressionId((state, action) => ({
      expression: moveItemsInTree(getCurrentExpression(state, action), action.payload.destination, [
        action.payload.itemToMove,
      ]),
    })),
  },
  defaultState,
);

export { actionCreators, reducer, joinDictionaryTermId };
