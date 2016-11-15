#!/bin/bash
cat copyright.txt $1 > $1.tmp
mv $1.tmp $1
