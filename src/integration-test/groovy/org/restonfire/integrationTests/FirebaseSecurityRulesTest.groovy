package org.restonfire.integrationTests

import com.google.gson.Gson
import org.jdeferred.Promise
import org.restonfire.FirebaseRestNamespace
import org.restonfire.FirebaseSecurityRulesReference
import org.restonfire.exceptions.FirebaseAccessException
import org.restonfire.exceptions.FirebaseRuntimeException
import org.restonfire.responses.FirebaseSecurityRules
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.util.concurrent.AsyncConditions

/**
 * Verifies security rules operations against a real Firebase namespace.
 */
class FirebaseSecurityRulesTest extends AbstractTest {

  static Logger LOG = LoggerFactory.getLogger(FirebaseSecurityRulesTest.class)

  private FirebaseRestNamespace namespaceWithToken;
  private FirebaseRestNamespace namespaceWithSecret;

  void setup() {
    // Run this inside the setup to ensure that the setup function in the AbstractTest class is completed before the
    // namespace is crated. This ensures that the token will be created
    namespaceWithToken = createNamespace()
    namespaceWithSecret = createNamespaceWithSecret()
  }

  def "Get current security rules without permission"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve the current security rules without permission"
    namespaceWithToken.getSecurityRules()
      .get()
      .always({ Promise.State state, FirebaseSecurityRules val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert val == null
        assert ex.getClass() == FirebaseAccessException.class
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Get current security rules"() {
    AsyncConditions cond = new AsyncConditions();

    when: "making request to retrieve the current security rules"
    namespaceWithSecret.getSecurityRules()
      .get()
      .always({ Promise.State state, FirebaseSecurityRules val, FirebaseRuntimeException ex ->
      cond.evaluate {
        assert ex == null
        assert val != null
        assert val.rules[FirebaseSecurityRules.READ_KEY] == false
        assert val.rules[FirebaseSecurityRules.WRITE_KEY] == false
      }
    })
    then: "wait for result evaluation"
    cond.await(5);
  }

  def "Set security rules"() {
    AsyncConditions[] conditions = [new AsyncConditions(), new AsyncConditions(), new AsyncConditions()]
    int conditionIndex = 0

    FirebaseSecurityRulesReference ref = namespaceWithSecret.getSecurityRules()

    when: "making request to retrieve a string value without permission"

    ref
      .get()
      .always({ Promise.State getState, FirebaseSecurityRules getVal, FirebaseRuntimeException getEx ->
      conditions[conditionIndex++].evaluate {
        assert getEx == null
        assert getVal != null
      }

      getVal.rules.put(FirebaseSecurityRules.READ_KEY, true);

      LOG.info("New FB rules: " + new Gson().toJson(getVal))
      ref
        .set(getVal)
        .always({ Promise.State setState, FirebaseSecurityRules setVal, FirebaseRuntimeException setEx ->

        conditions[conditionIndex++].evaluate {
          assert setEx == null
          assert setVal == getVal
        }

        namespaceWithToken
          .getReference("noReadAccess")
          .getValue(String.class)
          .always({ Promise.State state, String val, FirebaseRuntimeException ex ->

          conditions[conditionIndex++].evaluate {
            assert getEx == null
            assert val == "BIG SECRET"
          }
        })
      })
    })
    then: "wait for result evaluation"
    conditions[0].await(15);
    conditions[1].await(15);
    conditions[2].await(15);
  }
}
