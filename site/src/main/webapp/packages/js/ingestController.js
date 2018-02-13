app.controller("IngestController", ['$scope', 'endpoint', function($scope, endpoint) {

    _.extend($scope, {

        action : "",
        form: null,

        initialise : function() {
            this.form = document.getElementById("uploadForm");
            this.form.action = endpoint.getIngestUrl();
            this.form.method = 'post';

            document.getElementById("redirectTo").value = document.location.href;
        },

        backToList : function() {
        	document.location.href = "index.jsp";
        }

    });


    $scope.initialise();

}]);