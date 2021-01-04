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