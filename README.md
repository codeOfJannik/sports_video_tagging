# sports_video_tagging
Software Code related to my bachelor thesis with the topic "Simplify video analytics in sports with a standardized data format for tagging data" (GERMAN)

# Parts
## SportsTaggingSchema.xsd
The XML schema for the svt-file format. In the svt-file format tagging data of events from sports matches can be represented in a standarized format. [20181112_1900_6_matchExport.svt](samples/20181112_1900_6_matchExport.svt)
contains tagging data for the first five minutes of the handball match Germany vs. Poland (https://www.youtube.com/watch?v=NzdQosHygKs). Start of tagging was at the start of the game.

## sports_video_tagging_app
A prototype Android app written in Kotlin to create tags for events in sport matches live during the match. The created tags can be exported into the .svt-file format.

## sports_video_tagging_player
A prototype web application written created in React to use an video along with tagging data of a svt-file. A demo version of the web app is hosted at https://js329.pages.mi.hdm-stuttgart.de/sports_video_tagging/.
