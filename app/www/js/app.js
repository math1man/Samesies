(function() {
	// Ionic Starter App

	// angular.module is a global place for creating, registering and retrieving Angular modules
	// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
	// the 2nd parameter is an array of 'requires'
	var app = angular.module('samesies', ['ionic', 'directives']);
	
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

	app.filter('checkName', function () {
		return function (items, string) {
			var filtered = [];
			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				var regex = new RegExp(".*" + string + ".*", 'i');
				if (regex.test(item.name) || regex.test(item.alias)) {
					filtered.push(item);
				}
			}
			return filtered;
		};
	});

	app.controller('MainController', function($scope, $window, $ionicPopup, $ionicGesture,
			$ionicModal, $http, $timeout, $interval, $ionicScrollDelegate) {

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
		};

		//----------------------------
		//           Data
		//----------------------------

		// Eventually, this stuff needs to be retrieved directly from the server, as needed

		$scope.questions = [];

		$scope.getAllQuestions = function(category) {
			gapi.client.samesies.samesiesApi.questions({category: category}).then(function(resp){
				$scope.questions = resp.result.items;
			});
		};

		$http.get('data/users.json').success(function(data){
			$scope.users = data;
		});

		var getUserById = function(uid) {
			for (var i=0; i<$scope.users.length; i++) {
				if ($scope.users[i].uid === uid) {
					return $scope.users[i];
				}
			}
			return null;
		};

		var getUsersById = function(uids) {
			var output = [];
			if (uids.length > 0) {
				for (var i=0; i<$scope.users.length; i++) {
					if (uids.indexOf($scope.users[i].uid) > -1) {
						output.push($scope.users[i]);
					}
				}
			}
			return output;
		};

		var getUsersByLocation = function(location) {
			var output = [];
			for (var i=0; i<$scope.users.length; i++) {
				if ($scope.users[i].location === location) {
					output.push($scope.users[i]);
				}
			}
			return output;
		};

		//----------------------------
		//    Modals and Functions
		//----------------------------

		$ionicModal.fromTemplateUrl('templates/login.html', {
			scope: $scope,
			animation: 'slide-in-up',
			backdropClickToClose: false,
			hardwareBackButtonClose: false
		}).then(function(modal) {
			$scope.login = modal;
			$scope.show('login'); // open the login on app-start
		});
		
		$ionicModal.fromTemplateUrl('templates/menu.html', {
			scope: $scope,
			animation: 'slide-in-right',
			backdropClickToClose: false,
			hardwareBackButtonClose: false
		}).then(function(modal) {
			$scope.menu = modal;
		});
		
		$ionicModal.fromTemplateUrl('templates/help.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			$scope.help = modal;
		});
		
		$ionicModal.fromTemplateUrl('templates/profile.html', {
			scope: $scope,
			animation: 'slide-in-left'
		}).then(function(modal) {
			$scope.profile = modal;
		});
		
		$scope.show = function(modal) {
			this.resetToggle();
			this[modal].show();
		};
		
		$scope.close = function(modal) {
			this[modal].hide();
		};
		
		$scope.$on('$destroy', function() {
			// Cleanup the modals when we're done with them!
			$scope.menu.remove();
			$scope.help.remove();
			$scope.profile.remove();
		});

		//----------------------------
		//      Login Functions
		//----------------------------

		$scope.loginData = {
			isCreate: false,
			error: false,
			toggle: function() {
				this.isCreate = !this.isCreate;
				this.error = false;
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
				gapi.client.samesies.samesiesApi.login($scope.loginData).then(function (resp) {
					$scope.user = resp.result;
					$scope.close('login');
					$scope.loginData = null;
					$scope.go('menu');
				}, function (reason) { // error
					if (reason.status === 404) {
						$scope.loginData.error = 'Invalid email';
					} else {
						$scope.loginData.error = 'Invalid password';
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
				gapi.client.samesies.samesiesApi.create($scope.loginData).then(function(resp){
					$scope.user = resp.result;
					$scope.close('login');
					$scope.loginData = null;
					$scope.go('menu');
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
			$scope.error = null;
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
			if (status.s != state) {
				interruptAll();
				$scope.search = [''];
			}
			if (state === 'menu') {
				this.show('menu');
			} else {
				status.s = state;
				this.close('menu');
				$ionicScrollDelegate.scrollTop();
			}
		};
		
		$scope.is = function(state) {
			if (this.menu.isShown()) {
				return state === "menu";
			} else if (state === "episode") {
				return this.is("matching") || this.is("entry") || this.is("waiting") || this.is("continue");
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

		$scope.find = function() {
			$scope.episode = {
				stage: 0
			};
			$scope.go('matching');
			// add searching code here
			timeout(function() {
				$scope.initEpisode(1);
			}, 2000);
		};
		
		$scope.play = function(episode) {
			this.episode = episode;
			this.go(episode.status);
		};
		
		$scope.initEpisode = function(uid) {
			$scope.episode = {
				stage: 0,
				partnerId: uid,
				questions: []
			};
			gapi.client.samesies.samesiesApi.makeEpisode({'count': 5}).then(function(resp){
				$scope.episode.questions = resp.result.questions;
				$scope.next();
			});
		};
	
		$scope.matchText = function() {
			if (this.is('matching')) {
				return "Matching...";
			} else {
				return "Matched!";
			}
		};

		$scope.getQuestion = function() {
			return this.episode.questions[this.episode.stage - 1].q;
		};

		$scope.answer = function() {
			// send myAnswer to server
			this.go("waiting");			
			this.episode.theirAnswer = "Waiting for your partner to answer...";
			timeout(function() {
				$scope.retrieve();
			}, 1000);
		};
	
		$scope.retrieve = function() {
			interval(function() {
				// eventually ping server for their answer
				var temp = $scope.episode.questions[$scope.episode.stage - 1].a;
				if (temp != null) {
					$scope.episode.theirAnswer = temp;
					$scope.go("continue");
					// call to go interrupts interval command
				}
			}, 1000);
		};

		$scope.next = function() {
			this.episode.myAnswer = '';
			if (this.episode.stage == 5) {
				this.message(getUserById(this.episode.partnerId));
			} else {
				this.episode.stage++;
				this.go("entry");			
			}
		};

		//----------------------------
		//     Message Functions
		//----------------------------

		$scope.message = function(user) {
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

		$scope.getFriends = function(user) {
			return getUsersById(user.friends);
		};
		
		$scope.challenge = function(user) {
			this.close('profile');
			this.initEpisode(user.uid);
		};

		//----------------------------
		//    Connection Functions
		//----------------------------

		$scope.goConnections = function() {
			if (this.user.connections.length > 0) {
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

		$scope.getNearby = function() {
			// eventually, this should get this from the server
			return getUsersByLocation(this.user.location);
		};

		$scope.goBrowse = function() {
			if (this.questions.length === 0) {
				this.getAllQuestions("random");
			}
			this.go("browse");
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

		var transform = function(data){
			data = JSON.stringify(data);		// to json
			data = data.replace(/:/g, "=");	// add in '='
			data = data.replace(/,/g, "&");	// add in '&'
			data = data.replace(/[{}"]/g, "");	// remove ends
			return data;
		};

		String.prototype.hashCode = function() {
			var hash = 0, i, chr, len;
			if (this.length == 0) return hash;
			for (i = 0, len = this.length; i < len; i++) {
				chr   = this.charCodeAt(i);
				hash  = ((hash << 5) - hash) + chr;
				hash |= 0; // Convert to 32bit integer
			}
			return hash;
		};

		//----------------------------
		//           Debug
		//----------------------------

		$scope.getStatus = function() {
			return status;
		};
	});

})();
