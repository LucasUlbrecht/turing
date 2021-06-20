turingApp.controller('TurAdminCtrl', [
	"$rootScope", "$scope", "vigLocale", "$translate", "turAPIServerService", "$http", "$window",
	function ($rootScope, $scope, vigLocale, $translate, turAPIServerService, $http, $window) {
		$scope.vigLanguage = vigLocale.getLocale().substring(0, 2);
		$translate.use($scope.vigLanguage);

		if (!$rootScope.authenticated) {
			$http.get(turAPIServerService.get().concat("/v2/user/current"))
				.then(function (response) {
					if (response.data.username) {
						$rootScope.principal = response.data;
						$rootScope.authenticated = true;
					} else {
						$rootScope.authenticated = false;
					}

				}, function () {
					$rootScope.authenticated = false;
				});
		}

		$rootScope.logout = function () {
			$http.post('logout', {}).then(function () {
				$rootScope.authenticated = false;
				$window.location.href = "/";
			}, function (data) {
				$rootScope.authenticated = false;
			});
		}
	}]);
