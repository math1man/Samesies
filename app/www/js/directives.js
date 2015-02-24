(function() {

    var app = angular.module('directives', ['ionic']);

    app.directive('ssMenu', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/menu.html'
        };
    });

    app.directive('ssEpisode', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/episode.html'
        };
    });

    app.directive('ssCommunities', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/communities.html'
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

    app.directive('ssConnections', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/connections.html'
        };
    });

    app.directive('ssProfile', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/edit-profile.html'
        };
    });

    app.directive('ssBrowse', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/browse.html'
        };
    });

})();