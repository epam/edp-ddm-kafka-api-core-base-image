# kafka-api-core

This library contains **all** the business logic of the Kafka API microservice which is to be generated with `service-generation-utility`. The Kafka API microservice is to process Kafka events from `Rest API` microservice and work with DB.

# Related components
* `model-core`
* Kafka
* DB

# Extension points
1. retrieval operations (GET) are not fully supported yet
2. modifying operations (POST, PUT, PATCH, DELETE)
    * `GenericQueryListener` to be sub-classed as a Kafka Listener
    * `AbstractCommandHandler` to be sub-classed as a DB-layer

# Deployment
The library is delivered as a docker image with all dependencies inside.
