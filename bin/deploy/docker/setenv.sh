REP_OPTS="-Dcfg.mysql.host=${MYSQL_HOST} -Dcfg.mysql.database=${MYSQL_DATABASE} -Dcfg.mysql.port=${MYSQL_PORT} "
REP_OPTS="${REP_OPTS} -Dcfg.mysql.username=${MYSQL_USERNAME} -Dcfg.mysql.password=${MYSQL_PASSWORD} -Dcfg.xin.cmsHost=${BRXM_CMS_HOST} "
REP_OPTS="${REP_OPTS} -Drepo.upgrade=false -Drepo.config=file:${CATALINA_BASE}/conf/repository.xml -Drepo.path=/var/lib/hippostorage -Dproject.basedir=/tmp "
REP_OPTS="${REP_OPTS} -Dorg.apache.jackrabbit.core.cluster.node_id=\`hostname -f\`"
L4J_OPTS="-Dlog4j.configurationFile=file:${CATALINA_BASE}/conf/log4j2.xml -Dlog4j2.formatMsgNoLookups=true"
JVM_OPTS="-server -Xmx1024m -Xms128m"

SCALR_OPTS="-Dimgscalr.async.threadCount=2 -Dasset.concurrent.max=20 -Djava.awt.headless=true"

CATALINA_OPTS="${JVM_OPTS} ${REP_OPTS} ${L4J_OPTS} ${REMOTE_OPTS} ${SCALR_OPTS}"

if [ -d "/usr/lib/jvm/jre-1.8.0" ]; then
	JAVA_HOME="/usr/lib/jvm/jre-1.8.0"
fi
