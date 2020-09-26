<?xml version='1.0' encoding='UTF-8'?>
 
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
      <param name='forceConsistencyCheck' value='false'/>
      <param name='enableConsistencyCheck' value='false'/>
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
      <param name="bundleCacheSize" value="64"/>
    </PersistenceManager>
 
    <ISMLocking class='org.apache.jackrabbit.core.state.FineGrainedISMLocking'/>
  </Versioning>
 
  <Cluster>
    <Journal class='org.apache.jackrabbit.core.journal.DatabaseJournal'>
      <param name='dataSourceName' value='repositoryDS'/>
      <param name='databaseType' value='mysql'/>
      <param name='schemaObjectPrefix' value='repository_'/>
      <param name='revision' value='\${rep.home}/revision.log'/>
    
      <!-- clean up journal table -->
      <param name="janitorEnabled" value="true"/>
      <param name="janitorSleep" value="86400"/> <!-- a day in seconds -->
      <param name="janitorFirstRunHourOfDay" value="3"/>
    </Journal>
  </Cluster>
 
  <DataStore class='org.apache.jackrabbit.core.data.db.DbDataStore'>
    <param name='dataSourceName' value='repositoryDS'/>
    <param name='minRecordLength' value='1024'/>
    <param name='maxConnections' value='5'/>
    <param name='copyWhenReading' value='true'/>
  </DataStore>
 
</Repository>