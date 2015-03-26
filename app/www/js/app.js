(function() {

    var app = angular.module('samesies', ['ionic', 'samesies.controllers', 'samesies.services', 'samesies.filters', 'samesies.directives']);

    app.run(function ($window, $ionicPlatform, $cordovaKeyboard) {
        $ionicPlatform.ready(function () {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if ($window.cordova) {
                $cordovaKeyboard.hideAccessoryBar(true);
                $cordovaKeyboard.disableScroll(true);
            }
        });
    });

    app.config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            .state('menu', {
                url: "/menu",
                templateUrl: "templates/menu.html"
            })
            .state('play', {
                cache: false,
                url: "/play",
                templateUrl: "templates/episode.html"
            })
            .state('communities', {
                url: "/communities",
                templateUrl: "templates/communities.html"
            })
            .state('chats', {
                url: "/chats",
                templateUrl: "templates/chats.html"
            })
            .state('chat', {
                cache: false,
                url: "/chat",
                templateUrl: "templates/chat.html"
            })
            .state('friends', {
                url: "/friends",
                templateUrl: "templates/friends.html"
            })
            .state('connections', {
                url: "/connections",
                templateUrl: "templates/connections.html"
            })
            .state('questions', {
                url: "/questions",
                templateUrl: "templates/questions.html"
            })
            .state('edit-profile', {
                url: "/edit-profile",
                templateUrl: "templates/edit-profile.html"
            })
            .state('profile', {
                cache: false,
                url: "/profile",
                templateUrl: "templates/profile.html"
            })
            .state('feedback', {
                cache: false,
                url: "/feedback",
                templateUrl: "templates/feedback.html"
            })
            .state('about', {
                cache: false,
                url: "/about",
                templateUrl: "templates/about.html"
            });

        // if none of the above states are matched, use this as the fallback
        $urlRouterProvider.otherwise('/menu');

    });

})();