# Object Finder Library.
A simple object finder library that supports a predicate based queries and CQN based queries for objects filtering.

```
<dependency>
  <groupId>com.github.saiprasadkrishnamurthy.objectfinder</groupId>
  <artifactId>object-finder</artifactId>
  <version>1.0</version>
</dependency>
```
For example we have a list of Developer POJOs (which can be visualised as below):
```
      [
           {
               "name": "Sai",
               "language": "Kotlin",
               "framework": "SparkJava"
           },
           {
               "name": "Sai",
               "language": "Java",
               "framework": "SpringBoot"
           },
           {
               "name": "Sai",
               "language": "Scala",
               "framework": "Play"
           }
      ]

```

### Filter by Predicate Query.
```
ObjectFinder objectFinder = new ObjectFinder();
List<Developer> scalaDevelopers = objectFinder.findByPredicateQuery(developers, "language='Scala'");
List<Developer> kotlinAndJavaDevelopers = objectFinder.findByPredicateQuery(developers, "(language='Java' OR language='Kotlin')");
List<Developer> kotlinAndJavaDevelopersWithSpring = objectFinder.findByPredicateQuery(developers, "(framework='SpringBoot' AND (language='Java' OR language='Kotlin'))");
```

### Filter by CQN Query.
CQN is a canonical query in prefix notation which can be easy to construct programatically (for ex: using a query builder)
```
ObjectFinder objectFinder = new ObjectFinder();
List<Developer> scalaDevelopers = objectFinder.findByCQNQuery(developers, "equal("language","Scala"));
List<Developer> kotlinAndJavaDevelopers = objectFinder.findByCQNQuery(developers, "or(equal("language","Java"), equal("language","Kotlin"))");
List<Developer> kotlinAndJavaDevelopersWithSpring = objectFinder.findByCQNQuery(developers, "and(equal("framework","SpringBoot"), or(equal("language","Java",equal("language","Kotlin")))");
```  

This uses https://github.com/npgall/cqengine under the hood.