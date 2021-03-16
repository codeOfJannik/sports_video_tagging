'use strict'
import XMLParser from "xml2js"

export function readXML(file, callback) {
    var reader = new FileReader();
    reader.onload = (function (theFile) {
        return function (e) {
            parseXML(e.target.result, callback)
        };
    })(file);
    reader.readAsText(file);
}

function parseXML(xmlString, callback) {
    var parseString = XMLParser.parseString
    parseString(xmlString, function (err, result) {
        let formatted = formatJSON(result)
        callback(formatted)
    })
}

function createPlayerObject(player) {
    let eventPlayer = {
        jerseyNumber: player.$.jerseyNumber
    }
    if (player.$.hasOwnProperty("playerName")) {
        eventPlayer.playerName = player.$.playerName
    }
    return eventPlayer
}

function isPlayerInList(player, list) {
    for (const listPlayer of list) {
        if (player.jerseyNumber == listPlayer.jerseyNumber && player.playerName == listPlayer.playerName) {
            return true
        }
    }
    return false
}

function comparePlayersByJerseyNum(a, b) {
    return a.jerseyNumber - b.jerseyNumber
}

function sortMatchPlayers(unsorted) {
    let matchPlayers = {
        home: unsorted.home.sort(comparePlayersByJerseyNum),
        away: unsorted.away.sort(comparePlayersByJerseyNum)
    }
    return matchPlayers
}

function formatJSON(json) {
    let matchEvents = []
    let matchPlayers = {
        home: [],
        away: []
    }
    let eventTypes = []
    let attributes = []
    let homeTeam = json.svt.match[0].homeTeam[0]
    let awayTeam = json.svt.match[0].awayTeam[0]

    for (const matchEvent of json.svt.match[0].matchEvents[0].matchEvent) {
        let elementAttributes = matchEvent.$
        let eventTitle = elementAttributes.eventTitle
        let orderNum = elementAttributes.matchEventOrderNum
        let timeOffset = elementAttributes.matchEventTimeOffset
        let eventPlayers = {
            home: [],
            away: []
        }
        let eventAttributes = []
        if (!eventTypes.includes(eventTitle)) {
            eventTypes.push(eventTitle)
        }
        if (matchEvent.hasOwnProperty("eventAttributes")) {
            for (const attribute of matchEvent.eventAttributes[0].attribute) {
                eventAttributes.push(attribute)
                if (!attributes.includes(attribute)) {
                    attributes.push(attribute)
                }
            }
        }
        if (matchEvent.players[0].hasOwnProperty("homeTeamPlayers")) {
            for (const player of matchEvent.players[0].homeTeamPlayers[0].player) {
                let eventPlayer = createPlayerObject(player)
                eventPlayers.home.push(eventPlayer)
                if (!isPlayerInList(eventPlayer, matchPlayers.home)) {
                    matchPlayers.home.push(eventPlayer)
                }
            }
        }
        if (matchEvent.players[0].hasOwnProperty("awayTeamPlayers")) {
            for (const player of matchEvent.players[0].awayTeamPlayers[0].player) {
                let eventPlayer = createPlayerObject(player)
                eventPlayers.away.push(eventPlayer)
                if (!isPlayerInList(eventPlayer, matchPlayers.away)) {
                    matchPlayers.away.push(eventPlayer)
                }
            }
        }
        let event = {
            eventType: eventTitle,
            orderNum: orderNum,
            timeOffset: timeOffset,
            players: eventPlayers,
            attributes: eventAttributes
        }
        matchEvents.push(event)
    }
    return {
        matchEvents: matchEvents,
        allPlayers: sortMatchPlayers(matchPlayers),
        allAttributes: attributes,
        allEventTypes: eventTypes,
        homeTeam: homeTeam,
        awayTeam: awayTeam
    }
}