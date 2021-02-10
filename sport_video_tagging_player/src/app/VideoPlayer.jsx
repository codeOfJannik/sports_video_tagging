'use strict'
import React from "react"
import "./VideoPlayer.css"

export class VideoPlayer extends React.Component {
    constructor(props) {
        super(props);
        this.player = React.createRef()
        this.componentDidUpdate = this.componentDidUpdate.bind(this)
        this.seek = this.jumpToTimeCode.bind(this)
    }

    componentDidUpdate(prevProps) {
        if (this.props.timestamp != prevProps.timestamp) {
            console.log("Updated Props in VideoPlayer")
            console.log(this.props)
            this.jumpToTimeCode(this.props.timestamp)
        }
    }

    jumpToTimeCode(timeCode) {
        console.log("Jump to time code: " + timeCode)
        if (this.player) {
            this.player.current.currentTime = timeCode
        }
    }

    render() {
        return (
            <video
                ref={this.player}
                controls
                muted
                src={this.props.sourceFile}></video>
        )
    }
}