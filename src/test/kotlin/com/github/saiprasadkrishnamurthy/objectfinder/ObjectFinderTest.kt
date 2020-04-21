package com.github.saiprasadkrishnamurthy.objectfinder

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

/**
 * @author Sai.
 */
class ObjectFinderTest {

    private val objectFinder = ObjectFinder()

    @Test
    fun `finds objects by predicate query`() {
        val results = objectFinder.findByPredicateQuery(developers, "language='Scala'")
        assertThat(results, equalTo(developers.filter { it.language == "Scala" }))
    }

    @Test
    fun `finds objects by predicate query with multiple conditions`() {
        val results = objectFinder.findByPredicateQuery(developers, "(language='Kotlin' AND framework='SpringBoot')")
        assertThat(results, equalTo(developers.filter { it.language == "Kotlin" && it.framework == "SpringBoot" }))
    }

    @Test
    fun `finds objects by predicate query with complex nested conditions`() {
        val results = objectFinder.findByPredicateQuery(developers, "((language='Kotlin' OR language='Java') AND framework='SpringBoot')")
        assertThat(results, equalTo(developers.filter { (it.language == "Kotlin" || it.language == "Java") && it.framework == "SpringBoot" }))
    }

    @Test
    fun `finds objects by predicate query no results found`() {
        val results = objectFinder.findByPredicateQuery(developers, "language='Python'")
        assertThat(results, equalTo(emptyList()))
    }

    @Test
    fun `finds objects by predicate query invalid attribute`() {
        val exception = assertFailsWith(BadQuery::class) { objectFinder.findByPredicateQuery(developers, "nonExistentAttribute='Python'") }
        assertThat(exception.message, equalTo("No such attribute has been registered with the parser: nonExistentAttribute"))
    }

    @Test
    fun `finds objects by predicate query malformed query`() {
        val exception = assertFailsWith(BadQuery::class) { objectFinder.findByPredicateQuery(developers, "(language='Kotlin' AND framework='SpringBoot'") }
        assertThat(exception.message, equalTo("Failed to parse query at line 1:70: mismatched input '<EOF>' expecting {')', K_AND}"))
    }

    @Test
    fun `finds objects by CQN query`() {
        val results = objectFinder.findByCQNQuery(developers, "equal(\"language\",\"Scala\")")
        assertThat(results, equalTo(developers.filter { it.language == "Scala" }))
    }

    @Test
    fun `finds objects by CQN query multiple conditions`() {
        val results = objectFinder.findByCQNQuery(developers, "and(equal(\"language\",\"Scala\"), equal(\"framework\",\"Play\"))")
        assertThat(results, equalTo(developers.filter { it.language == "Scala" && it.framework == "Play" }))
    }

    @Test
    fun `finds objects by CQN query multiple nested conditions`() {
        val results = objectFinder.findByCQNQuery(developers, "and(or(equal(\"language\",\"Java\"),equal(\"language\",\"Kotlin\")), equal(\"framework\",\"SpringBoot\"))")
        assertThat(results, equalTo(developers.filter { (it.language == "Kotlin" || it.language == "Java") && it.framework == "SpringBoot" }))
    }

    @Test
    fun `finds objects by CQN query no results found`() {
        val results = objectFinder.findByCQNQuery(developers, "equal(\"language\",\"Python\")")
        assertThat(results, equalTo(emptyList()))
    }

    @Test
    fun `finds objects by CQN query invalid attribute`() {
        val exception = assertFailsWith(BadQuery::class) { objectFinder.findByCQNQuery(developers, "equal(\"nonExistentAttribute\",\"Python\")") }
        assertThat(exception.message, equalTo("No such attribute has been registered with the parser: nonExistentAttribute"))
    }

    @Test
    fun `finds objects by CDN query malformed query`() {
        val exception = assertFailsWith(BadQuery::class) { objectFinder.findByCQNQuery(developers, "equal(\"language\",\"Scala\"") }
        assertThat(exception.message, equalTo("Failed to parse query at line 1:24: missing ')' at '<EOF>'"))
    }
}