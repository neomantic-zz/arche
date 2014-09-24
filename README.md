# arche


## Dependencies
Clojure
Leinigen

## Running
`lein with-profile dev ring server-headless`

## Bugs

## Development
To run unit tests: `lein with-profile spec,test spec`
To run cucumber: `lein with-profile test cucumber`

## Production
### Eenviroment variables

* `BASE_URI`
* `DATABASE_PASSWORD`
* `DATABASE_USER`
* `DATABASE_SUBNAME`
* `CACHE_EXPIRY`

## Arche Licensing Information

arche may be used under the terms of either the

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
