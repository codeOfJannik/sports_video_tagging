'use strict'

export function listContainsAllElements(list, elements) {
    for (const element of elements) {
        if (!list.includes(element)) {
            return false
        }
    }
    return true
}

function eventPlayersInPlayerfilter(eventPlayers, filterPlayers) {
    for (const player of filterPlayers) {
        if (!eventPlayers.some(element =>
            element.jerseyNumber === player.jerseyNumber
            && element.playerName === player.playerName
        )) {
            return false
        }
        return true
    }
}

export function eventContainsPlayers(event, filteredPlayers) {
    let homePlayers = filteredPlayers[0]
    let awayPlayers = filteredPlayers[1]
    let filterMatches = true
    if (homePlayers.size > 0) {
        filterMatches = eventPlayersInPlayerfilter(event.players.home, homePlayers)
    }
    if (!filterMatches) {
        return false
    }
    if (awayPlayers.size > 0) {
        filterMatches = eventPlayersInPlayerfilter(event.players.away, awayPlayers)
    }
    return filterMatches



}