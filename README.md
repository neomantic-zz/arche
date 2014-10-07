# arche

## Dependencies
* [Clojure](http://clojure.org/)
* [leiningen](http://leiningen.org/)
* mysql

# Setup

1. Save the example [profile.clj](example-profile.clj) file to the root
of the project under the name "profile.clj".
2. Edit the profile.clj file to match your mysql setup
3. Create a mysql database whose name corresponds to the `database-name`
in your custom `profile.clj` file.
4. Create the tables by running: `lein clj-sql-up migrate`

## Running Locally
Arche depends on values assigned to a number of environmental
variables described [below](#environmental-variables). For local
development purposes, these values can either be assigned as exports
in the shell or using a `profile.clj` file.

To run a local version arche, use the following command:

`lein with-profile dev ring server-headless`

## Standalone

1. Compile the application: `lein uberjar`
2. Assign all the environmental variables listed below in the shell
that you will run the web application
3. Execute the command in the same shell as the environmental
variables: `java -cp target/uberjar/arche-standalone.jar clojure.main
-m arche.core`

## Testing the Running App

If everything has been setup correctly, you can test the app using
curl.

If the arche is listening to port 3000 on localhost and its `BASE_URI`
assigned to localhost, and excute `curl -H "application/hal+json"
http://localhost:3000/`. The correct response is the following:

``` json
{
    "_links": {
        "profile": {
            "href": "http://localhost:3000/alps/EntryPoints"
        },
        "type": {
            "href": "http://localhost:3000/alps/EntryPoints#entry_points"
        },
        "self": {
            "href": "http://localhost:3000"
        }
    }
}
```

## Source Code

https://github.com/neomantic/arche

## Continuous Integration
[![Build Status](https://travis-ci.org/neomantic/arche.svg?branch=feature%2Ftravis)](https://travis-ci.org/neomantic/arche) [https://travis-ci.org/neomantic/arche](https://travis-ci.org/neomantic/arche)

## Issues and Feature Requests

https://github.com/neomantic/arche/issues

## Development
To run unit tests: `lein with-profile test spec`
To run cucumber: `lein with-profile test cucumber`

## Production
### Environmental variables

* `PORT` - The port the app should listen to for incoming requests
* `BASE_URI` - The URI that Arche use to construct links; e.g.,
   "http://localhost:3000" or "http://www.neomantic.com"
* `DATABASE_USER` - The mysql user who has access to the database
* `DATABASE_PASSWORD` - The user's credentials to the database
* `DATABASE_HOST` - The host of the mysql server
* `DATABASE_NAME` - The name of the database
* `CACHE_EXPIRY` - The number of seconds Arche assigns in the
  Cache-Control response headers

## Author

[Chad Albers](mailto:calbers@neomantic.com)

## License

Arche may be used under the terms of either the

  * GNU Lesser General Public License (LGPL)
    http://www.gnu.org/licenses/lgpl.html

or the

  * Eclipse Public License (EPL)
    http://www.eclipse.org/org/documents/epl-v10.php

As a recipient of arche, you may choose which license to receive the code
under. Certain files or entire directories may not be covered by this
dual license, but are subject to licenses compatible to both LGPL and EPL.
License exceptions are explicitly declared in all relevant files or in a
LICENSE file in the relevant directories.

Copyright © 2014 Chad Albers
