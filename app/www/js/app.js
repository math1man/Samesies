(function() {

    var app = angular.module('samesies', ['ionic', 'ngCordova', 'samesies.controllers', 'samesies.services', 'samesies.filters', 'samesies.directives']);

    app.run(function ($window, $rootScope, $ionicPlatform, $cordovaKeyboard, $cordovaPush, $cordovaToast) {
        $ionicPlatform.ready(function () {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if ($window.cordova) {
                $cordovaKeyboard.hideAccessoryBar(true);
                if (ionic.Platform.isIOS()) {
                    $cordovaKeyboard.disableScroll(true);
                }
                if (!$window.localStorage['deviceToken']) {
                    var config;
                    if (ionic.Platform.isAndroid()) {
                        config = {
                            "senderID": "340151147956"
                        };
                    } else if (ionic.Platform.isIOS()) {
                        config = {
                            "badge": "true",
                            "sound": "true",
                            "alert": "true"
                        }
                    }
                    $cordovaPush.register(config).then(function (result) {
                        if (ionic.Platform.isIOS()) {
                            $window.localStorage['deviceToken'] = result;
                        }
                    }, function (err) {
                        console.log("Register error " + err);
                    });
                }

                $rootScope.$on('$cordovaPush:notificationReceived', function (event, notification) {
                    if (ionic.Platform.isAndroid()) {
                        if (notification.event === "registered") {
                            $window.localStorage['deviceToken'] = notification.regid;
                        } else if (notification.event === "message") {
                            $cordovaToast.showLongTop(notification.message);
                        }
                    } else if (ionic.Platform.isIOS()) {
                        if (notification.foreground === "1") {
                            if (notification.body && notification.messageFrom) {
                                $cordovaToast.showLongTop(notification.body);
                            } else {
                                $cordovaToast.showLongTop(notification.alert);
                            }
                            if (notification.badge) {
                                $cordovaPush.setBadgeNumber(notification.badge).then();
                            }
                        }
                    }
                });
            }
        });
    });

    app.config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            .state('login', {
                url: "/login",
                templateUrl: "templates/views/login.html"
            })
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
        $urlRouterProvider.otherwise('/login');

    });

})();