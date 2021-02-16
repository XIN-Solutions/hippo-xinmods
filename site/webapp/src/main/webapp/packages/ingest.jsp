<!doctype>
<html>

    <head>

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="libs/bootstrap.min.css" type="text/css">
        <link rel="stylesheet" href="libs/bootstrap-theme.min.css" type="text/css">


        <link href="css/styles.css" type="text/css" rel="stylesheet">

        <title>Packages | XIN Solutions</title>

    </head>
    <body class="Modal_Content">

        <%@include file="dynamic.jsp" %>

        <div class="Modal_Top"><!-- --></div>

        <div ng-app="app"  ng-controller="IngestController" data-loading="{{1}}" ></div>
        <div>
            <div class="container">
                <div class="row">

                    <div class="
                            col-lg-offset-3 col-md-offset-3 col-sm-offset-2
                            col-lg-6 col-md-6 col-sm-8 col-xs-12
                        ">


                        <div class="ButtonBar">
                            <button class="btn btn-default" onclick="javascript: document.location.href='index.jsp'">
                                <i class="glyphicon glyphicon-arrow-left"></i>
                                Back
                            </button>
                        </div>

                        <p>Upload a package from disk for ingestion &hellip;</p>

                        <div class="alert alert-info" role="alert">
                            Hitting the submit button will upload and immediately install the package to the repository.
                        </div>
                        <form id="uploadForm" enctype="multipart/form-data">

                            <div class="well well-lg">
                                <div class="form-group">
                                    <label for="file">File to upload</label>
                                    <input id="file" name="file" type="file">
                                </div>

                                <label for="importContent">
                                    <input type="checkbox" value="true" name="importContent" id="importContent"> Ingest packaged content
                                </label>
                                <p class="help-block">
                                    Check this to box to import the content present in the package.
                                </p>

                                <label for="importCND">
                                    <input type="checkbox" value="true" name="importCND" id="importCND"> Ingest packaged CNDs
                                </label>
                                <p class="help-block">
                                    Check this to box to import the CND present in the package (this may be necessary for the content to work).
                                </p>

                                <label for="importPackageDef">
                                    <input type="checkbox" value="true" name="importPackageDef" id="importPackageDef" checked> Keep package definition
                                </label>
                                <p class="help-block">
                                    Check this box if you wish to keep the package definition 
                                </p>

                                <input type="hidden" id="redirectTo" name="redirectTo" value="">
                            </div>

                            <!-- button bar -->
                            <div class="ButtonBar ButtonBar--bottom">
                                <a href="#" class="btn right btn-md btn-primary" onclick="uploadPackage()"><i class="glyphicon glyphicon-upload"><!-- --></i> Ingest package</a>
                                <a class="btn btn-link" onclick="javascript: document.location.href='index.jsp'">Cancel</a>
                            </div>

                        </form>

                    </div>

                </div>
            </div>
        </div>

        <!-- include external libraries -->
        <script src="libs/angular.min.js"></script>
        <script src="libs/jquery.min.js"></script>
        <script src="libs/bootstrap.min.js"></script>
        <script src="libs/underscore-min.js"></script>

        <!-- package manger -->
        <script src="js/app.js"></script>
        <script src="js/endpoint.js"></script>
        <script src="js/ingestController.js"></script>

        <script>
            function uploadPackage() {

                var form = $("form");
                var fData = new FormData(form[0]);
                var xhr = new XMLHttpRequest();

                xhr.open("POST", form.attr("action"));
                xhr.setRequestHeader("Authorization", Config.ApiAuth);
                xhr.send(fData);

                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4) {
                        alert("Upload completed");
                        history.go(-2);
                    }
                }
            }
        </script>

    </body>
</html>