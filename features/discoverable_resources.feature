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
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
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
    | link_relation | href                                              |
    | profile       | http://example.org/alps/DiscoverableResources     |
    | self          | http://example.org/discoverable_resources/studies |
  And the response should have the following header fields:
    | field         | field_contents                                    |
    | Cache-Control | max-age=600, private                              |
    | ETag          | anything                                          |
    | Location      | http://example.org/discoverable_resources/studies |

Scenario: The request to create a discoverable resource fails, if no resource name is supplied
  When I invoke uniform interface method POST to "discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "href": "https://a-service.io/studies",
   "link_relation": "https://www.mydomain.com/studies"
  }
  """
  Then I should get a status of 422
  And I should get a response with the following errors:
  | attribute     | error_on_attribute |
  | resource_name | can't be blank     |

Scenario: The request to create a discoverable resource fails, if no href is supplied
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "link_relation": "https://www.mydomain.com/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 422
  And I should get a response with the following errors:
  | attribute | error_on_attribute |
  | href      | can't be blank     |
  | href      | is not valid       |

Scenario: The request to create a discoverable resource fails, if no link relation is supplied
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "href": "https://a-service.io/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 422
  And I should get a response with the following errors:
  | attribute     | error_on_attribute |
  | link_relation | can't be blank     |
  | link_relation | is not valid       |

Scenario: A client reads a discoverable resource index as hale+json
    Given a discoverable resource exists with the following attributes:
    | link_relation | https://www.mydomain.com/studies |
    | href          | https://a-service.io/studies     |
    | resource_name | studies                          |
    When I invoke the uniform interface method GET to "v2/discoverable_resources" accepting "application/vnd.hale+json"
    Then I should get a status of 200
    And the resource representation should have at least the following links:
    | link_relation | href                                                                    |
    | self          | http://example.org/v2/discoverable_resources                            |
    And the resource representation should have the following items in its links:
    | href                                                 | type                                                                   |
    | http://example.org/v2/discoverable_resources/studies | http://example.org/v2/alps/DiscoverableResources#discoverable_resource |
    And the resource representation should have an embedded resource items with the following links and properties:
    | self link                                            | link_relation property           | href property                | resource_name property |
    | http://example.org/v2/discoverable_resources/studies | https://www.mydomain.com/studies | https://a-service.io/studies | studies                |
