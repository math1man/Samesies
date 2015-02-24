(function() {

    var app = angular.module('filters', ['ionic']);

    app.filter('checkName', function () {
        return function (items, string) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var regex = new RegExp(".*" + string + ".*", 'i');
                if (regex.test(item.name) || regex.test(item.alias)) {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

    app.filter('questionFilter', function () {
        return function (items, string, category) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var regex = new RegExp(".*" + string + ".*", 'i');
                if ((category === 'All' || category === item.category) && regex.test(item.q)) {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

    app.filter('filterSelf', function () {
        return function (items, user) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (item.id != user.id) {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

    app.filter('requestConnections', function () {
        return function (items) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (item.status === 'MATCHING' && !item.data.is1) {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

    app.filter('yourTurnConnections', function () {
        return function (items) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (item.status === 'IN_PROGRESS' && item.data.state != 'waiting') {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

    app.filter('waitingConnections', function () {
        return function (items) {
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                if (item.status === 'MATCHING' && item.data.is1
                    || item.status === 'IN_PROGRESS' && item.data.state === 'waiting') {
                    filtered.push(item);
                }
            }
            return filtered;
        };
    });

})();