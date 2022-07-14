#!/usr/bin/env sh

# if given folder is empty throws error
if [ -d "$1" ]; then
  if [ $(ls -A $1 | wc -l) ];
  then
    echo ""
  else
    exit 1
  fi
else
  exit 1
fi
