package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions
/**
 * Verifies operations with priorities against a real Firebase database.
 */
class PriorityOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to get priority without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to get priority without permission"
    namespace.getReference("noReadAccess")
      .getPriority()
      .always({ Promise.State state, String val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert val == null
        assert ex.getClass() == FirebaseAccessException.class
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get priority for value without a priority"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to get priority"
    namespace.getReference("testData/text")
      .getPriority()
      .always({ Promise.State state, Double val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val == null
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Set priority for value"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set priority"
    def ref = namespace.getReference("testData/toBeSet")

    ref
      .setPriority(3.0)
      .always({ Promise.State setState, Void val, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getPriority()

      getValPromise.always { Promise.State getState, Double getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          
          assert getEx == null
          assert getResult == 3.0
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }
}
