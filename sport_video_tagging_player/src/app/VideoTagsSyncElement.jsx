'use strict'
import React from 'react'
import { Grid, TextField, Button } from "@material-ui/core";
import "./VideoTagsSyncElement.css"

const inputProps = { inputProps: { min: 0 } }

export class VideoTagsSyncElement extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            minutes: 0,
            seconds: 0,
            buttonText: "Confirm Time Sync",
            disabled: false
        }

        this.handleChange = this.handleChange.bind(this)
        this.handleSyncClick = this.handleSyncClick.bind(this)
    }

    handleChange(event) {
        const targetName = event.target.name
        const value = event.target.value != "" ? event.target.value : 0
        this.setState(previousState => {
            const newState = {
                ...previousState,
                [targetName]: parseInt(value)
            };
            return newState
        })
    }

    handleSyncClick(event) {
        this.setState(previousState => {
            const newState = {
                ...previousState,
                buttonText: "Completed Synchronization",
                disabled: true
            };
            return newState
        })
        const videoSeconds = 60 * this.state.minutes + this.state.seconds
        this.props.onVideoTimeSynced(videoSeconds)
    }

    render() {
        return (
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    <h1 className="in-grid" >Sync Tagging Data with Video</h1>
                </Grid>
                <Grid item xd={12}>
                    Enter the timecode of the video when the live tagging has started (prob. Match Start)
                </Grid>
                <Grid item xs={6}>
                    <TextField
                        disabled={this.state.disabled}
                        name="minutes"
                        label="Minutes"
                        type="number"
                        variant="outlined"
                        size="small"
                        value={this.state.minutes}
                        onChange={this.handleChange}
                        InputProps={inputProps}
                    ></TextField>
                </Grid>
                <Grid item xs={6}>
                    <TextField
                        disabled={this.state.disabled}
                        name="seconds"
                        label="Seconds"
                        type="number"
                        variant="outlined"
                        size="small"
                        value={this.state.seconds}
                        onChange={this.handleChange}
                        InputProps={inputProps}
                    ></TextField>
                </Grid>
                <Grid item xs={12}>
                    <Button
                        disabled={this.state.disabled}
                        variant="contained"
                        color="primary"
                        onClick={this.handleSyncClick}
                        style={
                            {
                                backgroundColor: this.state.disabled ? "green" : "primary",
                                color: "white"
                            }
                        }>
                        {this.state.buttonText}</Button>
                </Grid>
            </Grid>
        )
    }
}

