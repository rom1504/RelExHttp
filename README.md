# RelExHttp
RelEx is a relation extractor software (https://github.com/opencog/relex).

In order to make it easier to use it from perl(or any other language) for a few questions at a time I made this simple Http server to query RelEx quickly.


## Building
Open the project in eclipse then add all the needed library for relex as external jar then relex itself as external class path (see http://wiki.opencog.org/w/RelEx_Install for the list of library), you also need to put the data file in the project folder. Then you can compile and start the program

## Example
First start the program, then you can query RelEx with : curl http://localhost:9002/ --data-urlencode "text=Who is the daughter of Bill Clinton married to?"
