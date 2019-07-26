#!/usr/bin/env tclsh

package require http

#
#	Ask Procedure
#
proc ask {question {defaultValue {}}} {
	puts -nonewline $question
	flush stdout
	set answer [gets stdin]

	if { $answer == {} } then {
		if { $defaultValue == {} } then {
			puts " .. Didn't answer question, aborting."
			exit;
		}
		return $defaultValue
	}
	return $answer 
}



#
# This function will try to retrieve a configuration value from the
# configuration file that was loaded
#
proc askIfNull {configLocation question {defaultValue {}} } {

  lassign $configLocation bundle property

  global $bundle

  foreach {key value} [expr \$$bundle] {
    if { $key == $property } then {
      return $value
    }
  } 

  return [ask "($bundle.$property) -- $question" $defaultValue]
}

#
#	Get a list of tomcats
#
proc get-tomcat-versions {} {
	set handle [::http::geturl "http://www-us.apache.org/dist/tomcat/tomcat-8/"]
	set body [::http::data $handle]

	set hrefs [regexp -inline -all {href="([^"]+)"} $body]

	lappend versions
	foreach {_ href} $hrefs {

		if { [string first {v} $href] == 0 } then {
			lappend versions [string range $href 1 end-1]
		}

	}

	return $versions
}

puts ">> Just need some information to put into the configuration files"


#
# Get the configuration file from the command line parameters
#
proc get-config-file {} {
  global argv
  return [lindex $argv 0]
}

#
# Returns whether or not a file was present.
#
proc has-config-file {} {
  return [expr { [get-config-file] != "" }]
}


if [has-config-file] then {
  source [get-config-file]
  puts ">> Using configuration: [get-config-file]"
} else {
  set Build {}
  set MySQL {}
  set CMS {}
  puts ">> Just need some information to put into the configuration files"
}


#   ___                  _   _                 
#  / _ \ _   _  ___  ___| |_(_) ___  _ __  ___ 
# | | | | | | |/ _ \/ __| __| |/ _ \| '_ \/ __|
# | |_| | |_| |  __/\__ \ |_| | (_) | | | \__ \
#  \__\_\\__,_|\___||___/\__|_|\___/|_| |_|___/
#

set distributionName [askIfNull {Build distributionName} "Distribution name: " generic]

set ebApplicationName [askIfNull {Build ebApplicationName} "EB Application Name: "]
set ebEnvironmentName [askIfNull {Build ebEnvironmentName} "EB Environment Name: "]
set ebRegion [askIfNull {Build ebRegion} "EB Region: " ap-southeast-2]

set mysqlHost [askIfNull {MySQL host} "MySQL Host: " host]
set mysqlPort [askIfNull {MySQL port} "MySQL Port (3306): " 3306]
set mysqlDb [askIfNull {MySQL database} "MySQL Database name: " dbname]
set mysqlUser [askIfNull {MySQL username} "MySQL Username: " username]
set mysqlPassword [askIfNull {MySQL password} "MySQL Password: " password]
set cmsHost [askIfNull {CMS host} "CMS Host: " http://cmslocal]
set adminPass [askIfNull {CMS adminPass} "Admin password: " admin]


set distBasePath "/tmp/hippodistbuild/app-distribution"

puts ">> PREP: Clean workspace"
exec rm /tmp/hippodistbuild -Rf
exec mkdir $distBasePath -p




#  _____                         _     ____                      _                 _ 
# |_   _|__  _ __ ___   ___ __ _| |_  |  _ \  _____      ___ __ | | ___   __ _  __| |
#   | |/ _ \| '_ ` _ \ / __/ _` | __| | | | |/ _ \ \ /\ / / '_ \| |/ _ \ / _` |/ _` |
#   | | (_) | | | | | | (_| (_| | |_  | |_| | (_) \ V  V /| | | | | (_) | (_| | (_| |
#   |_|\___/|_| |_| |_|\___\__,_|\__| |____/ \___/ \_/\_/ |_| |_|_|\___/ \__,_|\__,_|
#

set versions [get-tomcat-versions]
set lastVersion [lindex $versions end]
set tomcatUrl "http://www-us.apache.org/dist/tomcat/tomcat-8/v$lastVersion/bin/apache-tomcat-$lastVersion.tar.gz"
puts ">> PREP: Downloading Tomcat bundle: $tomcatUrl"
exec bash -c "cd $distBasePath && wget -q $tomcatUrl; true"
puts ">> PREP: Unpacking Tomcat bundle"
exec bash -c "cd $distBasePath && tar -xvzf apache-tomcat*.tar.gz --strip 1"
exec bash -c "rm $distBasePath/apache-tomcat*.tar.gz"
exec bash -c "rm $distBasePath/webapps/* -R"

#  ____        _ _     _ _             
# | __ ) _   _(_) | __| (_)_ __   __ _ 
# |  _ \| | | | | |/ _` | | '_ \ / _` |
# | |_) | |_| | | | (_| | | | | | (_| |
# |____/ \__,_|_|_|\__,_|_|_| |_|\__, |
#                                |___/ 
#

puts ">> BUILD: Cleaning Workspace"
exec mvn clean
puts ">> BUILD: Building Web Applications"
exec mvn verify
puts ">> BUILD: Building Distribution"
exec mvn -Pdist clean package

puts ">> BUILD: Moving distribution to temporary workspace folder"
exec bash -c "cp [pwd]/target/*-distribution.tar.gz $distBasePath"

puts ">> BUILD: Extracting Distribution and removing original"
exec bash -c "cd $distBasePath && tar xvzf *tar.gz"
exec bash -c "rm $distBasePath/*distribution.tar.gz"



#  __  __       ____   ___  _       ____                      _                 _ 
# |  \/  |_   _/ ___| / _ \| |     |  _ \  _____      ___ __ | | ___   __ _  __| |
# | |\/| | | | \___ \| | | | |     | | | |/ _ \ \ /\ / / '_ \| |/ _ \ / _` |/ _` |
# | |  | | |_| |___) | |_| | |___  | |_| | (_) \ V  V /| | | | | (_) | (_| | (_| |
# |_|  |_|\__, |____/ \__\_\_____| |____/ \___/ \_/\_/ |_| |_|_|\___/ \__,_|\__,_|
#         |___/                                                                   
#


# MYSQL Connector Download:
# http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar

set mysqlJar "http://central.maven.org/maven2/mysql/mysql-connector-java/8.0.13/mysql-connector-java-8.0.13.jar"

puts ">> BUILD: Adding MySQL Connector `$mysqlJar`"

exec bash -c "cd $distBasePath/common/lib && wget -q $mysqlJar; true"


#   ____ ___  _   _ _____ _______  _______ 
#  / ___/ _ \| \ | |_   _| ____\ \/ /_   _|
# | |  | | | |  \| | | | |  _|  \  /  | |  
# | |__| |_| | |\  | | | | |___ /  \  | |  
#  \____\___/|_| \_| |_| |_____/_/\_\ |_|  
#                                         

puts ">> CONFIG: Setting up `context.xml`"

set context [open "$distBasePath/conf/context.xml" w]

puts $context "<?xml version='1.0' encoding='utf-8'?>
<Context>
    <!-- Disable session persistence across Tomcat restarts -->
    <Manager pathname='' />

	<Resource
		name='jdbc/repositoryDS' auth='Container' type='javax.sql.DataSource'
		maxTotal='20' maxIdle='10' initialSize='2' maxWaitMillis='10000'
		testWhileIdle='true' testOnBorrow='false' validationQuery='SELECT 1'
		timeBetweenEvictionRunsMillis='10000'
		minEvictableIdleTimeMillis='60000'
		username='$mysqlUser' password='$mysqlPassword'
		driverClassName='com.mysql.cj.jdbc.Driver'
		url='jdbc:mysql://$mysqlHost:$mysqlPort/$mysqlDb?characterEncoding=utf8&amp;useSSL=false'/>

	<Parameter name='apiUrl' value='$cmsHost/api' />
	<Parameter name='xinApiUrl' value='$cmsHost/api/xin' />

</Context>
"

close $context



#  ____                      _ _                   
# |  _ \ ___ _ __   ___  ___(_) |_ ___  _ __ _   _ 
# | |_) / _ \ '_ \ / _ \/ __| | __/ _ \| '__| | | |
# |  _ <  __/ |_) | (_) \__ \ | || (_) | |  | |_| |
# |_| \_\___| .__/ \___/|___/_|\__\___/|_|   \__, |
#           |_|                              |___/ 
#

puts ">> CONFIG: Setting up `repository.xml`"

set repository [open "$distBasePath/conf/repository.xml" w]

puts $repository "<?xml version='1.0' encoding='UTF-8'?>
 
<!DOCTYPE Repository
          PUBLIC '-//The Apache Software Foundation//DTD Jackrabbit 2.6//EN'
          'http://jackrabbit.apache.org/dtd/repository-2.6.dtd'>
 
<Repository>
 
  <DataSources>
    <DataSource name='repositoryDS'>
      <param name='driver' value='javax.naming.InitialContext'/>
      <param name='url' value='java:comp/env/jdbc/repositoryDS'/>
      <param name='databaseType' value='mysql'/>
    </DataSource>
  </DataSources>
 
  <FileSystem class='org.apache.jackrabbit.core.fs.db.DbFileSystem'>
    <param name='dataSourceName' value='repositoryDS'/>
    <param name='schemaObjectPrefix' value='repository_'/>
  </FileSystem>
 
  <Security appName='Jackrabbit'>
    <SecurityManager class='org.hippoecm.repository.security.SecurityManager'/>
    <AccessManager class='org.hippoecm.repository.security.HippoAccessManager'/>
    <LoginModule class='org.hippoecm.repository.security.HippoLoginModule'/>
  </Security>
 
  <Workspaces rootPath='\${rep.home}/workspaces' defaultWorkspace='default'/>
 
  <Workspace name='\${wsp.name}'>
    <FileSystem class='org.apache.jackrabbit.core.fs.db.DbFileSystem'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='schemaObjectPrefix' value='\${wsp.name}_'/>
    </FileSystem>
 
    <PersistenceManager class='org.apache.jackrabbit.core.persistence.pool.MySqlPersistenceManager'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='schemaObjectPrefix' value='\${wsp.name}_'/>
      <param name='externalBLOBs' value='true'/>
      <param name='consistencyCheck' value='false'/>
      <param name='consistencyFix' value='false'/>
      <param name='bundleCacheSize' value='64'/>
    </PersistenceManager>
 
    <SearchIndex class='org.hippoecm.repository.FacetedNavigationEngineImpl'>
      <param name='indexingConfiguration' value='indexing_configuration.xml'/>
      <param name='indexingConfigurationClass' value='org.hippoecm.repository.query.lucene.ServicingIndexingConfigurationImpl'/>
      <param name='path' value='\${wsp.home}/index'/>
      <param name='useSimpleFSDirectory' value='true'/>
      <param name='useCompoundFile' value='true'/>
      <param name='minMergeDocs' value='100'/>
      <param name='volatileIdleTime' value='10'/>
      <param name='maxMergeDocs' value='100000'/>
      <param name='mergeFactor' value='5'/>
      <param name='maxFieldLength' value='10000'/>
      <param name='bufferSize' value='1000'/>
      <param name='cacheSize' value='1000'/>
      <param name='onWorkspaceInconsistency' value='log'/>
      <param name='forceConsistencyCheck' value='true'/>
      <param name='enableConsistencyCheck' value='true'/>
      <param name='autoRepair' value='true'/>
      <param name='analyzer' value='org.hippoecm.repository.query.lucene.StandardHippoAnalyzer'/>
      <param name='queryClass' value='org.apache.jackrabbit.core.query.QueryImpl'/>
      <param name='respectDocumentOrder' value='false'/>
      <param name='resultFetchSize' value='1000'/>
      <param name='extractorTimeout' value='100'/>
      <param name='extractorBackLogSize' value='100'/>
      <param name='excerptProviderClass' value='org.apache.jackrabbit.core.query.lucene.DefaultHTMLExcerpt'/>
      <param name='supportSimilarityOnStrings' value='true'/>
      <param name='supportSimilarityOnBinaries' value='false'/>
    </SearchIndex>
 
    <ISMLocking class='org.apache.jackrabbit.core.state.FineGrainedISMLocking'/>
  </Workspace>
 
  <Versioning rootPath='\${rep.home}/version'>
    <FileSystem class='org.apache.jackrabbit.core.fs.db.DbFileSystem'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='schemaObjectPrefix' value='version_'/>
    </FileSystem>
 
    <PersistenceManager class='org.apache.jackrabbit.core.persistence.pool.MySqlPersistenceManager'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='schemaObjectPrefix' value='version_'/>
      <param name='externalBLOBs' value='true'/>
      <param name='consistencyCheck' value='false'/>
      <param name='consistencyFix' value='false'/>
    </PersistenceManager>
 
    <ISMLocking class='org.apache.jackrabbit.core.state.FineGrainedISMLocking'/>
  </Versioning>
 
  <Cluster>
    <Journal class='org.apache.jackrabbit.core.journal.DatabaseJournal'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='databaseType' value='mysql'/>
      <param name='schemaObjectPrefix' value='repository_'/>
      <param name='revision' value='\${rep.home}/revision.log'/>
    </Journal>
  </Cluster>
 
  <DataStore class='org.apache.jackrabbit.core.data.db.DbDataStore'>
    <param name='dataSourceName' value='repositoryDS'/>
    <param name='minRecordLength' value='1024'/>
    <param name='maxConnections' value='5'/>
    <param name='copyWhenReading' value='true'/>
  </DataStore>
 
</Repository>
"

close $repository

#
#   ____      _        _ _               ____                            _   _           
#  / ___|__ _| |_ __ _| (_)_ __   __ _  |  _ \ _ __ ___  _ __   ___ _ __| |_(_) ___  ___ 
# | |   / _` | __/ _` | | | '_ \ / _` | | |_) | '__/ _ \| '_ \ / _ \ '__| __| |/ _ \/ __|
# | |__| (_| | || (_| | | | | | | (_| | |  __/| | | (_) | |_) |  __/ |  | |_| |  __/\__ \
#  \____\__,_|\__\__,_|_|_|_| |_|\__,_| |_|   |_|  \___/| .__/ \___|_|   \__|_|\___||___/
#                                                       |_|                              
#

puts ">> CONFIG: Setting up `catalina.properties`"

set catalina [open "$distBasePath/conf/catalina.properties" w]

puts $catalina {
package.access=sun.,org.apache.catalina.,org.apache.coyote.,org.apache.jasper.,org.apache.tomcat.
package.definition=sun.,java.,org.apache.catalina.,org.apache.coyote.,\
org.apache.jasper.,org.apache.naming.,org.apache.tomcat.
common.loader="${catalina.base}/common/lib/*.jar", "${catalina.base}/lib","${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar"
server.loader=
shared.loader="${catalina.base}/shared/lib/*.jar"

tomcat.util.scan.StandardJarScanFilter.jarsToSkip=\
annotations-api.jar,\
ant-junit*.jar,\
ant-launcher.jar,\
ant.jar,\
asm-*.jar,\
aspectj*.jar,\
bootstrap.jar,\
catalina-ant.jar,\
catalina-ha.jar,\
catalina-jmx-remote.jar,\
catalina-storeconfig.jar,\
catalina-tribes.jar,\
catalina-ws.jar,\
catalina.jar,\
cglib-*.jar,\
cobertura-*.jar,\
commons-beanutils*.jar,\
commons-codec*.jar,\
commons-collections*.jar,\
commons-daemon.jar,\
commons-dbcp*.jar,\
commons-digester*.jar,\
commons-fileupload*.jar,\
commons-httpclient*.jar,\
commons-io*.jar,\
commons-lang*.jar,\
commons-logging*.jar,\
commons-math*.jar,\
commons-pool*.jar,\
dom4j-*.jar,\
easymock-*.jar,\
ecj-*.jar,\
el-api.jar,\
geronimo-spec-jaxrpc*.jar,\
h2*.jar,\
hamcrest-*.jar,\
hibernate*.jar,\
httpclient*.jar,\
icu4j-*.jar,\
jasper-el.jar,\
jasper.jar,\
jaspic-api.jar,\
jaxb-*.jar,\
jaxen-*.jar,\
jdom-*.jar,\
jetty-*.jar,\
jmx-tools.jar,\
jmx.jar,\
jsp-api.jar,\
jstl.jar,\
jta*.jar,\
junit-*.jar,\
junit.jar,\
log4j*.jar,\
mail*.jar,\
objenesis-*.jar,\
oraclepki.jar,\
oro-*.jar,\
servlet-api-*.jar,\
servlet-api.jar,\
slf4j*.jar,\
taglibs-standard-spec-*.jar,\
tagsoup-*.jar,\
tomcat-api.jar,\
tomcat-coyote.jar,\
tomcat-dbcp.jar,\
tomcat-i18n-en.jar,\
tomcat-i18n-es.jar,\
tomcat-i18n-fr.jar,\
tomcat-i18n-ja.jar,\
tomcat-i18n-ru.jar,\
tomcat-jdbc.jar,\
tomcat-jni.jar,\
tomcat-juli-adapters.jar,\
tomcat-juli.jar,\
tomcat-util-scan.jar,\
tomcat-util.jar,\
tomcat-websocket.jar,\
tools.jar,\
websocket-api.jar,\
wsdl4j*.jar,\
xercesImpl.jar,\
xml-apis.jar,\
xmlParserAPIs-*.jar,\
xmlParserAPIs.jar,\
xom-*.jar

tomcat.util.scan.StandardJarScanFilter.jarsToScan=\
log4j-taglib*.jar,\
log4j-web*.jar,\
log4javascript*.jar,\
slf4j-taglib*.jar

# String cache configuration.
tomcat.util.buf.StringCache.byte.enabled=true
}


close $catalina


#  ____                         __  ____  __ _     
# / ___|  ___ _ ____   _____ _ _\ \/ /  \/  | |    
# \___ \ / _ \ '__\ \ / / _ \ '__\  /| |\/| | |    
#  ___) |  __/ |   \ V /  __/ |  /  \| |  | | |___ 
# |____/ \___|_|    \_/ \___|_| /_/\_\_|  |_|_____|
#

puts ">> CONFIG: Setting up `server.xml`"

set serverXml [open "$distBasePath/conf/server.xml" w]                                                 

puts $serverXml {<?xml version="1.0" encoding="UTF-8"?>
<Server port="8005" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>

    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">

	<!-- bind to localhost and set the upload max size to 512mb -->
    <Connector port="8080" protocol="HTTP/1.1"
			   address="127.0.0.1"
               connectionTimeout="20000"
               maxThreads="300" 
               enableLookups="false"
               compression="on"
               maxPostSize="536870912"
               redirectPort="8443" />

    <!-- Connector port="8009" protocol="AJP/1.3" redirectPort="8443" / -->


    <Engine name="Catalina" defaultHost="localhost">

      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">

        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log" suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

      </Host>
    </Engine>
  </Service>
</Server>


}

close $serverXml

#  ____       _   _____            
# / ___|  ___| |_| ____|_ ____   __
# \___ \ / _ \ __|  _| | '_ \ \ / /
#  ___) |  __/ |_| |___| | | \ V / 
# |____/ \___|\__|_____|_| |_|\_/  
#                                 

puts ">> CONFIG: Setting up `setenv.sh`"

set setEnvFile [open "$distBasePath/bin/setenv.sh" w]

puts $setEnvFile "
REP_OPTS=\"-Drepo.upgrade=false -Drepo.config=file:\${CATALINA_BASE}/conf/repository.xml -Drepo.path=/var/lib/hippostorage -Dproject.basedir=/tmp -Dadmin.password=$adminPass\"
L4J_OPTS=\"-Dlog4j.configurationFile=file:\${CATALINA_BASE}/conf/log4j2.xml\"
JVM_OPTS=\"-server -Xmx386m -Xms128m\"
#REMOTE_OPTS=\"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000\"

CATALINA_OPTS=\"\${JVM_OPTS} \${REP_OPTS} \${L4J_OPTS} \${REMOTE_OPTS}\"
"

close $setEnvFile


#  _____ ____     ____             __ _       
# | ____| __ )   / ___|___  _ __  / _(_) __ _ 
# |  _| |  _ \  | |   / _ \| '_ \| |_| |/ _` |
# | |___| |_) | | |__| (_) | | | |  _| | (_| |
# |_____|____/   \____\___/|_| |_|_| |_|\__, |
#                                       |___/ 
#


puts ">> EB: Extract base configuration"

exec bash -c "cp ./bin/ebconfig $distBasePath -rT"

puts ">> EB: Writing config.yml"

set configYml [open "$distBasePath/.elasticbeanstalk/config.yml" w]
puts $configYml "
deploy:
  artifact: /tmp/hippodistbuild/app-distribution/$distributionName.zip
branch-defaults:
  master:
    environment: $ebEnvironmentName
environment-defaults:
  $ebEnvironmentName:
    branch: null
    repository: null
global:
  application_name: $ebApplicationName
  default_ec2_keyname: null
  default_platform: arn:aws:elasticbeanstalk:ap-southeast-2::platform/Java 8 running
    on 64bit Amazon Linux/2.7.2
  default_region: $ebRegion
  include_git_submodules: true
  instance_profile: null
  platform_name: null
  platform_version: null
  sc: git
  workspace_type: Application
"

close $configYml


#     _             _     _           
#    / \   _ __ ___| |__ (_)_   _____ 
#   / _ \ | '__/ __| '_ \| \ \ / / _ \
#  / ___ \| | | (__| | | | |\ V /  __/
# /_/   \_\_|  \___|_| |_|_| \_/ \___|
#

puts ">> ARCHIVE: Archiving distribution"
exec bash -c "cd /tmp/hippodistbuild/app-distribution/ && zip ../$distributionName.zip . -r"

set deploy [askIfNull {Deployment deploy} "Deploy? (y/n)" y]

if { $deploy == "yes" || $deploy == "y" } then {

	set appVersion "v[clock format [clock seconds] -format "%Y-%m-%d"]_[clock seconds]"
	set appVersion [ask "EB App Version: (default: $appVersion)" $appVersion]
	set s3Bucket [ask "S3 Deploy Bucket? (xinmods)" xinmods]
	set s3Key "apps/artifacts/$distributionName-$appVersion.zip"
	set s3Key [ask "S3 Key? ($s3Key)" $s3Key]
	set zipfile "/tmp/hippodistbuild/$distributionName.zip"

	set awsRegion [ask "Application region: (default: ap-southeast-2)" ap-southeast-2]

	puts ">> DEPLOY: Uploading to 's3://$s3Bucket/$s3Key'"
	exec bash -c "aws s3 cp $zipfile s3://$s3Bucket/$s3Key"
	puts ">> DEPLOY: Creating a Version: $appVersion"
	exec bash -c "aws elasticbeanstalk create-application-version --auto-create-application --application-name $ebApplicationName --version-label $appVersion --source-bundle S3Bucket=\"$s3Bucket\",S3Key=\"$s3Key\" --region $awsRegion"
	puts ">> DEPLOY: Updating environment to new version"
	exec bash -c "aws elasticbeanstalk update-environment --environment-name $ebEnvironmentName --version-label $appVersion --region $awsRegion"
	puts ">> DEPLOY: Completed!"

}

puts "Distribution ZIP found here: /tmp/hippodistbuild/app-distribution/$distributionName"
