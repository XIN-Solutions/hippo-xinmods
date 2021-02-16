(function(Config, undefined) {

    app.service("endpoint", function($q, $http) {

        /**
         * Where to look for the endpoints
         * @type {String}
         */
        var BASE_URL = Config.XinApiUrl;

        $http.defaults.headers.common.Authorization = Config.ApiAuth;

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
             * Returns a promise to the package content response
             * 
             * @param  {[type]} pkgId [description]
             * @return {[type]}       [description]
             */
            getPackage : function(pkgId) {
                return $http({
                    method: 'get',
                    url: BASE_URL + "/packages/" + pkgId
                });
            },

            /**
             * Create a package
             */
            createPackage : function(pkgId, packageInfo) {
                return $http.put(BASE_URL + "/packages/" + pkgId, packageInfo);
            },


            /**
             * Update a package
             */
            updatePackage : function(pkgId, packageInfo) {
                return $http.post(BASE_URL + "/packages/" + pkgId, packageInfo);
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
             * Clone a package definition and give it a different name
             *
             * @param fromPkgId     the package to clone
             * @param toPkgId       the new package definition 
             */
            clonePackage : function(fromPkgId, toPkgId) {
                return (
                    $http.post(
                        BASE_URL + "/packages/clone", {
                            fromPackage : fromPkgId,
                            toPackage: toPkgId
                        }
                    )
                );
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
            downloadPackage : function(id, postfix) {
                $http.get(
                    BASE_URL + "/packages/" + id + "/export?postfix=" + encodeURIComponent(postfix),
                    {
                        responseType: 'arraybuffer'
                    }
                ).then(function(result) {
                    var file = new Blob([result.data], {type: 'application/zip'});
                    var fileURL = window.URL.createObjectURL(file);
                    var a = document.createElement("a");
                    a.href = fileURL;
                    a.download = id + "_" + postfix + ".zip";
                    a.click();
                })
            }

        };

    });

})(window.Config);