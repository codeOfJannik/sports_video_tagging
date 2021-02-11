'use strict';
import React from 'react';
import { VideoPlayer } from './VideoPlayer';
import { FileControlPanel } from "./FileControlPanel";
import { Grid, Button } from '@material-ui/core';
import { VideoTagsSyncElement } from "./VideoTagsSyncElement";
import { readXML } from "./TaggingImport";
import { EventList } from './EventList';
import { FilterList } from './FilterList';
import { PlayersFilter } from "./PlayersFilter";
import './App.css'


export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            videoSrc: "",
            taggingStartTime: 0,
            matchEvents: [],
            filteredEvents: [],
            homeTeam: "",
            awayTeam: "",
            players: {
                home: [],
                away: []
            },
            eventTypes: [],
            attributes: [],
            videoTimestamp: 0,
            selectedFilterEventTypes: new Set(),
            selectedFilterAttributes: new Set(),
            selectedFilterPlayers: [new Set(), new Set()]
        };
    }

    taggingFormattedCallback = (taggingDataObject) => {
        this.setState(previousState => {
            const players = {
                home: taggingDataObject.allPlayers.home,
                away: taggingDataObject.allPlayers.away
            }
            const newState = {
                ...previousState,
                matchEvents: taggingDataObject.matchEvents,
                filteredEvents: taggingDataObject.matchEvents,
                homeTeam: taggingDataObject.homeTeam,
                awayTeam: taggingDataObject.awayTeam,
                players: players,
                eventTypes: taggingDataObject.allEventTypes,
                attributes: taggingDataObject.allAttributes,
            };
            return newState
        })
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

    handleTaggingFileSelection = (taggingFile) => {
        readXML(taggingFile, this.taggingFormattedCallback)
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

    handleEventSelected = (matchEvent) => {
        console.log("Selected Event with Timestamp: " + matchEvent.timeOffset)
        this.setState(previousState => {
            const newState = {
                ...previousState,
                videoTimestamp: parseInt(this.state.taggingStartTime) + parseInt(matchEvent.timeOffset)
            };
            return newState
        })
    }

    handleFilterEventTypesChanged = (selectedItems) => {
        this.setState(previousState => {
            const newState = {
                ...previousState,
                selectedFilterEventTypes: selectedItems
            };
            return newState
        })
    }

    handleFilterAttributesChanged = (selectedItems) => {
        this.setState(previousState => {
            const newState = {
                ...previousState,
                selectedFilterAttributes: selectedItems
            };
            return newState
        })
    }

    filterEventList = () => {
        // TODO: apply filters on Event List
    }

    render() {
        return (
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <h1>Video Player</h1>
                    <p>Video Player for Syncing and Playback of Videos with Live Tagging Data</p>
                </Grid>
                <Grid item xs={6}>
                    <VideoPlayer sourceFile={this.state.videoSrc} timestamp={this.state.videoTimestamp} />
                </Grid>
                <Grid item xs={6} >
                    <EventList events={this.state.filteredEvents} eventSelected={this.handleEventSelected} />
                </Grid>
                <Grid item xs={8}>
                    <h1 className="sub-header" >Select Video File and corresponding Live Tagging File</h1>
                    <FileControlPanel videoSelectionHandler={this.handleVideoFileSelection} taggingFileSelectionHandler={this.handleTaggingFileSelection} />
                </Grid>
                <Grid item xs={4}>
                    <VideoTagsSyncElement onVideoTimeSynced={this.handleVideoTagSync} />
                </Grid>
                <Grid item xs={6}>
                    <h1 >Filter Events</h1>
                </Grid>
                <Grid item xs={6}>
                    <Button id="reset-filter-button">Reset All Filters</Button>
                </Grid>
                <Grid item xs={4}>
                    <FilterList
                        listType="Event Type"
                        listElements={this.state.eventTypes}
                        selectedItems={this.state.selectedFilterEventTypes}
                        handleFilterItemsChanged={this.handleFilterEventTypesChanged} />
                </Grid>
                <Grid item xs={4}>
                    <FilterList
                        listType="Attributes"
                        listElements={this.state.attributes}
                        selectedItems={this.state.selectedFilterAttributes}
                        handleFilterItemsChanged={this.handleFilterAttributesChanged} />
                </Grid>
                <Grid item xs={4}>
                    <PlayersFilter
                        homeTeam={this.state.homeTeam}
                        awayTeam={this.state.awayTeam}
                        players={this.state.players}
                        selectedPlayers={this.state.selectedFilterPlayers} />
                </Grid>
            </Grid>
        )
    }
}