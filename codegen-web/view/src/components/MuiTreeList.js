import React from 'react';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';
import { List, ListItem, ListItemText, ListItemIcon } from '@material-ui/core';
import OpenIcon from '@material-ui/icons/ExpandMore';
import CloseIcon from '@material-ui/icons/ExpandLess';
import FolderIcon from '@material-ui/icons/Folder';
import FileIcon from '@material-ui/icons/InsertDriveFile';

class TreeList extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      expandedListItems: [],
      activeListItem: null,
    };
    this.handleTouchTap = this.handleTouchTap.bind(this);
  }

  handleTouchTap(listItem, index) {
    if (listItem.children) {
      const indexOfListItemInArray = this.state.expandedListItems.indexOf(index);
      if (indexOfListItemInArray === -1) {
        this.setState({
          expandedListItems: this.state.expandedListItems.concat([index]),
        });
      } else {
        let newArray = [].concat(this.state.expandedListItems);
        newArray.splice(indexOfListItemInArray, 1);
        this.setState({
          expandedListItems: newArray,
        });
      }
    } else {
      this.setState({
        activeListItem: index,
      });
    }
  }

  render() {
    // required props
    const { children, listItems, contentKey } = this.props;
    // optional props
    const style = this.props.style ? this.props.style : {};
    const startingDepth = this.props.startingDepth ? this.props.startingDepth : 1;
    const expandedListItems = this.props.expandedListItems
      ? this.props.expandedListItems
      : this.state.expandedListItems;
    const activeListItem = this.props.activeListItem
      ? this.props.activeListItem
      : this.state.activeListItem;
    const listHeight = this.props.listHeight ? this.props.listHeight : '48px';

    let listItemsModified = listItems.map((listItem, i, inputArray) => {
      listItem._styles = {
        root: {
          paddingLeft: (listItem.depth - startingDepth) * 16,
          backgroundColor: activeListItem === i ? 'rgba(0,0,0,0.2)' : null,
          height: listHeight,
          cursor: listItem.disabled ? 'not-allowed' : 'pointer',
          color: listItem.disabled ? 'rgba(0,0,0,0.4)' : 'rgba(0,0,0,0.87)',
          overflow: 'hidden',
          transform: 'translateZ(0)',
        },
      };
      return listItem;
    });
    listItemsModified = listItemsModified.map((listItem, i) => {
      listItem._shouldRender = listItem.depth >= startingDepth && parentsAreExpanded(listItem);
      listItem._primaryText = listItem[contentKey];
      return listItem;
    });

    // JSX: array of listItems
    const listItemsJSX = listItemsModified.map((listItem, i) => {
      if (listItem._shouldRender) {
        var inputProps = {
          key : 'treeListItem-' + i,
          style: listItem._styles.root
        };
        if(listItem.link) {
          inputProps.component=Link;
          inputProps.to = listItem.link;
        }
        return (
          <ListItem
            {...inputProps}
            onClick={() => {
              if (listItem.disabled) return;
              this.handleTouchTap(listItem, i);
            }}
            button
          >
            <ListItemIcon>{getLeftIcon(listItem, this.props.useFolderIcons)}</ListItemIcon>
            <ListItemText primary={listItem._primaryText} />
            {!listItem.children ? null : expandedListItems.indexOf(i) === -1 ? (
              <OpenIcon />
            ) : (
              <CloseIcon />
            )}
          </ListItem>
        );
      } else {
        return null;
      }
    });

    // styles for entire wrapper
    const styles = {
      root: {
        padding: 0,
        paddingBottom: 8,
        paddingTop: children ? 0 : 8,
      },
    };
    return (
      <div style={Object.assign({}, styles.root, style)}>
        {children}
        <List>
          {listItemsJSX}
        </List>
      </div>
    );

    function getLeftIcon(listItem, useFolderIcons) {
      if (useFolderIcons) {
        if (listItem.children) {
          return <FolderIcon />;
        } else {
          return <FileIcon />;
        }
      } else {
        return listItem.icon;
      }
    }

    function parentsAreExpanded(listitem) {
      if (listitem.depth > startingDepth) {
        if (expandedListItems.indexOf(listitem.parentIndex) === -1) {
          return false;
        } else {
          const parent = listItems.filter((_listItem, index) => {
            return index === listitem.parentIndex;
          })[0];
          return parentsAreExpanded(parent);
        }
      } else {
        return true;
      }
    }
  }
}

TreeList.contextTypes = {
  muiTheme: PropTypes.object,
};

TreeList.propTypes = {
  activeListItem: PropTypes.number,
  children: PropTypes.any,
  contentKey: PropTypes.string.isRequired,
  expandedListItems: PropTypes.array,
  handleTouchTap: PropTypes.func,
  listHeight: PropTypes.number,
  listItems: PropTypes.array.isRequired,
  startingDepth: PropTypes.number,
  style: PropTypes.object,
  useFolderIcons: PropTypes.bool,
};

export default TreeList;
