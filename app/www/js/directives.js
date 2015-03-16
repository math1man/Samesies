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

    app.directive('ssAvatar', function () {
        return {
            restrict: 'A',
            link: function ($scope, $element, $attrs) {
                if ($attrs.ssAvatar === "true") {
                    angular.element($element).addClass("item-avatar-left");
                    console.log("left")
                } else {
                    angular.element($element).addClass("item-avatar-right align-right");
                    console.log("right")
                }
            }
        }
    });

})();