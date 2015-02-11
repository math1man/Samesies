(function() {

    var app = angular.module('directives', ['ionic']);

    app.directive('ssMenu', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/menu.html'
        };
    });

    app.directive('ssProfile', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/edit-profile.html'
        };
    });

    app.directive('ssEpisode', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/episode.html'
        };
    });

    app.directive('ssNearby', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/nearby.html'
        };
    });

    app.directive('ssFriends', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/friends.html'
        };
    });

    app.directive('ssChat', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/chat.html'
        };
    });

    app.directive('ssBrowse', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/browse.html'
        };
    });

})();