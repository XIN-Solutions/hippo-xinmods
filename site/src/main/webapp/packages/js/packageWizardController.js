var PackageWizardController = app.controller("PackageWizardController", ['$scope', 'endpoint', function($scope, endpoint) {

    _.extend($scope, {

        initialise : function() {
        },

        isEditing : function() {
            return this.packageIdParam();
        },

        packageIdParam : function() {
            return this.getParameters().get('id');
        },

        getParameters : function() {
            return (new URL(document.location)).searchParams;
        }

    });

    $scope.initialise();

}]);