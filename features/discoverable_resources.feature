Feature: API for discoverable resources
  In order to manage entry points to find resources
  As an API client
  I want read a catalogue of available resource to discover

Scenario: A client reads a discoverable resource as hale+json
    Given a discoverable resource exists with the following attributes:
    | link_relation | https://www.mydomain.com/studies |
    | href          | https://a-service.io/studies     |
    | resource_name | studies                          |
    When I invoke the uniform interface method GET to "/discoverable_resources/studies" accepting "application/hal+json"
    Then I should get a status of 200
    And the resource representation should have exactly the following properties:
    | link_relation | https://www.mydomain.com/studies |
    | href          | https://a-service.io/studies     |
    | resource_name | studies                          |
    And the resource representation should have exactly the following links:
    | link_relation | href                                              |
    | self          | http://example.org/discoverable_resources/studies |
    | profile       | http://example.org/alps/DiscoverableResources     |
    And the response should have the following header fields:
    | field         | field_contents                                       |
    | Cache-Control | max-age=600, private                                 |
    | ETag          | anything                                             |
    | Location      | http://example.org/discoverable_resources/studies    |

Scenario: The client can successfully create a discoverable resource
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/vnd.hale+json" responses:
  """
  {
   "link_relation": "https://www.mydomain.com/studies",
   "href": "https://a-service.io/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 201
  And the resource representation should have exactly the following properties:
   | link_relation | href                             |
   | link_relation | https://www.mydomain.com/studies |
   | href          | https://a-service.io/studies     |
   | resource_name | studies                          |
  And the resource representation should have exactly the following links:
    | link_relation | href                                                                   |
    | profile       | http://example.org/alps/DiscoverableResources                       |
    | self          | http://example.org/discoverable_resources/studies                   |
    | type          | http://example.org/alps/DiscoverableResources#discoverable_resource |
    | help          | http://example.org/help/discoverable_resources                         |
  And the response should have the following header fields:
    | field         | field_contents                                       |
    | Cache-Control | max-age=0, private                                   |
    | ETag          | anything                                             |
    | Location      | http://example.org/discoverable_resources/studies    |
