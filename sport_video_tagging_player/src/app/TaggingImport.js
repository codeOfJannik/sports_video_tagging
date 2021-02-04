'use strict'
import XMLParser from "xml2js"

export function readXML(file) {
    var reader = new FileReader();
    reader.onload = (function (theFile) {
        return function (e) {
            parseXML(e.target.result)
        };
    })(file);
    reader.readAsText(file);
}

function parseXML(xmlString) {
    var parseString = XMLParser.parseString
    parseString(xmlString, function (err, result) {
        formatJSON(result)
    })
}

function formatJSON(json) {
    let matchEvents = {}
    let players = {
        home: [],
        away: []
    }
    let eventTypes = []
    let attributes = []
    let homeTeam = json.svt.match[0].homeTeam[0]
    let awayTeam = json.svt.match[0].awayTeam[0]

    //TODO: Read JSON and write into objects above
    console.log(json)
    console.log(json.svt.match[0].matchEvents[0])
    for (const matchEvent of json.svt.match[0].matchEvents[0].matchEvent) {
        console.log(matchEvent)
    }

}