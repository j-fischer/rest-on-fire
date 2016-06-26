package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions

/**
 * Unit tests for retrieving shallow values.
 */
class GetShallowValueOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to get value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve a string value without permission"
    namespace.getReference("noReadAccess")
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
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
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val == new Integer(1)
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get value for boolean type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/bool")
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val == true
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get value for string type"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/text")
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val == "aString"
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get shallow value for map of objects"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/dinosaurs")
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedKeysInOrder = [ "lambeosaurus", "stegosaurus" ] as Queue
        ((Map<String, Object>)val).each{ k, v ->
          assert k == expectedKeysInOrder.poll();
          assert v == true
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Get shallow value for map of strings"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve integer value"
    namespace.getReference("testData/sortedStrings")
      .getShallowValue()
      .always({ Promise.State state, Object val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedKeysInOrder = [ "a", "b", "c" ] as Queue
        ((Map<String, Object>)val).each{ k, v ->
          assert k == expectedKeysInOrder.poll();
          assert v == true
        }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }
}
