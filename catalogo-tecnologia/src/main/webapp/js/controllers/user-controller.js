'use strict';

/* Controllers */
var controllers = angular.module('catalogo.controllers');

controllers.controller('UserNew',
 	function UserNew($scope, $http, $location, AlertService) {
	
		var MIN_NUMBER_OF_NAME_CARACTERS = 5;
		$scope.users = [];
		$scope.name = {}; 
		
		function init(grupos) {
	        $scope.grupos = grupos;
	    }
		
		$http({
			url : 'api/grupo',
			method : "GET"
		}).success(function(response) {
			init(response);
		});
		
		$scope.pesquisar = function(cpf,nome) {
			$scope.users = [];
			$scope.user = {};
			if(cpf != "" && cpf != null){
				$scope.pesquisarCPF(cpf);
			}else if(nome != "" && nome != null){
				$scope.pesquisarNome(nome);
			}else{
				AlertService.addWithTimeout('warning', 'Preencha um dos campos antes executar a pesquisa!');
			}
		};
		
		$scope.pesquisarCPF = function(cpf) {
			$http.get('api/user/cpf/' + cpf).success(function(data) {
				if (data == "") {
					AlertService.addWithTimeout('warning', 'Usuário não cadastrado no LDAP');
				}else{
					$scope.user = data;
					$scope.user.grupos = [];
					$scope.users.push($scope.user);
				}
			}).error(function(data, status) {
				if (status == 412) {
					AlertService.addWithTimeout('danger', data[0].message);
				}
			});
		};
		
		$scope.pesquisarNome = function(nome) {
			$scope.name = nome;
			if($scope.name.length >= MIN_NUMBER_OF_NAME_CARACTERS){
				$http.get('api/user/nome/' + nome).success(function(data) {
					if (data == "") {
						AlertService.addWithTimeout('warning', 'Usuário não cadastrado no LDAP');
					}else{
						$scope.users = data;
						$.each($scope.users, function(i, user) {
							user.grupos = [];
						});
					}
				}).error(function(data, status) {
					if (status == 412) {
						AlertService.addWithTimeout('danger', data[0].message);
					}
				});
			}else{
				AlertService.addWithTimeout('warning', 'Digite pelo menos ' + MIN_NUMBER_OF_NAME_CARACTERS + ' caracteres para realizar a consulta.');
			}
		};
		
		$scope.editar = function(user) {
			$scope.user = user;
		};
		
		// toggle selection for a given fruit by name
		$scope.toggleSelection = function toggleSelection(grupo) {
			var idx = $scope.isGrupoInGrupos(grupo);
			// is currently selected
			if (idx > -1) {
				$scope.user.grupos.splice(idx, 1);
			}
			// is newly selected
			else {
				$scope.user.grupos.push(grupo);
			}
		};
		
		$scope.isGrupoInGrupos = function isGrupoInUserGrupos(grupo){
			var id = grupo.id;
			
			for(var i=0; i<$scope.user.grupos.length; i++){
				if(id == $scope.user.grupos[i].id){
					return i;
				}
			}
			return -1;
		};
		
		$scope.salvar = function() {
			$("[id$='-message']").text("");
				$http({
					url : 'api/user',
					method : "POST",
					data : $scope.user,
					headers : {
						'Content-Type' : 'application/json;charset=utf8'
					}
				}).success(
						function(data) {
							AlertService.addWithTimeout('success', 'Usuário salvo com sucesso');
							$location.path('/user');
						}).error(
								function(data, status) {
									if (status == 412) {
										AlertService.addWithTimeout('danger', data[0].message);
									}
								});
		}
});


controllers.controller('UserList',

function UserList($scope, $http, $location) {

	function carregarUser() {
		$http.get('api/user').success(function(data) {
			$scope.users = data;
		});
	}

	$scope.editar = function(user) {
		$location.path('/user/edit/' + user.id);
	};
	
	$scope.novo = function() {
		$location.path('/user/new');
	};

	carregarUser();
});

controllers.controller('UserEdit', function Analise($scope, $http, $location, $routeParams, AlertService) {

	var id = $routeParams.id;
	$scope.grupos = [];
	$scope.user = {};
	$scope.user.grupos = [];
	
	$http.get('api/grupo').success(function(response) {
		 $scope.grupos = response;
	});

	$http.get('api/user/' + id).success(function(response) {
		$scope.user = response;
	});

	// toggle selection for a given fruit by name
	$scope.toggleSelection = function toggleSelection(grupo) {
		var idx = $scope.isGrupoInGrupos(grupo);
		// is currently selected
		if (idx > -1) {
			$scope.user.grupos.splice(idx, 1);
		}
		// is newly selected
		else {
			$scope.user.grupos.push(grupo);
		}
	};
	
	$scope.isGrupoInGrupos = function isGrupoInUserGrupos(grupo){
		var id = grupo.id;
		for(var i=0; i<$scope.user.grupos.length; i++){
			if(id == $scope.user.grupos[i].id){
				return i;
			}
		}
		return -1;
	};
	
	$scope.salvar = function() {
		$("[id$='-message']").text("");
		$http({
			url : 'api/user',
			method : "PUT",
			data : $scope.user,
			headers : {
				'Content-Type' : 'application/json;charset=utf8'
			}
		}).success(
				function(data) {
					AlertService.addWithTimeout('success',
							'Usuário salvo com sucesso');
					$location.path('/user');
				}).error(
				function(data, status) {
					if (status == 412) {
						$.each(data, function(i, violation) {
							$("#" + violation.property + "-message").text(
									violation.message);
						});
					}
				});
	};

});