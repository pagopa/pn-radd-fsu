#!/bin/bash

# Useful links:
# - https://pagopa.atlassian.net/wiki/spaces/PN/pages/503972476/Ambienti+di+TEST#Utenti-di-test-Helpdesk
# - https://github.com/pagopa/pn-radd-alt/tree/develop/scripts/registryMassiveInsert

# Set manually positional script parameters
set -- "<env>" "<user>" "<clientid>" "/path/to/pn-configuration" # <taxid1.csv,taxid2.csv,...,taxidN.csv>

ENV=$1
USER=$2
CLIENTID=$3
PN_CONF_PATH=$4
CSV_FILES=$5 # Comment this variable means *.csv available in $CSV_PATH

# --------------------------

if [ $# -lt 4 ] || [ $# -gt 5 ] || [ $1 == "-h" ] || [ $1 == "--help" ]; then
    echo -e "\n    Usage: $0 <env> <user> <clientid> <pn-configuration path> [<csv1,...,csvN>]"
    echo -e "\n    Note: If no csv list is provided, then all of them will be loaded"
    echo -e "\n    Exit 0\n"
    exit 0
fi

CSV_PATH="${PN_CONF_PATH}/${ENV}/_conf/core/app_config/pn-radd-alt"


if [ $# -eq 4 ]; then
    echo -e "\nThis command will upload all csv available into the $CSV_PATH folder:"
    CSV_PATH_2=$(echo $CSV_PATH | sed -e 's|\/|\\\/|g')
    CSV_LIST=$(ls -1 ${CSV_PATH}/*.csv | sed -e "s/${CSV_PATH_2}\///g")
    echo -e "\n${CSV_LIST}\n"
    echo -e "Do you agree?\n"
    while [ "$ANSW" != "y" ] && [ "$ANSW" != "n" ]; do
        read -p "[y/n]: " ANSW
        if [ "$ANSW" == "n" ]; then
            exit 0
        fi
    done
else
    CSV_LIST=$(for i in $(echo $CSV_FILES | sed -r 's/,/ /g'); do echo $i; done)
    echo -e "\nChecking if provided files exists in $CSV_PATH folder...\n"
    for i in $CSV_LIST; do
        if [ ! -f ${CSV_PATH}/$i ]; then
            echo -e " - ${i}: Not Available -> Exit.\n"
            exit 0
        else
            echo -e " - ${i}: Available"
        fi
    done
    echo -e "\nThis command will upload the following csv files:"
    echo -e "\n$CSV_LIST\n"
    echo -e "Do you agree?\n"
    while [ "$ANSW" != "y" ] && [ "$ANSW" != "n" ]; do
        read -p "[y/n]: " ANSW
        if [ "$ANSW" == "n" ]; then
            exit 0
        fi
    done
fi

export AWS_PROFILE=sso_pn-core-${ENV}
aws sso login --profile $AWS_PROFILE

# Reading password
echo ""
read -p "Insert Password: " PW
echo ""

RESULTS_NAME=${ENV}_$(date +%Y%m%d_%H%M%S)_radd
OUTPUT_FOlDER=${RESULTS_NAME}_results
OUTPUT_SCRIPT=./${OUTPUT_FOlDER}/${RESULTS_NAME}_output.txt
echo -e "\nGenerating ${OUTPUT_FOlDER} folder..."
mkdir ${OUTPUT_FOlDER}

for CSV_FILE in $CSV_LIST
do
    TAX_ID=$(echo $CSV_FILE | grep -oP '\d+(?=\.csv$)')
    echo -e "\n - Uploading ${TAX_ID}.csv file..."
    node index.js $ENV $USER $PW $CLIENTID ${CSV_PATH}/${CSV_FILE} >> ${OUTPUT_SCRIPT}
    echo "   Return code: $?."
    mv report-${TAX_ID}-*.csv ${OUTPUT_FOlDER}
done

echo -e "\nReports and script output available into ./${OUTPUT_FOlDER} folder."

echo -e "\nCreating a .tar archive containing all generated reports..."
cd ${OUTPUT_FOlDER}
tar -cf ${OUTPUT_FOlDER}.tar *.csv

echo -e "\nDone.\n"
