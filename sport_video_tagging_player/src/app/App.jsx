'use strict';
import React from 'react';
import { VideoPlayer } from './VideoPlayer'
import { FileControlPanel } from "./FileControlPanel";

export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            videoSrc: ""
        };
    }

    handleVideoFileSelection = (sourceFile) => {
        const fileURL = URL.createObjectURL(sourceFile)
        console.log("created file URL: " + fileURL)
        this.setState({ videoSrc: fileURL })
    }

    render() {
        return (
            <div>
                <h1>Video Player</h1>
                <p>Video Player for Syncing and Playback of Videos with Live Tagging Data</p>
                <VideoPlayer sourceFile={this.state.videoSrc} />
                <FileControlPanel videoSelectionHandler={this.handleVideoFileSelection} />
            </div>
        )
    }
}