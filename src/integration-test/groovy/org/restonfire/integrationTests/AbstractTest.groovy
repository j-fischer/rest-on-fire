package org.restonfire.integrationTests

import com.firebase.security.token.TokenGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.AsyncHttpClientConfig
import org.jdeferred.Promise
import org.restonfire.BaseFirebaseRestDatabaseFactory
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseRuntimeException
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

    setupPromise = factory.create(
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

    cond.await(10)
  }

  FirebaseRestDatabase createNamespace() {
    assert firebaseToken != null

    return factory.create(
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
