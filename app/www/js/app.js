(function() {

    var app = angular.module('samesies', ['ionic', 'samesies.controllers', 'samesies.services', 'samesies.filters']);

    app.run(function ($ionicPlatform) {
        $ionicPlatform.ready(function () {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                // org.apache.cordova.statusbar required
                StatusBar.styleDefault();
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
            .state('chat', {
                cache: false,
                url: "/chat",
                templateUrl: "templates/chat.html"
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

    app.directive('ssLoading', function () {
        return {
            restrict: 'E',
            template: '<div class="item" ng-show="isLoading"><h1 class="align-center"><i class="icon ion-loading-c"></i></h1></div>'
        }
    });

    app.directive('ssBrowseTo', function ($ionicGesture) {
        return {
            restrict: 'A',
            link: function ($scope, $element, $attrs) {
                var handleTap = function () {
                    window.open(encodeURI($attrs.ssBrowseTo), '_system');
                };
                var tapGesture = $ionicGesture.on('tap', handleTap, $element, {});
                $scope.$on('$destroy', function () {
                    // Clean up - unbind drag gesture handler
                    $ionicGesture.off(tapGesture, 'tap', handleTap);
                });
            }
        }
    });

})();