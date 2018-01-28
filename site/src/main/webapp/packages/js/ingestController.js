app.controller("IngestController", ['$scope', 'endpoint', function($scope, endpoint) {

    _.extend($scope, {

        action : "",

        initialise : function() {
            $scope.action = endpoint.getIngestUrl();
        }

    });


    $scope.initialise();

}]);