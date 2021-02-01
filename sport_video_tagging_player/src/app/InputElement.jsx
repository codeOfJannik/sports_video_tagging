'use strict'
import React from "react"
import { Input as input, Button, TextField } from "@material-ui/core";
import "./InputElement.css"
import changeVideoSource from "./VideoPlayer"

export class InputElement extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            textFieldContent: "No File Selected"
        };
    }

    onFieldChange(event) {
        const file = event.target.files[0]
        const filename = file.name
        console.log("selected file: " + filename)

        this.setState({ textFieldContent: filename })
        this.props.selectionHandler(file)
    }

    render() {
        return (
            <div className="input-element">
                <div id="upload-button">
                    <label htmlFor={this.props.name}>
                        <input
                            id={this.props.name}
                            name={this.props.name}
                            type="file"
                            accept={this.props.accepted}
                            onChange={this.onFieldChange.bind(this)} />
                        <Button color="primary"
                            variant="contained"
                            component="span" >
                            {this.props.buttonText}
                        </Button>
                    </label>
                </div>
                <TextField
                    id="input-file-name-textfield"
                    label={this.props.labelText}
                    fullWidth
                    variant="outlined"
                    size="small"
                    value={this.state.textFieldContent}
                    InputProps={{
                        readOnly: true,
                    }} />
            </div>
        )
    }
}