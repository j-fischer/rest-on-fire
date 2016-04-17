package org.restonfire;

/**
 * Dummy test.
 *
 * @author Johannes Fischer
 * @since 16.04.2016
 */
class DummyTest extends AbstractTest {

    def "Check something important"() {

        when: "do something"
        Integer checkAssignment = 1
        then: "check result"
        checkAssignment == 1
    }
}
