# Marky

This little programm is based on [commonmark-java](https://github.com/atlassian/commonmark-java).

The project has been started to convert multiple markdown documents into a single HTML file.

## Usage

You can run it via command line interface; just call `org.marky.cli.Main` and set VM argument `src.dir` and `out.file`.

Sample calls:

    -Dsrc.dir=C:/projects/SapiEng -Dout.file=C:/projects/SapiEng/site/content.html -Dsrc.walk=1 -Dout.file.toc=C:/projects/SapiEng/site/index.html
    -Dsrc.dir=C:/projects/SapiEng/SeProcs/site/main -Dout.file=C:/projects/SapiEng/site
    -Dsrc.dir=C:/projects/SapiEng/SeProcs/site/main -Dout.file=C:/projects/SapiEng/site/content.html
    -Dsrc.dir=C:/projects/SapiEng/SeProcs -Dout.file=C:/projects/SapiEng/site/content.html -Dsrc.walk=1
