package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions
/**
 * Verifies update operations against a real Firebase namespace.
 */
class UpdateValueOperationTest extends AbstractTest {
  private FirebaseRestNamespace namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to update value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set the integer value"
    def ref = namespace.getReference("testData/sampleData")

    ref
      .updateValue([ foo: "newVal" ])
      .always({ Promise.State setState, Map<String, Object> setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(Map.class)

      getValPromise.always { Promise.State getState, Map<String, Object> getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setResult == null
          assert setEx.getClass() == FirebaseAccessException.class

          assert getEx == null
          assert getResult == [foo: "bar"]
        }
      }
    })

    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Update value with new property"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to update value by adding new property"
    def ref = namespace.getReference("testData/toBeUpdated")

    ref
      .updateValue([ newInt: 10 ])
      .always({ Promise.State setState, Map<String, Object> setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(Map.class)

      getValPromise.always { Promise.State getState, Map<String, Object> getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult == [ newInt: 10 ]

          assert getEx == null
          assert getResult == [ newInt: 10, foo: "bar" ]
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Update value existing property"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to update value by updating existing property"
    def ref = namespace.getReference("testData/toBeUpdated")

    ref
      .updateValue([ foo: "another bar" ])
      .always({ Promise.State setState, Map<String, Object> setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(Map.class)

      getValPromise.always { Promise.State getState, Map<String, Object> getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult == [ foo: "another bar" ]

          assert getEx == null
          assert getResult == [ foo: "another bar" ]
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Update value removing property"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request with null value to be ignored"
    def ref = namespace.getReference("testData/toBeUpdated")

    ref
      .updateValue([ foo: null, newProp: "bar" ])
      .always({ Promise.State setState, Map<String, Object> setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(Map.class)

      getValPromise.always { Promise.State getState, Map<String, Object> getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult == [ foo: null, newProp: "bar" ]

          assert getEx == null
          assert getResult == [ foo: "bar", newProp: "bar" ]
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }
}
