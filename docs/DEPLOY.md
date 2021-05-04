# Prerequisites

Make sure to install the official Oracle Java JDK 1.8 and have your alternatives point at the java executables.

# Elastic Beanstalk

In the root of the project, run `./bin/deploy/build-eb.sh <configFile>` with a reference to a configuration file
of the shape below. This will prepare tomcat settings, repository xmls etc and zips it into an EB-ready ZIP artifact
that can be uploaded into your AWS account. 

Nginx will be configured as well to route traffic properly.  

    {
        "distributionName" : "",
    
        "eb" : {
            "app" : "appname",
            "env" : "envname",
            "region" : "ap-southeast-2"
        },
    
        "mysql": {
            "host" : "mysql server (rds) url",
            "port": 3306,
            "database" : "dbname",
            "username" : "username",
            "password": "password"
        },
    
        "xin" : {
            "cmsHost": "base url for cms"
        }
    
    }

# Create hippo distribution

Checkout the tag you wish to build a distribution for:

	$ mvn -Pdist,without-content verify

The `target/*tar.gz` file contains the distributed version of the hippo instance, complete with shared libs and common libs

**!!** Make sure `common/lib/jcl-over-slf4j-1.7.25.jar` does not exist.

To install new releases you no longer need all of the distribution, you would unpack only the `shared`, `common` and `webapps` folders like so:

	$ tar xvzf dist-version.tar.gz shared common webapps

# Tomcat Setup

To setup tomcat:

* grab a bundle from the tomcat website
* install it locally by unpacking it
* unzip the hippo distribution
* create bin/setenv.sh

        JAVA_HOME="/opt/jdk1.8.0_161"
        REP_OPTS="-Drepo.upgrade=false -Drepo.config=file:${CATALINA_BASE}/conf/repository.xml -Drepo.path=./storage"
        L4J_OPTS="-Dlog4j.configurationFile=file:${CATALINA_BASE}/conf/log4j2.xml"
        JVM_OPTS="-server -Xmx512m -Xms128m"
        CATALINA_OPTS="${JVM_OPTS} ${REP_OPTS} ${L4J_OPTS}"

* in the setenv.sh you could also specify an additional flag called `-DAWS_HIPPOBUS_ARN` of which its value would be
the SNS topic Hippo CMS events are sent to.

* setup conf/tomcat-users.xml 

        <?xml version="1.0" encoding="UTF-8"?>
        <tomcat-users xmlns="http://tomcat.apache.org/xml"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
                      version="1.0">
    
          <role rolename="tomcat" />
          <role rolename="manager-gui" />
          <role rolename="admin-gui" />
    
          <user username="tomcat" password="<password>" roles="tomcat,manager-gui,admin-gui"/>
        </tomcat-users>

* setup conf/catalina.properties
    * replace value for `common.loader` with the one below:

	        common.loader="${catalina.base}/common/lib/*.jar", "${catalina.base}/lib","${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar"

    * create `shared.loader` property with these values:

        	shared.loader="${catalina.base}/shared/lib/*.jar"

* make sure Tomcat only binds to localhost. Go into `tomcat/conf/server.xml` and change the Connector to read:

        <Connector port="8080" protocol="HTTP/1.1"
                   address="127.0.0.1"
                   connectionTimeout="20000"
                   redirectPort="8443" />



# MySQL setup:

	$ apt-get install mysql-server

Login as MySQL 

	$ mysql -u root -p

Create xinmods database
 
	$ drop database if exists xinmods;
	$ create database xinmods default charset 'utf8';

Grant all access to xinmods database to hippodb

	$ grant all privileges on xinmods.* to 'hippodb'@'localhost' identified by 'password_here'


Add resource definition in `tomcat/conf/context.xml`

	<Resource
		name="jdbc/repositoryDS" auth="Container" type="javax.sql.DataSource"
		maxTotal="20" maxIdle="10" initialSize="2" maxWaitMillis="10000"
		testWhileIdle="true" testOnBorrow="false" validationQuery="SELECT 1"
		timeBetweenEvictionRunsMillis="10000"
		minEvictableIdleTimeMillis="60000"
		username="<USERNAME>" password="<PASSWORD>"
		driverClassName="com.mysql.cj.jdbc.Driver"
		url="jdbc:mysql://localhost:3306/<DATABASE>?characterEncoding=utf8&amp;useSSL=false"/>


Create repository.xml at `tomcat/conf/repository.xml`:

	<?xml version="1.0" encoding="UTF-8"?>
	 
	<!DOCTYPE Repository
	          PUBLIC "-//The Apache Software Foundation//DTD Jackrabbit 2.6//EN"
	          "http://jackrabbit.apache.org/dtd/repository-2.6.dtd">
	 
	<Repository>
	 
	  <DataSources>
	    <DataSource name="repositoryDS">
	      <param name="driver" value="javax.naming.InitialContext"/>
	      <param name="url" value="java:comp/env/jdbc/repositoryDS"/>
	      <param name="databaseType" value="mysql"/>
	    </DataSource>
	  </DataSources>
	 
	  <FileSystem class="org.apache.jackrabbit.core.fs.db.DbFileSystem">
	    <param name="dataSourceName" value="repositoryDS"/>
	    <param name="schemaObjectPrefix" value="repository_"/>
	  </FileSystem>
	 
	  <Security appName="Jackrabbit">
	    <SecurityManager class="org.hippoecm.repository.security.SecurityManager"/>
	    <AccessManager class="org.hippoecm.repository.security.HippoAccessManager"/>
	    <LoginModule class="org.hippoecm.repository.security.HippoLoginModule"/>
	  </Security>
	 
	  <Workspaces rootPath="${rep.home}/workspaces" defaultWorkspace="default"/>
	 
	  <Workspace name="${wsp.name}">
	    <FileSystem class="org.apache.jackrabbit.core.fs.db.DbFileSystem">
	      <param name="dataSourceName" value="repositoryDS"/>
	      <param name="schemaObjectPrefix" value="${wsp.name}_"/>
	    </FileSystem>
	 
	    <PersistenceManager class="org.apache.jackrabbit.core.persistence.pool.MySqlPersistenceManager">
	      <param name="dataSourceName" value="repositoryDS"/>
	      <param name="schemaObjectPrefix" value="${wsp.name}_"/>
	      <param name="externalBLOBs" value="true"/>
	      <param name="consistencyCheck" value="false"/>
	      <param name="consistencyFix" value="false"/>
	      <param name="bundleCacheSize" value="64"/>
	    </PersistenceManager>
	 
	    <SearchIndex class="org.hippoecm.repository.FacetedNavigationEngineImpl">
	      <param name="indexingConfiguration" value="indexing_configuration.xml"/>
	      <param name="indexingConfigurationClass" value="org.hippoecm.repository.query.lucene.ServicingIndexingConfigurationImpl"/>
	      <param name="path" value="${wsp.home}/index"/>
	      <param name="useSimpleFSDirectory" value="true"/>
	      <param name="useCompoundFile" value="true"/>
	      <param name="minMergeDocs" value="100"/>
	      <param name="volatileIdleTime" value="10"/>
	      <param name="maxMergeDocs" value="100000"/>
	      <param name="mergeFactor" value="5"/>
	      <param name="maxFieldLength" value="10000"/>
	      <param name="bufferSize" value="1000"/>
	      <param name="cacheSize" value="1000"/>
	      <param name="onWorkspaceInconsistency" value="log"/>
	      <param name="forceConsistencyCheck" value="false"/>
	      <param name="enableConsistencyCheck" value="false"/>
	      <param name="autoRepair" value="true"/>
	      <param name="analyzer" value="org.hippoecm.repository.query.lucene.StandardHippoAnalyzer"/>
	      <param name="queryClass" value="org.apache.jackrabbit.core.query.QueryImpl"/>
	      <param name="respectDocumentOrder" value="false"/>
	      <param name="resultFetchSize" value="1000"/>
	      <param name="extractorTimeout" value="100"/>
	      <param name="extractorBackLogSize" value="100"/>
	      <param name="excerptProviderClass" value="org.apache.jackrabbit.core.query.lucene.DefaultHTMLExcerpt"/>
	      <param name="supportSimilarityOnStrings" value="true"/>
	      <param name="supportSimilarityOnBinaries" value="false"/>
	    </SearchIndex>
	 
	    <ISMLocking class="org.apache.jackrabbit.core.state.FineGrainedISMLocking"/>
	  </Workspace>
	 
	  <Versioning rootPath="${rep.home}/version">
	    <FileSystem class="org.apache.jackrabbit.core.fs.db.DbFileSystem">
	      <param name="dataSourceName" value="repositoryDS"/>
	      <param name="schemaObjectPrefix" value="version_"/>
	    </FileSystem>
	 
	    <PersistenceManager class="org.apache.jackrabbit.core.persistence.pool.MySqlPersistenceManager">
	      <param name="dataSourceName" value="repositoryDS"/>
	      <param name="schemaObjectPrefix" value="version_"/>
	      <param name="externalBLOBs" value="true"/>
	      <param name="consistencyCheck" value="false"/>
	      <param name="consistencyFix" value="false"/>
	    </PersistenceManager>
	 
	    <ISMLocking class="org.apache.jackrabbit.core.state.FineGrainedISMLocking"/>
	  </Versioning>
	 
	  <Cluster>
	    <Journal class="org.apache.jackrabbit.core.journal.DatabaseJournal">
	      <param name="dataSourceName" value="repositoryDS"/>
	      <param name="databaseType" value="mysql"/>
	      <param name="schemaObjectPrefix" value="repository_"/>
	      <param name="revision" value="${rep.home}/revision.log"/>
	    </Journal>
	  </Cluster>
	 
	  <DataStore class="org.apache.jackrabbit.core.data.db.DbDataStore">
	    <param name="dataSourceName" value="repositoryDS"/>
	    <param name="minRecordLength" value="1024"/>
	    <param name="maxConnections" value="5"/>
	    <param name="copyWhenReading" value="true"/>
	  </DataStore>
	 
	</Repository>


## References:

* https://www.a2hosting.com/kb/developer-corner/mysql/managing-mysql-databases-and-users-from-the-command-line
* https://www.onehippo.org/library/deployment/configuring/configuring-hippo-for-mysql.html 


# Setup Apache Virtualhost

Install apache:
    
    $ sudo apt-get install apache2
    $ sudo a2enmod proxy_http
    $ sudo a2enmod headers
    $ sudo a2enmod substitute
    $ sudo a2enmod rewrite

Create new Virtualhost file. It contains three definitions:

* http://cms.hippo.local/
* http://cms.hippo.local/packages/
* http://api.hippo.local/api/xin (goes to custom-api/)
* http://api.hippo.local/api/* (goes to api/)


Make sure to adjust the hostnames to your taste. It assumes your tomcat is running on port 8080.

Create `/etc/apache2/sites-available/hippo.conf`:
    
    #
    #       CMS definition
    #
    <VirtualHost *:80>
    
        ServerName cms.hippo.local
    
        <Location />
            Order deny,allow
            Allow from all
        </Location>
    
        ProxyPreserveHost Off 
    
        # /packages should redirect to the package manager
        ProxyPass /packages/ http://127.0.0.1:8080/site/packages/
        ProxyPassReverse /packages/ http://127.0.0.1:8080/site/packages/
        ProxyPassReverseCookiePath /site/packages /packages/
    
        # / should go to the CMS
        ProxyPass / http://127.0.0.1:8080/cms/
        ProxyPassReverse /cms/ http://127.0.0.1:8080/cms/
        ProxyPassReverseCookiePath /cms /
        
    </VirtualHost>
    
    #
    #       CaaS delivery virtualhost
    #
    <VirtualHost *:80>
    
        ServerName api.hippo.local
    
        <Location />
            Order deny,allow
            Deny from all
        </Location>
    
        <Location /api/>
            Order deny,allow
            Allow from all
        </Location>
    
        <Location /binaries/>
            Order deny,allow
            Allow from all
        </Location>
        
        <Location /assetmod/>
            Order deny,allow
            Allow from all
        </Location>

        #
        # -- substitute resulting links with correct domain
        #
        AddOutputFilterByType SUBSTITUTE application/json
        Substitute "s|http://localhost(:8080)?/site/|http://api.hippo.local/|i"

        #
        # -- preflight option checks dont send Authorization header causing
        # -- to fail JAAS Realm definition, returning 401. Sending 204 with OPTIONS
        # -- response here.
        #
        Header always set Access-Control-Allow-Origin "*"                                                                                          
        Header always set Access-Control-Allow-Credentials "true"                                                                                  
        Header always set Access-Control-Allow-Methods "POST, GET, OPTIONS, DELETE, PUT, HEAD"                                                     
        Header always set Access-Control-Max-Age "1000"                                                                                            
        Header always set Access-Control-Allow-Headers "x-requested-with, Content-Type, origin, authorization, accept, client-security-token"      
                                                                                                                                                   
        # -- Added a rewrite to respond with a 204 SUCCESS on every OPTIONS request
        # -- L flag causes breaking of processing chain
        RewriteEngine On
        RewriteCond %{REQUEST_METHOD} OPTIONS
        RewriteRule ^(.*)$ $1 [R=204,L]

            
        RequestHeader set Host "localhost"
        ProxyPreserveHost On
        ProxyPass /api/xin/ http://127.0.0.1:8080/site/custom-api/
        ProxyPass / http://127.0.0.1:8080/site/
        ProxyPassReverse /site/ http://127.0.0.1:8080/site/
        ProxyPassReverseCookiePath /site /
    
    </VirtualHost>


Make sure to replace the host names with proper host names.  Enable the virtual host by typing:

    $ a2ensite hippo


# Useful scripts

`new-instance.sh`:

This script deletes the current tomcat instance, drops the db and restarts it.

	#!/bin/bash

	rm logs/*
	rm storage -Rf
	mysql -u root -p -e "drop database if exists xinmods; create database xinmods default charset 'utf8'";
	> logs/catalina.out
	./bin/startup.sh

`stop.sh`:

Kills the tomcat instance very much:

	#!/bin/bash

	PID=$(ps aux | grep tomcat | head -n 1 | cut -d' ' -f4)

	echo "Killing $PID"
	kill -9 $PID


