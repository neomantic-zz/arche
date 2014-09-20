Feature: Discovering a way to find a resource
  In order to start finding a resource
  As an API client
  I want to a list of links to entry points to the available discoverable resources

Scenario: A client receives a list of links to find resource entries points
  Given a discoverable resource exists with the following attributes:
    | link_relation | https://www.mydomain.com/alps/study |
    | href          | https://service.com/study           |
    | resource_name | studies                             |
  And a discoverable resource exists with the following attributes:
    | link_relation | https://www.mydomain.com/alps/users |
    | href          | https://service.com/users           |
    | resource_name | users                               |
  When I invoke the uniform interface method GET to "/" accepting "application/hal+json"
  Then I should get a status of 200
  And the resource representation should have exactly the following links:
  | link_relation | href                                                |
  | studies       | https://service.com/study                           |
  | users         | https://service.com/users                           |
  | profile       | http://example.org/alps/EntryPoints                 |
  | self          | http://example.org/                                 |
  | type          | http://example.org/alps/EntryPoints#entry_points    |
  And the response should have the following header fields:
  | field         | field_contents       |
  | Cache-Control | max-age=600, private |
  | ETag          | anything             |
  | Location      | http://example.org/  |

Scenario: A Client receives a empty list of links when no resources have been registered
  Given I invoke the uniform interface method GET to "/" accepting "application/hal+json"
  Then I should get a status of 200
  And the resource representation should have exactly the following links:
  | link_relation | href                                                |
  | profile       | http://example.org/alps/EntryPoints                 |
  | self          | http://example.org/                                 |
  | type          | http://example.org/alps/EntryPoints#entry_points    |
  And the response should have the following header fields:
  | field         | field_contents       |
  | Cache-Control | max-age=600, private |
  | ETag          | anything             |
  | Location      | http://example.org/  |
