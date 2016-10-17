#!/bin/bash
seekdir=$1
upload_dir=./dest/uploads
TEXT=./dest/text
STIL=./dest/stil
function build_file
{
    first=1
    if [ ! -z "$2" ]; then
        sed -f $2 $1 > temp.out
    else
        cp $1 temp.out
    fi
    while read line
    do
        if [ $first == 1 ]; then
            first=2
            str="$str$line"
        else
            str="$str"\\n"$line"
        fi
        line=""
    done < temp.out
    if [ ! -z "$line" ]; then
        str="$str"\\n"$line"
    fi
#   rm temp.out
    echo $str
}
function wrap
{
    if [ -z "$4" ] && [ -z "$5" ]; then
        echo "db.$1.insert({docid:\"$2\",body:\"$3\"});"
    elif [ -z "$5" ]; then
        echo "db.$1.insert({docid:\"$2\",body:\"$3\",format:\"$4\"});"
    else
        echo "db.$1.insert({docid:\"$2\",body:\"$3\",format:\"$4\",style:\"$5\"});"
    fi
}
function make_upload
{
    file=$1
    script=$2
    db=$3
    content=`build_file "$1" $script`
    suffix=""
    if [[ $1 =~ .*corcode\-pages.* ]]; then
        suffix=/pages
    elif [[ $1 =~ .*corcode\-default.* ]]; then
        suffix=/default
    fi
    b=${file#./dest/*/*}
    docid="english/harpur/letters/"${b%%.*}$suffix
    fmt=TEXT
    if [ ! -z $suffix ]; then
        fmt=STIL
    fi
    echo "db.$db.remove({docid:\"$docid\"});">>upload.js
    echo `wrap $db "$docid" "$content" $fmt english/harpur/letters`>>upload.js
}
function build_folder {
    for i in "$1"/*;do
        if [[ ( -z $seekdir ) || ( $i =~ .*$seekdir.* ) ]]; then
            if [ -d "$i" ];then
                build_folder "$i" $2 $3
            elif [ -f "$i" ]; then
                make_upload "$i" $2 $3
            fi
        fi
    done
}
if [ -f upload.js ]; then
    rm upload.js
    touch upload.js
fi
build_folder $TEXT text.sed cortex
build_folder $STIL code.sed corcode
if [ -z $seekdir ]; then
    # build dialect file and add to upload.js
    dialect=`build_file dialect-letters.json code.sed`
    echo db.dialects.remove\({docid:\"english/harpur/letters\"}\)\;>>upload.js
    echo `wrap dialects english/harpur/letters "$dialect"`>>upload.js
    corform=`build_file letter.css code.sed`
    echo db.corform.remove\({docid:\"english/harpur/letters\"}\)\;>>upload.js
    echo `wrap corform english/harpur/letters $corform text/css`>>upload.js
fi


