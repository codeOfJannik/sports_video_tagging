'use strict'
import React from "react"
import { InputElement } from "./InputElement";
import "./FileControlPanel.css"

export class FileControlPanel extends React.Component {

    handleVideoSelection = (sourceFile) => {
        this.props.videoSelectionHandler(sourceFile)
    }

    render() {
        return (
            <form id="file-input-form">
                <InputElement
                    name="upload-video"
                    accepted="video/*"
                    buttonText="Upload Video"
                    labelText="Video File"
                    selectionHandler={this.handleVideoSelection} />
                <InputElement
                    name="upload-tagging-file"
                    accepted=".svt"
                    buttonText="Upload Tagging File"
                    labelText="Tagging File" />
            </form>
        )
    }
}