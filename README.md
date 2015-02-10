Samesies: A new way to meet people
========

Samesies is a new app for Android and iOS that allows you to casually interact with other people in your area.

The current structure is built on the yeoman scaffold with grunt distribution and cordova platform porting, 
all on top of nodejs. In order to use the project, all of these libraries must be installed.  If you don't 
have node, you can download it from http://nodejs.org/. Then, use the following commands to install the other
libraries.

    npm install -g node
    npm install -g yo
    npm install -g cordova
    
Now you are ready to download this repository. Once you do that, navigate to its parent directory.
Next, you must give permission to the scripts in the hooks directory. Use the following command.

    chmod -R +x hooks 

In order to build and run the code, first install platforms using 

    cordova platform add [platform]

Important platforms initially are iOS and Android. You must have the appropriate SDKs already installed, or
cordova will be unable to install them. Note that the folder in which it puts the platforms is on the .gitignore. 
Because this is a generated folder, it should not be added to git. The app is now functional, and can be run via 
either of the following two commands. Note that to emulate, you must have an appropriate virtual device already 
active, while to run, you must have an appropriate device connected.

    cordova emulate [platform]
    cordova run [platform]
    
Cordova will run a hook script before anything else, which triggers the grunt build and copies the built files
into the www directory. This directory is also on the .gitignore as another generated folder.
