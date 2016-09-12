Description
===========

MMLStripper strips MML style markup from a plain text files into
a plain text substrate and a set of markup codes defined by:
a) the MML (minimal markup language) tags in the document
b) their definitions in a recipe (schema) file

Stripped text can be recombined to form HTML using an approporaite CSS
stylesheet and the formatter program q.v.

USE
===
To use, run the program, giving the root directory with the -d paramater. This will create a dest directory with two sub-directories: text adn stil. The stil directory contains the corcode (stil) default and pages standoff markup sets, and the text directory the plain text striupped from the mml files. The original directory structure is copied to each of these sub-directories to hold the respective stil and text files. The build-upload.sh script then takes the dest directory, the letters.css and dialect-letters.json files and wraps them up into a single uploadable upload.js file. This is then loaded via:
mongo calliope upload.js
THe file should first be copied to the remote server to be installed thus into its mongo database.
