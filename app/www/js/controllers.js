(function() {

    const URL = 'https://samesies-app.appspot.com/_ah/api';
    // https://samesies-app.appspot.com/_ah/api
    // http://localhost:8080/_ah/api
    const DEBUG_USER = {
        error: false,
        email: "ari@samesies.com",
        password: "samesies123"
    };
    const PING_INTERVAL = 1000; // ms

    var app = angular.module('samesies.controllers', []);

    app.controller('IndexCtrl', function($scope, $state, $window, $ionicHistory, $ionicPopup, API, Data, Utils){

        $window.init = function() {
            $ionicHistory.nextViewOptions({
                disableBack: true
            });
            $state.go('menu');
            gapi.client.load('samesies', 'v1', function() {
                // initialize API variable
                API.init(gapi.client.samesies.samesiesApi);

                API.getAllQuestions().then(function(resp) {
                    Data.questions = resp.result.items;
                });

                API.getCategories().then(function(resp) {
                    Data.categories = resp.result.items;
                });
                // TODO: load game modes here

            }, URL);
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

    });

    app.controller('LoginCtrl', function($scope, API, Data, Utils) {

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
            $scope.isLoading = false;
            Data.user = user;
            API.getFriends(user.id).then(function(resp) {
                var friends = resp.result.items;
                if (friends) {
                    Data.friends = friends;
                }
                // nested so that it can pull from friends
                API.getConnections(user.id).then(function(resp) {
                    Data.connections = resp.result.items;
                    if (Data.connections && Data.connections.length) {
                        var uids = [];
                        for (var i = 0; i < Data.connections.length; i++) {
                            var cxn = Data.connections[i];
                            Data.connections[i].data = Utils.getData(cxn);
                            uids.push(Utils.getPartnerId(cxn));
                        }
                        API.getUsers(uids).then(function (resp) {
                            var users = resp.result.items;
                            for (var i = 0; i < users.length; i++) {
                                Data.connections[i].data.partner = users[i];
                                // getUsers data is stranger data, so check if they are in friends
                                var isFriend = false;
                                for (var j=0; j<Data.friends.length && !isFriend; j++) {
                                    if (Data.friends[j].user.id === uids[i]) {
                                        isFriend = true;
                                        Data.connections[i].data.partner = Data.friends[j].user;
                                    }
                                }
                            }
                        })
                    }
                });
            });
            // TODO: load communities
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
                    if (reason.status === 404) {
                        $scope.loginData.error = 'Invalid email';
                    } else if (reason.status === 400) {
                        $scope.loginData.error = 'Invalid password';
                    } else {
                        $scope.loginData.error = 'Server error'
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
            } else if (!$scope.loginData.location) {
                $scope.loginData.error = "Invalid location";
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
                        title: 'Invalid Email',
                        template: 'That email is already being used.',
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

        $scope.quickLogin = function() {
            $scope.loginData = DEBUG_USER;
            $scope.doLogin();
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

    app.controller('MenuCtrl', function($scope, $ionicModal, $ionicPopup, $ionicPopover, API, Data) {

        $ionicModal.fromTemplateUrl('templates/login.html', {
            scope: $scope,
            animation: 'slide-in-up',
            backdropClickToClose: false,
            hardwareBackButtonClose: false
        }).then(function(modal) {
            $scope.loginPopup = modal;
            $scope.logout();
        });

        $scope.showLogin = function() {
            $scope.loginPopup.show();
        };

        $scope.closeLogin = function() {
            $scope.loginPopup.hide();
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
            $scope.settingsPopup.hide();
        };

        $scope.logout = function() {
            Data.user = null;
            $scope.showLogin();
        };

        $scope.getFriendRequestCount = function() {
            var count = 0;
            if (Data.friends && Data.friends.length > 0) {
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
            if (Data.connections) {
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
            $scope.settingsPopup.remove();
        });

    });

    app.controller('EpisodeCtrl', function($scope, $state, $ionicPopup, $ionicModal, API, Utils, Data) {

        $ionicModal.fromTemplateUrl('templates/help-episode.html', {
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

        var go = function(state) {
            Utils.interruptAll();
            $scope.episodeData.state = state;
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
            if (!episode || !episode.isPersistent) {
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
                API.getQuestions(ep.qids).then(function (resp) {
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
                    template: 'Your partner has left the episode.',
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

        $scope.$on('$ionicView.leave', function() {
            Utils.interruptAll();
            if (episode.isPersistent) {
                if (episode.status === "IN_PROGRESS") {
                    episode.data = $scope.episodeData;
                    Data.connections.push(episode);
                }
            } else {
                API.endEpisode(episode.id);
            }
        });

        $scope.$on('destroy', function() {
            $scope.helpPopup.remove();
        });

        if (Data.episode) {
            episode = Data.episode;
            loadEpisode(episode);
        } else {
            $scope.find();
        }

    });

    app.controller('CommunitiesCtrl', function($scope, $ionicPopover, API, Data) {
        $scope.selected = Data.communities[0];

        $scope.loadCommunity = function(community) {
            $scope.community = {
                location: community,
                users: []
            };
            if ($scope.selectPopup) {
                $scope.closeSelect();
            }
            API.getCommunity(community).then(function(resp) {
                $scope.community = resp.result;
                $scope.$apply();
            });
        };

        $ionicPopover.fromTemplateUrl('templates/help-communities.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.helpPopup = popover;
        });

        $ionicPopover.fromTemplateUrl('templates/select-community.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.selectPopup = popover;
        });

        $scope.showHelp = function($event) {
            $scope.helpPopup.show($event);
        };

        $scope.closeHelp = function() {
            $scope.helpPopup.hide();
        };

        $scope.showSelect = function($event) {
            $scope.selectPopup.show($event);
        };

        $scope.closeSelect = function() {
            $scope.selectPopup.hide();
        };

        $scope.$on('$destroy', function() {
            $scope.helpPopup.remove();
            $scope.selectPopup.remove();
        });

        $scope.loadCommunity($scope.selected);

    });

    app.controller('FriendsCtrl', function($scope, $state, $ionicPopover, Data, API) {

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
                var done = false;
                for (var i=0; i<Data.friends.length && !done; i++) {
                    if (Data.friends[i].id === friend.id) {
                        Data.friends[i] = friend;
                        done = true;
                    }
                }
                $scope.profile(friend.user);
            });
        };

        $scope.reject = function(friend) {
            API.removeFriend(friend.id, Data.user.id);
            var done = false;
            for (var i=0; i<Data.friends.length && !done; i++) {
                if (Data.friends[i].id === friend.id) {
                    Data.friends.splice(i, 1);
                    done = true;
                }
            }
        };

        $scope.$on('$ionicView.beforeEnter', function() {
            API.getFriends(Data.user.id).then(function(resp) {
                Data.friends = resp.result.items;
            });
        });

        $scope.$on('$destroy', function() {
            $scope.findPopup.remove();
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
            for (var i=0; i<Data.connections.length; i++) {
                if (Data.connections[i].id === cxn.id) {
                    Data.connections.splice(i, 1);
                    return;
                }
            }
        };

        $scope.$on('$ionicView.beforeEnter', function() {
            API.getConnections(Data.user.id).then(function(resp) {
                var connections = resp.result.items;
                if (connections && connections.length > 0) {
                    var uids = [];
                    for (var i = 0; i < connections.length; i++) {
                        var cxn = connections[i];
                        connections[i].data = Utils.getData(cxn);
                        uids.push(Utils.getPartnerId(cxn));
                    }
                    API.getUsers(uids).then(function (resp) {
                        var users = resp.result.items;
                        for (var i = 0; i < users.length; i++) {
                            connections[i].data.partner = users[i];
                            var isFriend = false;
                            for (var j=0; j<Data.friends.length && !isFriend; j++) {
                                if (Data.friends[j].user.id === uids[i]) {
                                    isFriend = true;
                                    connections[i].data.partner = Data.friends[j].user;
                                }
                            }
                        }
                        Data.connections = connections;
                    });
                }
            });
        });
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
                                title: 'Invalid Password',
                                template: 'The password you entered was invalid. Password has not been changed.',
                                okText: 'Okay',
                                okType: 'button-royal'
                            });
                        }
                    });
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
                        type: 'button-stable',
                        onTap: function() { return false; }
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() { return $scope.tempData.value; }
                    }
                ]
            }).then(function(update) {
                if (update != false) {
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
                        type: 'button-stable',
                        onTap: function() { return null; }
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function() {
                            return $scope.tempData.image;
                        }
                    }
                ]
            }).then(function(image) {
                if (image) {
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
            for (var i = 0; i < Data.friends.length; i++) {
                if (Data.friends[i].user.id === Data.tempUser.id) {
                    return false;
                }
            }
            return true;
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

    app.controller('ProfileCtrl', function($scope, $state, API, Data) {

        $scope.isMe = function() {
            return Data.user.id === Data.tempUser.id;
        };

        $scope.message = function() {
            $state.go('chat');
        };
    });

    app.controller('FindFriendCtrl', function($scope, API, Data) {
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
                    var isFriend = false;
                    for (var i=0; i<Data.friends.length && !isFriend; i++) {
                        if (Data.friends[i].id === friend.id) {
                            Data.friends[i] = friend;
                            isFriend = true;
                        }
                    }
                    if (!isFriend) {
                        Data.friends.push(friend);
                    }
                    $scope.$apply();
                });
            }
            $scope.closeFind();
        };

        $scope.$on('popover.hidden', function() {
            $scope.tempUser = null;
            $scope.email = [''];
        });

    });

    app.controller('UploadCtrl', function($scope) {

        var fileInput = document.getElementById('fileInput');

        fileInput.addEventListener('change', function() {
            var file = fileInput.files[0];
            var imageType = /image.*/;

            if (file.type.match(imageType)) {
                var reader = new FileReader();

                reader.onload = function () {
                    $scope.tempData.image = reader.result;
                    $scope.$apply();
                };

                reader.readAsDataURL(file);
            } else {
                console.log("Bad file type");
            }
        });

        $scope.uploadImage = function() {
            fileInput.click();
        }

    });

    app.controller('FeedbackCtrl', function($scope, API) {
        $scope.feedback = {};

        $scope.submit = function() {
            if ($scope.feedback) {
                API.sendFeedback($scope.feedback);
            }
        }
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

    app.controller('SettingsCtrl', function($scope, $ionicPopup, Data) {

        $scope.settings = Data.settings;

        $scope.saveSettings = function() {
            if (!$scope.settings.matchMale && !$scope.settings.matchFemale && !$scope.settings.matchOther) {
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
        }

    });

})();
