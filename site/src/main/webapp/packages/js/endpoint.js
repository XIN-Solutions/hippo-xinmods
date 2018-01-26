(function(undefined) {

    app.service("endpoint", function($q, $http) {

        /**
         * Where to look for the endpoints
         * @type {String}
         */
        var BASE_URL = "/site/custom-api";


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
             * Build a package with a certain identifier
             *
             * @return {[type]} [description]
             */
            buildPackage : function(id) {
                return $http({
                    method: "post",
                    url: BASE_URL + "/packages/" + id + "/build"
                });
            }

        };

    });

})();