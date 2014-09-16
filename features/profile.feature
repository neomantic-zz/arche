Feature: Profile
  In order to understand a the representions I receive
  As an API client
  I want receive a machine parsable alps descriptor document

Scenario: A client should be able to receive a profile of entry in application/alps+json
   Given a discoverable resource exists with the following attributes:
   | link_relation | https://www.mydomain.com/alps/study |
   | href          | https://service.com/study           |
   | resource_name | studies                             |
   And I invoke the uniform interface method GET to "/alps/DiscoverableResources" accepting "application/alps+json"
   Then I should receive the following "application/alps+json" response:
   """
   {
     "alps": {
       "descriptor": [
         {
           "doc": {
             "value": "The LinkRelation of the DiscoverableResource"
           },
           "id": "link_relation",
           "type": "semantic",
           "href": "http://alps.io/schema.org/URL"
         },
         {
           "doc": {
             "value": "The HREF to the entry point of the DiscoverableResource"
           },
           "id": "href",
           "type": "semantic",
           "href": "http://alps.io/schema.org/URL"
         },
         {
           "doc": {
             "value": "The name of the DiscoverableResource"
           },
           "id": "resource_name",
           "type": "semantic",
           "href": "http://alps.io/schema.org/Text"
         },
         {
           "doc": {
             "value": "Returns an individual DiscoverableResource"
           },
           "id": "show",
           "rt": "discoverable_resource",
           "type": "safe"
         },
         {
           "doc": {
             "value": "A Resource that can be discovered via an entry point"
           },
           "link": {
               "rel": "self",
               "href": "http://example.org/alps/DiscoverableResources#discoverable_resource"
           },
           "id": "discoverable_resource",
           "type": "semantic",
           "descriptor": [
             {
               "href": "link_relation"
             },
             {
               "href": "href"
             },
             {
               "href": "resource_name"
             },
             {
               "href": "show"
             }
           ]
         }
       ],
       "doc": {
         "value": "Describes the semantics, states and state transitions associated with DiscoverableResources."
       },
       "link": {
           "rel": "self",
           "href": "http://example.org/alps/DiscoverableResources"
       }
     }
   }
   """

Scenario: A client should be able to receive the correct headers of a profile response
   Given a discoverable resource exists with the following attributes:
   | link_relation | https://www.mydomain.com/alps/study |
   | href          | https://service.com/study           |
   | resource_name | studies                             |
   And I invoke the uniform interface method GET to "alps/DiscoverableResources" accepting "application/alps+json"
    And the response should have the following header fields:
    | field         | field_contents                                   |
    | Cache-Control | max-age=600, private                             |
    | ETag          | anything                                         |
    | Location      | http://example.org/alps/DiscoverableResources    |
