<!doctype>
<html>

    <head>

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="libs/bootstrap.min.css" type="text/css">
        <link rel="stylesheet" href="libs/bootstrap-theme.min.css" type="text/css">


        <link href="css/styles.css" type="text/css" rel="stylesheet">

        <title>Packages | XIN Solutions</title>

    </head>
    <body ng-app="app">

        <%@include file="dynamic.jsp" %>

        <div ng-controller="ListController" data-loading="{{1}}">
            <div class="container">
                <div class="row">

                    <div class="
                            col-lg-offset-1 col-md-offset-1 col-sm-offset-1
                            col-lg-10 col-md-10 col-sm-10 col-xs-12
                    ">
                        <h1 class="Title">
                            <i class="Title__icon glyphicon glyphicon-folder-open"></i>
                            Packages
                        </h1>

                        <div class="ButtonBar">

                            <button class="btn btn-primary" ng-click="ingestPackage()">
                                <i class="glyphicon glyphicon-upload"><!-- --></i>
                                Ingest package
                            </button>
                            <button class="btn btn-default" ng-click="createPackage()">
                                <i class="glyphicon glyphicon-plus"><!-- --></i>
                                New package
                            </button>

                            <button class="btn btn-default" ng-click="refresh()">
                                <i class="glyphicon glyphicon-refresh"><!-- --></i>
                                Refresh
                            </button>
                        </div>

                        <div ng-show="flashText()">
                            <div class="alert alert-{{flashType()}}" role="alert">
                                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                {{flashText()}}
                            </div>
                        </div>

                        <div ng-show="noPackages()" class="NoPackages">
                            No packages available in the repository
                        </div>

                        <div ng-repeat="pkg in packages">
                            <div class="Package panel panel-default">
                                <div class="Package__title panel-heading" ng-click="collapse(pkg)">
                                    <div class="right">
                                        <i ng-show="pkg.collapsed" class="glyphicon glyphicon-plus"></i>
                                        <i ng-hide="pkg.collapsed" class="glyphicon glyphicon-minus"></i>
                                    </div>

                                    Package: {{pkg.id}}
                                </div>
                                <div ng-hide="pkg.collapsed" class="Package__options panel-body">
                                    <div class="right">
                                        <button class="btn btn-default" ng-click="deletePackage(pkg.id)">
                                            <i class="glyphicon glyphicon-remove"></i> Delete
                                        </button>
                                    </div>
                                    <button class="btn btn-default" ng-click="editPackage(pkg.id)">
                                        <i class="glyphicon glyphicon-pencil"></i>&nbsp;&nbsp;Edit
                                    </button>
                                    <button class="btn btn-default" ng-click="downloadPackage(pkg.id)">
                                        <i class="glyphicon glyphicon-refresh"></i>&nbsp;&nbsp;Download
                                    </button>
                                    <button class="btn btn-default" ng-click="clonePackage(pkg.id)">
                                        <i class="glyphicon glyphicon-share"></i>&nbsp;&nbsp;Clone
                                    </button>
                                </div>
                                <div ng-hide="pkg.collapsed" class="Package__body panel-body">

                                    <div ng-show="pkg.modified" class="alert alert-warning Package__modified" role="alert">
                                        The package was modified.
                                    </div>


                                    <div ng-show="pkg.filters.length > 0">
                                        <h3>Filters</h3>
                                        <div class="well">
                                            <ul ng-repeat="filter in pkg.filters">
                                                <li><code>{{filter}}</code></li>
                                            </ul>
                                        </div>
                                    </div>

                                    <div ng-show="pkg.cnds.length > 0">
                                        <h3>CND entities</h3>
                                        <div class="well">
                                            <ul ng-repeat="cnd in pkg.cnds">
                                                <li><code>{{cnd}}</code></li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <!-- include external libraries -->
        <script src="libs/angular.min.js"></script>
        <script src="libs/jquery.min.js"></script>
        <script src="libs/bootstrap.min.js"></script>
        <script src="libs/underscore-min.js"></script>
        <script src="libs/moment.min.js"></script>

        <!-- package manger -->
        <script src="js/app.js"></script>
        <script src="js/endpoint.js"></script>
        <script src="js/listController.js"></script>

    </body>
</html>