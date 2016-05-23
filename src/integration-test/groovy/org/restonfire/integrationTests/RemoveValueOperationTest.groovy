package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions

/**
 * Verifies removal operations against a real Firebase namespace.
 */
class RemoveValueOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to remove value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to remove a string value without permission"
    namespace.getReference("testData/text")
      .removeValue()
      .always({ Promise.State state, Void val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert val == null
        assert ex.getClass() == FirebaseAccessException.class
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Remove value"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to remove value"
    def ref = namespace.getReference("testData/toBeRemoved")

    ref
      .removeValue()
      .always({ Promise.State setState, Void setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(String.class)

      getValPromise.always { Promise.State getState, String getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setResult == null
          assert setEx == null

          assert getEx == null
          assert getResult == null
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }
}
