(function() {
	// Ionic Starter App

	// angular.module is a global place for creating, registering and retrieving Angular modules
	// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
	// the 2nd parameter is an array of 'requires'
	MATCHING = 0;
	UNMATCHED = 2;
	IN_PROGRESS = 4;
	ABANDONED = 6;
	COMPLETE = 8;

	PING_INT = 500; // ms

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

		$window.init= function() {
			$scope.$apply($scope.loadLib);
		};

		$scope.loadLib = function() {
			gapi.client.load('samesies', 'v1', function() {
				$scope.isBackendReady = true;
			}, 'http://localhost:8080/_ah/api');
			// http://localhost:8080/_ah/api
			// https://samesies-app.appspot.com/_ah/api
		};

		//----------------------------
		//           Data
		//----------------------------

		var loadQuestions = function() {
			gapi.client.samesies.samesiesApi.getAllQuestions().then(function(resp){
				$scope.questions = resp.result.items;
			});
			gapi.client.samesies.samesiesApi.getCategories().then(function(resp){
				$scope.categories = resp.result.items;
			});
			$scope.selected = {
				cat: "All"
			};
		};

		var loadUserData = function(user) {
			gapi.client.samesies.samesiesApi.getFriends({id: user.id}).then(function(resp){
				$scope.friends = resp.result.items;
			});
			// TODO: load connections here
			// TODO: load communities here
			$scope.connections = [];
		};

		var getUser = function(uid) {
			return gapi.client.samesies.samesiesApi.getUser({id: uid});
		};

		var getEpisode = function(eid) {
			return gapi.client.samesies.samesiesApi.getEpisode({id: eid});
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
			$scope.menuPopup.remove();
			$scope.helpPopup.remove();
			$scope.profilePopup.remove();
			$scope.categoryPopup.remove();
		});

		//----------------------------
		//      Login Functions
		//----------------------------

		$scope.login = function(user) {
			loadQuestions();
			loadUserData(user);
			$scope.loginData = null;
			$scope.user = user;
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
				gapi.client.samesies.samesiesApi.login($scope.loginData).then(function (resp) {
					$scope.login(resp.result);
				}, function (reason) { // error
					if (reason.status === 404) {
						$scope.loginData.error = 'Invalid email';
					} else if (reason.status === 400) {
						$scope.loginData.error = 'Invalid password';
					} else {
						$scope.loginData.error = 'Server is down'
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
			if (!this.loginData.error) {
				gapi.client.samesies.samesiesApi.createUser($scope.loginData).then(function(resp){
					$scope.login(resp.result);
				}, function () { // error
					$ionicPopup.alert({
						title: 'Invalid Email',
						template: 'That email is already being used.',
						okText: 'Okay',
						okType: 'button-royal'
					});
					$scope.loginData.error = '';
					$scope.$apply();
				});
			}
		};

		$scope.logOut = function() {
			this.close('menu');
			this.show('login');
			$scope.user = null;
			$scope.friends = null;
			$scope.loginData = {
				isCreate: false,
				error: false,
				toggle: function() {
					this.isCreate = !this.isCreate;
					this.error = false;
				}
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
				email: "ari@samesies.com",
				password: "samesies123"
			};
			this.doLogin();
		};
		
		//----------------------------
		//      Status Functions
		//----------------------------

		var status = { 
			s: "", 
			t: false
		};

		$scope.go = function(state) {
			// kill everything that may have been going
			interruptAll();
			if ($scope.episode && $scope.episode.id) {
				gapi.client.samesies.samesiesApi.abandonEpisode({id: $scope.episode.id}).then();
				$scope.episode = null;
				$scope.episodeData = null;
			}
			$scope.search = [''];
			// go wherever
			if (state === 'menu') {
				this.show('menu');
			} else {
				status.s = state;
				this.close('menu');
				$ionicScrollDelegate.scrollTop();
			}
		};
		
		$scope.is = function(state) {
			if (this.menuPopup.isShown()) {
				return state === "menu";
			} else {
				return status.s === state;
			}
		};

		$scope.toggle = function() {
			status.t = !status.t;
		};
		
		$scope.isToggled = function() {
			return status.t;
		};
		
		$scope.resetToggle = function() {
			status.t = false;
		};		
		
		//----------------------------
		//      Episode Control
		//----------------------------

		var epGo = function(state) {
			interruptAll();
			$scope.episodeData.state = state;
			$scope.$apply();
		};

		$scope.epIs = function(state) {
			return ($scope.episodeData.state === state);
		};

		$scope.find = function() {
			// episodeData holds local references that do not get directly transmitted
			$scope.go('episode');
			$scope.episodeData = {
				state: 'matching',
				stage: 0,
				questions: [],
				myAnswer: '',
				theirAnswer: ''
			};
			gapi.client.samesies.samesiesApi.findEpisode({myId: $scope.user.id}).then(function(resp){
				// if it returns matching, I am the first user
				// if it returns otherwise, I am the second user
				$scope.episodeData.is1 = (resp.result.status <= MATCHING);
				if ($scope.episodeData.is1) {
					$scope.episode = resp.result;
					interval(function() {
						getEpisode($scope.episode.id).then(function(resp) {
							// second condition prevents multiple calls
							if (resp.result.status >= IN_PROGRESS && $scope.epIs('matching')) {
								loadEpisode(resp.result);
							}
						});
					}, PING_INT);
				} else {
					loadEpisode(resp.result);
				}
			});
		};

		var loadEpisode = function(data) {
			$scope.episode = data;
			gapi.client.samesies.samesiesApi.getQuestions({ids: data.qids}).then(function(resp){
				$scope.episodeData.questions = resp.result.items;
				$scope.episodeData.stage = 1;
				epGo('entry');
			});
		};

		// TODO: make this work right
		$scope.play = function(episode) {
			this.episode = episode;
			this.go(episode.status);
		};

		$scope.matchText = function() {
			if (this.epIs('matching')) {
				return "Matching...";
			} else {
				return "Matched!";
			}
		};

		$scope.next = function() {
			$scope.episodeData.myAnswer = '';
			if ($scope.episodeData.stage == 10) {
				getUser(getPartnerId(this.episode)).then(function(resp) {
					$scope.message(resp.result);
				});
			} else {
				$scope.episodeData.stage++;
				epGo('entry');
			}
		};

		$scope.getQuestion = function() {
			return $scope.episodeData.questions[$scope.episodeData.stage - 1].q;
		};

		$scope.answer = function() {
			$scope.episodeData.theirAnswer = "Waiting for your partner to answer...";
			epGo('waiting');
			gapi.client.samesies.samesiesApi.answerEpisode({
				id: $scope.episode.id,
				myId: $scope.user.id,
				answer: $scope.episodeData.myAnswer
			}).then(function(resp) {
				$scope.error = resp.result;
				getResponse(resp.result);
				if ($scope.epIs('waiting')) {
					interval(function () {
						getEpisode($scope.episode.id).then(function (resp) {
							if ($scope.epIs('waiting')) {
								getResponse(resp.result);
							}
						});
					}, PING_INT);
				}
			}, function(reason) {
				$scope.error = reason;
			});
		};

		var getResponse = function(data) {
			var index = $scope.episodeData.stage - 1;
			var answer;
			if ($scope.episodeData.is1) {
				// make sure it has the list and that the list is long enough
				if (data.answers2 && data.answers2.length > index) {
					answer = data.answers2[index];
				}
			} else if (data.answers1 && data.answers1.length > index) {
				answer = data.answers1[index];
			}
			if (answer != null) { // they also answered
				$scope.episodeData.theirAnswer = answer;
				epGo('continue');
			}
		};

		var getPartnerId = function(episode) {
			if (episode.uid1 === $scope.user.id) {
				return episode.uid2;
			} else {
				return episode.uid1;
			}
		};

		//----------------------------
		//     Message Functions
		//----------------------------

		$scope.message = function(user) {
			// TODO: this should only use displayName if they are a friend...
			this.resetChat(this.displayName(user));
			this.go('chat');
			this.close('profile');
			var n = 0;
			interval(function() {
				$scope.chat.history.push({
					isYou: false,
					text: n.toString()
				});
				n++;
			}, 2000);
		};

		$scope.resetChat = function(username) {
			$scope.chat = {
				recipient: username,
				history: [],
				buffer: ''
			};
		};

		$scope.sendChat = function() {
			if (this.chat.buffer) {
				this.chat.history.push({
					isYou: true,
					text: this.chat.buffer
				});
				this.chat.buffer = '';
				$ionicScrollDelegate.scrollBottom(true);
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
				gapi.client.samesies.samesiesApi.updateUser($scope.user).then(function (resp) {
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

		$scope.challenge = function(user) {
			this.close('profile');
			// TODO: finish this method
		};

		//----------------------------
		//    Connection Functions
		//----------------------------

		$scope.loadCommunity = function(location) {
			gapi.client.samesies.samesiesApi.getCommunity({location: location}).then(function(resp){
				$scope.community = resp.result;
			});
		};

		$scope.goConnections = function() {
			// TODO: these need to be retrieved from server
			if (this.connections && this.connections.length > 0) {
				this.go('connections');
			} else {
				$ionicPopup.alert({
					title: 'No Connections',
					template: 'You have no pending connections.',
					okText: 'Okay',
					okType: 'button-royal'
				});
			}
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

		$scope.getStatus = function() {
			return status;
		};
	});

})();
