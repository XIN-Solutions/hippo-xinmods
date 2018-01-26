(function(undefined) {

    app.controller("packages", function($scope, endpoint) {

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
                        _.each($scope.packages, function(pkg) {pkg.collapsed = true});
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
             * Add a package from a certain URL
             */
            addPackage : function() {

                var pkg = prompt("Ingest package from URL:");
                if (!pkg) {
                    console.log("Cancelled");
                    return;
                }
                console.log("Ingesting package from: ", pkg);
            },

            /**
             * Create a new package
             */
            createPackage : function() {
                var id = prompt("Package ID of new package:");
                if (!id) {
                    console.log("Cancelled");
                    return;
                }
                console.log("New package: ", id);
            },

            /**
             * Build a new package
             *
             * @param id  the identifier to use
             */
            buildPackage : function(id) {
                endpoint.buildPackage(id);
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