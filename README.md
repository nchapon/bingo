# bingo

[![CircleCI](https://circleci.com/gh/nchapon/bingo.svg?style=shield)](https://circleci.com/gh/nchapon/bingo)

Experimental CLI written in Clojure to get bing's daily walpaper.

## Installation ##


### Basic Usage ###

    $ bingo [args]


-h --help

-n --nb-images <number>, default 1, max 7

-o --output-dir

-m --mkt


## Installation from sources ##

Download from https://github.com/nchapon/bingo

This application is built with [Clojure Tools CLI](https://clojure.org/guides/deps_and_cli), you need to install Java and Clojure before. 


### Run the application with Clojure CLI ###


Run the project directly, via `:main-opts`:

    $ clojure -M:run
    

You can also pass options :

    $ clojure -M:run -n 7    # Get the last 7 images
  
    $ clojure -M:run -h      # Display help
    

### Project tests ###

Run the project's tests from the task **test** in `build.clj`

    $ clojure -T:build test

### Continuous Integration ###

Run the project's CI pipeline and build an uberjar from the task **ci** in `build.clj`:

    $ clojure -T:build ci

Run that uberjar:

    $ java -jar target/my-app-0.1.0-SNAPSHOT.jar

If you remove `version` from `build.clj`, the uberjar will become `target/my-app-standalone.jar`.


### Native Executable ###

Native compilation is possible with GraalVM, you need to Install GraalVM before.

#### Linux ####

``` shell
./scripts/build-native.sh
```

#### Mac OS ####

``` shell
./scripts/build-native.sh
```

#### Windows ####

``` shell
./scripts/build-native.bat
```


## License

Copyright © 2022 Nicolas CHAPON

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
