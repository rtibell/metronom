#!/bin/bash
#
# Run
#
java -cp target/classes:/opt/pi4j/lib/* -Dpi4j.linking=dynamic com.tibell.metronom.MetronomTimer
#java -cp target/classes:/opt/pi4j/lib/* com.tibell.metronom.MetronomTimer
