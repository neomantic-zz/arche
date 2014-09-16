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
    | link_relation | href                                                 |
    | self          | http://example.org/discoverable_resources/studies    |
    And the response should have the following header fields:
    | field         | field_contents                                       |
    | Cache-Control | max-age=600, private                                 |
    | ETag          | anything                                             |
    | Location      | http://example.org/discoverable_resources/studies    |
