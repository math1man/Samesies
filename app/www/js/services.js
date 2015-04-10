(function() {

    var app = angular.module('samesies.services', []);

    app.factory('API', function() {

        var api = null;

        return {
            init: function(apiInit) {
                api = apiInit;
            },
            login: function(user) {
                return api.login(user);
            },
            createUser: function(user) {
                return api.createUser(user);
            },
            recoverUser: function(email) {
                return api.recoverUser({email: email});
            },
            getUser: function(id) {
                return api.getUser({id: id});
            },
            updateUser: function(user) {
                return api.updateUser(user);
            },
            searchUsers: function(string) {
                return api.searchUsers({string: string});
            },
            flagUser: function(flaggedId, flaggerId, reason) {
                api.flagUser({flaggedId: flaggedId, flaggerId: flaggerId, reason: reason}).then();
            },
            getFriends: function(id) {
                return api.getFriends({id: id});
            },
            checkFriend: function(myId, theirId) {
                return api.checkFriend({myId: myId, theirId: theirId});
            },
            addFriend: function(myId, theirId) {
                return api.addFriend({myId: myId, theirId: theirId});
            },
            removeFriend: function(id, myId) {
                api.removeFriend({id: id, myId: myId}).then();
            },
            getCommunity: function(id) {
                return api.getCommunity({id: id});
            },
            getUserCommunities: function(id) {
                return api.getUserCommunities({id: id});
            },
            searchCommunities: function(string) {
                return api.searchCommunities({string: string});
            },
            joinCommunity: function(id, myId, string) {
                return api.joinCommunity({id: id, myId: myId, string: string});
            },
            leaveCommunity: function(id, myId) {
                api.leaveCommunity({id: id, myId: myId}).then();
            },
            getQuestion: function(id) {
                return api.getQuestion({id: id});
            },
            getQuestions: function() {
                return api.getQuestions();
            },
            getCategories: function() {
                return api.getCategories();
            },
            suggestQuestion: function(question) {
                api.suggestQuestion({question: question}).then();
            },
            getModes: function() {
                return api.getModes();
            },
            findEpisode: function(myId, settings, params) {
                return api.findEpisode({
                    myId: myId,
                    isPersistent: !settings.isNotPersistent,
                    mode: settings.mode.mode,
                    matchMale: settings.matchMale,
                    matchFemale: settings.matchFemale,
                    matchOther: settings.matchOther,
                    cid: params.cid,
                    latitude: params.latitude,
                    longitude: params.longitude
                });
            },
            connectEpisode: function(myId, theirId, settings) {
                return api.connectEpisode({
                    myId: myId,
                    theirId: theirId,
                    mode: settings.mode.mode
                });
            },
            acceptEpisode: function(id) {
                api.acceptEpisode({id: id}).then();
            },
            getEpisode: function(id) {
                return api.getEpisode({id: id});
            },
            answerEpisode: function(id, myId, answer) {
                return api.answerEpisode({id: id, myId: myId, answer: answer});
            },
            endEpisode: function(id) {
                api.endEpisode({id: id}).then();
            },
            getConnections: function(id) {
                return api.getConnections({id: id});
            },
            getEpisodeQuestions: function(eid) {
                return api.getEpisodeQuestions({eid: eid});
            },
            startChat: function(eofid, isEpisode, myId, theirId) {
                return api.startChat({eofid: eofid, isEpisode: isEpisode, myId: myId, theirId: theirId});
            },
            getChat: function(id) {
                return api.getChat({id: id});
            },
            getChats: function(myId) {
                return api.getChats({myId: myId});
            },
            updateChat: function(id, eofid, isEpisode) {
                api.updateChat({id: id, eofid: eofid, isEpisode: isEpisode}).then();
            },
            closeChat: function(id) {
                api.closeChat({id: id}).then();
            },
            sendMessage: function(chatId, myId, message, random) {
                return api.sendMessage({chatId: chatId, myId: myId, message: message, random: random});
            },
            getMessages: function(chatId, after, myId) {
                return api.getMessages({chatId: chatId, after: after, myId: myId});
            },
            sendFeedback: function(feedback) {
                api.sendFeedback(feedback).then();
            },
            registerPush: function(id, type, deviceToken) {
                api.registerPush({id: id, type: type, deviceToken: deviceToken}).then();
            }
        };
    });

    app.factory('Utils', function($interval, $parse, Data) {
        var promises = [];

        return {
            getPartnerId: function(episode) {
                if (episode.uid1 === Data.user.id) {
                    return episode.uid2;
                } else {
                    return episode.uid1;
                }
            },
            hasAllQuestions: function(user) {
                var hasAllQuestions = user.questions && user.questions.length;
                for (var i=0; i<5 && hasAllQuestions; i++) {
                    hasAllQuestions = user.questions[i];
                }
                return hasAllQuestions;
            },
            getData: function(episode) {
                if (episode.data) {
                    return episode.data;
                } else {
                    var is1 = (episode.uid1 === Data.user.id);
                    var myAnswers, theirAnswers;
                    if (is1) {
                        myAnswers = episode.answers1;
                        theirAnswers = episode.answers2;
                    } else {
                        theirAnswers = episode.answers1;
                        myAnswers = episode.answers2;
                    }
                    var state, stage, myAnswer, theirAnswer;
                    if (!myAnswers) {
                        stage = 0;
                    } else {
                        stage = myAnswers.length;
                    }
                    if (stage === 0) {
                        state = 'entry';
                        stage = 1;
                        myAnswer = '';
                    } else {
                        myAnswer = myAnswers[stage - 1];
                        if (!theirAnswers || theirAnswers.length < stage) {
                            state = 'waiting';
                            theirAnswer = "Waiting for your partner to answer...";
                        } else {
                            state = 'continue';
                            theirAnswer = theirAnswers[stage - 1];
                        }
                    }
                    return {
                        state: state,
                        stage: stage,
                        is1: is1,
                        myAnswer: myAnswer,
                        theirAnswer: theirAnswer
                    }
                }
            },
            isUpdated: function(chat) {
                if (chat) {
                    if (chat.uid1 === Data.user.id) {
                        return !chat.isUpToDate1;
                    } else {
                        return !chat.isUpToDate2;
                    }
                }
            },
            addById: function(list, item, expr) {
                var index = this.indexOfById(list, item, expr);
                if (index === -1) {
                    list.push(item);
                } else {
                    list[index] = item;
                }
            },
            removeById: function(list, item, expr) {
                var index = this.indexOfById(list, item, expr);
                if (index > -1) {
                    list.splice(index, 1);
                }
            },
            containsById: function(list, item, expr) {
                return this.indexOfById(list, item, expr) > -1;
            },
            indexOfById: function(list, item, expr) {
                if (list && list.length) {
                    for (var i = 0; i < list.length; i++) {
                        var element;
                        if (expr) {
                            element = $parse(expr)(list[i]);
                        } else {
                            element = list[i];
                        }
                        if (element.id === item.id) {
                            return i;
                        }
                    }
                }
                return -1;
            },
            interval: function(fn, delay) {
                var temp = $interval(fn, delay);
                promises.push(temp);
                return temp;
            },
            interruptAll: function() {
                while (promises.length > 0) {
                    $interval.cancel(promises.pop());
                }
            }
        }
    });

    app.service('Data', function() {
        // global static data
        this.questions = [];
        this.categories = [];
        this.modes = [];
        this.defaultMode = null;
        this.communities = [];
        // global dynamic data
        this.community = {
            name: 'Click to select a Community',
            users: []
        };
        this.settings = {
            mode: 'Random',
            isNotPersistent: false,
            matchMale: true,
            matchFemale: true,
            matchOther: true
        };
        // user data
        this.user = null;
        this.connections = [];
        this.chats = [];
        this.friends = [];
        // control data
        this.tempUser = null;
        this.episode = null;
        this.friend = null;
        this.chat = null;
        this.isLoading = 0;
    });

    app.service('Loading', function() {
        this.community = false;
        this.communities = false;
        this.friends = false;
        this.chats = false;
        this.connections = false;
    });

    app.factory('Refresh', function(API, Data, Utils, Loading) {
        var all = function($scope) {
            communities($scope);
            friends($scope, function() {
                chats($scope);
                connections($scope);
            });
        };
        var communities = function($scope, fn) {
            if (Data.user) {
                API.getUserCommunities(Data.user.id).then(function(resp) {
                    var communities = resp.result.items;
                    if (communities && communities.length) {
                        Data.communities = communities;
                    }
                    Loading.communities = false;
                    if ($scope) {
                        $scope.$apply();
                    }
                    if (angular.isFunction(fn)) {
                        fn();
                    }
                });
            }
        };
        var friends = function($scope, fn) {
            API.getFriends(Data.user.id).then(function (resp) {
                var friends = resp.result.items;
                if (friends && friends.length) {
                    Data.friends = friends;
                }
                Loading.friends = false;
                if ($scope) {
                    $scope.$apply();
                }
                if (angular.isFunction(fn)) {
                    fn();
                }
            });
        };
        var chats = function($scope, fn) {
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
                    Loading.chats = false;
                    if ($scope) {
                        $scope.$apply();
                    }
                    if (angular.isFunction(fn)) {
                        fn();
                    }
                });
            }
        };
        var connections = function($scope, fn) {
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
                    Loading.connections = false;
                    if ($scope) {
                        $scope.$apply();
                    }
                    if (angular.isFunction(fn)) {
                        fn();
                    }
                });
            }
        };
        return {
            all: all,
            communities: communities,
            friends: friends,
            chats: chats,
            connections: connections
        }
    })

})();
