(function() {

    var app = angular.module('samesies', ['ionic', 'ngCordova', 'samesies.controllers', 'samesies.services', 'samesies.filters', 'samesies.directives']);

    app.run(function ($window, $ionicPlatform, $cordovaKeyboard) {
        $ionicPlatform.ready(function () {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if ($window.cordova) {
                $cordovaKeyboard.hideAccessoryBar(true);
                if (ionic.Platform.isIOS()) {
                    $cordovaKeyboard.disableScroll(true);
                }
            }
        });
    });

    app.config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            .state('menu', {
                url: "/menu",
                templateUrl: "templates/views/menu.html"
            })
            .state('play', {
                cache: false,
                url: "/play",
                templateUrl: "templates/views/episode.html"
            })
            .state('browse', {
                url: "/browse",
                templateUrl: "templates/views/browse.html"
            })
            .state('chats', {
                url: "/chats",
                templateUrl: "templates/views/chats.html"
            })
            .state('chat', {
                cache: false,
                url: "/chat",
                templateUrl: "templates/views/chat.html"
            })
            .state('friends', {
                url: "/friends",
                templateUrl: "templates/views/friends.html"
            })
            .state('connections', {
                url: "/connections",
                templateUrl: "templates/views/connections.html"
            })
            .state('communities', {
                url: "/communities",
                templateUrl: "templates/views/communities.html"
            })
            .state('questions', {
                url: "/questions",
                templateUrl: "templates/views/questions.html"
            })
            .state('edit-profile', {
                url: "/edit-profile",
                templateUrl: "templates/views/edit-profile.html"
            })
            .state('profile', {
                cache: false,
                url: "/profile",
                templateUrl: "templates/views/profile.html"
            })
            .state('feedback', {
                cache: false,
                url: "/feedback",
                templateUrl: "templates/views/feedback.html"
            })
            .state('about', {
                cache: false,
                url: "/about",
                templateUrl: "templates/views/about.html"
            });

        // if none of the above states are matched, use this as the fallback
        $urlRouterProvider.otherwise('/menu');

    });

})();