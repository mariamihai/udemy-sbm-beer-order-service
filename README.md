CircleCI [![CircleCI](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service.svg?style=svg)](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service)

Docker [![Docker](https://img.shields.io/docker/v/mariamihai/sbm-beer-order-service?sort=semver)](https://img.shields.io/docker/v/mariamihai/sbm-beer-order-service?sort=semver)

# SBM Beer Order Service
Spring Boot Microservice project

## Description
The current project encapsulates the ordering of beer of initial [monolith brewery project](https://github.com/mariamihai/udemy-sbm-brewery-monolith).
The initial project was split in 3 microservices:
* [SBM (Spring Boot Microservices) Beer Service](https://github.com/mariamihai/udemy-sbm-beer-service)
* SBM (Spring Boot Microservices) Beer Order Service [current project]
* [SBM (Spring Boot Microservices) Beer Inventory Service](https://github.com/mariamihai/udemy-sbm-beer-inventory-service)

Overview of the project [here](https://github.com/mariamihai/udemy-sbm-overview).

## API Version
Currently the application is at _v1_.

## Implementation Details
### Properties
```
spring.application.name=beer-order-service

server.port=8081
```

### Environment variables for running locally
**sbm.brewery.beer-service-host** contained originally the beer service host. 
As the project currently is being used with both Docker and running locally, I've added a new variable, 
BEER_SERVICE_HOST, which should be set for both environments. For local use, the value should be 
`BEER_SERVICE_HOST=http://localhost:8080 `. For creating a Docker container, the value is set in the docker-compose file.

### API calls
#### Customer calls
##### Obtain all customers
 * __URI:__ _/api/v1/customers/_

 * __Method:__ _GET_

 * __URL params:__ <br/>
    * required: - <br/>
    * optional: - <br/>
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
    * required: - <br/>
        customerId=[uuid] <br/>
    * optional: - <br/>
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
    * required: - <br/>
        customerId=[uuid] <br/>
    * optional: - <br/>

 * __Data params:__ <br/>
    * required: - <br/>
        customerId=[uuid] <br/>
    * optional: - <br/>
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
    * required: - <br/>
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
    * required: - <br/>
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
    