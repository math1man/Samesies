(function() {

    const PING_INTERVAL = 1000; // ms

    var app = angular.module('samesies.controllers', []);

    app.controller('IndexCtrl', function($scope, $window, $ionicHistory, API, Data) {
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

            }, 'http://localhost:8080/_ah/api');
        };

        $scope.back = function() {
            $ionicHistory.goBack();
        };

    });

    app.controller('MenuCtrl', function($scope, $window, $ionicModal, API, Data, Utils) {

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
            $scope.resetToggle();
            $scope.loginPopup.show();
        };

        $scope.closeLogin = function() {
            $scope.loginPopup.hide();
        };

        $scope.login = function(user) {
            $scope.loginData = null;
            Data.user = user;
            API.getFriends(user.id).then(function(resp) {
                Data.friends = resp.result.items;
            });
            API.getConnections(user.id).then(function(resp) {
                Data.connections = resp.result.items;
                if (Data.connections.length > 0) {
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
                        }
                    })
                }
            });
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
                API.login($scope.loginData).then(function (resp) {
                    $scope.login(resp.result);
                }, function (reason) { // error
                    if (reason.status === 404) {
                        $scope.loginData.error = 'Invalid email';
                    } else if (reason.status === 400) {
                        $scope.loginData.error = 'Invalid password';
                    } else {
                        $scope.loginData.error = 'Server error'
                    }
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
                API.createUser($scope.loginData).then(function(resp){
                    $scope.login(resp.result);
                }, function () { // error
                    $ionicPopup.alert({
                        title: 'Invalid Email',
                        template: 'That email is already being used.',
                        okText: 'Okay',
                        okType: 'button-royal'
                    });
                    $scope.loginData.error = '';
                });
            }
        };

        $scope.logout = function() {
            Data.user = null;
            $scope.loginData = {
                error: false
            };
            $scope.showLogin();
        };

        $scope.recoverPassword = function() {
            $ionicPopup.confirm({
                scope: $scope,
                title: 'Recover Password',
                template: 'Shit sucks...',
                cancelText: 'Cancel',
                cancelType: 'button-stable',
                okText: 'Okay :(',
                okType: 'button-royal'
            });
        };

        $scope.quickLogin = function() {
            $scope.loginData = {
                error: false,
                email: "luke@samesies.com",
                password: "samesies456"
            };
            $scope.doLogin();
        };

        var toggle = false;

        $scope.toggle = function() {
            toggle = !toggle;
        };

        $scope.isToggled = function() {
            return toggle;
        };

        $scope.resetToggle = function() {
            toggle = false;
        };

        $scope.getFriendRequestCount = function() {
            // TODO: make this work properly
            return Data.friends.length;
        };

        $scope.getConnectionCount = function() {
            // TODO: make this work properly
            var count = 0;
            for (var i=0; i<Data.connections.length; i++) {
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
            return count;
        };

        $scope.$on('$destroy', function() {
            $scope.loginPopup.remove();
        });

    });

    app.controller('EpisodeCtrl', function($scope, $state, API, Utils, Data) {

        var episode;
        var user = Data.user;

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
                $scope.episodeData = {
                    state: 'matching',
                    stage: 0
                };
                API.findEpisode(user.id).then(function (resp) {
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
            API.answerEpisode(episode.id, user.id, $scope.episodeData.myAnswer).then(function(resp) {
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

        $scope.dispName = function() {
            if ($scope.episodeData && $scope.episodeData.partner) {
                return Utils.dispName($scope.episodeData.partner);
            } else {
                return '';
            }
        };

        $scope.$on('$ionicView.leave', function() {
            Utils.interruptAll();
            if (episode.isPersistent) {
                if (episode.status === "IN_PROGRESS") {
                    episode.data = $scope.episodeData;
                    Data.episode = episode;
                    // TODO handle this
                }
            } else {
                API.endEpisode(episode.id);
            }
        });

        if (Data.episode) {
            // TODO: load episode here
            episode = Data.episode;
        } else {
            $scope.find();
        }

    });

    app.controller('CommunitiesCtrl', function($scope, API) {
        $scope.community = {
            location: '',
            users: []
        };

        API.getCommunity("Saint Paul, MN").then(function(resp) {
            $scope.community = resp.result;
            console.log($scope.community);
        });
    });

    app.controller('FriendsCtrl', function($scope, $state, Data, Utils) {
        $scope.search = '';
        $scope.friends = Data.friends;

        $scope.dispName = function(friend) {
            return Utils.dispName(friend);
        };

        $scope.profile = function(friend) {
            Data.tempUser = friend;
            $state.go('profile');
        };

    });

    app.controller('ConnectionsCtrl', function($scope, $state, Data, API, Utils) {
        $scope.connections = Data.connections;

        $scope.accept = function(cxn) {
            cxn.status = "IN_PROGRESS";
            API.acceptEpisode(cxn.id);
            $scope.play(cxn);
        };

        $scope.reject = function(cxn) {
            cxn.status = "UNMATCHED";
            API.endEpisode(cxn.id);
        };

        $scope.play = function(cxn) {
            Data.episode = cxn;
            $state.go('play');
        };

        $scope.dispName = function(cxn) {
            return Utils.dispName(cxn.data.partner);
        }
    });

    app.controller('QuestionsCtrl', function($scope, $ionicPopover, $ionicScrollDelegate, Data) {
        $scope.questions = Data.questions;
        $scope.categories = Data.categories;
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

        $scope.closePopup = function() {
            $scope.categoryPopup.hide();
            $ionicScrollDelegate.scrollTop(true);
        };

        $scope.$on('$destroy', function() {
            $scope.categoryPopup.remove();
        })

    });

    app.controller('EditProfileCtrl', function($scope, $state, $ionicPopup, Data, API) {
        $scope.isChanged = false;
        $scope.user = Data.user;

        $scope.edit = function(property, field, type, required) {
            $scope.data = {
                value: $scope.user[field],
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
                        type: 'button-stable',
                        onTap: function() { return false; }
                    }, {
                        text: 'Okay',
                        type: 'button-royal',
                        onTap: function(e) {
                            if (!required || $scope.data.value) {
                                return $scope.data.value;
                            } else {
                                e.preventDefault();
                            }
                        }
                    }
                ]
            }).then(function(update) {
                if (update != false) {
                    $scope.user[field] = update;
                    $scope.isChanged = true;
                }
                $scope.data = null;
            });
        };

        $scope.editPassword = function() {
            $scope.data = {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
                error: function() {
                    if ($scope.data.oldPassword != $scope.user.password) {
                        return "Invalid password!";
                    } else if ($scope.data.newPassword.length <= 5) {
                        return "New password too short!";
                    } else if ($scope.data.newPassword != $scope.data.confirmPassword) {
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
                            if (!$scope.data.error()) {
                                return $scope.data.newPassword;
                            } else {
                                e.preventDefault();
                            }
                        }
                    }
                ]
            }).then(function(newPassword) {
                if (newPassword) {
                    $scope.user.password = newPassword;
                    $scope.isChanged = true;
                }
                $scope.data = null;
            });
        };

        $scope.editQuestion = function(num) {
            $scope.data = {
                value: $scope.user.questions[num],
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
                        onTap: function() { return $scope.data.value; }
                    }
                ]
            }).then(function(update) {
                if (update != false) {
                    $scope.user.questions[num] = update;
                    $scope.isChanged = true;
                }
                $scope.data = null;
            });
        };

        $scope.preview = function() {
            Data.tempUser = $scope.user;
            $state.go('profile');
        };

        $scope.$on('$ionicView.leave', function() {
            if ($scope.isChanged) {
                $scope.isChanged = false;
                Data.user = $scope.user;
                API.updateUser($scope.user);
            }
        });

    });

    app.controller('ChatCtrl', function($scope, $ionicScrollDelegate, API, Data, Utils) {
        var user = Data.user;
        var recipient = Data.tempUser;
        var chat;

        $scope.buffer = '';
        $scope.history = [];

        API.startChat(user.id, recipient.id).then(function(resp) {
            chat = resp.result;
            API.getMessages(chat.id, chat.startDate).then(function (resp) {
                $scope.history = resp.result.items;
                $scope.$apply();
                $ionicScrollDelegate.scrollBottom();
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
                    senderId: user.id,
                    random: random
                });
                $ionicScrollDelegate.scrollBottom(true);
                API.sendMessage(chat.id, user.id, $scope.buffer, random).then(function (resp) {
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
            for (var i=$scope.history.length-1; i>=0 && index === -1; i--) {
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
                if (messages.length > 0) {
                    for (var i=0; i<messages.length; i++) {
                        addMessage(messages[i]);
                    }
                    $scope.$apply();
                    $ionicScrollDelegate.scrollBottom(true);
                    focusInput();
                }
            });
        };

        var focusInput = function() {
            document.getElementById("chatInput").focus();
        };

        $scope.dispName = function() {
            return Utils.dispName(recipient);
        };

        $scope.isMine = function(message) {
            return (message.senderId === user.id);
        };

        $scope.dispDate = function(message) {
            if (message.sentDate) {
                return "Sent on " + new Date(message.sentDate).toLocaleString();
            } else {
                return "Sending...";
            }
        };

        $scope.$on('$ionicView.leave', function() {
            Utils.interruptAll();
        });

    });

    app.controller('ProfileCtrl', function($scope, $state, $ionicPopup, API, Data, Utils) {
        $scope.tempUser = Data.tempUser;

        $scope.dispName = function() {
            return Utils.dispName($scope.tempUser);
        };

        $scope.hasQuestions = function() {
            return Utils.hasQuestions($scope.tempUser);
        };

        $scope.isMe = function() {
            return Data.user.id === $scope.tempUser.id;
        };

        $scope.message = function() {
            $state.go('chat');
        };

        $scope.connect = function() {
            API.connectEpisode(Data.user.id, $scope.tempUser.id).then(function(resp) {
                var episode = resp.result;
                episode.data = Utils.getData(episode);
                API.getUser(Utils.getPartnerId(episode)).then(function(resp) {
                    episode.data.partner = resp.result;
                });
                Data.connections.push(episode);
            });
            $ionicPopup.alert({
                title: 'Connection Sent',
                template: 'You have sent a connection to ' + $scope.dispName() + '.',
                okText: 'Okay',
                okType: 'button-royal'
            });
        }
    });

})();