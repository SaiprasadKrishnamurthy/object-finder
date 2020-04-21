package com.github.saiprasadkrishnamurthy.objectfinder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.googlecode.cqengine.ConcurrentIndexedCollection
import com.googlecode.cqengine.attribute.SimpleNullableAttribute
import com.googlecode.cqengine.query.option.QueryOptions
import com.googlecode.cqengine.query.parser.common.InvalidQueryException
import com.googlecode.cqengine.query.parser.cqn.CQNParser
import com.googlecode.cqengine.query.parser.sql.SQLParser

/**
 * <b>Object Finder By Predicate.</b>
 * Finds the objects by means of a simple query DSL that represents a predicate.
 *
 * For example consider the following sample:
 * <pre>
 *     [
 *      {
 *          "name": "Sai",
 *          "language": "Kotlin",
 *          "framework": "SparkJava"
 *      },
 *      {
 *          "name": "Sai",
 *          "language": "Java",
 *          "framework": "SpringBoot"
 *      },
 *      {
 *          "name": "Sai",
 *          "language": "Scala",
 *          "framework": "Play"
 *      }
 *     ]
 * </pre>
 *
 * The following predicates are valid:
 * <pre>
 *      name = 'Sai'
 * </pre>
 *
 * <b>If you use 'AND' 'OR' you must use parantheses</b>
 * <pre>
 *      (name = 'Sai' AND language='Kotlin')
 * </pre>
 *
 * <b>Nested conditions</b>
 * <pre>
 *      (name = 'Sai' AND (language='Kotlin' OR language='Java') AND framework='SpringBoot')
 * </pre>

<br/>
 * <b>Object Finder By CQN.</b>
 * <p>CQN is a canonical query in prefix notation which can be easy to construct programatically (for ex: using a query builder).</p>
 * For example consider the following sample:
 * <pre>
 *     [
 *      {
 *          "name": "Sai",
 *          "language": "Kotlin",
 *          "framework": "SparkJava"
 *      },
 *      {
 *          "name": "Sai",
 *          "language": "Java",
 *          "framework": "SpringBoot"
 *      },
 *      {
 *          "name": "Sai",
 *          "language": "Scala",
 *          "framework": "Play"
 *      }
 *     ]
 * </pre>
 *
 * The following predicates are valid:
 * <pre>
 *      equal("name","Sai")
 * </pre>
 *
 * <pre>
 *      and(equal("name","Sai"), equal("framework","SpringBoot"))
 * </pre>
 *
 * <pre>
 *      and(equal("name","Sai"), or(equal("language","Java"),equal("language","Kotlin")), equal("framework","SpringBoot"))
 * </pre>
 *
 * @author Sai.
 */
class ObjectFinder {

    /**
     * Finds the matching objects by predicate query.
     * @param objects input objects.
     * @param query query predicate DSL.
     * @return objects matching the predicate.
     */
    fun <T> findByPredicateQuery(objects: List<T>, query: String): List<T> {
        try {
            val (coll, mapBackToObjects, attributes) = triple(objects)
            val parser = SQLParser.forPojoWithAttributes(Map::class.java, attributes)
            val resultSet = parser.retrieve(coll, "SELECT * FROM docs WHERE $query")
            return resultSet.map { mapBackToObjects[it]!! }
        } catch (iqe: InvalidQueryException) {
            if (iqe.cause != null) {
                throw BadQuery(iqe.cause?.message)
            }
            throw BadQuery(iqe.message)
        }
    }

    /**
     * Finds the matching objects by CQN query.
     * A few examples:
     * <pre>
     *      and(equal("name","Sai"), equal("framework","SpringBoot"))
     * </pre>
     *
     * <pre>
     *      and(equal("name","Sai"), or(equal("language","Java"),equal("language","Kotlin")), equal("framework","SpringBoot"))
     * </pre>
     * @param objects input objects.
     * @param query query predicate DSL.
     * @return objects matching the predicate.
     */
    fun <T> findByCQNQuery(objects: List<T>, query: String): List<T> {
        try {
            val (coll, mapBackToObjects, attributes) = triple(objects)
            val parser = CQNParser.forPojoWithAttributes(Map::class.java, attributes)
            val resultSet = parser.retrieve(coll, query)
            return resultSet.map { mapBackToObjects[it]!! }
        } catch (iqe: InvalidQueryException) {
            if (iqe.cause != null) {
                throw BadQuery(iqe.cause?.message)
            }
            throw BadQuery(iqe.message)
        }
    }

    private fun <T> triple(objects: List<T>): Triple<ConcurrentIndexedCollection<Map<*, *>>, MutableMap<Map<*, *>, T>, MutableMap<String, SimpleNullableAttribute<Map<*, *>, *>>> {
        val objectMapper = jacksonObjectMapper()
        val coll = ConcurrentIndexedCollection<Map<*, *>>()
        val mapBackToObjects = mutableMapOf<Map<*, *>, T>()
        val attributes = mutableMapOf<String, SimpleNullableAttribute<Map<*, *>, *>>()
        objects
                .forEach {
                    val objectAsMap = objectMapper.convertValue(it, Map::class.java)
                    objectAsMap.entries.forEach { entry ->
                        val key = entry.key.toString()
                        val pair = when (entry.value) {
                            is Number -> Pair(key, addNumericIndex(key))
                            else -> Pair(key, addStringIndex(key))
                        }
                        attributes.plusAssign(pair)
                    }
                    mapBackToObjects[objectAsMap] = it
                    coll.add(objectAsMap)
                }
        return Triple(coll, mapBackToObjects, attributes)
    }

    private fun addStringIndex(attribute: String) = object : SimpleNullableAttribute<Map<*, *>, String>(attribute) {
        override fun getValue(o: Map<*, *>?, p1: QueryOptions?): String? {
            return o?.get(attribute).toString()
        }
    }

    private fun addNumericIndex(attribute: String) = object : SimpleNullableAttribute<Map<*, *>, Double>(attribute) {
        override fun getValue(o: Map<*, *>?, p1: QueryOptions?): Double? {
            return o?.get(attribute).toString().toDouble()
        }
    }
}