'use strict'
import React from "react"
import { List, ListItem, ListItemText, Divider, Grid } from "@material-ui/core";
import { EventProperties } from "./EventProperties";
import "./EventList.css"

export class EventList extends React.Component {
    render() {
        return (
            <div id="container">
                <List id="list">
                    {this.props.events.map((item, index) => {
                        return (
                            <ListItem key={index}>
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
                            </ListItem>
                        )
                    })}
                </List>
            </div>
        )
    }

}