Feature: Profile
  In order to understand a the representions I receive
  As an API client
  I want receive a machine parsable alps descriptor document

Scenario: A client should be able to receive a profile of entry in application/alps+json
   Given a discoverable resource exists with the following attributes:
   | link_relation | https://www.mydomain.com/alps/study |
   | href          | https://service.com/study           |
   | resource_name | studies                             |
   And I invoke the uniform interface method GET to "v2/alps/DiscoverableResources" accepting "application/alps+json"
   Then I should receive the following "application/alps+json" response:
   """
   {
    "alps": {
       "descriptor": [
          {
              "href": "http://alps.io/schema.org/URL",
              "type": "semantic",
              "id": "link_relation",
              "doc": {
                  "value": "The LinkRelation of the DiscoverableResource"
              }
          },
          {
              "href": "http://alps.io/schema.org/URL",
              "type": "semantic",
              "id": "href",
              "doc": {
                  "value": "The HREF to the entry point of the DiscoverableResource"
              }
          },
          {
              "href": "http://alps.io/schema.org/Text",
              "type": "semantic",
              "id": "resource_name",
              "doc": {
                  "value": "The name of the DiscoverableResource"
              }
          }
       ]
     }
   }
   """

Scenario: A client should be able to receive the correct headers of a profile response
   Given a discoverable resource exists with the following attributes:
   | link_relation | https://www.mydomain.com/alps/study |
   | href          | https://service.com/study           |
   | resource_name | studies                             |
   And I invoke the uniform interface method GET to "v2/alps/DiscoverableResources" accepting "application/alps+json"
    And the response should have the following header fields:
    | field         | field_contents                                 |
    | Cache-Control | max-age=600, private                           |
    | ETag          | anything                                       |
    | Location      | http://test.host/v2/alps/DiscoverableResources |
