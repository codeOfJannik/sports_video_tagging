'use strict'
import React from "react"
import { Button, TextField, Grid } from "@material-ui/core";
import "./InputElement.css"

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
            <Grid container spacing={2}>
                <Grid item xs={4} id="upload-button">
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
                </Grid>
                <Grid item xs={8}>
                    <TextField
                        label={this.props.labelText}
                        fullWidth
                        variant="outlined"
                        size="small"
                        value={this.state.textFieldContent}
                        InputProps={{
                            readOnly: true,
                        }} />
                </Grid>
            </Grid>
        )
    }
}