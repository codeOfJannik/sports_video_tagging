'use strict';
import React from 'react';
import { VideoPlayer } from './VideoPlayer'
import { FileControlPanel } from "./FileControlPanel";
import { Grid } from '@material-ui/core';
import { VideoTagsSyncElement } from "./VideoTagsSyncElement";

export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            videoSrc: "",
            taggingStartTime: 0
        };
    }

    handleVideoFileSelection = (sourceFile) => {
        const fileURL = URL.createObjectURL(sourceFile)
        console.log("created file URL: " + fileURL)
        this.setState(previousState => {
            const newState = {
                ...previousState,
                videoSrc: fileURL
            };
            return newState
        })
    }

    handleVideoTagSync = (videoSeconds) => {
        this.setState(previousState => {
            const newState = {
                ...previousState,
                taggingStartTime: videoSeconds
            };
            return newState
        })
    }

    render() {
        return (
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <h1>Video Player</h1>
                    <p>Video Player for Syncing and Playback of Videos with Live Tagging Data</p>
                </Grid>
                <Grid item xs={6}>
                    <VideoPlayer sourceFile={this.state.videoSrc} />
                </Grid>
                <Grid item xs={8}>
                    <FileControlPanel videoSelectionHandler={this.handleVideoFileSelection} />
                </Grid>
                <Grid item xs={4}>
                    <VideoTagsSyncElement onVideoTimeSynced={this.handleVideoTagSync} />
                </Grid>
            </Grid>
        )
    }
}