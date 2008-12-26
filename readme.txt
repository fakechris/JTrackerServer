1) What is Quash?
Quash is a simple java-based bittorrent tracker.

"Simple" means that it does not implement any major features on top of the
plain bittorrent standard, like user-authentication and ratio tracking.
"Java-based" means that it utilises the Java Servlet technology to avoid having
to reimplement much of HTTP, and rather concentrate on the core parts of what
makes up a bittorrent tracker.

The rationale for making this has so far been two-fold. One, to learn webapp
development using Java Servlets and two, to scratch an itch for myself. Most
other bittorrent trackers are either; a) complex beasts with tons of features
I would not personally use, b) unmaintained and with numerous known bugs,
c) completely lacking in documentation, both in the source code and elsewhere or,
d) all of the above. This application aims to fill a gap as a no-frills
alternative to the established options.

"But hold on, you say 'no frills' and 'java' practically in the same sentence?"

For those who feel that the word "java" gives off a bit of a smell, please
consider that code written in it is easily portable to other platforms (unlike
poor C/C++ code), with some care has very passable performance and using a
servlet container like Apache Tomcat makes it easy to deploy to the web.

2) How do I use it?
Quash depends on a couple of things. Primarily, you need the JDK and the Apache
Commons FileUpload classes (which is used for the uploading of torrentfiles),
which is available from <http://commons.apache.org/fileupload/>.

You also need to configure the database you are going to use for persistent
information, and possibly install the JDBC drivers for this particular database.
Quash uses JPA (Java Persistence API), so several options should be possible,
but it has mostly been tested with MySQL.
The proper JDBC URL, as well as authentication details should be put in
"persistence.xml" which may be found in src/conf/.

Finally, you need a server which can run the application. Any Java EE 5
application server should be sufficient, but it also works (and indeed, has been
mostly used with) using a servlet container such as Apache Tomcat.

If this all seems daunting, you may consider checking out Netbeans. Netbeans is
an IDE for development of, among other things, java servlets, and they kindly
offer bundles with Java and application servers, which could quickstart things
a bit. In addition, this application is also distributed as a netbeans project.
Netbeans may be obtained from <http://www.netbeans.org/>.

If you do not want to use netbeans, the application may be compiled using ant
(<http://ant.apache.org/>). Build a .war file for deploying on an application
server by issuing "ant dist". Optionally, run "ant test" to run the unit tests.

3) HACKING
This project is distributed as a netbeans project, using netbeans is probably
the easiest way to start hacking on it. If you do not wish to use netbeans, the
filestructure should be fairly self-evident.

The code is formatted using the usual java coding guidelines.

The code is organised into different packages under the "com.tracker"-tree.
The packages ending in "entity" is the entity-classes (ie, the information that
is persisted to the database), while the "webinterface" packages are things
that are meant to be used by the front-ends of the application (Browse, Upload,
etc).
The real meat of the tracker functionality exists in the "backend" package.

In addition, there are some highly rudimentary jsp-files providing a very basic
webinterface in the web/ folder. These are organised such that the real information
(list of torrents, etc) is acquired from the client-side using AJAX. The project
currently includes the JQuery library for this purpose.

This project is by no means finished, several things need attention, including
polishing of current features as well as development of new features. Contact me
for further information if you have any questions regarding the design, code,
bugs or any outstanding TODOs.

4) Contact
You may contact me at <bo.bjornsen@gmail.com>.
The latest version of this software will be available through its googlecode
webpage.

5) License
This code is licensed under the GPL version 3.

The included JSON classes is also licensed under its own license,
which can be found at <http://www.json.org/license.html>.
The classes themselves (and many alternatives) can be found
at <http://www.json.org>.

The code also depends on the Apache Commons FileUpload classes which may be
found at <http://commons.apache.org/fileupload/> and is licensed under the
Apache License 2.0.

Thanks to both of these projects for making my job easier!