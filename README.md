[![CircleCI](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service.svg?style=svg)](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service)
[![Docker](https://img.shields.io/docker/v/mariamihai/sbm-beer-order-service?sort=date)](https://hub.docker.com/r/mariamihai/sbm-beer-order-service)

# SBM Beer Order Service
Spring Boot Microservice project.

  - [API Version](#api-version)
  - [Docker images](#docker-images)
  - [Implementation Details](#implementation-details)
    - [Properties](#properties)
    - [Environment variables for running locally](#environment-variables-for-running-locally)
    - [Available states](#available-states)
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

## API Version
_V1_ is the current implementation. No changes to the project are expected to be made in the future that will affect 
the existing endpoints.

## Docker images
Automatic building was not implemented for this project. The `latest` tag contains the best implementation considered 
appropriate to be used.

For automatic building of Docker images check the next projects:
- for [CircleCI](https://github.com/mariamihai/CIToDockerExampleProject)
- for [TravisCI](https://github.com/mariamihai/sma-overview) (all projects implemented under the "Spring Microservices in Action" book)

## Implementation Details
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

### API calls
#### Customer calls
##### Obtain all customers
 * __URI:__ _/api/v1/customers/_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: - <br/>
    * optional: <br/>
        pageNumber=[int] <br/>
        pageSize=[int]
    
 * __Success response:__
    * Code: 200 <br/>
    * Content: (TODO - response will be added)
       ``` 
       
       ```

 * __Error Response:__ -
    * __Code:__  <br/>
    * __Content:__ (TODO - response will be added)
    ``` 
    
    ```
    
#### Beer Order calls
##### Obtain all orders for customer
 * __URI:__ _/api/v1/customers/:customerId/orders_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
    * optional: <br/>
        pageNumber=[int] <br/>
        pageSize=[int]
    
 * __Success response:__
    * Code: 200 <br/>
    * Content: (TODO - response will be added)
       ``` 
       
       ```

 * __Error Response:__ -
    * __Code:__  <br/>
    * __Content:__ (TODO - response will be added)
    ``` 
    
    ```
    
##### Place new order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders_

 * __Method:__ _POST_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
    * optional: - <br/>

 * __Data params:__ <br/>
    * required: - <br/>
    * optional: <br/>
        beerOrderDto=[BeerOrderDto] (TODO - beerOrderDto example will be added)
        ``` 
        
        ```
        
 * __Success response:__
    * Code: 200 <br/>
    * Content: (TODO - response will be added)
       ``` 
       
       ```

 * __Error Response:__ -
    * __Code:__  <br/>
    * __Content:__ (TODO - response will be added)
    ``` 
    
    ```
    
##### Obtain order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders/:orderId_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
        orderId=[uuid] <br/>
    * optional: - <br/>
    
 * __Success response:__
    * Code: 200 <br/>
    * Content: (TODO - response will be added)
       ``` 
       
       ```

 * __Error Response:__ -
    * __Code:__  <br/>
    * __Content:__ (TODO - response will be added)
    ``` 
    
    ```
    
##### Pickup order for customer
 * __URI:__ _/api/v1/customers/:customerId/orders/:orderId/pickup_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: <br/>
        customerId=[uuid] <br/>
        orderId=[uuid] <br/>
    * optional: - <br/>
    
 * __Success response:__
    * Code: 200 <br/>
    * Content: (TODO - response will be added)
       ``` 
       
       ```

 * __Error Response:__ -
    * __Code:__  <br/>
    * __Content:__ (TODO - response will be added)
    ``` 
    
    ```
    
##### Cancel order
No endpoint was added for the actual cancelling of an order but a status and an event are added for this possibility.
All states and associated actions (including the cancelling of an order) have been tested under the `BeerOrderManagerImplIT` class.