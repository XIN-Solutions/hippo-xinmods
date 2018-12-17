STORAGE_PATH="storage"
if [ ! -z "$1" ]; then
	STORAGE_PATH="$1"
fi

mvn -DskipTests=true clean verify && mvn -Pcargo.run -Drepo.path=$STORAGE_PATH
