'use strict';

/* Services */
var services = angular.module('catalogo.services');

services.factory('UserService', function($http, $q, AlertService) {

	var MIN_NUMBER_OF_NAME_CARACTERS = 5;
	var grupos = [];
	var service = {};

	service.getGrupos = function() {
		var deferred = $q.defer();
		$http({
			url : 'api/grupo',
			method : "GET"
		}).success(function(data) {
			grupos = data;
			deferred.resolve(grupos);
		}).error(function(data, status) {
			console.log(data, status);
		});
		return deferred.promise;
	};

	service.searchByCPF = function(cpf) {
		var deferred = $q.defer();
		$http.get('api/user/cpf/' + cpf).success(
				function(data) {
					if (data == "") {
						AlertService.addWithTimeout('warning',
								'Usuário não cadastrado no LDAP');
					} else {
						deferred.resolve(data);
					}
				}).error(function(data, status) {
			if (status == 412) {
				AlertService.addWithTimeout('danger', data[0].message);
			}
		});
		return deferred.promise;
	};

	service.searchByName = function(name) {
		var deferred = $q.defer();
		if (name.length >= MIN_NUMBER_OF_NAME_CARACTERS) {
			$http.get('api/user/nome/' + name).success(	function(data) {
				if (data == "") {
					AlertService.addWithTimeout('warning', 'Usuário não cadastrado no LDAP');
				} else {
					deferred.resolve(data);
				}
			}).error(function(data, status) {
				if (status == 412) {
					AlertService.addWithTimeout('danger', data[0].message);
				}
			});
		} else {
			AlertService.addWithTimeout('warning', 'Digite pelo menos '
					+ MIN_NUMBER_OF_NAME_CARACTERS
					+ ' caracteres para realizar a consulta.');
		}
		return deferred.promise;
	};

	return service;
});