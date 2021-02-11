'use strict'
import { Button, Checkbox, List, ListItem, ListItemIcon, ListItemText } from "@material-ui/core";
import React from 'react'

export class FilterList extends React.Component {
    handleItemClick = (item) => {
        let selectedItems = this.props.selectedItems
        if (selectedItems.has(item)) {
            selectedItems.delete(item)
        } else {
            selectedItems.add(item)
        }
        this.props.handleFilterItemsChanged(selectedItems)
    }

    handleResetClick = () => {
        this.props.handleFilterItemsChanged(new Set())
    }

    render() {
        return (
            <React.Fragment>
                <h2>Filter by {this.props.listType}</h2>
                <Button
                    id="reset-filter-button"
                    onClick={() => this.handleResetClick()}>
                    Clear {this.props.listType} Filter
                    </Button>
                <List id="filter-list">
                    {this.props.listElements.map((item, index) => {
                        return (
                            <ListItem
                                key={index}
                                button
                                divider
                                onClick={() => this.handleItemClick(item)}
                            >
                                <ListItemIcon>
                                    <Checkbox
                                        edge="start"
                                        checked={this.props.selectedItems.has(item)}
                                        tabIndex={-1}
                                        disableRipple
                                    />
                                </ListItemIcon>
                                <ListItemText
                                    primary={item}
                                />
                            </ListItem>
                        )
                    })}
                </List>
            </React.Fragment>
        )
    }

}