Feature: API for discoverable resources
  In order to manage entry points to find resources
  As an API client
  I want read a catalogue of available resource to discover

Scenario: A client reads a discoverable resource as hale+json
    Given a discoverable resource exists with the following attributes:
    | link_relation_url | https://www.mydomain.com/studies |
    | href              | https://a-service.io/studies     |
    | resource_name     | studies                          |
    When I invoke the uniform interface method GET to "/discoverable_resources/studies" accepting "application/hal+json"
    Then I should get a status of 200
    And the resource representation should have exactly the following properties:
    | link_relation_url | https://www.mydomain.com/studies |
    | href              | https://a-service.io/studies     |
    | resource_name     | studies                          |
    And the resource representation should have exactly the following links:
    | link_relation | href                                              |
    | self          | http://example.org/discoverable_resources/studies |
    | profile       | http://example.org/alps/DiscoverableResources     |
    And the response should have the following header fields:
    | field         | field_contents                                       |
    | Cache-Control | max-age=600, private                                 |
    | ETag          | anything                                             |
    | Location      | http://example.org/discoverable_resources/studies    |

Scenario: A client gets an error when the link_relation_url is not registered in wormhole
  Given no discoverable resource is registered
  When I invoke the uniform interface method GET to "/discoverable_resources/study" accepting "application/vnd.hal+json"
  Then I should get a status of 404

Scenario: The client can successfully create a discoverable resource
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "link_relation_url": "https://www.mydomain.com/studies",
   "href": "https://a-service.io/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 201
  And the resource representation should have exactly the following properties:
   | link_relation_url | https://www.mydomain.com/studies |
   | href              | https://a-service.io/studies     |
   | resource_name     | studies                          |
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
   "link_relation_url": "https://www.mydomain.com/studies"
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
   "link_relation_url": "https://www.mydomain.com/studies",
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
  | attribute         | error_on_attribute |
  | link_relation_url | can't be blank     |
  | link_relation_url | is not valid       |

Scenario: The request to create a discoverable resource fails, if the href is not HTTP or HTTPS
  Given I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "link_relation_url": "https://www.mydomain.com/studies",
   "href": "mailto:someone@somewhere.org",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 422
  And I should get a response with the following errors:
  | attribute | error_on_attribute |
  | href      | is not valid       |

Scenario: If the link relation submitted is not https or http, then the request to create a discoverable resource fails
  Given I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "link_relation_url": "mailto:someone@somewhere.org",
   "href": "https://a-service.io/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 422
  And I should get a response with the following errors:
  | attribute         | error_on_attribute |
  | link_relation_url | is not valid       |

Scenario: A client reads a discoverable resource index as hal+json
  Given a discoverable resource exists with the following attributes:
    | link_relation_url | https://www.mydomain.com/studies |
    | href          | https://a-service.io/studies     |
    | resource_name | studies                          |
    When I invoke the uniform interface method GET to "/discoverable_resources" accepting "application/hal+json"
    Then I should get a status of 200
    And the resource representation should have at least the following links:
    | link_relation_url | href                                      |
    | self          | http://example.org/discoverable_resources |
    And the resource representation "items" property should have the following items:
    | attribute | value                                             |
    | href      | http://example.org/discoverable_resources/studies |
    And the resource representation should have an embedded "items" property with the following links and properties:
    | type     | identifer         | value                                             |
    | property | link_relation_url | https://www.mydomain.com/studies                  |
    | property | href              | https://a-service.io/studies                      |
    | property | resource_name     | studies                                           |
    | link     | self              | http://example.org/discoverable_resources/studies |
   And the response should have the following header fields:
    | field         | field_contents                            |
    | Cache-Control | max-age=0, private                        |
    | ETag          | anything                                  |
    | Location      | http://example.org/discoverable_resources |
