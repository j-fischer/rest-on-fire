package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions

/**
 * Verifies ordered get operations against a real Firebase namespace.
 */
class OrderByOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Try to get ordered value without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve a string values without permission"
    namespace.getReference("noReadAccess")
      .query()
      .orderByValue()
      .run(Map.class)
      .always({ Promise.State state, String val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert val == null
        assert ex.getClass() == FirebaseAccessException.class
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Get ordered string list"() {
    AsyncConditions cond = new AsyncConditions();

    when: "only using order by does not really order the results"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "x", "z", "y"] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }
}
