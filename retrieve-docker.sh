if [[ "$1" != "" ]]
then
  `dirname $0`/retrieve2.sh $1 A01
else
  TODAY=`date "+%Y-%m-%d"`
  `dirname $0`/retrieve2.sh `date "+%Y"`-`date "+%m"`-`date "+%d"`  A01
fi
