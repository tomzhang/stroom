import React from 'react';
import PropTypes from 'prop-types';

import { compose, withProps, withHandlers } from 'recompose';
import { connect } from 'react-redux';
import { Input, Icon, Button } from 'semantic-ui-react';

import DocRefPropType from 'lib/DocRefPropType';
import { findItem, filterTree } from 'lib/treeUtils';
import {
  actionCreators as appSearchBarActionCreators,
  defaultPickerState,
  SEARCH_MODE,
} from './redux';
import { searchApp } from 'components/FolderExplorer/explorerClient';
import { DocRefBreadcrumb } from 'components/DocRefBreadcrumb';
import DocRefListingEntry from 'components/DocRefListingEntry';
import { withDocRefTypes } from 'components/DocRefTypes';
import withSelectableItemListing, {
  defaultSelectableItemListingState,
} from 'lib/withSelectableItemListing';
import withDocumentTree from 'components/FolderExplorer/withDocumentTree';

const { searchTermUpdated, navigateToFolder, openDropdown, closeDropdown, switchMode, chooseDocRef } = appSearchBarActionCreators;

const enhance = compose(
  withDocRefTypes,
  withDocumentTree,
  connect(
    (
      { appSearch, selectableItemListings, folderExplorer: { documentTree } },
      { pickerId, typeFilters },
    ) => {
      const appSearchForPicker = appSearch[pickerId] || defaultPickerState;
      const selectableItemListing =
        selectableItemListings[pickerId] || defaultSelectableItemListingState;
      const {
        searchTerm, navFolder, searchResults, searchMode, isOpen, chosenDocRef
      } = appSearchForPicker;
      const documentTreeToUse =
        typeFilters.length > 0
          ? filterTree(documentTree, d => typeFilters.includes(d.type))
          : documentTree;

      let docRefs = searchResults;
      let thisFolder;
      let parentFolder;
      let valueToShow;
      if (isOpen) {
        valueToShow = searchTerm;
      } else if (chosenDocRef) {
        valueToShow = chosenDocRef.name;
      } else {
        valueToShow = '';
      }

      if (searchMode === SEARCH_MODE.NAVIGATION) {
        const navFolderToUse = navFolder || documentTreeToUse;
        const navFolderWithLineage = findItem(documentTreeToUse, navFolderToUse.uuid);
        docRefs = navFolderWithLineage.node.children;
        thisFolder = navFolderWithLineage.node;

        if (navFolderWithLineage.lineage && navFolderWithLineage.lineage.length > 0) {
          parentFolder = navFolderWithLineage.lineage[navFolderWithLineage.lineage.length - 1];
        }
      }

      const modeOptions = [
        {
          mode: SEARCH_MODE.GLOBAL_SEARCH,
          icon: 'search'
        },
        {
          mode: SEARCH_MODE.NAVIGATION,
          icon: 'folder'
        }
      ]

      return {
        searchMode,
        valueToShow,
        docRefs,
        hasNoResults: docRefs.length === 0,
        noResultsText: searchMode === SEARCH_MODE.NAVIGATION ? 'empty' : 'no results',
        provideBreadcrumbs: searchMode !== SEARCH_MODE.NAVIGATION,
        thisFolder,
        parentFolder,
        isOpen,
        modeOptions
      };
    },
    {
      searchApp,
      searchTermUpdated,
      navigateToFolder,
      openDropdown,
      closeDropdown,
      switchMode,
      chooseDocRef
    },
  ),
  withHandlers({
    chooseDocRef: ({chooseDocRef, pickerId}) => d => chooseDocRef(pickerId, d)
  }),
  withSelectableItemListing(({
    pickerId, docRefs, navigateToFolder, parentFolder, chooseDocRef
  }) => ({
    listingId: pickerId,
    items: docRefs,
    openItem: chooseDocRef,
    enterItem: d => navigateToFolder(pickerId, d),
    goBack: () => navigateToFolder(pickerId, parentFolder),
  })),
  withProps(({
    searchMode, parentFolder, navigateToFolder, pickerId, thisFolder,
  }) => {
    let headerTitle;
    let headerIcon;
    let headerAction = () => {};

    switch (searchMode) {
      case SEARCH_MODE.GLOBAL_SEARCH: {
        headerIcon = 'search';
        headerTitle = 'Search';
        break;
      }
      case SEARCH_MODE.NAVIGATION: {
        headerTitle = thisFolder.name;
        if (parentFolder) {
          headerIcon = 'arrow left';
          headerAction = () => navigateToFolder(pickerId, parentFolder);
        } else {
          headerIcon = 'dont';
        }
        break;
      }
      default:
        break;
    }

    return {
      headerTitle,
      headerIcon,
      headerAction,
    };
  }),
);

const AppSearchBar = ({
  pickerId,
  docRefs,
  searchMode,
  searchApp,
  navigateToFolder,
  valueToShow,
  searchTermUpdated,
  onKeyDownWithShortcuts,
  isOpen,
  openDropdown,
  closeDropdown,
  headerTitle,
  headerIcon,
  headerAction,
  hasNoResults,
  noResultsText,
  provideBreadcrumbs,
  switchMode,
  modeOptions,
  chooseDocRef
}) => (
  <div
    className="dropdown"
    tabIndex={0}
    onFocus={() => openDropdown(pickerId)}
    onKeyDown={onKeyDownWithShortcuts}
  >
    <Input
      onFocus={() => openDropdown(pickerId)}
      fluid
      className="border flat"
      icon="search"
      placeholder="Search..."
      value={valueToShow}
      onChange={({ target: { value } }) => {
        searchTermUpdated(pickerId, value);
        searchApp(pickerId, { term: value });
      }}
    />
    <div className={`dropdown__content ${isOpen ? 'open' : ''}`}>
      <div className="app-search-header">
        <Icon name={headerIcon} size="large" onClick={headerAction} />
        {headerTitle}
        <Button.Group floated='right'>
          {modeOptions.map(modeOption =>
            <Button icon={modeOption.icon} onClick={() => switchMode(pickerId, modeOption.mode)} />
          )}
        </Button.Group>
      </div>
      <div className="app-search-listing">
        {hasNoResults && <div className="app-search-listing__empty">{noResultsText}</div>}
        {docRefs.map((searchResult, index) => (
          <DocRefListingEntry
            key={searchResult.uuid}
            index={index}
            listingId={pickerId}
            docRef={searchResult}
            openDocRef={chooseDocRef}
            enterFolder={d => navigateToFolder(pickerId, d)}
          >
            {provideBreadcrumbs && (
              <DocRefBreadcrumb docRefUuid={searchResult.uuid} openDocRef={d => navigateToFolder(pickerId, d)} />
            )}
          </DocRefListingEntry>
        ))}
      </div>
      <div className="app-search-footer">
        <Button primary>Choose</Button>
        <Button secondary onClick={() => closeDropdown(pickerId)}>Cancel</Button>
      </div>
    </div>
  </div>
);

const EnhancedAppSearchBar = enhance(AppSearchBar);

EnhancedAppSearchBar.propTypes = {
  pickerId: PropTypes.string.isRequired,
  typeFilters: PropTypes.array.isRequired,
  onChange: PropTypes.func.isRequired,
  value: DocRefPropType,
};

EnhancedAppSearchBar.defaultProps = {
  pickerId: 'global',
  typeFilters: [],
  onChange: () => console.log('onChange not provided for app search bar'),
};

export default EnhancedAppSearchBar;
