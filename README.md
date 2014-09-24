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

## License

Copyright © 2014 Chad Albers

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
