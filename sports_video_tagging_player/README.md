# Sports Video Tagging Player
A demo version of the sports video tagging player web app can be found at https://js329.pages.mi.hdm-stuttgart.de/sports_video_tagging/.  
To run the web app locally, follow these steps:

## Setup Instructions
**node.js** must be installed in order to run npm commands. Follow [node.js installation manual](https://nodejs.org/en/download/package-manager/)

After node.js is installed to your device, `cd` into [sports_video_tagging_player](/sports_video_tagging_player) and run the following command  
```
npm install
```
The required node packages (defined in package.json) will be installed.  

When installation of packages finished successfully, run the following command  
```
npm run compile
```
This will create a directory *dist* and composes all React Components of the app in a single bundle.js file.  

As soon as the bundle.js has been created, run the following command to host the application locally
```
npm run serve
```
You can access the application at http://localhost:8080/ using your browser 
