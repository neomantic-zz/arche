Feature: API for discoverable resources
  In order to manage entry points to find resources
  As an API client
  I want read a catalogue of available resource to discover

Scenario Outline: A client reads a discoverable resource as hale+json
    Given a discoverable resource exists with the following attributes:
    | link_relation_url | https://www.mydomain.com/studies |
    | href              | https://a-service.io/studies     |
    | resource_name     | studies                          |
    When I invoke the uniform interface method GET to "/discoverable_resources/studies" accepting "<Mime-Type>"
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
    | field         | field_contents                                                  |
    | Cache-Control | max-age=600, private                                            |
    | ETag          | anything                                                        |
    | Location      | http://example.org/discoverable_resources/studies               |
    | Accept        | application/hal+json,application/vnd.hale+json,application/json |

 Examples:
  | Mime-Type                 |
  | application/hal+json      |
  | application/vnd.hale+json |
  | application/json          |

Scenario: A client gets an error when the link_relation_url is not registered in wormhole
  Given no discoverable resource is registered
  When I invoke the uniform interface method GET to "/discoverable_resources/study" accepting "application/hal+json"
  Then I should get a status of 404

Scenario Outline: The client can successfully create a discoverable resource
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "<Mime-Type>" responses:
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
  | field         | field_contents                                                  |
  | Cache-Control | max-age=600, private                                            |
  | ETag          | anything                                                        |
  | Location      | http://example.org/discoverable_resources/studies               |
  | Accept        | application/hal+json,application/vnd.hale+json,application/json |

 Examples:
  | Mime-Type                 |
  | application/hal+json      |
  | application/vnd.hale+json |

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

Scenario: The request to create a discoverable resource fails, if the resource already exists
  Given a discoverable resource exists with the following attributes:
  | link_relation_url | https://www.mydomain.com/studies |
  | href              | https://a-service.io/studies     |
  | resource_name     | studies                          |
  When I invoke uniform interface method POST to "/discoverable_resources" with the "application/json" body and accepting "application/hal+json" responses:
  """
  {
   "link_relation_url": "https://www.mydomain.com/studies",
   "href": "https://a-service.io/studies",
   "resource_name": "studies"
  }
  """
  Then I should get a status of 400
  And I should get a response with the following errors:
  | attribute     | error_on_attribute |
  | resource_name | is already taken   |

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
    | field         | field_contents                                 |
    | Cache-Control | max-age=600, private                           |
    | ETag          | anything                                       |
    | Location      | http://example.org/discoverable_resources      |
    | Accept        | application/vnd.hale+json,application/hal+json |

Scenario: A client reads a discoverable resource index as vnd.hale+json
  Given a discoverable resource exists with the following attributes:
  | link_relation_url | https://www.mydomain.com/studies |
  | href              | https://a-service.io/studies     |
  | resource_name     | studies                          |
  When I invoke the uniform interface method GET to "/discoverable_resources" accepting "application/vnd.hale+json"
  Then I should get a status of 200
  And the resource representation should have at least the following links:
  | link_relation_url | href                                      |
  | self              | http://example.org/discoverable_resources |
  And the resource representation "items" property should have the following items:
  | attribute | value                                             |
  | href      | http://example.org/discoverable_resources/studies |
  And the resource representation should have an embedded "items" property with the following links and properties:
  | type     | identifer         | value                                             |
  | property | link_relation_url | https://www.mydomain.com/studies                  |
  | property | href              | https://a-service.io/studies                      |
  | property | resource_name     | studies                                           |
  | link     | self              | http://example.org/discoverable_resources/studies |
  And the resource representation should have a "create" link relation with at least the following properties:
  | property name | value                                        |
  | href          | http://example.org/discoverable_resources    |
  | method        | POST                                         |
  And the data form for the "create" link relation should contain the following:
  | input name        | input type |
  | href              | text:text  |
  | link_relation_url | text:text  |
  | resource_name     | text:text  |
  And the response should have the following header fields:
  | field         | field_contents                                 |
  | Cache-Control | max-age=600, private                           |
  | ETag          | anything                                       |
  | Location      | http://example.org/discoverable_resources      |
  | Accept        | application/vnd.hale+json,application/hal+json |


@WIP
Scenario: I should not receive paginations link relations (prev or next)
  Given 25 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources" accepting "application/hal+json"
  And the resource representation should not have the following links:
   | next |
   | prev |

@WIP
Scenario: I should receive paginations link relations (prev and next)
  Given 51 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=2" accepting "application/hal+json"
  And the resource representation should have at least the following links:
   | link_relation | href                                                            |
   | next          | http://example.org/discoverable_resources?page=3&per_page=25    |
   | prev          | http://example.org/discoverable_resources?page=1&per_page=25    |

@WIP
Scenario: I can retrieve a specific page of discoverable resources, I'll receive a link to the prev page, but not a next link
  Given 26 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=2" accepting "application/hal+json"
  Then the resource representation should have at least the following links:
   | link_relation | href                                                            |
   | prev          | http://example.org/discoverable_resources?page=1&per_page=25    |
  And the resource representation should not have the following links:
   | next |

@WIP
Scenario: I can retrieve a specific page of discoverable resources, I'll receive a link to the next page, but not a prev link
  Given 26 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=1" accepting "application/hal+json"
  Then the resource representation should have at least the following links:
   | link_relation | href                                                            |
   | next          | http://example.org/v1/discoverable_resources?page=2&per_page=25 |
  And the resource representation should not have the following links:
   | prev |

@WIP
Scenario: When I retrieve a specific page, I should receive a self like that indicates the page, and the per page count
  Given 26 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=2" accepting "application/hal+json"
  Then the resource representation should have at least the following links:
   | link_relation | href                                                |
   | self          | http://example.org/discoverable_resources?page=2    |

@WIP
Scenario: When I retrieve a specific page, I'll receive only a limited set of items
  Given 26 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=2" accepting "application/hal+json"
  Then the resource representation should have 1 items in its links
  And the resource representation should have 1 embedded resource items

@WIP
Scenario: When twice as many items of the default number of 25, I should receive a prev link
  Given 51 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?page=2" accepting "application/hal+json"
  Then the resource representation should have at least the following links:
   | link_relation | href                                                            |
   | prev          | http://example.org/discoverable_resources?page=1&per_page=25    |

@WIP
Scenario: I can specify the number discover resources items returned
  Given 26 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?per_page=3" accepting "application/hal+json"
  Then the resource representation should have 3 items in its links
  And the resource representation should have 3 embedded resource items

@WIP
Scenario: I can request at most 100 items
  Given 101 discoverable resource exists
  When I invoke the uniform interface method GET to "/discoverable_resources?per_page=101" accepting "application/hal+json"
  Then the resource representation should have 100 items in its links
  And the resource representation should have 100 embedded resource items
