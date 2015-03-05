(function() {

    var app = angular.module('samesies.services', []);

    app.factory('API', function() {

        return {
            api: null,
            init: function(api) {
                this.api = api;
            },
            login: function(user) {
                return this.api.login(user);
            },
            createUser: function(user) {
                return this.api.createUser(user);
            },
            getUser: function(id) {
                return this.api.getUser({id: id});
            },
            getUsers: function(ids) {
                return this.api.getUsers({ids: ids});
            },
            updateUser: function(user) {
                return this.api.updateUser(user);
            },
            getFriends: function(id) {
                return this.api.getFriends({id: id});
            },
            findUser: function(email) {
                return this.api.findUser({email: email});
            },
            addFriend: function(myId, theirId) {
                return this.api.addFriend({myId: myId, theirId: theirId});
            },
            removeFriend: function(id, myId) {
                this.api.removeFriend({id: id, myId: myId}).then();
            },
            getCommunity: function(location) {
                return this.api.getCommunity({location: location});
            },
            getQuestion: function(id) {
                return this.api.getQuestion({id: id});
            },
            getQuestions: function(ids) {
                return this.api.getQuestions({ids: ids});
            },
            getAllQuestions: function() {
                return this.api.getAllQuestions();
            },
            getCategories: function() {
                return this.api.getCategories();
            },
            suggestQuestion: function(question) {
                this.api.suggestQuestion({question: question}).then();
            },
            findEpisode: function(myId, settings) {
                settings.myId = myId;
                return this.api.findEpisode(settings);
            },
            connectEpisode: function(myId, theirId, settings) {
                settings.myId = myId;
                settings.theirId = theirId;
                return this.api.connectEpisode(settings);
            },
            acceptEpisode: function(id) {
                this.api.acceptEpisode({id: id}).then();
            },
            getEpisode: function(id) {
                return this.api.getEpisode({id: id});
            },
            answerEpisode: function(id, myId, answer) {
                return this.api.answerEpisode({id: id, myId: myId, answer: answer});
            },
            endEpisode: function(id) {
                this.api.endEpisode({id: id}).then();
            },
            getConnections: function(id) {
                return this.api.getConnections({id: id});
            },
            startChat: function(myId, theirId) {
                return this.api.startChat({myId: myId, theirId: theirId});
            },
            getChat: function(id) {
                return this.api.getChat({id: id});
            },
            sendMessage: function(chatId, myId, message, random) {
                return this.api.sendMessage({chatId: chatId, myId: myId, message: message, random: random});
            },
            getMessages: function(chatId, after) {
                return this.api.getMessages({chatId: chatId, after: after});
            },
            sendFeedback: function(feedback) {
                this.api.sendFeedback(feedback).then();
            }
        };
    });

    app.factory('Utils', function($interval, Data) {
        var promises = [];

        return {
            getPartnerId: function(episode) {
                if (episode.uid1 === Data.user.id) {
                    return episode.uid2;
                } else {
                    return episode.uid1;
                }
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
        this.modes = ['Random'];
        this.settings = {
            mode: 'Random',
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
