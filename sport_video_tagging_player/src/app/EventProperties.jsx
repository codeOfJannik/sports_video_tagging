'use strict'
import { Grid } from "@material-ui/core"
import React from "react"

export class EventProperties extends React.Component {
    render() {
        let attributes = "Attributes: " + this.props.attributes.join(', ')
        let homePlayers = "Home Players: "
        let guestPlayers = "Guest Players: "
        this.props.homePlayers.map((item) => {
            homePlayers += item.jerseyNumber + ", "
        })
        homePlayers = homePlayers.slice(0, -1)
        this.props.guestPlayers.map((item) => {
            guestPlayers += item.jerseyNumber + ", "
        })
        guestPlayers = guestPlayers.slice(0, -1)
        return (
            <Grid container >
                { this.props.attributes.length > 0 &&
                    <Grid item xs={12}>
                        {attributes}
                    </Grid>
                }
                { this.props.homePlayers.length > 0 &&
                    <Grid item xs={12}>
                        {homePlayers}
                    </Grid>
                }
                { this.props.guestPlayers.length > 0 &&
                    <Grid item xs={12}>
                        {guestPlayers}
                    </Grid>
                }
            </Grid>
        )
    }
}