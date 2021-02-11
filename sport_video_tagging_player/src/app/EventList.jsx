'use strict'
import React from "react"
import { List, ListItem, ListItemText, Box, Divider, Grid } from "@material-ui/core";
import { EventProperties } from "./EventProperties";
import "./EventList.css"

export class EventList extends React.Component {
    handleEventItemClick = (event) => {
        this.props.eventSelected(event)
    }

    render() {
        return (
            <div id="container">
                <List id="event-list">
                    {this.props.events.map((item, index) => {
                        return (
                            <ListItem
                                key={index}
                                button
                                divider
                                onClick={() => this.handleEventItemClick(item)}>
                                <Grid container>
                                    <Grid item xs={9}>
                                        <ListItemText
                                            primary={item.eventType}
                                            secondary={
                                                <EventProperties
                                                    attributes={item.attributes}
                                                    homePlayers={item.players.home}
                                                    awayPlayers={item.players.away} />
                                            }
                                            secondaryTypographyProps={{ component: 'div' }}
                                        />
                                    </Grid>
                                    <Grid id="time-offset-grid-item" item xs={3}>
                                        <div>
                                            <p>Event Match Time</p>
                                            <p>{new Date(item.timeOffset * 1000).toISOString().substr(11, 8)}</p>
                                        </div>
                                    </Grid>
                                </Grid>
                            </ListItem>
                        )
                    })}
                </List>
            </div>
        )
    }

}