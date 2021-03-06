<!doctype>
<html>

    <head>

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="libs/bootstrap.min.css" type="text/css">
        <link rel="stylesheet" href="libs/bootstrap-theme.min.css" type="text/css">


        <link href="css/styles.css" type="text/css" rel="stylesheet">

        <title>Packages | XIN Solutions</title>

    </head>
    <body ng-app="app" class="Modal_Content">

        <%@include file="dynamic.jsp" %>

        <div class="Modal_Top"><!-- --></div>

        <div ng-controller="PackageWizardController" data-loading="{{1}}">
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


                        <div ng-show="isEditing()">
                            <h4>Edit package `{{packageIdParam()}}`</h4>
                            <p class="alert alert-info">
                                The form below allows you to change the package definition of the <strong>{{packageIdParam()}}</strong>
                                package. 
                                <br><br>
                                Once you have changed the definition, the package should be downloaded again. The old package definition is lost
                                but can be regained by importing a package 'Definition only'.
                            </p>
                        </div>
                        <div ng-show="!isEditing()">
                            <h4>New package</h4>
                            <p class="alert alert-info">
                                Use the form below to create a new package definition. After creating a new package definition
                                you can capture the content by downloading the package.
                            </p>
                        </div>

                        <form>

                            <div class="form-group">
                                <label for="name">Package name</label>
                                <input type="text" id="name" placeholder="Package identifier" class="form-control input-lg" ng-model="name" autofocus>
                            </div>

                            <!--
                                Content paths
                            -->
                            <div class="form-group">
                                <label>Content paths</label>
                                <p class="help-block">Capture specific content, use full absolute paths like <code>/content/documents/shop-content</code></p>

                                <div ng-repeat="(idx, path) in contentPaths">
                                    <div class="spaced clearfix">
                                        <input type="text" class="form-control" ng-model="path.value">
                                        <button class="btn btn-xs right right--space btn-flat--top" ng-click="removeFrom(contentPaths, idx)"><i class="glyphicon glyphicon-minus"></i> Remove</button>
                                    </div>
                                </div>

                                <div ng-show="contentPaths.length === 0" class="alert alert-warning">
                                    This package definition currently has no content paths setup.
                                </div>

                                <button class="btn btn-default btn-sm" ng-click="addTo(contentPaths)"><i class="glyphicon glyphicon-plus"></i> Add</button>
                            </div>

                            <!--
                                CND Identifiers
                            -->
                            <div class="form-group">
                                <label>CND identifiers</label>
                                <p class="help-block">You can use the following notation <code>mynamespace:</code> to capture a complete namespace</p>


                                <div ng-repeat="(idx, cndId) in cndIdentifiers">
                                    <div class="spaced clearfix">
                                        <input type="text" class="form-control" ng-model="cndId.value">
                                        <button class="btn btn-xs right right--space btn-flat--top" ng-click="removeFrom(cndIdentifiers, idx)"><i class="glyphicon glyphicon-minus"></i> Remove</button>
                                    </div>
                                </div>

                                <div ng-show="cndIdentifiers.length === 0" class="alert alert-warning">
                                    This package definition currently captures no CND identifiers.
                                </div>

                                <button class="btn btn-default btn-sm" ng-click="addTo(cndIdentifiers)"><i class="glyphicon glyphicon-plus"></i> Add</button>
                            </div>

                            <!--
                                    Required CNDs 
                            -->
                            <div class="form-group">
                                <label>Required CNDs</label>
                                <p class="help-block">Packages may rely on certain CND elements to be available</p>

                                <div ng-repeat="(idx, cndId) in requiredCnds">
                                    <div class="spaced clearfix">
                                        <input type="text" class="form-control" ng-model="cndId.value">
                                        <button class="btn btn-xs right right--space btn-flat--top" ng-click="removeFrom(requiredCnds, idx)"><i class="glyphicon glyphicon-minus"></i> Remove</button>
                                    </div>
                                </div>

                                <div ng-show="requiredCnds.length === 0" class="alert alert-warning">
                                    This package definition currently has no dependent CNDs.
                                </div>


                                <button class="btn btn-default btn-sm" ng-click="addTo(requiredCnds)"><i class="glyphicon glyphicon-plus"></i> Add</button>
                            </div>

                            <!-- submission options -->
                            <div class="ButtonBar ButtonBar--bottom">

                                <button ng-show="!isEditing()" class="right btn btn-primary" ng-click="createPackage()">Create</button>
                                <button ng-show="isEditing()" class="right btn btn-primary" ng-click="updatePackage()">Update</button>
                                <button class="btn btn-link" ng-click="back()">Cancel</button>

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
        <script src="js/packageWizardController.js"></script>


    </body>
</html>