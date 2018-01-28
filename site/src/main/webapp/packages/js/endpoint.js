(function(undefined) {

    app.service("endpoint", function($q, $http) {

        /**
         * Where to look for the endpoints
         * @type {String}
         */
        var BASE_URL = "http://localhost:8080/site/custom-api";


        var package = {
            collapsed: true,
            modified: false,
            origin: "",
            filters: [
                "/content/documents/shop1",
                "/content/documents/shop2"
            ],
            cnds : [
                "xinmods:shop",
                "xinmods:content",
                "xinmods:shopcategory",
                "xinmods:product"
            ]
        };

        return {

            /**
             * Retrieve all packages
             */
            getPackages : function() {
                return $http({
                    method: "get",
                    url: BASE_URL + "/packages/list"
                });
            },

            /**
             * Delete a package
             *
             * @param pkgId     is the package definition to delete.
             */
            deletePackage : function(pkgId) {
                return $http({
                    method: 'delete',
                    url: BASE_URL + "/packages/" + pkgId
                });
            },

            /**
             * @returns {string} the ingestion url to post the form to
             */
            getIngestUrl : function() {
                return BASE_URL + "/packages/import";
            },

            /**
             * Build a package with a certain identifier
             *
             * @return {[type]} [description]
             */
            downloadPackage : function(id) {
                return window.open(BASE_URL + "/packages/" + id + "/export");
            }

        };

    });

})();