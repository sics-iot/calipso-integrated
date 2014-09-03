#!/bin/bash

gnuplot << EOF

set terminal png 
set output '$1.png'

set xlabel "$3"

set ylabel "$4"

plot "$2" with lines title ''

EOF