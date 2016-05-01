package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions

/**
 * Verifies basic operations against a real Firebase namespace.
 */
class BasicFirebaseOperationsTest extends AbstractTest {

  private FirebaseRestNamespace namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespaceWithToken()
  }

  def "Get value for integer type"() {
    AsyncConditions cond = new AsyncConditions();

      when: "making request to retrieve integer value"
      namespace.getReference("testData/integer")
        .getValue(Integer.class)
        .always({ Promise.State state, Integer val, FirebaseRuntimeException ex ->
        cond.evaluate {
          assert ex == null
          assert val == 1
        }
      })
    then: "wait for result evaluation"
      cond.await(3);
  }
}
