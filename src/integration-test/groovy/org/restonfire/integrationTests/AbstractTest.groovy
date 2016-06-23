package org.restonfire.integrationTests

import com.firebase.security.token.TokenGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncHttpClientConfig
import org.jdeferred.Promise
import org.restonfire.BaseFirebaseRestDatabaseFactory
import org.restonfire.FirebaseRestDatabase
import org.jdeferred.impl.DefaultDeferredManager
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.responses.FirebaseSecurityRules
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
/**
 * Base class for tests managing Firebase setup and token generation.
 */
abstract class AbstractTest extends Specification {

  private AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();

  private Gson gson = new GsonBuilder().create()
  private String namespaceUrl = System.getProperty("firebase.namespace") ?: System.getenv("FIREBASE_NAMESPACE")
  private String firebaseSecret = System.getProperty("firebase.secret") ?: System.getenv("FIREBASE_SECRET")

  private String firebaseToken
  private Promise<Map<String, Object>, FirebaseRuntimeException, Void> setupPromise

  private BaseFirebaseRestDatabaseFactory factory

  void setup() {
    AsyncConditions cond = new AsyncConditions()

    factory = new BaseFirebaseRestDatabaseFactory(
      new AsyncHttpClient(builder
        .setCompressionEnforced(true)
        .setAllowPoolingConnections(true)
        .build()
      ),
      gson
    )

    def adminNamespace = factory.create(
      namespaceUrl,
      firebaseSecret
    )

    def securityPromise = adminNamespace
      .getSecurityRules()
      .set(getDefaultSecurityRules())

    def dataPromise = adminNamespace
      .getReference("/")
      .setValue(getDefaultDataSet())
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
        cond.evaluate{
          assert state == Promise.State.RESOLVED
        }
      })

    setupPromise = new DefaultDeferredManager()
      .when(securityPromise, dataPromise)

    def payload = [
      uid: "1"
    ]

    def tokenGenerator = new TokenGenerator(firebaseSecret)
    firebaseToken = tokenGenerator.createToken(payload)

    cond.await(10)
  }

  FirebaseRestDatabase createNamespace() {
    assert firebaseToken != null

    return factory.create(
      namespaceUrl,
      firebaseToken
    )
  }

  FirebaseRestDatabase createNamespaceWithSecret() {
    return factory.create(
      namespaceUrl,
      firebaseSecret
    )
  }

  private Map<String, Object> getDefaultDataSet() {
    def defaultData = [
      testData: [
        sampleData: [ foo: "bar" ],
        integer: 1,
        bool: true,
        text: "aString",
        toBeSet: "SET ME",
        toBeRemoved: "DELETE ME",
        toBeUpdated: [ foo: "bar" ],
        sortedStrings: [
          c: "y",
          b: "z",
          a: "x"
        ],
        sortedStringsWithPriorities: [
          c: [ ".value": "y", ".priority": 1.0 ],
          b: [ ".value": "z", ".priority": 2.0 ],
          a: [ ".value": "x", ".priority": 3.0 ]
        ],
        dinosaurs: [
          lambeosaurus: [
            height : 2.1,
            length : 12.5,
            weight: 5000
          ],
          stegosaurus: [
            height : 4,
            length : 9,
            weight : 2500
          ]
        ]
      ],
      noReadAccess: "BIG SECRET"
    ]

    return defaultData
  }

  FirebaseSecurityRules getDefaultSecurityRules() {
    def rulesJson = '''{
      "rules": {
        ".read": false,
        ".write": false,
        "testData": {
          ".read": "auth != null",
          ".write": false,
          "toBeSet": {
            ".write": "auth != null"
          },
          "toBeRemoved": {
            ".write": "auth != null"
          },
          "toBeUpdated": {
            ".write": "auth != null"
          },
          "sortedStrings": {
            ".indexOn": ".value"
          },
          "sortedStringsWithPriorities": {
            ".indexOn": ".value",
            ".write": "auth != null"
          },
          "dinosaurs": {
            ".indexOn": ["height", "length"]
          }
        }
      }
    }'''

    return gson.fromJson(rulesJson, FirebaseSecurityRules.class)
  }
}
