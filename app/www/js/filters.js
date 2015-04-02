(function() {

    var app = angular.module('samesies.filters', []);

    app.filter('filterSelf', function (Data) {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.id != Data.user.id) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('requestConnections', function () {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'MATCHING' && !item.data.is1) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('yourTurnConnections', function () {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'IN_PROGRESS' && item.data.state != 'waiting') {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('pendingConnections', function () {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'MATCHING' && item.data.is1) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('waitingConnections', function () {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'IN_PROGRESS' && item.data.state === 'waiting') {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('checkName', function () {
        return function (items, string) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    var regex = new RegExp(".*" + string + ".*", 'i');
                    if (regex.test(item.user.name) || regex.test(item.user.alias)) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('friendRequests', function (Data) {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'PENDING' && item.uid2 === Data.user.id) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('friends', function () {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'ACCEPTED') {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('pendingFriends', function (Data) {
        return function (items) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.status === 'PENDING' && item.uid1 === Data.user.id) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

    app.filter('questionFilter', function () {
        return function (items, string, category) {
            var filtered = [];
            if (items) {
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    var regex = new RegExp(".*" + string + ".*", 'i');
                    if ((category === 'All' || category === item.category) && regex.test(item.q)) {
                        filtered.push(item);
                    }
                }
            }
            return filtered;
        };
    });

})();