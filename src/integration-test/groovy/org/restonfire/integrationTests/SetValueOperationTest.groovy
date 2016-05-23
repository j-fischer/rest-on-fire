package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.integrationTests.data.SampleData
import spock.util.concurrent.AsyncConditions

/**
 * Verifies set operations against a real Firebase namespace.
 */
class SetValueOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to set value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set the integer value"
    def ref = namespace.getReference("testData/integer")

    ref
      .setValue(4)
      .always({ Promise.State setState, Integer setResult, FirebaseRuntimeException setEx ->
        def getValPromise = ref.getValue(Integer.class)

        getValPromise.always { Promise.State getState, Integer getResult, FirebaseRuntimeException getEx ->
          cond.evaluate {
            assert setResult == null
            assert setEx.getClass() == FirebaseAccessException.class

            assert getEx == null
            assert getResult == 1
          }
        }
      })

    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Set value for integer type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set integer value"
    def ref = namespace.getReference("testData/toBeSet")

    ref
      .setValue(2)
      .always({ Promise.State setState, Integer setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(Integer.class)

      getValPromise.always { Promise.State getState, Integer getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult == 2

          assert getEx == null
          assert getResult == 2
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Set value for String type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set string value"
    def ref = namespace.getReference("testData/toBeSet")

    def newValue = "some different string"
    ref
      .setValue(newValue)
      .always({ Promise.State setState, String setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(String.class)

      getValPromise.always { Promise.State getState, String getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult == newValue

          assert getEx == null
          assert getResult == newValue
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Set value for SampleData type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to set string value"
    def ref = namespace.getReference("testData/toBeSet")

    def newValue = new SampleData(foo: "new bar")
    ref
      .setValue(newValue)
      .always({ Promise.State setState, SampleData setResult, FirebaseRuntimeException setEx ->
      def getValPromise = ref.getValue(SampleData.class)

      getValPromise.always { Promise.State getState, SampleData getResult, FirebaseRuntimeException getEx ->
        cond.evaluate {
          assert setEx == null
          assert setResult.foo == "new bar"

          assert getEx == null
          assert getResult.foo == "new bar"
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }
}
