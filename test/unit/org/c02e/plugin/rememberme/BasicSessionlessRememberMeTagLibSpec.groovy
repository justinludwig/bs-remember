package org.c02e.plugin.rememberme

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(BasicSessionlessRememberMeTagLib)
@Mock(BasicSessionlessRememberMeService)
class BasicSessionlessRememberMeTagLibSpec extends Specification {

	def "user renders nothing by default"() {
        setup: service { it.demand.getUser(1..1) { -> null } }
        expect:
            applyTemplate('<rememberme:user />') == ''
            applyTemplate('<rememberme:user property="foo" />') == ''
	}

	def "user renders property when remembered and specified"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'bar'] } }
        expect: applyTemplate('<rememberme:user property="foo" />') == 'bar'
	}

	def "user renders nothing when no property"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'bar'] } }
        expect: applyTemplate('<rememberme:user />') == ''
	}

	def "user renders nothing when property is falsey"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: false] } }
        expect: applyTemplate('<rememberme:user property="foo" />') == ''
	}

	def "user renders objects to string"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: [1]] } }
        expect: applyTemplate('<rememberme:user property="foo" />') == '[1]'
	}

	def "user encodes value as HTML"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'M&M'] } }
        expect: applyTemplate('<rememberme:user property="foo" />') == 'M&amp;M'
	}


	def "withUser renders nothing by default"() {
        expect:
            applyTemplate('<rememberme:withUser>foo</rememberme:withUser>') == ''
            applyTemplate('<rememberme:withUser var="u">foo</rememberme:withUser>') == ''
	}

	def "withUser renders body when remembered"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'bar'] } }
        expect: applyTemplate(
            '<rememberme:withUser>foo</rememberme:withUser>'
        ) == 'foo'
	}

	def "withUser makes user properties available via user var by default"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'bar'] } }
        expect: applyTemplate(
            '<rememberme:withUser>${user.foo}</rememberme:withUser>'
        ) == 'bar'
	}

	def "withUser makes user properties available via custom var"() {
        setup: service { it.demand.getUser(1..1) { -> [foo: 'bar'] } }
        expect: applyTemplate(
            '<rememberme:withUser var="u">${u.foo}</rememberme:withUser>'
        ) == 'bar'
	}


	def "yes renders nothing by default"() {
        expect: applyTemplate('<rememberme:yes>yes</rememberme:yes>') == ''
	}

	def "when remembered, yes renders body"() {
        setup: service { it.demand.isRemembered(1..1) { -> true } }
        expect: applyTemplate('<rememberme:yes>yes</rememberme:yes>') == 'yes'
	}


	def "no renders body by default"() {
        expect: applyTemplate('<rememberme:no>no</rememberme:no>') == 'no'
	}

	def "when remembered, no renders nothing"() {
        setup: service { it.demand.isRemembered(1..1) { -> true } }
        expect: applyTemplate('<rememberme:no>no</rememberme:no>') == ''
	}


    protected service(Closure c) {
        def control = mockFor(BasicSessionlessRememberMeService)
        c.call(control)
        tagLib.basicSessionlessRememberMeService = control.createMock()
    }
}
