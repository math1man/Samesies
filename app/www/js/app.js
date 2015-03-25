(function() {

    var app = angular.module('samesies', ['ionic', 'ngCordova', 'samesies.controllers', 'samesies.services', 'samesies.filters', 'samesies.directives']);

    app.run(function ($window, $rootScope, $ionicPlatform, $cordovaKeyboard, $cordovaPush, $cordovaToast) {
        $ionicPlatform.ready(function () {
            $cordovaKeyboard.hideKeyboardAccessoryBar(true);
            $cordovaKeyboard.disableScroll(true);

            if (!$window.localStorage['pushId']) {
                var config;
                if ($ionicPlatform.isAndroid()) {
                    config = {
                        "senderID": "samesies-app"
                    };
                } else if ($ionicPlatform.isIOS()) {
                    config = {
                        "badge": "true",
                        "sound": "true",
                        "alert": "true"
                    }
                }
                $cordovaPush.register(config).then(function (result) {
                    console.log("Register success " + result);
                    // ** NOTE: Android regid result comes back in the pushNotificationReceived, only iOS returned here
                    if ($ionicPlatform.isIOS()) {
                        $window.localStorage['pushId'] = result.deviceToken;
                    }
                }, function (err) {
                    console.log("Register error " + err)
                });
            }

            $rootScope.$on('$cordovaPush:notificationReceived', function (event, notification) {
                if ($ionicPlatform.isAndroid()) {
                    if (notification.event == "registered") {
                        $window.localStorage['pushId'] = notification.regid;
                    } else if (notification.event == "message") {
                        $cordovaToast.showLongTop(notification.message);
                    } else if (notification.event == "error") {
                        console.log("Push notification error event", notification.msg);
                    } else {
                        // TODO: what is this?
                        console.log("Push notification handler - Unprocessed Event", notification.event);
                    }
                } else if ($ionicPlatform.isIOS()) {
                    if (notification.foreground === "1") {
                        if (notification.body && notification.messageFrom) {
                            // TODO: what is this code for?
                            $cordovaToast.showLongTop(notification.body);
                        } else {
                            $cordovaToast.showLongTop(notification.alert);
                        }

                        if (notification.badge) {
                            $cordovaPush.setBadgeNumber(notification.badge).then(function () {}, function (err) {
                                console.log(err)
                            });
                        }
                    }
                }
            });
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