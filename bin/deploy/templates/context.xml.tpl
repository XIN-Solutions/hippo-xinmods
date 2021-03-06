<?xml version='1.0' encoding='utf-8'?>
<Context>
    <!-- Disable session persistence across Tomcat restarts -->
    <Manager pathname='' />

	<Resource
		name='jdbc/repositoryDS' auth='Container' type='javax.sql.DataSource'
		maxTotal='20' maxIdle='10' initialSize='2' maxWaitMillis='10000'
		testWhileIdle='true' testOnBorrow='false' validationQuery='SELECT 1'
		timeBetweenEvictionRunsMillis='10000'
		minEvictableIdleTimeMillis='60000'
		username='${cfg.mysql.username}' password='${cfg.mysql.password}'
		driverClassName='com.mysql.cj.jdbc.Driver'
		url='jdbc:mysql://${cfg.mysql.host}:${cfg.mysql.port}/${cfg.mysql.database}?characterEncoding=utf8&amp;useSSL=false&amp;nullDatabaseMeansCurrent=true'/>

	<Parameter name='apiUrl' value='${cfg.xin.cmsHost}/api' />
	<Parameter name='xinApiUrl' value='${cfg.xin.cmsHost}/api/xin' />

</Context>