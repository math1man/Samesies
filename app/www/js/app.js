(function() {

	const MATCHING = "MATCHING";
	const UNMATCHED = "UNMATCHED";
	const IN_PROGRESS = "IN_PROGRESS";
	const ABANDONED = "ABANDONED";
	const COMPLETE = "COMPLETE";

	const WAITING = "Waiting for your partner to answer...";
	const PING_INTERVAL = 500; // ms

	var app = angular.module('samesies', ['ionic', 'directives', 'filters']);
	
	app.run(function($ionicPlatform) {
		$ionicPlatform.ready(function() {
			// Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
			// for form inputs)
			if(window.cordova && window.cordova.plugins.Keyboard) {
				cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
			}
			if(window.StatusBar) {
				StatusBar.styleDefault();
			}
		});
	});

	app.controller('MainController', function($scope, $window, $ionicPopup, $ionicPopover,
			$ionicModal, $ionicGesture, $timeout, $interval, $ionicScrollDelegate) {

		//----------------------------
		//       Cloud Endpoint
		//----------------------------

		var API;

		$window.init = function() {
			gapi.client.load('samesies', 'v1', function() {
				// initialize API variable
				API = gapi.client.samesies.samesiesApi;
				// initialize question DB
				API.getAllQuestions().then(function(resp) {
					$scope.questions = resp.result.items;
				});
				API.getCategories().then(function(resp) {
					$scope.categories = resp.result.items;
				});
				$scope.selected = {
					cat: "All"
				};
			}, 'http://localhost:8080/_ah/api');
			// http://localhost:8080/_ah/api
			// https://samesies-app.appspot.com/_ah/api
		};

		//----------------------------
		//           Data
		//----------------------------

		var getUser = function(uid) {
			for (var i=0; i<$scope.friends; i++) {
				if ($scope.friends[i].id === uid) {
					return $scope.friends[i];
				}
			}
			return API.getUser({id: uid});
		};

		var getEpisode = function(eid) {
			return API.getEpisode({id: eid});
		};

		var getMessages = function (cid, after) {
			return API.getMessages({chatId: cid, after: after});
		};

		//----------------------------
		//    Modals and Popovers
		//----------------------------

		$ionicModal.fromTemplateUrl('templates/login.html', {
			scope: $scope,
			animation: 'slide-in-up',
			backdropClickToClose: false,
			hardwareBackButtonClose: false
		}).then(function(modal) {
			$scope.loginPopup = modal;
		});
		
		$ionicModal.fromTemplateUrl('templates/menu.html', {
			scope: $scope,
			animation: 'slide-in-right',
			backdropClickToClose: false,
			hardwareBackButtonClose: false
		}).then(function(modal) {
			$scope.menuPopup = modal;
			$scope.logOut(); // open the login on app-start
		});
		
		$ionicModal.fromTemplateUrl('templates/help.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			$scope.helpPopup = modal;
		});
		
		$ionicModal.fromTemplateUrl('templates/profile.html', {
			scope: $scope,
			animation: 'slide-in-left'
		}).then(function(modal) {
			$scope.profilePopup = modal;
		});

		$ionicPopover.fromTemplateUrl('templates/select-category.html', {
			scope: $scope
		}).then(function(popover) {
			$scope.categoryPopup = popover;
		});

		$scope.show = function(item, $event) {
			this.resetToggle();
			this[item + "Popup"].show($event);
		};
		
		$scope.close = function(item) {
			this[item + "Popup"].hide();
		};

		$scope.$on('$destroy', function() {
			// Cleanup the modals when we're done with them!
			$scope.go(); // interrupts things and abandons episode
			$scope.menuPopup.remove();
			$scope.helpPopup.remove();
			$scope.profilePopup.remove();
			$scope.categoryPopup.remove();

		});

		//----------------------------
		//      Login Functions
		//----------------------------

		$scope.login = function(user) {
			$scope.loginData = null;
			$scope.user = user;
			$scope.friends = [];
			$scope.getFriends();
			$scope.connections = [];
			$scope.getCxns();
			// TODO: load communities here
			$scope.loadCommunity(user.location);
			$scope.close('login');
			$scope.go('menu');
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
				}, function () {
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

		$scope.loginShortcut = function() {
			if ($scope.isToggled()) {
				$scope.createAccount();
			} else {
				$scope.doLogin()
			}
		};

		$scope.logOut = function() {
			$scope.close('menu');
			$scope.show('login');
			$scope.user = null;
			$scope.friends = null;
			$scope.resetToggle();
			$scope.loginData = {
				error: false
			};
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
		
		//----------------------------
		//      Status Functions
		//----------------------------

		var state = {
			s: "", 
			t: false
		};

		$scope.go = function(newState) {
			// kill everything that may have been going
			interruptAll();
			if ($scope.episode) {
				if ($scope.episode.isPersistent) {
					if ($scope.episode.status === IN_PROGRESS) {
						$scope.episode.data = $scope.episodeData;
						$scope.connections.push($scope.episode);
					}
				} else {
					$scope.endEpisode($scope.episode);
				}
				$scope.episode = null;
			}
			$scope.chat = null;
			$scope.search = [''];
			// go wherever
			if (newState === 'menu') {
				this.show('menu');
			} else {
				state.s = newState;
				this.close('menu');
				$ionicScrollDelegate.scrollTop();
			}
		};
		
		$scope.is = function(checkState) {
			if ($scope.menuPopup.isShown()) {
				return checkState === "menu";
			} else {
				return state.s === checkState;
			}
		};

		$scope.toggle = function() {
			state.t = !state.t;
		};
		
		$scope.isToggled = function() {
			return state.t;
		};
		
		$scope.resetToggle = function() {
			state.t = false;
		};		
		
		//----------------------------
		//      Episode Control
		//----------------------------

		const matchingData = {
			state: 'matching',
			stage: 0
		};

		var getPartnerId = function(episode) {
			if (episode.uid1 === $scope.user.id) {
				return episode.uid2;
			} else {
				return episode.uid1;
			}
		};

		var epGo = function(state) {
			interruptAll();
			$scope.episodeData.state = state;
		};

		$scope.epIs = function(state) {
			if ($scope.episodeData) {
				return ($scope.episodeData.state === state);
			} else {
				return false;
			}
		};

		$scope.find = function() {
			// episodeData holds local references that do not get directly transmitted
			$scope.episodeData = matchingData;
			$scope.go('episode');
			API.findEpisode({myId: $scope.user.id}).then(function(resp){
				// if it returns matching, I am the first user
				// if it returns otherwise, I am the second user
				if (resp.result.status === MATCHING) {
					$scope.episode = resp.result;
					$scope.episodeData = matchingData;
					interval(function() {
						getEpisode($scope.episode.id).then(function(resp) {
							// second condition prevents multiple calls
							if (resp.result.status === IN_PROGRESS && $scope.epIs('matching')) {
								loadEpisode(resp.result);
							}
						});
					}, PING_INTERVAL);
				} else {
					loadEpisode(resp.result);
				}
			});
		};

		var getEpisodeData = function(episode) {
			if (episode.data) {
				return episode.data;
			} else {
				var is1 = episode.uid1 === $scope.user.id;
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
						theirAnswer = WAITING;
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
		};

		var loadEpisode = function(episode) {
			$scope.episode = episode;
			$scope.episodeData = getEpisodeData(episode);
			if (!$scope.episodeData.partner) {
				getUser(getPartnerId(episode)).then(function (resp) {
					$scope.episodeData.partner = $scope.displayName(resp.result);
				});
			}
			if ($scope.episodeData.questions) {
				epGo($scope.episodeData.state);
				if ($scope.epIs('waiting')) {
					retrieve();
				}
			} else {
				API.getQuestions({ids: episode.qids}).then(function (resp) {
					$scope.episodeData.questions = resp.result.items;
					epGo($scope.episodeData.state);
					if ($scope.epIs('waiting')) {
						retrieve();
					}
				});
			}
		};

		$scope.matchText = function() {
			if ($scope.epIs('matching')) {
				return "Matching...";
			} else {
				return "Matched!"
			}
		};

		$scope.next = function() {
			$scope.episodeData.myAnswer = '';
			if ($scope.episodeData.stage == 10) {
				$scope.endEpisode($scope.episode);
				$scope.episode.status = COMPLETE;
				getUser(getPartnerId($scope.episode)).then(function(resp) {
					$scope.message(resp.result);
				});
			} else {
				$scope.episodeData.stage++;
				epGo('entry');
			}
		};

		$scope.getQuestion = function() {
			if ($scope.episodeData) {
				return $scope.episodeData.questions[$scope.episodeData.stage - 1].q;
			} else {
				return "";
			}
		};

		$scope.answer = function() {
			$scope.episodeData.theirAnswer = WAITING;
			epGo('waiting');
			API.answerEpisode({
				id: $scope.episode.id,
				myId: $scope.user.id,
				answer: $scope.episodeData.myAnswer
			}).then(function(resp) {
				$scope.episode = resp.result;
				if ($scope.episode.status === ABANDONED) {
					abandonMessage();
				} else {
					getResponse(resp.result);
					if ($scope.epIs('waiting')) {
						retrieve();
					}
				}
			}, function(reason) {
				$scope.error = reason;
			});
		};

		var getResponse = function(episode) {
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
				epGo('continue');
			}
		};

		var abandonMessage = function() {
			$ionicPopup.confirm({
				title: 'Partner Left',
				template: 'Your partner has left the episode.',
				okText: 'Play Again!',
				okType: 'button-royal',
				cancelText: 'Main Menu',
				cancelType: 'button-stable'
			}).then(function(resp) {
				if (resp) {
					$scope.find();
				} else {
					$scope.go('menu');
				}
			})
		};

		var retrieve = function() {
			interval(function () {
				getEpisode($scope.episode.id).then(function (resp) {
					if ($scope.epIs('waiting')) {
						if (resp.result.status === ABANDONED) {
							interruptAll();
							abandonMessage();
						} else {
							getResponse(resp.result);
						}
					}
				});
			}, PING_INTERVAL);
		};

		$scope.endEpisode = function(episode) {
			API.endEpisode({id: episode.id}).then();
		};

		//----------------------------
		//     Message Functions
		//----------------------------

		$scope.message = function(user) {
			$scope.close('profile');
			$scope.chat = null;
			API.startChat({
				myId: $scope.user.id,
				theirId: user.id
			}).then(function(resp) {
				$scope.chat = resp.result;
				$scope.chat.recipient = $scope.displayName(user);
				$scope.chat.buffer = '';
				getMessages($scope.chat.id, $scope.chat.startDate).then(function (resp) {
					$scope.chat.history = resp.result.items;
					$ionicScrollDelegate.scrollBottom();
				});
			}, function(reason) {
				$scope.error = reason;
			});
			$scope.go('chat');
			interval(function() {
				checkChat();
			}, PING_INTERVAL);
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
			if ($scope.chat.buffer) {
				var id = randomId();
				$scope.chat.history.push({
					message: $scope.chat.buffer,
					senderId: $scope.user.id,
					random: id
				});
				$ionicScrollDelegate.scrollBottom();
				API.sendMessage({
					chatId: $scope.chat.id,
					myId: $scope.user.id,
					message: $scope.chat.buffer,
					random: id
				}).then(function (resp) {
					var message = resp.result;
					$scope.chat.history.splice(historyIndexOf(message.random), 1, message);
					updateLastModified(message.sentDate);
				}, function(reason) {
					$scope.error = reason;
				});
				$scope.chat.buffer = '';
			}
		};

		var historyIndexOf = function(random) {
			for (var i=$scope.chat.history.length-1; i>=0; i--) {
				if ($scope.chat.history[i].random === random) {
					return i;
				}
			}
			return -1;
		};

		var checkChat = function() {
			getMessages($scope.chat.id, $scope.chat.lastModified).then(function (resp) {
				var messages = resp.result.items;
				if (messages.length > 0) {
					for (var i=0; i<messages.length; i++) {
						if (historyIndexOf(messages[i].random) === -1) {
							$scope.chat.history.push(messages[i]);
							updateLastModified(messages[i].sentDate);
						}
					}
					$ionicScrollDelegate.scrollBottom();
					document.getElementById("chatInput").focus();
				}
			});
		};

		var updateLastModified = function(modified) {
			if (new Date(modified) > new Date($scope.chat.lastModified)) {
				$scope.chat.lastModified = modified;
			}
		};

		$scope.isYou = function(message) {
			return (message.senderId === $scope.user.id);
		};

		$scope.dispDate = function(message) {
			if (message.sentDate) {
				return new Date(message.sentDate).toLocaleString();
			} else {
				return null;
			}
		};

		//----------------------------
		//      Editor Functions
		//----------------------------

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
						onTap: function() { return $scope.user[field]; }
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
				$scope.user[field] = update;
				$scope.data = null;
				$scope.user.isChanged = true;
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
						onTap: function() { return $scope.user.password; }
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
				$scope.user.password = newPassword;
				$scope.data = null;
				$scope.user.isChanged = true;
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
						onTap: function() { return $scope.user.questions[num]; }
					}, {
						text: 'Okay',
						type: 'button-royal',
						onTap: function() { return $scope.data.value; }
					}
				]			
			}).then(function(update) {
				$scope.user.questions[num] = update;
				$scope.data = null;
				$scope.user.isChanged = true;
			});
		};

		$scope.saveProfile = function() {
			if ($scope.user.isChanged) {
				API.updateUser($scope.user).then(function (resp) {
					$ionicPopup.alert({
						title: "Saved",
						template: "Your profile has been saved!",
						okText: "Okay",
						okType: "button-royal"
					});
					$scope.user.isChanged = false;
				});
			} else {
				$ionicPopup.alert({
					title: "No Change",
					template: "Your profile has not been changed!",
					okText: "Okay",
					okType: "button-royal"
				})
			}
		};

		//----------------------------
		//       User Functions
		//----------------------------

		$scope.preview = function(user) {
			$scope.tempUser = user;
			this.show('profile');
		};

		$scope.displayName = function(user) {
			if (user.name) {
				return user.name;
			} else {
				return user.alias;
			}
		};

		$scope.hasQuestions = function(user) {
			for (var i=0; i<5; i++) {
				if (user.questions[i]) {
					return true;
				}
			}
			return false;
		};

		$scope.connect = function(user) {
			// this method sends a connection request to user
			API.connectEpisode({
				myId: $scope.user.id,
				theirId: user.id
			}).then(function(resp) {
				$scope.connections.push(resp.result);
			});
			$ionicPopup.alert({
				title: 'Connection Sent',
				template: 'You have sent a connection to ' + $scope.displayName(user) + '.',
				okText: 'Okay',
				okType: 'button-royal'
			});
		};

		$scope.getFriends = function() {
			API.getFriends({id: $scope.user.id}).then(function (resp) {
				$scope.friends = resp.result.items;
				$scope.$broadcast('scroll.refreshComplete');
			});
		};

		$scope.loadCommunity = function(location) {
			API.getCommunity({location: location}).then(function(resp) {
				$scope.community = resp.result;
			});
		};

		//----------------------------
		//    Connection Functions
		//----------------------------

		$scope.goCxns = function() {
			$scope.go('connections');
			$scope.getCxns();
		};

		$scope.acceptCxn = function(cxn) {
			cxn.status = IN_PROGRESS;
			$scope.playCxn(cxn);
			API.acceptEpisode({id: cxn.id}).then();
		};

		$scope.playCxn = function(cxn) {
			var index = $scope.connections.indexOf(cxn);
			$scope.connections.splice(index, 1);
			$scope.go('episode');
			loadEpisode(cxn);
		};

		$scope.getCxns = function() {
			API.getConnections({id: $scope.user.id}).then(function(resp) {
				const cxns = resp.result.items;
				if (cxns.length === 0) {
					$scope.connections = [];
				} else {
					var uids = [];
					for (var i = 0; i < cxns.length; i++) {
						cxns[i].data = getEpisodeData(cxns[i]);
						uids.push(getPartnerId(cxns[i]));
					}
					API.getUsers({ids: uids}).then(function (resp) {
						for (var j = 0; j < cxns.length; j++) {
							cxns[j].data.partner = $scope.displayName(resp.result.items[j]);
						}
						$scope.connections = cxns;
						$scope.$broadcast('scroll.refreshComplete');
					}, function(reason) {
						$scope.error = reason;
					});
				}
			});
		};

		//----------------------------
		//      Utility Functions
		//----------------------------

		var promises1 = [];
		var promises2 = [];

		var timeout = function(fn, delay) {
			var temp = $timeout(fn, delay);
			promises1.push(temp);
			return temp;
		};

		var interval = function(fn, delay) {
			var temp = $interval(fn, delay);
			promises2.push(temp);
			return temp;
		};

		var interruptAll = function() {
			while (promises1.length > 0) {
				$timeout.cancel(promises1.pop());
			}
			while (promises2.length > 0) {
				$interval.cancel(promises2.pop());
			}
		};

		//----------------------------
		//           Debug
		//----------------------------

		$scope.getState = function() {
			return state;
		};
	});

})();
