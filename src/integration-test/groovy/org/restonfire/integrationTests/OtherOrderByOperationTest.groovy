package org.restonfire.integrationTests

import org.jdeferred.DeferredManager
import org.jdeferred.DoneCallback
import org.jdeferred.Promise
import org.jdeferred.impl.DefaultDeferredManager
import org.jdeferred.multiple.MultipleResults
import org.restonfire.FirebaseRestDatabase
import org.restonfire.exceptions.FirebaseRuntimeException
import spock.util.concurrent.AsyncConditions
/**
 * Tests for various queries requireing the order-by clause.
 */
class OtherOrderByOperationTest extends AbstractTest {

  private FirebaseRestDatabase namespace;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespace = createNamespace()
  }

  def "Get ordered key without filter"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByKey()
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "a", "b", "c"] as Queue
        val.each{ k, v -> assert k == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by key with start at"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStrings")
      .query()
      .orderByKey()
      .startAt("b")
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "b", "c" ] as Queue
        val.each{ k, v -> assert k == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by child with end at"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/dinosaurs")
      .query()
      .orderByChild("length")
      .endAt("z")
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "stegosaurus", "lambeosaurus" ] as Queue
        val.each{ k, v -> assert k == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(3);
  }

  def "Order by priority with end at"() {
    AsyncConditions cond = new AsyncConditions();

    when: "querying for records"
    namespace.getReference("testData/sortedStringsWithPriorities")
      .query()
      .orderByPriority()
      .endAt(2.9)
      .run(Map.class)
      .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null

        def expectedValuesInOrder = [ "c", "b" ] as Queue
        val.each{ k, v -> assert k == expectedValuesInOrder.poll() }
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Order by priority after overriding it"() {
    AsyncConditions cond = new AsyncConditions();

    DeferredManager deferredManager = new DefaultDeferredManager();

    def sortedStringsRef = namespace.getReference("testData/sortedStringsWithPriorities")

    def p1 = sortedStringsRef.child("c").setPriority(1.0);
    def p2 = sortedStringsRef.child("a").setPriority(2.0);
    def p3 = sortedStringsRef.child("b").setPriority(3.0);

    when: "querying for records"

    deferredManager.when(p1, p2, p3)
      .done(new DoneCallback<MultipleResults>() {
      @Override
      void onDone(MultipleResults result) {

        sortedStringsRef
          .query()
          .orderByPriority()
          .startAt(1.0)
          .run(Map.class)
          .always({ Promise.State state, Map<String, String> val, FirebaseRuntimeException ex ->
          cond.evaluate {
            assert ex == null

            def expectedValuesInOrder = [ "c", "a", "b" ] as Queue
            val.each{ k, v -> assert k == expectedValuesInOrder.poll() }
          }
        })
      }
    })

    then: "wait for result evaluation"
    cond.await(10)
  }

}
