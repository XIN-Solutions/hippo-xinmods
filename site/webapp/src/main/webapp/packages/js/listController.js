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
                        const incomingPackages = [...payload.data];
                        incomingPackages.sort(function(a, b) {
                            return a.id > b.id ? 1 : -1;
                        });


                        _.each(incomingPackages, function(pkg, idx) {
                            pkg.collapsed = true; 
                        });

                        $scope.packages = incomingPackages;
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
                document.location.href = "ingest.jsp";
            },

            /**
             * Create a new package
             */
            createPackage : function() {
                document.location.href = "package.jsp";
            },

            /**
             * Go to the edit page
             * @param id is the package identifier to change
             */
            editPackage : function(id) {
                document.location.href = "package.jsp?id=" + id;
            },

            clonePackage : function(id) {
                var newId = prompt("Name of cloned package definition?");
                if (newId) {
                    endpoint
                        .clonePackage(id, newId)
                        .then(function(response) {
                            $scope.refresh({
                                flash : "Package cloned into '" + newId + "'."
                            });
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
                        $scope.refresh({
                            flash : "Package definition deletion for '" + id + "' completed.",
                            flashType: 'warning'
                        });
                    });
                }
            },

            /**
             * Build a new package
             *
             * @param id  the identifier to use
             */
            downloadPackage : function(id) {
                var date = moment().format("YYYY/MM/DD");
                var postfix = prompt('Filename postfix:', date);
                if (postfix === null) {
                    return;
                }
                endpoint.downloadPackage(id, postfix);
            },

            /**
             * Refresh the page
             */
            refresh : function(options) {
                if (options) {
                    var serParams = _.map(options, function(val, key) {
                        return key + "=" + encodeURIComponent(val);
                    });
                    var url = document.location.pathname + "?" + serParams.join("&");
                    document.location.href = url;
                }
                else {
                    document.location.href = document.location.pathname;
                }
            },

            /**
             * @return boolean if there are no packages
             */
            noPackages : function() {
                return !this.packages || this.packages.length === 0;
            },

            /**
             * @return {string} flash text to show at the top of the page
             */
            flashText : function() {
                return (new URL(document.location)).searchParams.get('flash')
            },

            /**
             * @return {string} type of flash (bootstrap alert css modifier class)
             */
            flashType : function() {
                return (new URL(document.location)).searchParams.get('flashType') || 'success';
            }


        });

        $scope.initialise();

    });


})();