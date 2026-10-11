package com.restaurant.pos

import spock.lang.Specification

class TicketMathSpec extends Specification {
    def 'percentage tips use exact decimal monetary arithmetic'() {
        given:
        def subtotal = new BigDecimal('140.00')

        when:
        def tip = subtotal.multiply(new BigDecimal('15')).divide(new BigDecimal('100'))

        then:
        tip == new BigDecimal('21.00')
        subtotal + tip == new BigDecimal('161.00')
    }

    def 'fixed tips are added to the subtotal'() {
        expect:
        new BigDecimal('85.00') + new BigDecimal('12.50') == new BigDecimal('97.50')
    }
}
