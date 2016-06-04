package org.restonfire.integrationTests

import org.jdeferred.Promise
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions

/**
 * Verifies ordered get operations against a real Firebase namespace.
 */
class OrderByValueOperationTest extends AbstractTest {

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

  def "Order by value without filter"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
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

  def "Order by value with start at"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .startAt("y")
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "y", "z" ] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by value with end at"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .endAt("y")
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "x", "y" ] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by value limit to first"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .limitToFirst(2)
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "x", "y" ] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by value limit to last"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .limitToLast(1)
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "z" ] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by value with start and end"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByValue()
      .startAt("y")
      .endAt("y")
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "y" ] as Queue
        val.each{ k, v -> assert v == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }
}
