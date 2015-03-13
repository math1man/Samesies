(function() {

    var app = angular.module('samesies.services', []);

    app.factory('API', function() {

        var API = null;

        return {
            init: function(api) {
                API = api;
            },
            login: function(user) {
                return API.login(user);
            },
            createUser: function(user) {
                return API.createUser(user);
            },
            getUser: function(id) {
                return API.getUser({id: id});
            },
            getUsers: function(ids) {
                return API.getUsers({ids: ids});
            },
            updateUser: function(user) {
                return API.updateUser(user);
            },
            getFriends: function(id) {
                return API.getFriends({id: id});
            },
            findUser: function(email) {
                return API.findUser({email: email});
            },
            addFriend: function(myId, theirId) {
                return API.addFriend({myId: myId, theirId: theirId});
            },
            removeFriend: function(id, myId) {
                API.removeFriend({id: id, myId: myId}).then();
            },
            getCommunity: function(location) {
                return API.getCommunity({location: location});
            },
            getQuestion: function(id) {
                return API.getQuestion({id: id});
            },
            getQuestions: function(ids) {
                return API.getQuestions({ids: ids});
            },
            getAllQuestions: function() {
                return API.getAllQuestions();
            },
            getCategories: function() {
                return API.getCategories();
            },
            suggestQuestion: function(question) {
                API.suggestQuestion({question: question}).then();
            },
            getModes: function() {
                return API.getModes();
            },
            findEpisode: function(myId, settings) {
                return API.findEpisode({
                    myId: myId,
                    mode: settings.mode.mode,
                    matchMale: settings.matchMale,
                    matchFemale: settings.matchFemale,
                    matchOther: settings.matchOther
                });
            },
            connectEpisode: function(myId, theirId, settings) {
                return API.connectEpisode({
                    myId: myId,
                    theirId: theirId,
                    mode: settings.mode.mode,
                    matchMale: settings.matchMale,
                    matchFemale: settings.matchFemale,
                    matchOther: settings.matchOther
                });
            },
            acceptEpisode: function(id) {
                API.acceptEpisode({id: id}).then();
            },
            getEpisode: function(id) {
                return API.getEpisode({id: id});
            },
            answerEpisode: function(id, myId, answer) {
                return API.answerEpisode({id: id, myId: myId, answer: answer});
            },
            endEpisode: function(id) {
                API.endEpisode({id: id}).then();
            },
            getConnections: function(id) {
                return API.getConnections({id: id});
            },
            getEpisodeQuestions: function(eid) {
                return API.getEpisodeQuestions({eid: eid});
            },
            startChat: function(myId, theirId) {
                return API.startChat({myId: myId, theirId: theirId});
            },
            getChat: function(id) {
                return API.getChat({id: id});
            },
            sendMessage: function(chatId, myId, message, random) {
                return API.sendMessage({chatId: chatId, myId: myId, message: message, random: random});
            },
            getMessages: function(chatId, after) {
                return API.getMessages({chatId: chatId, after: after});
            },
            sendFeedback: function(feedback) {
                API.sendFeedback(feedback).then();
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
        this.questions = [];
        this.categories = [];
        this.communities = ['Macalester College'];
        this.community = {
            location: this.communities[0],
            users: []
        };
        this.modes = [];
        this.defaultMode = null;
        this.settings = {
            mode: null,
            matchMale: true,
            matchFemale: true,
            matchOther: true
        };
        this.user = null;
        this.friends = [];
        this.connections = [];
        this.episode = null;
        this.tempUser = null;
    });

})();
