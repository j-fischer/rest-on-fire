package org.restonfire.integrationTests

import org.jdeferred.DoneCallback
import org.jdeferred.Promise
import org.restonfire.FirebaseRestNamespace
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.integrationTests.data.SampleData
import spock.util.concurrent.AsyncConditions

/**
 * Verifies get operations against a real Firebase namespace.
 */
class GetValueOperationTest extends AbstractTest {

  private FirebaseRestNamespace namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to get value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve a string value without permission"
    namespace.getReference("noReadAccess")
      .getValue(String.class)
      .always({ Promise.State state, String val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert val == null
        assert ex.getClass() == FirebaseAccessException.class
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
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

  def "Get value for boolean type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/bool")
      .getValue(Boolean.class)
      .always({ Promise.State state, Boolean val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get value for string type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/text")
      .getValue(String.class)
      .always({ Promise.State state, String val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val == "aString"
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get value for string type with done callback"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/text")
      .getValue(String.class)
      .done(new DoneCallback<String>() {
        @Override
        void onDone(String result) {
          cond.evaluate {
            assert result == "aString"
          }
        }
      })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get value for SampleData type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/sampleData")
      .getValue(SampleData.class)
      .always({ Promise.State state, SampleData val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val != null
        assert val.foo == "bar"
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }
}
