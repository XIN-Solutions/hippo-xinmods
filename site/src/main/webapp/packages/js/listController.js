(function(undefined) {

    app.controller("ListController", function($scope, endpoint) {

        /**
         * Add to scope
         */
        _.extend($scope, {

            /**
             * Contains package descriptions
             */
            packages : [],

            /**
             * Initialisation of interface data
             */
            initialise : function() {

                endpoint
                    .getPackages()
                    .then( function(payload)  {
                        $scope.packages = payload.data;
                        _.each($scope.packages, function(pkg, idx) { 
                            pkg.collapsed = true; 
                        });
                    })
                    .catch( function(err) {console.log(err)} )
                ;
            },

            /**
             * Collapse a package
             */
            collapse : function(pkg) {
                pkg.collapsed = !pkg.collapsed;
            },

            /**
             * Ingest a package
             */
            ingestPackage : function() {
                document.location.href = "ingest.html";
            },

            /**
             * Create a new package
             */
            createPackage : function() {
                document.location.href = "package.html";
            },

            /**
             * Go to the edit page
             * @param id is the package identifier to change
             */
            editPackage : function(id) {
                document.location.href = "package.html?id=" + id;
            },

            clonePackage : function(id) {
                var newId = prompt("Name of cloned package definition?");
                if (newId) {
                    endpoint
                        .clonePackage(id, newId)
                        .then(function(response) {
                            $scope.refresh();
                        })
                        .catch(function(err) {
                            console.log("Error occured during package cloning: ", err);
                        })
                    ;
                } 
            },

            /**
             * Ask to delete definition
             *
             * @param id        is the package definition to delete
             */
            deletePackage : function(id) {
                if (confirm(
                        "Are you sure you wish to delete the package definition?\n" + 
                        "This will not remove the contents in the package from the repository")
                    ) {
                    endpoint.deletePackage(id).then(function() {
                        $scope.refresh();
                    });
                }
            },

            /**
             * Build a new package
             *
             * @param id  the identifier to use
             */
            downloadPackage : function(id) {
                var date = moment().format("d/M/YYYY");
                var postfix = prompt('Filename postfix:', date);
                if (postfix === null) {
                    return;
                }
                endpoint.downloadPackage(id, postfix);
            },

            /**
             * Refresh the page
             */
            refresh : function() {
                location.reload();
            },

            /**
             * @return boolean if there are no packages
             */
            noPackages : function() {
                return !this.packages || this.packages.length === 0;
            }

        });

        $scope.initialise();

    });


})();