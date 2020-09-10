[![CircleCI](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service.svg?style=svg)](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service)
[![Docker](https://img.shields.io/docker/v/mariamihai/sbm-beer-order-service?sort=date)](https://hub.docker.com/r/mariamihai/sbm-beer-order-service)

# SBM Beer Order Service
Spring Boot Microservice project.

  - [Description](#description)
  - [API version](#api-version)
  - [Docker images](#docker-images)
  - [Implementation details](#implementation-details)
    - [Properties](#properties)
    - [Environment variables for running locally](#environment-variables-for-running-locally)
    - [Available states](#available-states)
    - [Profiles](#profiles)
    - [Additional applications needed](#additional-applications-needed)
      - [MySQL](#mysql)
      - [JMS with ActiveMQ Artemis](#jms-with-activemq-artemis)
      - [Distributed Tracing with Spring Cloud Sleuth and Zipkin](#distributed-tracing-with-spring-cloud-sleuth-and-zipkin)
  - [API calls](#api-calls)
    - [Customer calls](#customer-calls)
      - [Obtain all customers](#obtain-all-customers)
    - [Beer Order calls](#beer-order-calls)
      - [Obtain all orders for customer](#obtain-all-orders-for-customer)
      - [Place new order for customer](#place-new-order-for-customer)
      - [Obtain order for customer](#obtain-order-for-customer)
      - [Pickup order for customer](#pickup-order-for-customer)
      - [Cancel order](#cancel-order)


## Description
The current project is part of the "Spring Boot Microservices with Spring Cloud" [Udemy course](https://www.udemy.com/course/spring-boot-microservices-with-spring-cloud-beginner-to-guru/). 

The project constantly places new orders, which are validated against the [Beer Service](https://github.com/mariamihai/udemy-sbm-beer-service) 
and allocated by the [Beer Inventory Service](https://github.com/mariamihai/udemy-sbm-beer-inventory-service).

An overview of all the projects involved can be found [here](https://github.com/mariamihai/udemy-sbm-overview).

## API version
_V1_ is the current implementation. No changes to the project are expected to be made in the future that will affect 
the existing endpoints.

## Docker images
Automatic building was not implemented for this project. The `latest` tag contains the best implementation considered 
appropriate to be used.

For automatic building of Docker images check the next projects:
- for [CircleCI](https://github.com/mariamihai/CIToDockerExampleProject)
- for [TravisCI](https://github.com/mariamihai/sma-overview) (all projects implemented under the "Spring Microservices in Action" book)

## Implementation details
### Properties
- the name of the application, used by Eureka and the other services 
```
spring.application.name=beer-order-service
```
- application server port
```
server.port=8081
```
- a new order is placed every 12 s under `TastingRoomService.placeTastingRoomOrder()` method
- the available states for an order can be found in the StateMachine configuration java class - `BeerOrderStateMachineConfig`

### Environment variables for running locally
**sbm.brewery.beer-service-host** contained originally the beer service host. 

As the project currently is being used with both Docker and running locally, I've added a new variable, 
BEER_SERVICE_HOST, which should be set for both environments. For local use, the value should be 
`BEER_SERVICE_HOST=http://localhost:8080 `. For creating a Docker container, the value is set in the docker-compose file.

### Available states
|State|Description|
|:---:|-----|
|NEW|State associated with a newly created order.|
|VALIDATION_PENDING|State associated with interrogating the Beer Service related to the validity of the requested beers from each beer line of the order.|
|VALIDATED|Valid upcs for the beers from each beer line of the order.|
|VALIDATION_EXCEPTION|Invalid upcs for at least one beer associated with the order. The compensating transaction consists of notifying the event.|
|ALLOCATION_PENDING|Validated order needs to check existing inventory.|
|ALLOCATED|Beer Inventory Service contains enough inventory to cover the current order.|
|ALLOCATION_EXCEPTION| The order couldn't be process by the inventory. The compensating transaction consists of notifying the event.|
|PENDING_INVENTORY|Order can be partially covered at the moment.|
|PICKED_UP|An allocated order can be picked up.|
|DELIVERED|An allocated order could be delivered. Potential state, currently not used.|
|DELIVERY_EXCEPTION|Any potential exception or error in the delivery of the order.|
|CANCELLED|An order can be cancelled at any time in the process, when the order is in the NEW, VALIDATION_PENDING, VALIDATED, ALLOCATION_PENDING or ALLOCATED state.|

### Profiles
Active profiles: `local`, `local-discovery`.

(The `localmysql` profile was used for the local MySQL connection when starting to develop the services and breaking 
the monolith, the `local` profile is obtained from the Config Service and it is used currently.)

### Additional applications needed
#### MySQL
When running locally, I am using a Docker container for the MySQL databases. Check the Docker Hub [MySQL page](https://hub.docker.com/_/mysql).

Creating the container:
```
docker run -p 3306:3306 --name beer-mysql -e MYSQL_ROOT_PASSWORD=root_pass -d mysql:8
```

The initial script for the project's database is under `src/main/scripts/mysql-init.sql` file.

| Property | Value | 
| --------| -----|
| database name | beerorderservice; |
| port | 3306 (default) |
| username | beer_order_service |
| password | password | 

#### JMS with ActiveMQ Artemis
JMS is used for communication with the Beer Inventory Service and Beer Order Service.

Creating the container:
```
docker run -it --rm -p 8161:8161 -p 61616:61616 vromero/activemq-artemis
```

The queues related to the current project are:

- `validate-order` - sending request for validating an order to the Beer Service
- `validate-order-result` - receiving the result back from the Beer Service
- `allocate-order` - sends an allocation request for an order to the Beer Inventory Service
- `allocate-order-response` - receives the result of the allocation from the Beer Inventory Service
- `allocate-order-failed` - not implemented - receives an exceptional case for the failed allocation (database issue, etc.)
- `de-allocate-order` - receives a deallocation response from the Beer Inventory Service if the order was invalidated

| Property | Value | 
| --------| -----|
| username | artemis |
| password | simetraehcapa | 

#### Distributed Tracing with Spring Cloud Sleuth and Zipkin
Creating the Zipkin container:
```
docker run --name zipkin -p 9411:9411 openzipkin/zipkin
```

## API calls
### Customer calls
#### Obtain all customers
 * __URI:__ _/api/v1/customers/_

 * __Method:__ _GET_

 * __Query params:__ <br/>
    * required: - <br/>
    * optional: - 
 
  * __URL params:__ <br/>
     * required: - <br/>
     * optional: <br/>
         pageNumber=[int] <br/>
         pageSize=[int]
    
 * __Success response:__
    * Code: 200 OK <br/>
    * Content:
       ``` 
       {
           "content": [
               {
                   "customerName": "Tasting Room",
                   "id": "cfb3291b-e0f8-4711-b188-82b8fdea17d4",
                   "version": 0,
                   "createdDate": "2020-09-03T12:21:44+0000",
                   "lastModifiedDate": "2020-09-03T12:21:44+0000"
               }
           ],
           "pageable": {
               "sort": {
                   "sorted": false,
                   "unsorted": true,
                   "empty": true
               },
               "pageNumber": 0,
               "pageSize": 25,
               "offset": 0,
               "paged": true,
               "unpaged": false
           },
           "totalElements": 1,
           "last": true,
           "totalPages": 1,
           "first": true,
           "sort": {
               "sorted": false,
               "unsorted": true,
               "empty": true
           },
           "size": 25,
           "number": 0,
           "numberOfElements": 1,
           "empty": false
       }
       ```
 
 `pageNumber` defaults to `0`.
 
 `pageSize` defaults to `25`.
    
### Beer Order calls
#### Obtain all orders for customer
 * __URI:__ _/api/v1/customers/:customerId/orders_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
    * optional: -

 * __Query params:__ <br/>
    * required: -
    * optional: <br/>
        pageNumber=[int] <br/>
        pageSize=[int] 
    
 * __Success response:__
    * Code: 200 OK <br/>
    * Content:
       ``` 
       {
           "content": [
               {
                   "customerId": "cfb3291b-e0f8-4711-b188-82b8fdea17d4",
                   "customerRef": "8b104830-07fe-491d-ad73-6f57281a2889",
                   "beerOrderLines": [
                       {
                           "upc": "0631234200036",
                           "beerName": "Mango Bobs",
                           "beerId": "f5d5fa59-26f2-4c55-83df-e841c9fd9c22",
                           "beerStyle": "IPA",
                           "price": 12.95,
                           "orderQuantity": 1,
                           "quantityAllocated": 1,
                           "id": "07548945-ff8e-4a66-b29b-370d8ddb9db5",
                           "version": 1,
                           "createdDate": "2020-09-03T12:32:08+0000",
                           "lastModifiedDate": "2020-09-03T12:32:08+0000"
                       }
                   ],
                   "orderStatus": "ALLOCATED",
                   "orderStatusCallbackUrl": null,
                   "id": "00cdc8c8-ffa4-4b38-b912-49da82fa5e8b",
                   "version": 4,
                   "createdDate": "2020-09-03T12:32:08+0000",
                   "lastModifiedDate": "2020-09-03T12:32:08+0000"
               },
               {
                   "customerId": "cfb3291b-e0f8-4711-b188-82b8fdea17d4",
                   "customerRef": "6b00d716-6b8e-4430-a06e-46de217158a6",
                   "beerOrderLines": [
                       {
                           "upc": "0083783375213",
                           "beerName": "Pinball Porter",
                           "beerId": "94d47759-9e0f-4ed5-bf75-caeda6206da5",
                           "beerStyle": "PALE_ALE",
                           "price": 11.25,
                           "orderQuantity": 5,
                           "quantityAllocated": 5,
                           "id": "88b394d0-58e1-453c-95be-4b317adeb2f0",
                           "version": 1,
                           "createdDate": "2020-09-03T12:57:44+0000",
                           "lastModifiedDate": "2020-09-03T12:57:44+0000"
                       }
                   ],
                   "orderStatus": "ALLOCATED",
                   "orderStatusCallbackUrl": null,
                   "id": "011070b0-ca17-4bfd-8e48-680a70a29889",
                   "version": 4,
                   "createdDate": "2020-09-03T12:57:44+0000",
                   "lastModifiedDate": "2020-09-03T12:57:44+0000"
               }
           ],
           "pageable": {
               "sort": {
                   "sorted": false,
                   "unsorted": true,
                   "empty": true
               },
               "pageNumber": 0,
               "pageSize": 2,
               "offset": 0,
               "paged": true,
               "unpaged": false
           },
           "totalElements": 386,
           "last": false,
           "totalPages": 193,
           "first": true,
           "sort": {
               "sorted": false,
               "unsorted": true,
               "empty": true
           },
           "size": 2,
           "number": 0,
           "numberOfElements": 2,
           "empty": false
       }
       ```
    
#### Place new order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders_

 * __Method:__ _POST_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
    * optional: - <br/>

 * __Data params:__ <br/>
    * required: - <br/>
    * optional: <br/>
        beerOrderDto=[BeerOrderDto]
        ``` 
        {
            "customerRef": "8b104830-07fe-491d-ad73-6f57281a2889",
            "beerOrderLines": [
                {
                    "upc": "0631234200036",
                    "beerName": "Mango Bobs",
                    "beerId": "f5d5fa59-26f2-4c55-83df-e841c9fd9c22",
                    "beerStyle": "IPA",
                    "price": 12.95,
                    "orderQuantity": 1
                }
            ]
        }
        ```
        
 * __Success response:__
    * Code: 201 CREATED <br/>
    * Content:
       ``` 
       {
           "customerId": "cfb3291b-e0f8-4711-b188-82b8fdea17d4",
           "customerRef": "8b104830-07fe-491d-ad73-6f57281a2889",
           "beerOrderLines": [
               {
                   "upc": "0631234200036",
                   "beerName": "Mango Bobs",
                   "beerId": "f5d5fa59-26f2-4c55-83df-e841c9fd9c22",
                   "beerStyle": "IPA",
                   "price": 12.95,
                   "orderQuantity": 1,
                   "quantityAllocated": null,
                   "id": "280a3ea3-4d61-4682-ab34-697aeb99a272",
                   "version": 0,
                   "createdDate": "2020-09-03T13:57:06+0000",
                   "lastModifiedDate": "2020-09-03T13:57:06+0000"
               }
           ],
           "orderStatus": "VALIDATION_PENDING",
           "orderStatusCallbackUrl": null,
           "id": "47de2148-5e71-4056-a7cd-a36d2b4405a0",
           "version": 1,
           "createdDate": "2020-09-03T13:57:06+0000",
           "lastModifiedDate": "2020-09-03T13:57:07+0000"
       }
       ```
    ```
    
#### Obtain order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders/:orderId_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
        orderId=[uuid] <br/>
    * optional: - <br/>
    
 * __Success response:__
    * Code: 200 OK <br/>
    * Content:
       ``` 
       {
           "customerId": "cfb3291b-e0f8-4711-b188-82b8fdea17d4",
           "customerRef": "8b104830-07fe-491d-ad73-6f57281a2889",
           "beerOrderLines": [
               {
                   "upc": "0631234200036",
                   "beerName": "Mango Bobs",
                   "beerId": "f5d5fa59-26f2-4c55-83df-e841c9fd9c22",
                   "beerStyle": "IPA",
                   "price": 12.95,
                   "orderQuantity": 1,
                   "quantityAllocated": null,
                   "id": "280a3ea3-4d61-4682-ab34-697aeb99a272",
                   "version": 0,
                   "createdDate": "2020-09-03T13:57:06+0000",
                   "lastModifiedDate": "2020-09-03T13:57:06+0000"
               }
           ],
           "orderStatus": "PENDING_INVENTORY",
           "orderStatusCallbackUrl": null,
           "id": "47de2148-5e71-4056-a7cd-a36d2b4405a0",
           "version": 4,
           "createdDate": "2020-09-03T13:57:06+0000",
           "lastModifiedDate": "2020-09-03T13:57:07+0000"
       }
       ```
    
#### Pickup order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders/:orderId/pickup_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
        orderId=[uuid] <br/>
    * optional: - <br/>
    
 * __Success response:__
    * Code: 204 NO CONTENT <br/>
    
#### Cancel order
No endpoint was added for the actual cancelling of an order but a status and an event are added for this possibility.
All states and associated actions (including the cancelling of an order) have been tested under the `BeerOrderManagerImplIT` class.