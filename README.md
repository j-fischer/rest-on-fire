#Rest On Fire
[![License](https://img.shields.io/hexpm/l/plug.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](http://img.shields.io/travis/j-fischer/rest-on-fire.svg?style=flat&branch=master)](https://travis-ci.org/j-fischer/rest-on-fire)
[![Coverage Status](https://img.shields.io/coveralls/j-fischer/rest-on-fire.svg?style=flat)](https://coveralls.io/r/j-fischer/rest-on-fire?branch=master)
[![JCenter](https://img.shields.io/bintray/v/j-fischer/maven/rest-on-fire.svg?label=jcenter)](https://bintray.com/j-fischer/maven/rest-on-fire/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.j-fischer/rest-on-fire.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.j-fischer/rest-on-fire)

### About

This library is a wrapper for Firebase's REST API written in Java.

More info to come soon.

Features:
* Similar interfaces to the official Firebase APIs (Java/Javascript) for an easier adoption
* Supports all basic operations: get, set, update, push & remove
* Supports streaming events through the REST API
* Supports functional programming style through JDeferred's Promises
* Uses SLF4J for logging to allow for an easy adoption of the library's logs

This library uses [Ning's AsyncHttpClient](http://www.ning.com/code/2010/03/introducing-nings-asynchronous-http-client-library/),
[JDeferred](https://github.com/jdeferred/jdeferred), [Gson](https://github.com/google/gson) and [SLF4J](http://www.slf4j.org/)
as external dependencies.

Additional information on the AsyncHttpClient can be found [here](https://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/).

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published).

Maven:

```xml
<dependency>
  <groupId>com.github.j-fischer</groupId>
  <artifactId>rest-on-fire</artifactId>
  <version>0.2.0</version>
</dependency>
```

Gradle:

```groovy
compile 'com.github.j-fischer:rest-on-fire:0.2.0'
```

### Usage

Start by creating a factory for FirebaseDatabases. The factory manages the external
dependencies for all databases.

    BaseFirebaseRestDatabaseFactory factory = new BaseFirebaseRestDatabaseFactory(
      new AsyncHttpClient(),
      new GsonBuilder().create()
    );

Once the factory is created, you can create an instance of a FirebaseRestDatabase.

    // Expects that Firebase access rules do not require authentication
    FirebaseRestDatabase database = factory.create(
      "http://database123.firebaseio.com",
      null // accessToken
    );

The database can be used to create a FirebaseRestReference object, which is similar
to [Firebase](https://www.firebase.com/docs/android/api/#firebase_methods) object of the Java APIs.

    FirebaseRestReference ref = database.getReference("some/location");

The reference object can then be used to perform operations like retrieving or setting
the value.

    ref.getValue(String.class)
      .done(new DoneCallback<String>() {
        @Override
        void onDone(String result) {
          System.out.println(result);
        }
      });

The database can also be used to create a FirebaseRestEventStream object, which allows for listening
for changes to locations within the database.

    FirebaseRestEventStream eventStream = database.getEventStream("some/location");

The actual events are published through the progress handler of the Promise.

    eventStream
      .startListening()
      .progress(new ProgressCallback<StreamingEvent>() {
        @Override
        void onProgress(StreamingEvent event) {
          StreamingEvent.EventType eventType = event.getEventType();
          StreamingEventData eventData = event.getEventData(); // see override with TypeToke for more options

          String path = eventData.getPath();
          Object value = eventData.getData(); // the type depends on the value stored at the event location
      })
      .always(new AlwaysCallback<Void, FirebaseRuntimeException>() {
        @Override
        void onAlways(Promise.State state, Void resolved, FirebaseRuntimeException rejected) {
          assert state == Primise.State.RESOLVED;
          assert resolved == null;
          assert rejected == null;
        }
      })

Please take a look at [JDeferred's documentation](https://github.com/jdeferred/jdeferred) for
more information on the promises and its callback interfaces.

For more information, please take a look at the Rest On Fire's API documentation.

-
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)