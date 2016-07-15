#Rest On Fire
[![License](https://img.shields.io/hexpm/l/plug.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](http://img.shields.io/travis/j-fischer/rest-on-fire.svg?style=flat&branch=master)](https://travis-ci.org/j-fischer/rest-on-fire)
[![Coverage Status](https://img.shields.io/coveralls/j-fischer/rest-on-fire.svg?style=flat)](https://coveralls.io/r/j-fischer/rest-on-fire?branch=master)
[![JCenter](https://img.shields.io/bintray/v/j-fischer/maven/rest-on-fire.svg?label=jcenter)](https://bintray.com/j-fischer/maven/rest-on-fire/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.j-fischer/rest-on-fire.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.j-fischer/rest-on-fire)

### About

This library is a wrapper for Firebase's REST API written in Java.

Why use the REST API? While Firebase's Android and Server SDKs are great and the REST API is not meant to be a
replacement, it serves some purpose in server side environments. For example, if your server environment does 
not allow for a persistent socket connection to be opened, the REST API is the way to go. Also, Firebase's SDKs,
to my knowledge, cache all values you retrieve for offline availability. Those caches are unbound, which means 
that the grow indefinitely over time on a system with 24/7 uptime. To avoid out-of-memory exceptions, the REST API
can be used to have more control over the heap. Last, the REST API provides some unique features such as shallow
queries, which will only retrieve the keys at a location and not the values, which might be required in some situations.

To sum it up, always look at the Firebase SDKs first before switching to the REST API as they offer a lot more features
and are continuously improved. But if REST is the better option in your case, try out Rest On Fire as it supports
all relevant REST features with a very similar API than Firebase's SDKs. 

Features:
* Similar interfaces to the official Firebase APIs (Java/Javascript) for an easier adoption
* Supports all basic operations: get, set, update, push & remove
* Supports sorting and filtering of the data, including priorities
* Supports streaming events through the REST API
* Supports retrieval of shallow values to help work with large data sets
* Supports retrieving and setting of Firebase security rules 
* Supports functional programming style through JDeferred's Promises
* Uses SLF4J for logging to allow for an easy adoption of the library's logs

This library uses [Ning's AsyncHttpClient](http://www.ning.com/code/2010/03/introducing-nings-asynchronous-http-client-library/),
[JDeferred](https://github.com/jdeferred/jdeferred), [Gson](https://github.com/google/gson) and [SLF4J](http://www.slf4j.org/)
as external dependencies.

Additional information on the AsyncHttpClient can be found [here](https://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/).

Also, see the [Firebase documentation](https://firebase.google.com/docs/database/rest/retrieve-data) for more information

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published).

Maven:

```xml
<dependency>
  <groupId>com.github.j-fischer</groupId>
  <artifactId>rest-on-fire</artifactId>
  <version>0.7.0</version>
</dependency>
```

Gradle:

```groovy
compile 'com.github.j-fischer:rest-on-fire:0.7.0'
```

***Please check the badge on the top of this page for the latest release version!!!***

### Usage

Start by creating a factory for FirebaseDatabases. The factory manages the external
dependencies for all databases.

```java
BaseFirebaseRestDatabaseFactory factory = new BaseFirebaseRestDatabaseFactory(
  new AsyncHttpClient(),
  new GsonBuilder().create()
);
```

Once the factory is created, you can create an instance of a FirebaseRestDatabase.

```java
// Expects that Firebase access rules do not require authentication
FirebaseRestDatabase database = factory.create(
  "http://database123.firebaseio.com",
  null // accessToken
);
```

The database can be used to create a FirebaseRestReference object, which is similar
to [Firebase](https://www.firebase.com/docs/android/api/#firebase_methods) object of the Java APIs.

```java
FirebaseRestReference ref = database.getReference("some/location");
```

The reference object can then be used to perform operations like retrieving or setting
the value.

```java
ref.getValue(String.class)
  .done(new DoneCallback<String>() {
    @Override
    void onDone(String result) {
      System.out.println(result);
    }
  });
```

The `FirebaseRestReference` also provides a `query()` function to apply advanced sorting and filtering 
to the result set. The function returns a `FirebaseRestQuery` object instance that provides a simple 
interface to define and run the query.

```java
FirebaseRestQuery query = ref.query();
```
    
Assuming Firebase's dinosaur sample database, a query for all dinausaurs under a specific height would look like the following.
    
```java
query
  .orderByChild("height")
  .endAt(2.1)
  .run(Map.class)
  .done(new DoneCallback<Map<String, Object>>() {
    @Override
    void onDone(Map<String, Object> result) {
      // Process the result, which is a Map with the dinosaur's name
      // as the key and a Map<String, Object> of its properties as a value.
    }
  });
```

The database can also be used to create a FirebaseRestEventStream object, which allows for listening
for changes to locations within the database.

```java
FirebaseRestEventStream eventStream = database.getEventStream("some/location");
```

The actual events are published through the progress handler of the Promise.

```java
eventStream
  .startListening()
  .progress(new ProgressCallback<StreamingEvent>() {
    @Override
    void onProgress(StreamingEvent event) {
      StreamingEvent.EventType eventType = event.getEventType();
      StreamingEventData eventData = event.getEventData(); // see override with TypeToken for more options

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
  });
```

If you want to use this API to retrieve or update the Firebase security rules, use the FirebaseSecurityRulesReference to
access. However, it's important to note that accessing the security rules requires elevated access privileges,
such as the Firebase secret.

```java
FirebaseSecurityRulesReference securityRulesRef = database.getSecurityRules();
```
    
The security rules can now be retrieved, modified and written back to the database. 

```java
securityRulesRef
  .get()
  .done(new DoneCallback<FirebaseSecurityRules>() {
    @Override
    void onDone(FirebaseSecurityRules currentRules) {
      Map<String, Object> rulesMap = currentRules.getRules();
      
      // Let's assume that the default read permission is set to false
      rules.put(".read", true);
      
      FirebaseSecurityRules newRules = new FirebaseSecurityRules(rules);
      
      // set returns a promise that will resolve after the request succeeded 
      securityRulesRef.set(newRules);
    }
  });
```

Please take a look at [JDeferred's documentation](https://github.com/jdeferred/jdeferred) for
more information on the promises and its callback interfaces.

For more information, please take a look at the Rest On Fire's API documentation.

-
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)