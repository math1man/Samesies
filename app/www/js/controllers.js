(function() {

    const URL = 'https://samesies-app.appspot.com/_ah/api';
    const PING_INTERVAL = 1000; // ms
    const EVERYONE_CID = '5686812383117312'; // ID for the "Everyone" community

    var app = angular.module('samesies.controllers', []);

    app.controller('IndexCtrl', function($scope, $window, $state, $ionicHistory, $ionicViewSwitcher,
                                         $ionicPopup, $ionicModal, API, Data, Utils) {

        $window.init = function() {
            if (!$state.is('login')) {
                $scope.go('login', true, 'none');
            }
            gapi.client.load('samesies', 'v1', function() {
                // initialize API variable
                API.init(gapi.client.samesies.samesiesApi);

                API.getQuestions().then(function(resp) {
                    Data.questions = resp.result.items;
                });

                API.getCategories().then(function(resp) {
                    Data.categories = resp.result.items;
                });

                API.getModes().then(function(resp) {
                    Data.modes = resp.result.items;
                    for (var i = 0; i < Data.modes.length; i++) {
                        if (Data.modes[i].mode === 'Random') {
                            Data.defaultMode = Data.modes[i];
                        }
                    }
                    Data.settings.mode = Data.defaultMode;
                });

            }, URL);
        };

        $ionicModal.fromTemplateUrl('templates/modals/settings.html', {
            scope: $scope,
            animation: 'slide-in-left'
        }).then(function(modal) {
            $scope.settingsPopup = modal;
        });

        $scope.showSettings = function() {
            $scope.settingsPopup.show();
        };

        $scope.closeSettings = function() {
            // make sure settings aren't messed up
            if (Data.settings.mode.mode === 'Personal' && !Utils.hasAllQuestions(Data.user)) {
                Data.settings.mode = Data.defaultMode;
            }
            if (!Data.settings.matchMale && !Data.settings.matchFemale && !Data.settings.matchOther) {
                Data.settings.matchMale = true;
                Data.settings.matchFemale = true;
                Data.settings.matchOther = true;
            }
            $scope.settingsPopup.hide();
        };

        $scope.dispName = function(user) {
            if (!user) {
                return '';
            } else if (user.name) {
                return user.name;
            } else {
                return user.alias;
            }
        };

        $scope.hasQuestions = function(user) {
            if (user && user.questions) {
                for (var i = 0; i < 5; i++) {
                    if (user.questions[i]) {
                        return true;
                    }
                }
            }
            return false;
        };

        $scope.connect = function(user) {
            API.connectEpisode(Data.user.id, user.id, Data.settings).then(function(resp) {
                var episode = resp.result;
                episode.data = Utils.getData(episode);
                episode.user = user;
                Data.connections.push(episode);
            });
            $ionicPopup.alert({
                title: 'Connection Sent',
                template: 'You have sent a connection to ' + $scope.dispName(user) + '.',
                okText: 'Okay',
                okType: 'button-royal'
            });
        };

        $scope.data = function (field) {
            return Data[field];
        };

        $scope.back = function() {
            $ionicHistory.goBack();
        };

        $scope.go = function(state, historyRoot, direction) {
            if (angular.isUndefined(direction)) {
                direction = 'forward';
            }
            if (angular.isUndefined(historyRoot)) {
                historyRoot = false;
            }
            $ionicHistory.nextViewOptions({
                historyRoot: historyRoot,
                disableAnimate: direction === 'none'
            });
            $ionicViewSwitcher.nextDirection(direction);
            $state.go(state);
        };

        $scope.refresh = function() {
            $scope.refreshCommunities();
            if (Data.user) {
                API.getFriends(Data.user.id).then(function(resp) {
                    var friends = resp.result.items;
                    if (friends && friends.length) {
                        Data.friends = friends;
                    }
                    if (Data.isLoading) {
                        Data.isLoading--;
                    }
                    $scope.$apply();
                    // nested so that it can pull from friends
                    $scope.refreshChats();
                    $scope.refreshCxns();
                });
            }
        };

        $scope.refreshCommunities = function() {
            if (Data.user) {
                API.getUserCommunities(Data.user.id).then(function(resp) {
                    var communities = resp.result.items;
                    if (communities && communities.length) {
                        Data.communities = communities;
                        $scope.$apply();
                    }
                    if (Data.isLoading) {
                        Data.isLoading--;
                    }
                });
            }
        };

        $scope.refreshChats = function() {
            if (Data.user) { // need to add a second check in case they immediately log out
                API.getChats(Data.user.id).then(function (resp) {
                    var chats = resp.result.items;
                    if (chats && chats.length) {
                        for (var i = 0; i < chats.length; i++) {
                            var index = Utils.indexOfById(Data.friends, chats[i].user, 'user');
                            if (index > -1) {
                                chats[i].user = Data.friends[index].user;
                            }
                        }
                        Data.chats = chats;
                    }
                    if (Data.isLoading) {
                        Data.isLoading--;
                    }
                    $scope.$apply();
                });
            }
        };

        $scope.refreshCxns = function() {
            if (Data.user) { // need to add a second check in case they immediately log out
                API.getConnections(Data.user.id).then(function (resp) {
                    var cxns = resp.result.items;
                    if (cxns && cxns.length) {
                        for (var i = 0; i < cxns.length; i++) {
                            cxns[i].data = Utils.getData(cxns[i]);
                            if (cxns[i].user) {
                                var index = Utils.indexOfById(Data.friends, cxns[i].user, 'user');
                                if (index > -1) {
                                    cxns[i].user = Data.friends[index].user;
                                }
                            } else {
                                cxns[i].user = {
                                    name: 'Matching...',
                                    avatar: 'img/lone_icon.png'
                                }
                            }
                        }
                        Data.connections = cxns;
                    }
                    if (Data.isLoading) {
                        Data.isLoading--;
                    }
                    $scope.$apply();
                });
            }
        };

        $scope.$on('$destroy', function() {
            $scope.settingsPopup.remove();
        });

    });

    app.controller('LoginCtrl', function($scope, $window, $ionicPopup, API, Data) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.loginData = {
                error: false,
                email: $window.localStorage['email'],
                avatar: 'img/lone_icon.png'
            };
            $scope.loginCheck = {};
            $scope.isLoading = false;
        });

        $scope.login = function(user) {
            $window.localStorage['email'] = $scope.loginData.email;
            $scope.loginData = null;
            $scope.loginCheck = {};
            Data.user = user;
            Data.isLoading = 4;
            $scope.refresh();
            $scope.resetToggle();
            $scope.go('menu', true);
        };

        $scope.loginShortcut = function() {
            if ($scope.isToggled()) {
                $scope.createAccount();
            } else {
                $scope.doLogin()
            }
        };

        $scope.doLogin = function() {
            if (!$scope.loginData.email) {
                $scope.loginData.error = "Invalid email";
            } else if (!$scope.loginData.password) {
                $scope.loginData.error = "Invalid password";
            } else {
                $scope.loginData.error = "";
            }
            if (!$scope.loginData.error) {
                $scope.isLoading = true;
                API.login($scope.loginData).then(function (resp) {
                    $scope.login(resp.result);
                }, function (reason) { // error
                    $scope.isLoading = false;
                    if (reason.status >= 500) {
                        $scope.loginData.error = 'Server error'
                    } else if (reason.status >= 400) {
                        $scope.loginData.error = reason.result.error.message;
                    } else {
                        $scope.loginData.error = 'Unknown error'
                    }
                    $scope.$apply();
                });
            }
        };

        $scope.createAccount = function() {
            if (!$scope.loginData.email) {
                $scope.loginData.error = "Invalid email";
            } else if (!$scope.loginData.password || $scope.loginData.password.length <= 5) {
                $scope.loginData.error = "Password too short";
            } else if ($scope.loginData.password != $scope.loginData.confirmPassword) {
                $scope.loginData.error = "Passwords don't match";
            } else if (!$scope.loginCheck.accept) {
                $scope.loginData.error = "Accept Terms and Conditions";
            } else {
                $scope.loginData.error = "";
            }
            if (!/\S/.test($scope.loginData.alias)) {
                $scope.loginData.alias = null;
            }
            if (!$scope.loginData.error) {
                $scope.isLoading = true;
                API.createUser($scope.loginData).then(function(resp){
                    $scope.login(resp.result);
                }, function () { // error
                    $scope.isLoading = false;
                    $scope.loginData.error = '';
                    $scope.$apply();
                    $ionicPopup.alert({
                        title: 'Bad Email',
                        template: 'That email is already in use.',
                        okText: 'Okay',
                        okType: 'button-royal'
                    });
                });
            }
        };

        $scope.selectAvatar = function() {
            $scope.tempData = {
                image: $scope.loginData.avatar
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Update Profile Picture',
                templateUrl: 'templates/popups/upload-avatar.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData.image;
                        }
                    }
                ]
            }).then(function(image) {
                if (angular.isDefined(image)) {
                    $scope.loginData.avatar = image;
                }
                $scope.tempData = null;
            });
        };

        $scope.recoverPassword = function() {
            if ($scope.loginData.email) {
                $ionicPopup.confirm({
                    title: 'Recover Password',
                    template: 'Your password will be changed to a random sequence of eight characters, ' +
                            'which will then be emailed to you. Are you sure you want to proceed?',
                    okText: 'Okay',
                    okType: 'button-royal',
                    cancelText: 'Cancel',
                    cancelType: 'button-stable'
                }).then(function(resp) {
                    if (resp) {
                        API.recoverUser($scope.loginData.email).then(function () {
                            // no error
                            $ionicPopup.alert({
                                title: 'Recover Password',
                                template: 'Your password has been changed and emailed to ' + $scope.loginData.email + '.',
                                okText: 'Okay',
                                okType: 'button-royal'
                            });
                        }, function(reason) {
                            if (reason.status === 404) {
                                $ionicPopup.alert({
                                    title: 'Recover Password',
                                    template: 'The email you specified is not in our system.  Please try a different email.',
                                    okText: 'Okay',
                                    okType: 'button-royal'
                                });
                            }
                        });
                    }
                });
            } else {
                $ionicPopup.alert({
                    scope: $scope,
                    title: 'Recover Password',
                    template: 'Please enter your email above so that we can recover your account.',
                    okText: 'Okay',
                    okType: 'button-royal'
                });
            }
        };

        var toggle = false;

        $scope.toggle = function() {
            toggle = !toggle;
            $scope.loginData.error = false;
        };

        $scope.isToggled = function() {
            return toggle;
        };

        $scope.resetToggle = function() {
            toggle = false;
        };

    });

    app.controller('MenuCtrl', function ($scope, $ionicModal, $ionicPopup, $ionicPopover, API, Data, Utils) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.refresh();
            Data.tempUser = null;
        });

        $scope.logout = function() {
            Data.user = null;
            Data.connections = [];
            Data.chats = [];
            Data.friends = [];
            $scope.go('login', true, 'back');
        };

        $scope.getCxnRequestCount = function() {
            var count = 0;
            if (Data.connections && Data.connections.length) {
                for (var i = 0; i < Data.connections.length; i++) {
                    var item = Data.connections[i];
                    if (item.data) {
                        // Connections pending your approval
                        if (item.status === 'MATCHING' && !item.data.is1) {
                            count++;
                        }
                        // Connections pending your response
                        if (item.status === 'IN_PROGRESS' && item.data.state != 'waiting') {
                            count++;
                        }
                    }
                }
            }
            return count;
        };

        $scope.getChatCount = function() {
            var count = 0;
            if (Data.user && Data.chats && Data.chats.length) {
                for (var i = 0; i < Data.chats.length; i++) {
                    if (Utils.isUpdated(Data.chats[i])) {
                        count++;
                    }
                }
            }
            return count;
        };

        $scope.getFriendRequestCount = function() {
            var count = 0;
            if (Data.user && Data.friends && Data.friends.length) {
                for (var i = 0; i < Data.friends.length; i++) {
                    var item = Data.friends[i];
                    if (item.status === 'PENDING' && item.uid2 === Data.user.id) { // Connections pending your approval
                        count++;
                    }
                }
            }
            return count;
        };

    });

    app.controller('SettingsCtrl', function($scope, $ionicPopup, $ionicPopover, Data, Utils) {

        $scope.$on('modal.shown', function() {
            $scope.settings = Data.settings;
        });

        $ionicPopover.fromTemplateUrl('templates/popovers/select-community.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.selectPopup = popover;
        });

        $scope.showSelect = function($event) {
            $scope.selectPopup.show($event);
        };

        $scope.hideSelect = function() {
            $scope.selectPopup.hide();
        };

        var isShowModes = false;

        $scope.isShowModes = function() {
            return isShowModes;
        };

        $scope.showModes = function() {
            isShowModes = true;
        };

        $scope.selectMode = function() {
            isShowModes = false;
        };

        $scope.saveSettings = function() {
            if ($scope.settings.mode.mode === 'Personal' && !Utils.hasAllQuestions(Data.user)) {
                $ionicPopup.alert({
                    title: 'Cannot Match',
                    template: 'You must list 5 personal questions on your profile in order to play the Personal mode.',
                    okText: 'Okay',
                    okType: 'button-royal'
                });
            } else if (!$scope.settings.matchMale && !$scope.settings.matchFemale && !$scope.settings.matchOther) {
                $ionicPopup.alert({
                    title: 'Cannot Match',
                    template: 'You must specify at least one gender to match with.',
                    okText: 'Okay',
                    okType: 'button-royal'
                });
            } else {
                Data.settings = $scope.settings;
                $scope.closeSettings();
            }
        };

    });

    app.controller('SelectComCtrl', function($scope, $ionicPopup, API, Data, Utils) {

        $scope.$on('popover.shown', function() {
            $scope.selected = Data.community;
            $scope.search = '';
            $scope.searched = [];
        });

        $scope.searchCommunities = function(string) {
            API.searchCommunities(string).then(function(resp) {
                var list = resp.result.items;
                if (list && list.length) {
                    for (var i=0; i<Data.communities.length; i++) {
                        Utils.removeById(list, Data.communities[i]);
                    }
                    $scope.searched = list;
                    $scope.$apply();
                }
            })
        };

        $scope.join = function(community) {
            if (community.type === 'EMAIL') {
                $scope.tempData = [''];
                $ionicPopup.show({
                    scope: $scope,
                    title: 'Enter a valid ' + community.name + ' email',
                    template: '<label class="item item-input"><input type="email" placeholder="johndoe@community.org" ng-model="tempData[0]"></label>',
                    buttons: [{
                        text: 'Cancel',
                        type: 'button-stable'
                    },{
                        text: 'Submit',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData[0];
                        }
                    }]
                }).then(function(email) {
                    if (angular.isDefined(email)) {
                        API.joinCommunity(community.id, Data.user.id, email).then(function (resp) {
                            if (resp.result) {
                                Data.communities.push(resp.result);
                                $scope.loadCommunity(resp.result);
                            } else {
                                $scope.hideSelect();
                            }
                        });
                    }
                });
            } else if (community.type === 'PASSWORD') {
                $scope.tempData = [''];
                $ionicPopup.show({
                    scope: $scope,
                    title: "Enter " + community.name + "'s access password",
                    template: '<label class="item item-input"><input type="password" placeholder="••••••••" ng-model="tempData[0]"></label>',
                    buttons: [{
                        text: 'Cancel',
                        type: 'button-stable'
                    },{
                        text: 'Submit',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData[0];
                        }
                    }]
                }).then(function(password) {
                    if (angular.isDefined(password)) {
                        API.joinCommunity(community.id, Data.user.id, password).then(function (resp) {
                            if (resp.result) {
                                Data.communities.push(resp.result);
                                $scope.loadCommunity(resp.result);
                            }
                        });
                    }
                });
            } else { // handle OPEN and EXCLUSIVE together
                API.joinCommunity(community.id, Data.user.id).then(function (resp) {
                    if (resp.result) {
                        Data.communities.push(resp.result);
                        $scope.loadCommunity(resp.result);
                    }
                });
            }
        };

        $scope.loadCommunity = function(community) {
            Data.community = community;
            $scope.hideSelect();
            if ($scope.addCommPopup) {
                $scope.addCommPopup.hide();
            }
            API.getCommunity(community.id).then(function(resp) {
                Data.community = resp.result;
                $scope.$apply();
            });
        };

    });

    app.controller('EpisodeCtrl', function($scope, $window, $cordovaKeyboard, $ionicPopup, $ionicModal, API, Utils, Data) {

        $ionicModal.fromTemplateUrl('templates/modals/help.html', {
            scope: $scope,
            animation: 'slide-in-left'
        }).then(function(modal) {
            $scope.helpPopup = modal;
        });

        $scope.showHelp = function() {
            $scope.helpPopup.show();
        };

        $scope.closeHelp = function() {
            $scope.helpPopup.hide();
        };

        $scope.isPersistent = function() {
           return Data.episode && Data.episode.isPersistent;
        };

        var go = function(state) {
            Utils.interruptAll();
            $scope.episodeData.state = state;
            if (state === 'waiting') {
                Utils.interval(function () {
                    API.getEpisode(Data.episode.id).then(function (resp) {
                        if ($scope.is('waiting')) {
                            updateEpisode(resp.result);
                        }
                    });
                }, PING_INTERVAL);
            }
        };

        $scope.is = function(state) {
            if ($scope.episodeData) {
                return ($scope.episodeData.state === state);
            } else {
                return false;
            }
        };

        $scope.matchText = function() {
            if ($scope.is('matching')) {
                return "Matching...";
            } else {
                return "Matched!"
            }
        };

        $scope.find = function() {
            if (!$scope.isPersistent()) {
                if (Data.episode) {
                    API.endEpisode(Data.episode.id);
                }
                $scope.episodeData = {
                    state: 'matching',
                    stage: 0
                };
                var params = {};
                if (Data.community.id != EVERYONE_CID) {
                    params.cid = Data.community.id;
                }
                // **Eventually** TODO: location
                API.findEpisode(Data.user.id, Data.settings, params).then(function (resp) {
                    Data.episode = resp.result;
                    if (Data.episode.status === "MATCHING") {
                        Utils.interval(function () {
                            API.getEpisode(Data.episode.id).then(function (resp) {
                                // second condition prevents multiple calls
                                Data.episode = resp.result;
                                if (Data.episode.status === "IN_PROGRESS" && $scope.is('matching')) {
                                    loadEpisode(Data.episode);
                                }
                            });
                        }, PING_INTERVAL);
                    } else {
                        loadEpisode(Data.episode);
                    }
                });
            }
        };

        var loadEpisode = function(ep) {
            $scope.episodeData = Utils.getData(ep);
            if (!ep.user) {
                API.getUser(Utils.getPartnerId(ep)).then(function (resp) {
                    ep.user = resp.result;
                });
            }
            if ($scope.episodeData.questions) {
                go($scope.episodeData.state);
            } else {
                API.getEpisodeQuestions(ep.id).then(function (resp) {
                    $scope.episodeData.questions = resp.result.items;
                    $scope.$apply();
                    go($scope.episodeData.state);
                });
            }
        };

        $scope.next = function() {
            $scope.episodeData.myAnswer = '';
            if ($scope.episodeData.stage == 10) {
                API.endEpisode(Data.episode.id);
                Data.episode.status = "COMPLETE";
                API.getUser(Utils.getPartnerId(Data.episode)).then(function(resp) {
                    Data.tempUser = resp.result;
                    $ionicPopup.confirm({
                        title: 'Is it Samesies?',
                        template: "Would you describe your and your partner's answers as Samesies?",
                        okText: "Yes, let's chat!",
                        okType: 'button-royal',
                        cancelText: "Not really",
                        cancelType: 'button-stable'
                    }).then(function (answer) {
                        var index = Utils.indexOfById(Data.friends, Data.tempUser, 'user');
                        if (index > -1) {
                            if (answer) {
                                var friend = Data.friends[index];
                                API.startChat(friend.id, false, Data.user.id, friend.user.id).then(function (resp) {
                                    Data.chat = resp.result;
                                    Data.chat.user = friend.user;
                                    Utils.addById(Data.chats, Data.chat);
                                    $scope.go('chat');
                                });
                            } else if ($scope.isPersistent()) {
                                $scope.back();
                            } else {
                                $scope.find();
                            }
                        } else {
                            API.startChat(Data.episode.id, true, Data.user.id, Data.tempUser.id).then(function (resp) {
                                Data.chat = resp.result;
                                if (answer) {
                                    Data.chat.user = Data.tempUser;
                                    Data.chats.push(Data.chat);
                                    $scope.go('chat');
                                } else {
                                    API.closeChat(Data.chat.id);
                                    Data.chat = null;
                                    if ($scope.isPersistent()) {
                                        $scope.back();
                                    } else {
                                        $scope.find();
                                    }
                                }
                            });
                        }
                    });
                });
            } else {
                $scope.episodeData.stage++;
                go('entry');
            }
        };

        $scope.getQuestion = function() {
            if ($scope.episodeData.questions) {
                return $scope.episodeData.questions[$scope.episodeData.stage - 1].q;
            } else {
                return "";
            }
        };

        $scope.answer = function() {
            if (ionic.Platform.isIOS()) {
                $cordovaKeyboard.close();
            }
            $scope.episodeData.theirAnswer = "Waiting for your partner to answer...";
            go('waiting');
            API.answerEpisode(Data.episode.id, Data.user.id, $scope.episodeData.myAnswer).then(function(resp) {
                updateEpisode(resp.result);
            });
        };

        var updateEpisode = function(ep) {
            var user = Data.episode.user;
            Data.episode = ep;
            Data.episode.user = user;
            if (Data.episode.status === "ABANDONED") {
                Utils.interruptAll();
                $ionicPopup.confirm({
                    title: 'Partner Left',
                    template: 'Your partner has left the connection.',
                    okText: 'Play Again!',
                    okType: 'button-royal',
                    cancelText: 'Back',
                    cancelType: 'button-stable'
                }).then(function(resp) {
                    if (resp) {
                        $scope.find();
                    } else {
                        $scope.back();
                    }
                });
            } else {
                var index = $scope.episodeData.stage - 1;
                var answer, answers;
                if ($scope.episodeData.is1) {
                    answers = Data.episode.answers2;
                } else {
                    answers = Data.episode.answers1;
                }
                // make sure it has the list and that the list is long enough
                if (answers && answers.length > index) {
                    answer = answers[index];
                }
                if (answer != null) { // they also answered
                    $scope.episodeData.theirAnswer = answer;
                    go('continue');
                }
            }
        };

        var cleanUpEpisode = function() {
            Utils.interruptAll();
            if (Data.episode) {
                if (Data.episode.isPersistent) {
                    if (Data.episode.status === "IN_PROGRESS") {
                        Data.episode.data = $scope.episodeData;
                        Data.connections.push(Data.episode);
                    }
                } else {
                    API.endEpisode(Data.episode.id);
                }
                Data.episode = null;
            }
        };

        $scope.$on('$ionicView.beforeLeave', function() {
            cleanUpEpisode();
        });

        $scope.$on('destroy', function() {
            $scope.helpPopup.remove();
            cleanUpEpisode();
        });

        $window.onbeforeunload = function() {
            cleanUpEpisode();
        };

        if (Data.episode) {
            loadEpisode(Data.episode);
        } else {
            $scope.find();
        }

    });

    app.controller('BrowseCtrl', function($scope, $ionicPopup, $ionicPopover, API, Data) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.refreshCommunities();
        });

        $ionicPopover.fromTemplateUrl('templates/popovers/select-community.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.selectPopup = popover;
        });

        $scope.showSelect = function($event) {
            $scope.selectPopup.show($event);
        };

        $scope.hideSelect = function() {
            $scope.selectPopup.hide();
        };

        $scope.flag = function(user) {
            $scope.reason = [''];
            $ionicPopup.show({
                scope: $scope,
                title: 'Why is this inappropriate?',
                template: '<label class="item item-input"><input type="text" placeholder="Reason" ng-model="reason[0]"></label>',
                buttons: [{
                    text: 'Cancel',
                    type: 'button-stable'
                },{
                    text: 'Submit',
                    type: 'button-assertive',
                    onTap: function() {
                        return $scope.reason[0];
                    }
                }]
            }).then(function(reason) {
                if (angular.isDefined(reason)) {
                    API.flagUser(user.id, Data.user.id, reason);
                }
            });
        };

    });

    app.controller('ConnectionsCtrl', function($scope, $ionicPopup, Data, API, Utils) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.refreshCxns();
        });

        $scope.isLoading = function() {
            return Data.isLoading && Data.connections.length === 0;
        };

        $scope.accept = function(cxn) {
            cxn.status = "IN_PROGRESS";
            API.acceptEpisode(cxn.id);
            $scope.play(cxn);
        };

        $scope.reject = function(cxn) {
            API.endEpisode(cxn.id);
            removeCxn(cxn);
        };

        $scope.play = function(cxn) {
            Data.episode = cxn;
            removeCxn(cxn);
            $scope.go('play');
        };

        $scope.remove = function(cxn) {
            $ionicPopup.confirm({
                title: 'Abandon Connection',
                template: 'Are you sure you want to abandon your connection with ' + $scope.dispName(cxn.user) + '?',
                okText: 'Abandon',
                okType: 'button-assertive',
                cancelText: 'Cancel',
                cancelType: 'button-stable'
            }).then(function(resp) {
                if (resp) {
                    $scope.reject(cxn);
                }
            })
        };

        $scope.dispStage = function(cxn) {
            if (cxn.status === 'MATCHING') {
                if (cxn.uid2) {
                    return 'Pending';
                } else {
                    return 'Matching';
                }
            } else {
                return 'Stage ' + cxn.data.stage;
            }
        };

        var removeCxn = function(cxn) {
            Utils.removeById(Data.connections, cxn);
        };
    });

    app.controller('MessagesCtrl', function($scope, API, Data, Utils) {

        $scope.search = '';

        $scope.isLoading = function() {
            return Data.isLoading && Data.chats.length === 0;
        };

        $scope.goChat = function(chat) {
            if (chat.uid1 === Data.user.id) {
                chat.isUpToDate1 = true;
            } else {
                chat.isUpToDate2 = true;
            }
            Data.chat = chat;
            $scope.go('chat');
        };

        $scope.remove = function(chat) {
            if (chat.isEpisode) { // episode chats cannot be recovered
                $ionicPopup.confirm({
                    title: 'Close Chat',
                    template: 'Are you sure you want to close your chat with ' + $scope.dispName(chat.user) + '?' +
                            'You will not be able to return to it.',
                    okText: 'Close Chat',
                    okType: 'button-assertive',
                    cancelText: 'Cancel',
                    cancelType: 'button-stable'
                }).then(function (resp) {
                    if (resp) {
                        close(chat);
                    }
                });
            } else { // but friend chats can
                close(chat);
            }
        };

        $scope.showBadge = function(chat) {
            return Utils.isUpdated(chat);
        };

        var close = function(chat) {
            API.closeChat(chat.id);
            Utils.removeById(Data.chats, chat);
        }

    });

    app.controller('ChatCtrl', function($scope, $window, $timeout, $ionicPopup, $ionicScrollDelegate, API, Data, Utils) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.refreshChats();
        });

        // **Low-Priority** TODO: the focusInput/scrollBottom interactions are really awkward, so we aren't using focusInput atm
        //var focusInput = function() {
        //    document.getElementById("chatInput").focus();
        //};

        $scope.buffer = '';
        $scope.history = [];
        var friendPending = 0; // 0: they haven't clicked, 1: they clicked, 2: accepted
        if (Data.chat) {
            if (!Data.chat.isEpisode) {
                friendPending = 2;
            }
            API.getMessages(Data.chat.id, Data.chat.startDate, Data.user.id).then(function (resp) {
                if (resp.result.items && resp.result.items.length) {
                    $scope.history = resp.result.items;
                    $scope.$apply();
                    scrollBottom(true);
                }
                Utils.interval(function () {
                    checkChat();
                }, PING_INTERVAL);
            });
        }

        var scrollBottom = function(animate) {
            $timeout(function() {
                $ionicScrollDelegate.resize();
                $ionicScrollDelegate.scrollBottom(animate);
            }, 50);
        };

        var randomId = function() {
            var output = "";
            var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            for(var i=0; i<20; i++) {
                output += possible.charAt(Math.floor(Math.random() * possible.length));
            }
            return output;
        };

        $scope.sendChat = function() {
            if ($scope.buffer) {
                var random = randomId();
                addMessage({
                    message: $scope.buffer,
                    senderId: Data.user.id,
                    random: random
                });
                scrollBottom(true);
                API.sendMessage(Data.chat.id, Data.user.id, $scope.buffer, random).then(function (resp) {
                    addMessage(resp.result);
                });
                $scope.buffer = '';
            }
        };

        var addMessage = function(message) {
            var index = -1;
            // goes back to front because more likely to find it at back
            for (var i = $scope.history.length - 1; i >= 0 && index === -1; i--) {
                if ($scope.history[i].random === message.random) {
                    index = i;
                }
            }
            if (index === -1) {
                $scope.history.push(message);
            } else {
                $scope.history[index] = message;
            }
            var modified = message.sentDate;
            if (modified && new Date(modified) > new Date(Data.chat.lastModified)) {
                Data.chat.lastModified = modified;
            }
        };

        var checkChat = function() {
            if (Data.chat.isEpisode) {
                API.getChat(Data.chat.id).then(function (resp) {
                    if (resp.result.isClosed) {
                        Utils.interruptAll();
                        $ionicPopup.confirm({
                            title: "Partner didn't want to chat",
                            template: "Your partner wanted to keep playing Samesies. Do you want to play another game?",
                            okText: "Play Again",
                            okType: 'button-royal',
                            cancelText: 'Not right now',
                            cancelType: 'button-stable'
                        }).then(function (resp) {
                            if (resp) {
                                $scope.back();
                            } else {
                                $scope.go('menu', true, 'back');
                            }
                        });
                    }
                });
                if (!friendPending) { // if its pending, it will stay that way until user accepts
                    API.checkFriend(Data.user.id, Data.chat.user.id).then(function (resp) {
                        var friend = resp.result;
                        if (friend) {
                            Utils.addById(Data.friends, friend);
                            if (friend.status === 'ACCEPTED') {
                                friendPending = 2;
                            } else if (friend.uid2 === Data.user.id) {
                                friendPending = 1;
                            }
                        }
                    });
                }
            }
            API.getMessages(Data.chat.id, Data.chat.lastModified, Data.user.id).then(function (resp) {
                var messages = resp.result.items;
                if (messages && messages.length > 0) {
                    for (var i=0; i<messages.length; i++) {
                        addMessage(messages[i]);
                    }
                    var size = $scope.history.length;
                    if (size > 110) {
                        $scope.history = $scope.history.slice(size - 100, size);
                    }
                    $scope.$apply();
                    scrollBottom(true);
                }
            });
        };

        $scope.isMine = function(message) {
            return (message.senderId === Data.user.id);
        };

        $scope.getAvatar = function(message) {
            if ($scope.isMine(message)) {
                return Data.user.avatar;
            } else {
                return Data.chat.user.avatar;
            }
        };

        $scope.dispDate = function(message) {
            if (message.sentDate) {
                return "Sent on " + new Date(message.sentDate).toLocaleString();
            } else {
                return "Sending...";
            }
        };

        $scope.showAddFriend = function() {
            return friendPending < 2;
        };

        $scope.showOtherAdded = function() {
            return friendPending === 1;
        };

        $scope.addFriend = function() {
            API.addFriend(Data.user.id, Data.chat.user.id).then(function(resp) {
                var friend = resp.result;
                if (friend) {
                    Utils.addById(Data.friends, friend);
                    if (friend.status === 'ACCEPTED') {
                        API.updateChat(Data.chat.id, friend.id, false);
                        friendPending = 2;
                    }
                }
            });
        };

        if (ionic.Platform.isIOS()) {
            $window.addEventListener('native.keyboardshow', function (e) {
                document.getElementById("inputBar").style.marginBottom = e.keyboardHeight + "px";
            });

            $window.addEventListener('native.keyboardhide', function () {
                document.getElementById("inputBar").style.marginBottom = "0";
            });
        }

        $scope.$on('$ionicView.beforeLeave', function() {
            Utils.interruptAll();
            Data.chat = null;
        });

    });

    app.controller('FriendsCtrl', function($scope, $ionicPopover, $ionicPopup, Data, API, Utils) {

        $ionicPopover.fromTemplateUrl('templates/popovers/find-friends.html', {
            scope: $scope,
            focusFirstInput: true
        }).then(function(popover) {
            $scope.findPopup = popover;
        });

        $scope.showFind = function($event) {
            $scope.findPopup.show($event);
        };

        $scope.closeFind = function() {
            $scope.tempUser = null;
            $scope.email = [''];
            $scope.findPopup.hide();
        };

        $scope.isLoading = function() {
            return Data.isLoading && Data.friends.length === 0;
        };

        $scope.search = '';

        $scope.profile = function(friend) {
            Data.tempUser = friend.user;
            Data.friend = friend;
            $scope.go('profile');
        };

        $scope.accept = function(friend) {
            API.addFriend(Data.user.id, friend.user.id).then(function(resp) {
                var friend = resp.result;
                if (friend) {
                    Utils.addById(Data.friends, friend);
                    $scope.profile(friend);
                }
            });
        };

        $scope.reject = function(friend) {
            API.removeFriend(friend.id, Data.user.id);
            Utils.removeById(Data.friends, friend);
        };

        $scope.remove = function(friend) {
            $ionicPopup.confirm({
                title: 'Remove Friend',
                template: 'Are you sure you want to remove ' + friend.user.name + '?',
                okText: 'Remove',
                okType: 'button-assertive',
                cancelText: 'Cancel',
                cancelType: 'button-stable'
            }).then(function(resp) {
                if (resp) {
                    $scope.reject(friend);
                }
            })
        };

        $scope.$on('$destroy', function() {
            $scope.findPopup.remove();
        });

    });

    app.controller('FindFriendCtrl', function($scope, API, Data, Utils) {
        $scope.list = [];
        $scope.search = '';

        $scope.findFriend = function(string) {
            API.searchUsers(string).then(function(resp) {
                var list = resp.result.items;
                if (list && list.length) {
                    $scope.list = list;
                    $scope.$apply();
                }
            });
        };

        $scope.add = function(user) {
            if (user) {
                API.addFriend(Data.user.id, user.id).then(function(resp) {
                    var friend = resp.result;
                    if (friend && !Utils.containsById(Data.friends, friend)) {
                        Data.friends.push(friend);
                        $scope.$apply();
                    }
                });
            }
            $scope.closeFind();
        };

        $scope.$on('popover.hidden', function() {
            $scope.list = [];
            $scope.search = '';
        });

    });

    app.controller('ProfileCtrl', function($scope, API, Data) {

        $scope.isMe = function() {
            return Data.user.id === Data.tempUser.id;
        };

        $scope.message = function() {
            API.startChat(Data.friend.id, false, Data.user.id, Data.tempUser.id).then(function (resp) {
                Data.chat = resp.result;
                Data.chat.user = Data.tempUser;
                Data.chats.push(Data.chat);
                $scope.go('chat');
            });
        };
    });

    app.controller('CommunitiesCtrl', function($scope, $ionicPopover, API, Data, Utils) {

        $ionicPopover.fromTemplateUrl('templates/popovers/add-community.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.addCommPopup = popover;
        });

        $scope.showAddComm = function($event) {
            $scope.addCommPopup.show($event);
        };

        $scope.$on('$destroy', function() {
            $scope.addCommPopup.remove();
        });

        $scope.isLoading = function() {
            return Data.isLoading && Data.communities.length === 0;
        };

        $scope.leave = function(community) {
            API.leaveCommunity(community.id, Data.user.id);
            Utils.removeById(Data.communities, community);
        }

    });

    app.controller('QuestionsCtrl', function($scope, $ionicPopover, $ionicScrollDelegate) {

        $scope.category = ['All'];
        $scope.search = '';

        $ionicPopover.fromTemplateUrl('templates/popovers/select-category.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.categoryPopup = popover;
        });

        $scope.selectCategory = function($event) {
            $scope.categoryPopup.show($event);
        };

        $scope.closeCategoryPopup = function() {
            $scope.categoryPopup.hide();
            $ionicScrollDelegate.scrollTop(true);
        };

        $ionicPopover.fromTemplateUrl('templates/popovers/suggest-question.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.suggestPopup = popover;
        });

        $scope.makeSuggestion = function($event) {
            $scope.suggestPopup.show($event);
        };

        $scope.closeSuggestPopup = function() {
            $scope.suggestPopup.hide();
        };

        $scope.$on('$destroy', function() {
            $scope.categoryPopup.remove();
            $scope.suggestPopup.remove();
        })

    });

    app.controller('SuggestCtrl', function($scope, API) {

        $scope.suggestedQuestion = [''];

        $scope.suggest = function() {
            if ($scope.suggestedQuestion[0]) {
                API.suggestQuestion($scope.suggestedQuestion[0]);
                $scope.suggestedQuestion = [''];
            }
            $scope.closeSuggestPopup();
        }
    });

    app.controller('EditProfileCtrl', function($scope, $ionicPopup, Data, API) {

        var isChanged = false;

        $scope.edit = function(property, field, type, required) {
            $scope.tempData = {
                value: Data.user[field],
                type: type,
                placeholder: property
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Edit ' + property,
                templateUrl: 'templates/popups/edit.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function(e) {
                            if (!required || ($scope.tempData.value && /\S/.test($scope.tempData.value))) {
                                return $scope.tempData.value;
                            } else {
                                e.preventDefault();
                            }
                        }
                    }
                ]
            }).then(function(update) {
                if (angular.isDefined(update)) {
                    Data.user[field] = update;
                    isChanged = true;
                }
                $scope.tempData = null;
            });
        };

        $scope.editPassword = function() {
            $scope.tempData = {
                password: '',
                newPassword: '',
                confirmPassword: '',
                error: function() {
                    if ($scope.tempData.newPassword.length <= 5) {
                        return "New password too short!";
                    } else if ($scope.tempData.newPassword != $scope.tempData.confirmPassword) {
                        return "Passwords don't match!";
                    } else {
                        return "";
                    }
                }
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Change Password',
                templateUrl: 'templates/popups/edit-password.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable',
                        onTap: function() { return null; }
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function(e) {
                            if (!$scope.tempData.error()) {
                                return $scope.tempData;
                            } else {
                                e.preventDefault();
                            }
                        }
                    }
                ]
            }).then(function(passwordData) {
                if (passwordData) {
                    Data.user.password = passwordData.password;
                    Data.user.newPassword = passwordData.newPassword;
                    API.updateUser(Data.user).then(function() {
                        $ionicPopup.alert({
                            title: 'Password Changed',
                            template: 'Your password has successfully been changed.',
                            okText: 'Okay',
                            okType: 'button-royal'
                        });
                    }, function(reason) {
                        if (reason.status === 400) {
                            $ionicPopup.alert({
                                title: 'Incorrect Password',
                                template: 'The password you entered was incorrect. Password has not been changed.',
                                okText: 'Okay',
                                okType: 'button-royal'
                            });
                        }
                    });
                }
                $scope.tempData = null;
            });
        };

        $scope.editGender = function() {
            $scope.tempData = {
                value: Data.user.gender
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Edit Gender',
                templateUrl: 'templates/popups/edit-gender.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData.value;
                        }
                    }
                ]
            }).then(function(update) {
                if (angular.isDefined(update)) {
                    Data.user.gender = update;
                    isChanged = true;
                }
                $scope.tempData = null;
            });
        };

        $scope.editQuestion = function(num) {
            $scope.tempData = {
                value: Data.user.questions[num],
                type: "textarea",
                placeholder: "Question"
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Edit Question',
                templateUrl: 'templates/popups/edit.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() { return $scope.tempData.value; }
                    }
                ]
            }).then(function(update) {
                if (angular.isDefined(update)) {
                    Data.user.questions[num] = update;
                    isChanged = true;
                }
                $scope.tempData = null;
            });
        };

        $scope.editAvatar = function() {
            $scope.tempData = {
                image: Data.user.avatar
            };
            $ionicPopup.show({
                scope: $scope,
                title: 'Update Profile Picture',
                templateUrl: 'templates/popups/upload-avatar.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData.image;
                        }
                    }
                ]
            }).then(function(image) {
                if (angular.isDefined(image)) {
                    Data.user.avatar = image;
                    isChanged = true;
                }
                $scope.tempData = null;
            });
        };

        $scope.preview = function() {
            Data.tempUser = Data.user;
            $scope.go('profile');
        };

        $scope.$on('$ionicView.beforeLeave', function() {
            if (isChanged) {
                isChanged = false;
                API.updateUser(Data.user).then();
            }
        });

    });

    app.controller('UploadCtrl', function($scope) {

        var fileInput = document.getElementById('fileInput');

        fileInput.addEventListener('change', function() {
            var file = fileInput.files[0];
            var imageType = /image.*/;
            if (file.type.match(imageType)) {
                var orientation;
                loadImage.parseMetaData(file, function (data) {
                    if (data.exif) {
                        orientation = data.exif.get('Orientation');
                    }
                });
                loadImage(
                    file,
                    function (image) {
                        var canvas = document.createElement('canvas');
                        if (orientation && orientation > 4) { // image is sideways
                            //noinspection JSSuspiciousNameCombination
                            canvas.width = image.height;
                            //noinspection JSSuspiciousNameCombination
                            canvas.height = image.width;
                        } else {                              // image is fine or upside down
                            canvas.width = image.width;
                            canvas.height = image.height;
                        }
                        var ctx = canvas.getContext('2d');
                        if (orientation && orientation > 2) { // image is rotated
                            ctx.translate(canvas.width * 0.5, canvas.height * 0.5);
                            if (orientation > 6) {
                                ctx.rotate(1.5 * Math.PI);
                            } else if (orientation > 4) {
                                ctx.rotate(0.5 * Math.PI);
                            } else {
                                ctx.rotate(Math.PI);
                            }
                            ctx.translate(image.width * -0.5, image.height * -0.5);
                        }
                        ctx.drawImage(image, 0, 0);
                        $scope.tempData.image = canvas.toDataURL('image/jpeg');
                        $scope.$apply();
                    }
                );
            }
        });

        $scope.uploadImage = function() {
            fileInput.click();
        }

    });

    app.controller('FeedbackCtrl', function($scope, $ionicPopup, API) {
        $scope.feedback = {};
        $scope.frequency = ['Never', 'Sometimes', 'Often', 'Very Often'];
        $scope.yesNo = ['Yes', 'No'];
        $scope.times = ['Never', 'A week', 'A month', 'A few months', 'A year'];

        $scope.selector = function(title, list, field) {
            var value = '';
            if ($scope.feedback[field]) {
                value = $scope.feedback[field];
            }
            $scope.tempData = {
                list: list,
                value: value
            };
            $ionicPopup.confirm({
                scope: $scope,
                title: title,
                template: '<ion-radio ng-model="tempData.value" ng-value="item" ng-repeat="item in tempData.list">{{item}}</ion-radio>' +
                        '<ion-radio ng-model="tempData.value" ng-value="">Prefer not to answer</ion-radio>',
                okText: 'Okay',
                okType: 'button-royal',
                cancelText: 'Cancel',
                cancelType: 'button-stable'
            }).then(function(resp) {
                if (resp) {
                    $scope.feedback[field] = $scope.tempData.value;
                }
            });
        };

        $scope.submit = function() {
            $ionicPopup.alert({
                title: 'Feedback Sent',
                template: 'Your feedback has been sent!',
                okText: 'Okay' ,
                okType: 'button-royal'
            });
            if ($scope.feedback) {
                API.sendFeedback($scope.feedback);
            }
        }
    });

})();
