blue_underlined_bold_echo()
{
	printf '\033[34m\033[4m\033[1m%s\033[0m\n' "$@"
}

blue_bold_echo()
{
	printf '\033[34m\033[1m%s\033[0m\n' "$@"
}

red_bold_echo()
{
	printf '\033[31m\033[1m%s\033[0m\n' "$@"
}
