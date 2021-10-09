if [ $# -ne 1 ]; then
	exit 0
fi

i=1
while [ "$i" -le $1 ]; do
	tput cuu1 #Move the cursor up 1 line
	tput el	#Clear from the cursor to the end of the line
	i=$(( i + 1 ))
done
