IFS=$'\n' read -d '' -r -a lines < ips
printf "line 1: %s\n" "${lines[0]}"
printf "line 5: %s\n" "${lines[1]}"

# all lines
echo "${lines[@]}"
