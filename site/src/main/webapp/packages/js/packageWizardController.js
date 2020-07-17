var PackageWizardController = app.controller("PackageWizardController", ['$scope', 'endpoint', function($scope, endpoint) {

    _.extend($scope, {

        name :'',
        requiredCnds: [
        ],
        cndIdentifiers: [
        ],
        contentPaths: [
            { value: '/content/documents' },
            { value: '/content/gallery' }
        ],

        initialise : function() {
            if (!this.isEditing()) {
                return;
            }

            var remap = function(list) {
                return (
                    _.map(list || [], function(x) { 
                        return { value: x }; 
                    })
                );
            };

            var pkgId = this.packageIdParam();
            endpoint.getPackage(pkgId).then(function(content) {
                $scope.name = content.data.id;
                $scope.contentPaths = remap(content.data.filters);
                $scope.cndIdentifiers = remap(content.data.cnds);
                $scope.requiredCnds = remap(content.data.requiredCnds);
            })
        },

        isEditing : function() {
            return this.packageIdParam();
        },

        packageIdParam : function() {
            return this.getParameters().get('id');
        },

        getParameters : function() {
            return (new URL(document.location)).searchParams;
        },


        addTo : function(list) {
            list.push({value: ''});
        },

        removeFrom : function(list, idx) {
            list.splice(idx, 1);
        },


        valueToList : function(list) {
            return _.map(list || [], function(x) { return x.value; });
        },

        packageStructure : function() {
            return {
                id: $scope.name,
                filters : $scope.valueToList($scope.contentPaths),
                cnds : $scope.valueToList($scope.cndIdentifiers),
                requiredCnds : $scope.valueToList($scope.requiredCnds)
            };
        },

        /**
         * Create the package
         */
        createPackage : function() {
            var name = this.name;
            endpoint.createPackage(name, this.packageStructure()).then(function() {
                document.location.href = "index.jsp?flash=" + encodeURIComponent("Succesfully created package: `" + name + "`.");
            });
        },

        /**
         * Update the package
         */
        updatePackage : function() {
            var pkgId = this.packageIdParam();
            endpoint.updatePackage(pkgId, this.packageStructure()).then(function() {
                document.location.href = "index.jsp?flash=" + encodeURIComponent("Succesfully updated package");
            });
        },

        back : function() {
            document.location.href = "index.jsp";
        }


    });

    $scope.initialise();

}]);