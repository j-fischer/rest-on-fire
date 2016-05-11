#Rest On Fire
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/j-fischer/rest-on-fire.svg?style=flat&branch=master)](https://travis-ci.org/j-fischer/rest-on-fire)
[![Coverage Status](https://img.shields.io/coveralls/j-fischer/rest-on-fire.svg?style=flat)](https://coveralls.io/r/j-fischer/rest-on-fire?branch=master)

### About

This library is a wrapper for Firebase's REST API written in Java.

More info to come soon.

Features:
* Similar interfaces to the official Firebase APIs (Java/Javascript) for an easier adoption
* Supports all basic operations: get, set, update, push & remove
* Supports functional programming style through JDeferred's Promises

This library uses [Ning's AsyncHttpClient](http://www.ning.com/code/2010/03/introducing-nings-asynchronous-http-client-library/)
and [JDeferred](https://github.com/jdeferred/jdeferred).

Additional information on the AsyncHttpClient can be found [here](https://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/).

### Setup

NOTE: The initial release has only been published to JCenter.

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![JCenter](https://img.shields.io/bintray/v/j-fischer/maven/rest-on-fire.svg?label=jcenter)](https://bintray.com/j-fischer/maven/rest-on-fire/_latestVersion)
<!---
[![Maven Central](https://img.shields.io/maven-central/v/org.restonfire/rest-on-fire.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.restonfire/rest-on-fire)
-->

Maven:

```xml
<dependency>
  <groupId>org.restonfire</groupId>
  <artifactId>rest-on-fire</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'org.restonfire:rest-on-fire:0.1.0'
```

##### Snapshots

You can use snapshot versions through [JitPack](https://jitpack.io):

* Go to [JitPack project page](https://jitpack.io/#j-fischer/rest-on-fire)
* Select `Commits` section and click `Get it` on commit you want to use (top one - the most recent)
* Follow displayed instruction: add repository and change dependency (NOTE: due to JitPack convention artifact group will be different)

### Usage

Start by creating a factory for FirebaseNamespaces. The factory manages the external
dependencies for all namespaces.

    BaseFirebaseRestNamespaceFactory factory = new BaseFirebaseRestNamespaceFactory(
      new AsyncHttpClient(),
      new GsonBuilder().create()
    );

Once the factory is created, you can create an instance of a FirebaseRestNamespace.

    // Expects that Firebase access rules do not require authentication
    FirebaseRestNamespace namespace = factory.create(
      "http://namespace123.firebaseio.com",
      null // accessToken
    );

The namespace can be used to create a FirebaseRestReference object, which is similar
to [Firebase](https://www.firebase.com/docs/android/api/#firebase_methods) object of the Java APIs.

    FirebaseRestReference ref = namespace.getReference("some/location");

The reference object can then be used to perform operations like retrieving or setting
the value.

    ref.getValue(String.class)
      .done(new DoneCallback<String>() {
        @Override
        void onDone(String result) {
          cond.evaluate {
            assert result == "aString"
          }
        }
      });

Please take a look at [JDeferred's documentation](https://github.com/jdeferred/jdeferred) for
more information on the promises and its callback interfaces.

For more information, please take a look at the Rest On Fire's API documentation.

-
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)