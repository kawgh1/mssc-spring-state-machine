# Demo Project for Spring State Machine

So our case setup in this scenario is we're running some microservices and we need to persist the state of the state machine to the database
and then rehydrate it - reset the state - from the object in the database.

This type of case where we have separate processes running and they'll be running over time. So there will be time in between 
the **Pre-Authorization State** and the **Authorization State** of a payment. So thats a use case and it could be 1 minute, 1 hour, etc.

We want to bring that state machine back out of the database and rehyrdrate it to the propert state.

Normally it is considered an **expensive process** to restore the state of the state machine. There is some cost to this.
A lot of times you will see more complex processing and you might not even persist a state machine to a database.

Basically we want to get a state object out of the database and then set up a state machine on that state of that object.
**See build() method in services/PaymentServiceImpl**

    // since we're persisting the state of a payment through a state machine, we're basically checking the
    // state of the State Machine in and out of the database at each step/state change
    
<br><br>

John Thompson's comments on how he sees State Machines being implemented with **Event Sourcing** Blockchain and databases.
Summary:

with blockchain, and state changes of a state machine would be stored and permanent  
whereas on a relational database - the various states and transactions stored in the DB could be altered, dropped, or 
not recorded in the first place  

an example would be a credit approval process - every step, state change and transaction - with blockchain - would be
kept and unalterable ( could argue same for a voting software system)  

# [Introducing Sagas](#introducing-sagas)
  
- # The Problem with Transactions
    - A database transaction allows you to have a sequence of steps
        - All steps must complete ot be committed
        - Else, a rollback occurs returning the database to the original state
        
    - ### The Order Allocation Scenario
        - Allocate Inventory - Updating Inventory and Order with Allocation
        - Works well within a monolith
        - Order and Inventory are two different Microservices/Databases
        - Breaks traditional transactions
        
    - ### A.C.I.D. Transactions
        - ACID - Typically one database
            - **Atomicity** - All operations are completed successfully or database is returned to previous state
            - **Consistency** - Operations do not violate system integrity constraints
            - **Isolated** - Results are independent of concurrent transactions 
                - ie imagine 1000s of database transactions at one time, you want to guarantee your data is not unexpectedly changing mid-transaction
            - **Durable** - Results are made persistent in case of system failure (ie written to disk)
        - Database handles all locking and coordination to guarantee transaction
            - This is **expensive** to do - takes a lot of system resources
            
    - ### Distributed Transactions
        - When we start talking about **microservices**, obviously those transactions will be going out across potentially many, many nodes
        - With **microservices**, often multiple services are involved in what is considered a transaction
            - Order Allocation example - Order Service, Inventory Service, etc.
        - ### Java EE - Java Transaction API (JTA)
            - Enables distributed transactions for Java environments
            - Well supported by Spring
            - Transactions are managed across nodes by a **Transaction Manager**
            - Very Java centric
                - JTA sounds nice on paper, but in production it can become a headache very quickly
                
        - JTA uses a **Two-Phase Commit** or **2PC**
            - Happens in two phases - **Voting** and **Commit**
            - Transaction Coordinator asks each node if proposed transaction is ok?
                - If ***all*** respond ok:
                    - Commit message is sent
                    - Each Node commits work and sends acknowledgements to coordinator
                - If ***any*** Node responds **no**:
                    - Rollback message is sent
                    - Each node rollsback and sends acknowledgement to coordinator
                      
            - Problems with Two Phase Commit
                - Does not scale - expensive
                - Blocking Protocol - the various steps block and wait for others to complete
                - Performance is limited to the speed of the slowest Node
                -  Coordinator is a Single Point of Failure
                - Technology lock-in
                    - Can be very difficult to mix technology stacks
                    
            - Challenges with Microservices
                - A transaction for a Microservice architecture will often span multiple microservices
                - Each service should have its own database
                    - Could be a mix of SQL and NoSQL databases
                - Should be technology agnostic
                    - Services can be in Java, .NET, Ruby, etc.
                - How to coordinate the 'Transaction' across multiple microservices??
                
- # The Need for Sagas
  
    - ### The Microservice Death Star
        - As the number of microservices grows, so does complexity at a much faster rate
        - Death Star examples
            - Netflix
            - Twitter
            - Uber
            
    - Challenges
        - Business transactions often span multiple microservices
        - ACID transactions are not an option between microservices
        - Distributed Transactions / Two Phase Commits
            - Complex and do not scale
        - Microservices should be technology agnostic
            - Making 2PC even more difficult to implement
            
    - ### CAP Theorem
        - **CAP** - Consistency, Availability, Partition Tolerance
            - **Consistency** - Every read will have the most recent write
            - **Availability** - Each read will get a response, but without the guaranatee data is most recent write
            - **Partition Tolerance** - System continues in lieu of communications errors or delays
        - **CAP Theorem** - States that a distributed system can only maintain two of these three at a time
        
    - ### BASE - An ACID Alternative
        - **BASE** - Coined by Dan Pritchett of Ebay in 2008
        - **B**asically **A**vailable, **S**oft State, **E**ventually consistent
            - The opposite of ACID
            - **Basically Available** - Build system to support partial failures
                - Loss of some functionality vs total system loss
            - **Soft State** - Transactions cascade across nodes, it can inconsistent for a period of time
            - **Eventually Consistent** - When processing is complete, system will be consistent
            
    - ### Feral Concurrency Control
        - *Feral Concurrency Control: An Empirical Investigation of Modern Application Integrity*
           - Published by Peter Baillis in 2015
        - **Feral Concurrency Control - are application level mechanisms for maintaining database integrity**
            - Relational Databases can enforce a variety of constraints - such as foreign key constraints
            - Not available within a distributed system
            - Thus up to the application to enforce constraints
            
# Introducing Sagas

- 
    - Concept introduced in 1987 by Gracia-Molina / Salem of Princeton Univ
    - Was originally looking at Long Lived Transactions (LLTs) within a single database
        - LLTs hold on to database resources for an extended period of time
    - Paper proposed rather than long complex processes to break up into smaller atomic transactions
    - Introduced concept of compensating transactions to correct partial executions
    
    - ## Sagas
        - Sagas are simply a series of steps to complete a business process
        - Sagas coordinate the invocation of microservices via messages or requests
        - Sagas become the transactional model
        - Each step of the Saga can be considered a request
        - Each step of the Saga has a compensating transaction (request)
            - Semantically undoes the effect of the request
            - Might not restore to the exact previous state - but effectively the same
            
    - ## Saga Steps
        - Each step should be a message or event to be consumed by a microservice
        - **Steps are asynchronous**
        - Within a microservice, it's normal to use traditional database transactions
        - Each message (request) should be idempotent
            - meaning if same message / event is sent (re-delivered), there is no adverse effect on system state
        - Each step has a compensating transaction to undo the actions
        
    - ## Compensating Transactions
        - Effectively become the 'Feral Concurrency Control'
         
        
                
[Top](#top)