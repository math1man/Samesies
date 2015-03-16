(function() {

    var app = angular.module('samesies.directives', []);

    app.directive('ssLoading', function () {
        return {
            restrict: 'E',
            template: '<div class="item"><h1 class="align-center"><i class="icon ion-loading-c"></i></h1></div>'
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