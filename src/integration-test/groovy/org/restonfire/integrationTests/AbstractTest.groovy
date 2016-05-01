package org.restonfire.integrationTests

import com.firebase.security.token.TokenGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ning.http.client.AsyncHttpClient
import org.jdeferred.Promise
import org.restonfire.BaseFirebaseRestNamespaceFactory
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

/**
 * Base class for tests managing Firebase setup and token generation.
 */
abstract class AbstractTest extends Specification {

  private AsyncHttpClient asyncHttpClient = new AsyncHttpClient()
  private Gson gson = new GsonBuilder().create()
  private String namespaceUrl = System.getProperty("firebase.namespace") ?: System.getenv("FIREBASE_NAMESPACE")
  private String firebaseSecret = System.getProperty("firebase.secret") ?: System.getenv("FIREBASE_SECRET")

  private String firebaseToken
  private Promise<Map<String, Object>, FirebaseRuntimeException, Void> setupPromise

  void setup() {
    AsyncConditions cond = new AsyncConditions()

    setupPromise = BaseFirebaseRestNamespaceFactory.create(
        asyncHttpClient,
        gson,
        namespaceUrl,
        firebaseSecret
      )
      .getReference("/")
      .setValue(getDefaultDataSet())
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
        cond.evaluate{
          assert state == Promise.State.RESOLVED
        }
      })

    def payload = [
      uid: "1"
    ]

    def tokenGenerator = new TokenGenerator(firebaseSecret)
    firebaseToken = tokenGenerator.createToken(payload)

    cond.await(3)
  }

  FirebaseRestNamespace createNamespace() {
    assert firebaseToken != null

    return BaseFirebaseRestNamespaceFactory.create(
      asyncHttpClient,
      gson,
      namespaceUrl,
      firebaseToken
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
        toBeUpdated: [ foo: "bar" ]
      ],
      noReadAccess: "BIG SECRET"
    ]

    return defaultData
  }
}
