REP_OPTS="-Drepo.upgrade=false -Drepo.config=file:\${CATALINA_BASE}/conf/repository.xml -Drepo.path=/var/lib/hippostorage -Dproject.basedir=/tmp "
REP_OPTS="\${REP_OPTS} -Dadmin.password=${cfg.xin.adminPass} -Djwks.url=${cfg.xin.jwksUrl} -Djwt.repo.user=${cfg.xin.jwtRepoUser} -Djwt.repo.password=${cfg.xin.jwtRepoPassword}"
REP_OPTS="\${REP_OPTS} -Dorg.apache.jackrabbit.core.cluster.node_id=\`hostname -f\`"
L4J_OPTS="-Dlog4j.configurationFile=file:\${CATALINA_BASE}/conf/log4j2.xml"
JVM_OPTS="-server -Xmx386m -Xms128m"

#REMOTE_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"

CATALINA_OPTS="\${JVM_OPTS} \${REP_OPTS} \${L4J_OPTS} \${REMOTE_OPTS}"
JAVA_HOME="/usr/lib/jvm/jre-1.8.0"
