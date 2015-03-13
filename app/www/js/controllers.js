(function() {

    const URL = 'https://samesies-app.appspot.com/_ah/api';
    const PING_INTERVAL = 1000; // ms

    var app = angular.module('samesies.controllers', []);

    app.controller('IndexCtrl', function($scope, $state, $window, $ionicHistory, $ionicPopup, $ionicModal, API, Data, Utils){

        $window.init = function() {
            gapi.client.load('samesies', 'v1', function() {
                // initialize API variable
                API.init(gapi.client.samesies.samesiesApi);

                API.getAllQuestions().then(function(resp) {
                    Data.questions = resp.result.items;
                });

                API.getCategories().then(function(resp) {
                    Data.categories = resp.result.items;
                });

                API.getModes().then(function(resp) {
                    Data.modes = resp.result.items;
                    if (Data.modes && Data.modes.length) {
                        for (var i = 0; i < Data.modes.length; i++) {
                            if (Data.modes[i].mode === 'Random') {
                                Data.defaultMode = Data.modes[i];
                            }
                        }
                        Data.settings.mode = Data.defaultMode;
                    }
                });

            }, URL);
        };

        $ionicModal.fromTemplateUrl('templates/game-settings.html', {
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
                episode.data.partner = user;
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

        $scope.refresh = function() {
            if (Data.user) {
                API.getFriends(Data.user.id).then(function(resp) {
                    var friends = resp.result.items;
                    if (friends && friends.length) {
                        Data.friends = friends;
                        $scope.$apply();
                    }
                    // nested so that it can pull from friends
                    API.getConnections(Data.user.id).then(function(resp) {
                        var connections = resp.result.items;
                        if (connections && connections.length) {
                            var uids = [];
                            for (var i = 0; i < connections.length; i++) {
                                var cxn = connections[i];
                                connections[i].data = Utils.getData(cxn);
                                uids.push(Utils.getPartnerId(cxn));
                            }
                            API.getUsers(uids).then(function (resp) {
                                var users = resp.result.items;
                                for (var i = 0; i < users.length; i++) {
                                    var cxnIndex = Utils.indexOfById(Data.connections, connections[i]);
                                    if (cxnIndex === -1) {
                                        cxnIndex = Data.connections.length;
                                        connections[i].data.partner = users[i];
                                        Data.connections.push(connections[i]);
                                    }
                                    // getUsers data is stranger data, so check if they are in friends
                                    var index = Utils.indexOfById(Data.friends, users[i], 'user');
                                    if (index > -1) {
                                        Data.connections[cxnIndex].data.partner = Data.friends[index].user;
                                    }
                                }
                                $scope.$apply();
                            });
                        }
                    });
                });
            }
        };

        $scope.$on('$destroy', function() {
            $scope.settingsPopup.remove();
        });

    });

    app.controller('LoginCtrl', function($scope, $ionicPopup, API, Data) {

        $scope.$on('modal.shown', function() {
            $scope.loginData = {
                error: false,
                location: 'Macalester College',
                avatar: 'img/lone_icon.png'
            };
            $scope.isLoading = false;
        });

        $scope.login = function(user) {
            $scope.loginData = null;
            $scope.loginKey = [''];
            Data.user = user;
            $scope.refresh();
            API.getCommunity(user.location).then(function(resp) {
                Data.community = resp.result;
                $scope.$apply();
            });
            $scope.resetToggle();
            $scope.closeLogin();
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

        $scope.loginKey = [''];

        $scope.createAccount = function() {
            if (!$scope.loginData.email) {
                $scope.loginData.error = "Invalid email";
            } else if (!$scope.loginData.password || $scope.loginData.password.length <= 5) {
                $scope.loginData.error = "Password too short";
            } else if ($scope.loginData.password != $scope.loginData.confirmPassword) {
                $scope.loginData.error = "Passwords don't match";
            } else if (!$scope.loginData.location) {
                $scope.loginData.error = "Invalid location";
            } else if (!$scope.loginKey[0] || $scope.loginKey[0].toLowerCase() != 'macalester') {
                $scope.loginData.error = "Invalid login key";
            } else {
                $scope.loginData.error = "";
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

        $scope.recoverPassword = function() {
            $ionicPopup.alert({
                scope: $scope,
                title: 'Recover Password',
                template: 'Sorry, this feature has not been implemented yet.',
                okText: 'Okay',
                okType: 'button-royal'
            });
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

    app.controller('MenuCtrl', function ($scope, $ionicModal, $ionicPopup, $ionicPopover, API, Data) {

        $scope.$on('$ionicView.beforeEnter', function() {
            $scope.refresh();
        });

        $ionicModal.fromTemplateUrl('templates/login.html', {
            scope: $scope,
            animation: 'slide-in-up',
            backdropClickToClose: false,
            hardwareBackButtonClose: false
        }).then(function(modal) {
            $scope.loginPopup = modal;
            $scope.logout();
        });

        $scope.closeLogin = function() {
            $scope.loginPopup.hide();
        };

        $scope.logout = function() {
            Data.user = null;
            $scope.loginPopup.show();
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

        $scope.getConnectionCount = function() {
            var count = 0;
            if (Data.connections && Data.connections.length) {
                for (var i = 0; i < Data.connections.length; i++) {
                    var item = Data.connections[i];
                    if (item.data) { // Connections pending your approval
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

        $scope.$on('$destroy', function() {
            $scope.loginPopup.remove();
        });

    });

    app.controller('SettingsCtrl', function($scope, $ionicPopup, Data, Utils) {

        var isShowModes = false;

        $scope.isShowModes = function() {
            return isShowModes;
        };

        $scope.$on('modal.shown', function() {
            $scope.settings = Data.settings;
        });

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

    app.controller('EpisodeCtrl', function($scope, $state, $window, $ionicPopup, $ionicModal, API, Utils, Data) {

        $ionicModal.fromTemplateUrl('templates/help.html', {
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

        var episode;

        $scope.isPersistent = function() {
           return episode && episode.isPersistent;
        };

        var go = function(state) {
            Utils.interruptAll();
            $scope.episodeData.state = state;
            $scope.$apply();
            if (state === 'waiting') {
                Utils.interval(function () {
                    API.getEpisode(episode.id).then(function (resp) {
                        if ($scope.is('waiting')) {
                            getResponse(resp.result);
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
                if (episode) {
                    API.endEpisode(episode.id);
                    episode = null;
                }
                $scope.episodeData = {
                    state: 'matching',
                    stage: 0,
                    partner: null
                };
                API.findEpisode(Data.user.id, Data.settings).then(function (resp) {
                    episode = resp.result;
                    if (episode.status === "MATCHING") {
                        Utils.interval(function () {
                            API.getEpisode(episode.id).then(function (resp) {
                                // second condition prevents multiple calls
                                episode = resp.result;
                                if (episode.status === "IN_PROGRESS" && $scope.is('matching')) {
                                    loadEpisode(episode);
                                }
                            });
                        }, PING_INTERVAL);
                    } else {
                        loadEpisode(episode);
                    }
                });
            }
        };

        var loadEpisode = function(ep) {
            $scope.episodeData = Utils.getData(ep);
            if (!$scope.episodeData.partner) {
                API.getUser(Utils.getPartnerId(ep)).then(function (resp) {
                    $scope.episodeData.partner = resp.result;
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
                API.endEpisode(episode.id);
                episode.status = "COMPLETE";
                API.getUser(Utils.getPartnerId(episode)).then(function(resp) {
                    Data.tempUser = resp.result;
                    $state.go('chat');
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
            $scope.episodeData.theirAnswer = "Waiting for your partner to answer...";
            go('waiting');
            API.answerEpisode(episode.id, Data.user.id, $scope.episodeData.myAnswer).then(function(resp) {
                getResponse(resp.result);
            }, function(reason) {
                console.log(reason);
            });
        };

        var getResponse = function(ep) {
            episode = ep;
            if (episode.status === "ABANDONED") {
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
                    answers = episode.answers2;
                } else {
                    answers = episode.answers1;
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
            if (episode) {
                if (episode.isPersistent) {
                    if (episode.status === "IN_PROGRESS") {
                        episode.data = $scope.episodeData;
                        Data.connections.push(episode);
                    }
                } else {
                    API.endEpisode(episode.id);
                }
            }
        };

        $scope.$on('$ionicView.leave', function() {
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
            episode = Data.episode;
            loadEpisode(episode);
        } else {
            $scope.find();
        }

    });

    app.controller('CommunitiesCtrl', function($scope, $ionicPopover, $ionicPopup, API, Data) {

        $scope.selected = Data.community.location;

        $scope.loadCommunity = function(community) {
            if ($scope.selectPopup) {
                $scope.selectPopup.hide();
            }
            API.getCommunity(community).then(function(resp) {
                Data.community = resp.result;
                $scope.$apply();
            });
        };

        $ionicPopover.fromTemplateUrl('templates/select-community.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.selectPopup = popover;
        });

        $scope.showSelect = function($event) {
            $scope.selectPopup.show($event);
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
            })
        };

        $scope.$on('$destroy', function() {
            $scope.selectPopup.remove();
        });

    });

    app.controller('FriendsCtrl', function($scope, $state, $ionicPopover, Data, API, Utils) {

        $ionicPopover.fromTemplateUrl('templates/find-friends.html', {
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

        $scope.search = '';

        $scope.profile = function(user) {
            Data.tempUser = user;
            $state.go('profile');
        };

        $scope.accept = function(friend) {
            API.addFriend(Data.user.id, friend.user.id).then(function(resp) {
                var friend = resp.result;
                var index = Utils.indexOfById(Data.friends, friend, 'user');
                if (index > -1) {
                    Data.friends[index] = friend;
                } else {
                    Data.friends.push(friend);
                }
                $scope.profile(friend.user);
            });
        };

        $scope.reject = function(friend) {
            API.removeFriend(friend.id, Data.user.id);
            var index = Utils.indexOfById(Data.friends, friend, 'user');
            if (index > -1) {
                Data.friends.splice(index, 1);
            }
        };

        $scope.$on('$destroy', function() {
            $scope.findPopup.remove();
        });

    });

    app.controller('FindFriendCtrl', function($scope, API, Data, Utils) {
        $scope.tempUser = null;
        $scope.email = [''];

        $scope.findFriend = function() {
            API.findUser($scope.email[0]).then(function(resp) {
                $scope.tempUser = resp.result;
                $scope.$apply();
            });
        };

        $scope.add = function() {
            if ($scope.tempUser) {
                API.addFriend(Data.user.id, $scope.tempUser.id).then(function(resp) {
                    var friend = resp.result;
                    if (!Utils.containsById(Data.friends, friend.id)) {
                        Data.friends.push(friend);
                        $scope.$apply();
                    }
                });
            }
            $scope.closeFind();
        };

        $scope.$on('popover.hidden', function() {
            $scope.tempUser = null;
            $scope.email = [''];
        });

    });

    app.controller('ConnectionsCtrl', function($scope, $state, Data, API, Utils) {

        $scope.accept = function(cxn) {
            cxn.status = "IN_PROGRESS";
            API.acceptEpisode(cxn.id);
            $scope.play(cxn);
        };

        $scope.reject = function(cxn) {
            cxn.status = "UNMATCHED";
            API.endEpisode(cxn.id);
            removeCxn(cxn);
        };

        $scope.play = function(cxn) {
            Data.episode = cxn;
            removeCxn(cxn);
            $state.go('play');
        };

        var removeCxn = function(cxn) {
            var index = Utils.indexOfById(Data.connections, cxn);
            if (index > -1) {
                Data.connections.splice(index, 1);
            }
        };
    });

    app.controller('QuestionsCtrl', function($scope, $ionicPopover, $ionicScrollDelegate) {

        $scope.category = ['All'];
        $scope.search = '';

        $ionicPopover.fromTemplateUrl('templates/select-category.html', {
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

        $ionicPopover.fromTemplateUrl('templates/suggest-question.html', {
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

    app.controller('EditProfileCtrl', function($scope, $state, $ionicPopup, Data, API) {

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
                templateUrl: 'templates/edit.html',
                buttons: [
                    {
                        text: 'Cancel',
                        type: 'button-stable'
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function(e) {
                            if (!required || $scope.tempData.value) {
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
                templateUrl: 'templates/edit-password.html',
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
                    API.updateUser(Data.user).then(function(resp){
                        $ionicPopup.alert({
                            title: 'Password Changed',
                            template: 'Your password has successfully been changed.',
                            okText: 'Okay',
                            okType: 'button-royal'
                        });
                    }, function(reason) {
                        if (reason.status === 400) {
                            // TODO: recover password?
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
                templateUrl: 'templates/edit-gender.html',
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
                templateUrl: 'templates/edit.html',
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
                templateUrl: 'templates/upload-avatar.html',
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
            $state.go('profile');
        };

        $scope.$on('$ionicView.leave', function() {
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
                        if (!orientation || orientation < 5) { // image is fine or upside down
                            canvas.width = image.width;
                            canvas.height = image.height;
                        } else {                               // image is sideways
                            canvas.width = image.height;
                            canvas.height = image.width;
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

            } else {
                console.log("Bad file type");
            }
        });

        $scope.uploadImage = function() {
            fileInput.click();
        }

    });

    app.controller('ProfileCtrl', function($scope, $state, API, Data) {

        $scope.isMe = function() {
            return Data.user.id === Data.tempUser.id;
        };

        $scope.message = function() {
            $state.go('chat');
        };
    });

    app.controller('ChatCtrl', function($scope, $ionicScrollDelegate, API, Data, Utils) {
        var chat;
        $scope.buffer = '';
        $scope.history = [];

        API.startChat(Data.user.id, Data.tempUser.id).then(function(resp) {
            chat = resp.result;
            API.getMessages(chat.id, chat.startDate).then(function (resp) {
                if (resp.result.items) {
                    $scope.history = resp.result.items;
                    $scope.$apply();
                    // the scroll delegate is actually the most annoying thing
                    //$ionicScrollDelegate.scrollBottom();
                }
                focusInput();
                Utils.interval(function() {
                    checkChat();
                }, PING_INTERVAL);
            });
        }, function(reason) {
            console.log(reason);
        });

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
                // this one kind of works so it can stay
                $ionicScrollDelegate.scrollBottom(true);
                API.sendMessage(chat.id, Data.user.id, $scope.buffer, random).then(function (resp) {
                    addMessage(resp.result);
                }, function(reason) {
                    console.log(reason);
                });
                $scope.buffer = '';
                focusInput();
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
            if (modified && new Date(modified) > new Date(chat.lastModified)) {
                chat.lastModified = modified;
            }
        };

        var checkChat = function() {
            API.getMessages(chat.id, chat.lastModified).then(function (resp) {
                var messages = resp.result.items;
                if (messages && messages.length > 0) {
                    for (var i=0; i<messages.length; i++) {
                        addMessage(messages[i]);
                    }
                    $scope.$apply();
                    // this one also sucks
                    //$ionicScrollDelegate.scrollBottom(true);
                    focusInput();
                }
            });
        };

        var focusInput = function() {
            document.getElementById("chatInput").focus();
        };

        $scope.isMine = function(message) {
            return (message.senderId === Data.user.id);
        };

        $scope.dispDate = function(message) {
            if (message.sentDate) {
                return "Sent on " + new Date(message.sentDate).toLocaleString();
            } else {
                return "Sending...";
            }
        };

        $scope.showAddFriend = function() {
            return !Utils.containsById(Data.friends, Data.tempUser.id);
        };

        $scope.addFriend = function() {
            API.addFriend(Data.user.id, Data.tempUser.id).then(function(resp) {
                Data.friends.push(resp.result);
                // TODO: some sort of friend indication? for both parties?
            });
        };

        $scope.$on('$ionicView.leave', function() {
            Utils.interruptAll();
        });

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
            if ($scope.feedback) {
                API.sendFeedback($scope.feedback);
            }
        }
    });

})();
