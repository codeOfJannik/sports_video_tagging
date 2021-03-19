'use strict'
import { Button, Paper, Tab, Tabs, List, ListItem, ListItemIcon, ListItemText, Checkbox } from '@material-ui/core'
import React from 'react'

export class PlayersFilter extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            tabValue: 0
        }
    }

    handleTabChanged = (event, value) => {
        this.setState((previousState) => {
            const newState = {
                ...previousState,
                tabValue: value
            };
            return newState
        })
    }

    handlePlayerSelected = (item, tabValue) => {
        let selectedPlayers = this.props.selectedPlayers
        let team = selectedPlayers[tabValue]
        if (team.has(item)) {
            team.delete(item)
        } else {
            team.add(item)
        }
        selectedPlayers[tabValue] = team
        this.props.handleFilterPlayersChanged(selectedPlayers)
    }

    handleResetClick = () => {
        let selectedPlayers = this.props.selectedPlayers
        selectedPlayers[0].clear()
        selectedPlayers[1].clear()
        this.props.handleFilterPlayersChanged(selectedPlayers)
    }

    render() {
        let playerElements = []
        if (this.state.tabValue == 0) {
            playerElements = this.props.players.home
        } else {
            playerElements = this.props.players.guest
        }
        return (
            <React.Fragment>
                <h2>Filter by Players</h2>
                <Button
                    id="reset-filter-button"
                    onClick={() => this.handleResetClick()}>Clear Player Filter</Button>
                <Paper square>
                    <Tabs
                        value={this.state.tabValue}
                        indicatorColor="primary"
                        textColor="primary"
                        variant="fullWidth"
                        onChange={this.handleTabChanged}>
                        <Tab label={this.props.homeTeam} />
                        <Tab label={this.props.guestTeam} />
                    </Tabs>
                </Paper>
                <List>
                    {playerElements.map((item, index) => {
                        let listItemString = item.jerseyNumber
                        if (item.playerName) {
                            listItemString += ": " + item.playerName
                        }
                        return (
                            <ListItem
                                key={index}
                                button
                                divider
                                onClick={() => this.handlePlayerSelected(item, this.state.tabValue)}
                            >
                                <ListItemIcon>
                                    <Checkbox
                                        edge="end"
                                        checked={this.props.selectedPlayers[this.state.tabValue].has(item)}
                                        tabIndex={-1}
                                        disableRipple
                                    />
                                </ListItemIcon>
                                <ListItemText
                                    primary={listItemString}
                                />
                            </ListItem>
                        )
                    })}
                </List>
            </React.Fragment>
        )
    }
}