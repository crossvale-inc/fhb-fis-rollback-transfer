# PLEASE, SET PROJECT NAME IN THIS VARIABLE AND FOLLOW JOLT NAMING CONVENTION:
PROJECT=fhb-fis-o-rollback-transfer

# PROJECT CONFIGMAP:
# 1. Project configMap stored in src/k8s
# 2. Maming convetion: configmap-<projectname>.yml
# JOLT TRANSFORMATIONS:
# 3. Create a folter jolt in src/k8s to store jolt files
# 4. Naming convenition: <vendor-operation>.<request|response>.spec.json

echo "=====>> INITIALIZING SCRIPT: CONFIGMAPS FOR PROJECT $PROJECT"
DIR=$(echo "$(pwd)${0:1}" | awk -F"/" '{OFS="/"; $NF=""; print $0}' | sed 's/.$//')
echo "=====>> PROJECT CONFIGMAP IN $DIR"
find $DIR -name "*yml"
echo "=====>> CREATING CONFIGMAP FOR $PROJECT"
echo "oc replace --force -f $DIR/configmap-$PROJECT.yml"
oc replace --force -f $DIR/configmap-$PROJECT.yml
#echo "=====>> LOOKING FOR JOLT FILES IN PATH $DIR/jolt"
#find $DIR/jolt -name "*json"
#echo "=====>> CREATING JOLT CONFIGMAP $PROJECT-jolt USING DIRECTORY $DIR/jolt"
#echo "oc create configmap $PROJECT-jolt --from-file=$DIR/jolt/ -o yaml --dry-run | oc apply -f - && oc label --overwrite configmap $PROJECT-jolt app=$PROJECT"
#oc create configmap $PROJECT-jolt --from-file=$DIR/jolt/ -o yaml --dry-run | oc apply -f - && oc label --overwrite configmap $PROJECT-jolt app=$PROJECT
echo "=====>> SCRIPT FINISHED"