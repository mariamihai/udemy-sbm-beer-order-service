[![CircleCI](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service.svg?style=svg)](https://circleci.com/gh/mariamihai/udemy-sbm-beer-order-service)

# SBM Beer Order Service
Spring Boot Microservice project

## Description
The current project encapsulates the ordering of beer of initial [monolith brewery project](https://github.com/mariamihai/udemy-sbm-brewery-monolith).
The initial project was split in 3 microservices:
* [SBM (Spring Boot Microservices) Beer Service](https://github.com/mariamihai/udemy-sbm-beer-service)
* SBM (Spring Boot Microservices) Beer Order Service [current project]
* [SBM (Spring Boot Microservices) Beer Inventory Service](https://github.com/mariamihai/udemy-sbm-beer-inventory-service)

## Implementation Details
### Default port mapping - for single host

| Service Name | Port | 
| --------| -----|
| [SBM Beer Service](https://github.com/mariamihai/udemy-sbm-beer-service) | 8080 |
| BM  Beer Order Service [current project] | 8081 |
| [SBM Beer Inventory Service](https://github.com/mariamihai/udemy-sbm-beer-inventory-service) | 8082 |