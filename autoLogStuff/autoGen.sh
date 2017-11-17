#!/bin/bash

#add latex formatting stuff to start of file
echo "---
geometry: landscape, left=1cm,right=1cm,top=1cm,bottom=1cm
header-includes:
    - \usepackage{setspace}
    - \setstretch{0.5}
    - \usepackage{listings}
    - \lstset{ breakatwhitespace=false, breaklines=false, extendedchars=true}
    - \setlength{\parskip}{0em}
---

\begin{spacing}{0}
" > log.md
echo "Added yaml header"


echo "Creating temp file, with the formatted markdown of the log"
echo "-----------------------------------"

echo "Growing the Forest"
# process the git forest. store in temporary file for now, since we need to add extra newlines cuz markdown, but the yaml stuff cant have said lines
git forest --pretty=format:"--  %h  ::  %ad, by %an  ::  %s " --reverse --style=15 --date=short > temp.md

echo "Sapping Color"
# strip color codes from the log
sed -i "s,\x1B\[[0-9;]*[a-zA-Z],,g" temp.md

echo "Naming native fauna"
# replace the usernames with real names
sed -i 's/Burtorustum/Rob A/' temp.md
sed -i 's/weznon/Wen P/' temp.md

echo "Discovering Soap"
# remove profanity
sed -i 's/shit/stuff/' temp.md

echo "Harvesting tall trees"
# fix formatting of really long lines
sed -i 's/added intellij templates - must be manually added to intellij, allows for super easy new teleop\/auton creation/added intellij templates for easy creation of Teleop\/Auton/' temp.md

sed -i 's/Finished the test bench code. Began adding new servos to the bot class for the flipper and two release servos (now tested). TODO: find equilibrium distance for two release servos, find correct points for release and flip actions./Finished test bench code and added new servos to bot class for flipper and release servos/' temp.md

sed -i 's/cleaned the bot class by removing unnecessary comments and adding functions for things that could be automated./cleaned up bot class : removed unnecessary comments, added functions for simple automation/' temp.md

sed -i 's/Moved hardware mapping to the bot class constructor to make loading times faster when pressing init on the drivers station./Moved hardware mapping to constructor, lowers init phase lag/' temp.md 

sed -i 's/the inputs to the constructor of the bot class to take over the role of setting hardwaremap references to motor, servo, sensor etc objects. Also moved telemetry to the bot class. Each of these now have references within the bot class to eleviate the need for passing them in the future for other methods. This has the additional effect of making our init function faster (Also removed the second unnecessary init)./inputs to bot constructor, moved telemetry to bot class/' temp.md

# note that for this one, the character ' is ’ in the pdf for some reason, dont question it
# just use the fix here if it pops up again
sed -i $'s/Renamed wen\'s wip bot class for confusion reasons. Also added the Enderbot\'s closable vuforia localizer so that we can use both vuforia and OpenCV in our code./renamed wen\'s bot class to avoid confusion, added machine vision stuff by Enderbots/' temp.md

sed -i $'s/added enderbot\'s default exception handler for if the bot randomly crashes while on the field. Will auto restart the app./added enderbot\' exception handler for trying to mitigate random crashes/' temp.md

sed -i 's/Created a Vision package for all computer vision stuff. Minor comments in Telebop and additions to bot class for new servos for lift and new motor for lift./created a vision package for computer vision, minor servo additions to bot class/' temp.md

# note the really annoying unicode crap
sed -i 's/\xe2\x94\x81\[HEAD\]\xe2\x94\x80\xe2\x94\x80\[master\]\xe2\x94\x80\xe2\x94\x80\[remotes\/origin\/HEAD\]\xe2\x94\x80\xe2\x94\x80\[remotes\/origin\/master\]\xe2\x94\x80\xe2\x94\x80/ /' temp.md

echo "Giving some space"
# add in extra newlines
sed -i G temp.md

echo "-----------------------------------"

echo "Appending processed log to yaml"
# add to main file
cat temp.md >> log.md

echo "Removing temp.md"
# clean up temp.md
rm temp.md

echo "Finalizing latex"
# append the closing spacing tag
echo "\end{spacing}" >> log.md



echo "Generating pdf"
# finally, run pandoc. writes the pdf to output.pdf
pandoc log.md -o output.pdf --pdf-engine xelatex --variable mainfont="Hack"

echo "Done!"

