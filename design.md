# Arche Design

Arche list of entry points is provides in the generic hal+json
hypermedia format. The URLs are available on the root of the Arche
service. If environmental variable BASE_URI has been set to
"http://arche.host", then a GET request to the root with the
`application/hal+json` `Accept` header returns the following response.


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
`profile`, `type`, and `self`. There is one custom link relation type:
`discoverable_resources`. As is evident from the host name in its
`href` value, this is a resource that Arche itself manages.

## DiscoverableResources
A `DiscoverableResources` is a collection resource whose members Arche
uses to determine if they appear in the list of entry points.  A
`DiscoverableResource` (an individual resources) which is added to
the `DiscoverableResources` collection becomes an entry point.

### Registration
#### Manually

Creating a individual resources - or "registering" - is performed by a
POST request with a JSON body of `Content-type` `application/json` in
the following form:

``` json
{
	"resource_name": "users",
	"link_relation_url": "http://some.host/users/profile",
	"href": "http://some.host/users"
}
```

There three side-effects of this POST request.

1. A `DiscoverableResource` is corrected in added to the collection of
`DiscoverableResources`.

2. A new link will show up in the hal JSON document, where the value
   of `resource_name` will become a custom link relation type, and the
   value of `href` will become the link relation types `href`.

3. The ALPS profile of the EntryPoints document (at root) will contain
   a new `safe` semantic descriptor for custome link relation type -
   the resource name - and use at its profile URL the one provided in
   the POST requests `link_relation_url`.

#### Via hypermedia
The "manual" method described above uses so-called "out-of-band
knowledge" that is anathema to a hypermedia purists, who would say:
one should not need to know ahead of time that such a request is a) a
POST, b) JSON, and c) requires three fields. Such a purist would
insist that a hypermedia service profile a machine-parceable
description of such a request. A client would then follow href URL of the
`discoverable_resources` link relation and receive information from
that response about how to create such a resource.

If you have a hypermedia client that understands the
[HALE](https://github.com/mdsol/hale) JSON specification, or if you
send a request accepting `application/hale+json`, then Arche provides
such a descriptive entry point.  Here's the response:

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

In the response, we see that already one resource has been registered,
the `DiscoverableResources` resource.  More importantly, we see a
custom link relation type - `create` that represents a transition on
this collective resource. The hypermedia purists will now see all the
information we described in our manual registration process.
