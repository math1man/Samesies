Samesies: A new way to meet people
========

Samesies is a new app for Android and iOS that allows you to meet people in your community in a fun, natural, stress-free environment.

The current structure is built on the Ionic framework with AngularJS. In order to use the project, all of these 
libraries must be installed.  If you don't have node, you can download it from http://nodejs.org/. Then, use the 
following commands to install the other libraries.

    npm install -g node
    npm install -g cordova
    npm install -g ionic
    
Now you are ready to download this repository. Once you do that, navigate to the app directory.
You must give permission to the scripts in the hooks directory. Use the following command.

    chmod -R +x hooks 

In order to build and run the code, first install platforms using 

    ionic platform add [platform]

Important platforms initially are iOS and Android. You must have the appropriate SDKs already installed, or
Ionic will be unable to install them. Note that the folder in which it puts the platforms is on the .gitignore. 
Because this is a generated folder, it should not be added to git. The app is now functional, and can be run via 
either of the following two commands. Note that to emulate, you must have an appropriate virtual device available, 
while to run, you must have an appropriate device connected.

    ionic emulate [platform]
    ionic run [platform]
    
