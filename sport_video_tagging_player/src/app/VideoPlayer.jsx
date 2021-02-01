'use strict'
import React from "react"
import "./VideoPlayer.css"

export class VideoPlayer extends React.Component {
    render() {
        return (
            <video controls muted key={this.props.sourceFile}>
                <source src={this.props.sourceFile} />
            </video>
        )
    }
}