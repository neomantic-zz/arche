# Arche Design

Arche provides a list entry points in the generic hal+json hypermedia
format. The document is accessible via a GET request to the the root
URL of the Arche service. If the environmental variable BASE_URI has
been set to "http://arche.host", then a GET request to the root with
the `application/hal+json` `Accept` header returns the following
response.


``` json
{
    "_links": {
        "profile": {
            "href": "http://arche.host/alps/EntryPoints"
        },
        "type": {
            "href": "http://arche.host/alps/EntryPoints#entry_points"
        },
        "self": {
            "href": "http://arche.host"
        },
        "discoverable_resources": {
            "href": "http://arche.host/discoverable_resources"
        }
    }
}
```

In this HAL document, there are currently three registered
[IANA link relation types](http://www.iana.org/assignments/link-relations/link-relations.xhtml):
`profile`, `type`, and `self`. There is also one custom link relation
type: `discoverable_resources`.  From the host name in
its `href` value, we can see that it is a resource that Arche itself manages.

## DiscoverableResources
A `DiscoverableResources` is a collection resource whose members Arche
uses to determine if they should appear in the list of entry points.  A
`DiscoverableResource` (an individual) which is added to
the `DiscoverableResources` collection becomes an entry point.

### Registration
#### Manually

Creating a individual resources - or "registering" - is performed by a
POST request with a body of `Content-type` `application/json` in
the following form:

``` json
{
	"resource_name": "users",
	"link_relation_url": "http://some.host/users/profile",
	"href": "http://some.host/users"
}
```

There are three side-effects of this POST request.

1. Arche persists the `DiscoverableResource` and adds it to the
collection of `DiscoverableResources`.

2. A new link will show up in the HAL JSON document, where the value
   of `resource_name` will become a custom link relation type, and the
   value of `href` will become the link relation type's `href`.

3. The ALPS profile of the EntryPoints document will contain a new
   `safe` semantic descriptor for the custom link relation type - the
   resource name - and set its profile URL to the
   `link_relation_url` sent in the POST request's body.

#### Via hypermedia

The "manual" method described above uses so-called "out-of-band
knowledge" that is anathema to a hypermedia purist, who would say: one
should not need to know ahead of time that such a request is a) a
POST, b) JSON, and c) requires three fields with those specific
names. Such a purist would insist that a hypermedia service provide a
machine-parceable description of such a request.

Arche provides such a mechanism using the
[HALE](https://github.com/mdsol/hale) hypermedia type and its own
entry point for the `DiscoverableResources`. The process is as
follows:

A client first makes a request to Arche's root, and retrieve a list of
entry points. It then follows the URL for the `discoverable_resources`
link relation, by making a get request with the `Accept` header set to
`application/hale+json`.  Arche responds with the following:

Here's the response:

``` json
{
    "_links": {
        "self": {
            "href": "http://arche.host/discoverable_resources"
        },
        "profile": {
            "href": "http://arche.host/alps/DiscoverableResources"
        },
        "create": {
            "data": {
                "href": {
                    "type": "text:text"
                },
                "link_relation_url": {
                    "type": "text:text"
                },
                "resource_name": {
                    "type": "text:text"
                }
            },
            "href": "http://arche.host/discoverable_resources",
            "method": "POST"
        },
        "items": [
            {
                "href": "http://arche.host/discoverable_resources/discoverable_resources"
            }
        ]
    },
    "_embedded": {
        "items": [
            {
                "link_relation_url": "http://arche.host/alps/DiscoverableResources",
                "href": "http://arche.host/discoverable_resources",
                "resource_name": "discoverable_resource",
                "_links": {
                    "self": {
                        "href": "http://arche.host/discoverable_resources/discoverable_resources"
                    }
                }
            }
        ]
    }
}
```

Here we see that Arche already has one registered discoverable
resource: the `DiscoverableResources` resource itself - Arche's own managed
resource.

More importantly, we see a custom link relation type - `create` - that
represents a transition on this collective resource. Following this
transition, by making a POST request to the URL in the `href` and
populating the body with fields and corresponding values will register
a new `DiscoverableResource` with Arche.
