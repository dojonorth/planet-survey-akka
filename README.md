# planet-survey-akka
Learn Akka! Discover strange new alien species, then get killed by them!

#TL;DR
Ideally read everything. If you want to try and blaze through then:
* Read the Setup section.
* Read and do the Exercise section in order.
* To stop the console being swamped just run one exercise test case at a time - you can do this in intelliJ or using the console by ensuring that only the one you care about is not ignored (also make sure you're not rerunning the Pi tests each time - i.e. use **sbt 'test-only uk.co.bbc.dojo.awaymission.AwayMissionSpec'**).

## Introduction
Reasoning about concurrent code can be very hard - manually managing interacting threads is likely one of the hardest things you'd ever be asked to do as a programmer. Despite this, the need to parallelise to meet performance goals is ever increasing. This can be attributed to a shift over the last decade or so, whereby CPU performance increases have come much more from increasing numbers of logical cores and not increased single-core performance. Hence, it's no surprise that in recent years, new abstractions have gained popularity that attempt to insulate us from low-level thread management and enable reasons at a a higher level.

One such abstraction is the use of Futures / Promises that was covered in a [previous dojo](https://github.com/dojonorth/promise-lander-kata). I'm going to cover another, the [Actor Model](https://en.wikipedia.org/wiki/Actor_model); specifically, its most prominent implementation [Akka](http://akka.io).

**Disclaimer:** I'm *far* from an Akka expert. I learnt about it myself for the dojo, so if you notice any inefficiencies or unidiomatic practices, then flag me up and I'll look to improve upon my code.

## Dojo Format
I've written the dojo in Scala. A Java implementation also exists, but the Scala version is widely considered to be the nicer one to work with due to language support for pattern matching etc. (note that in terms of performance though, both will be approximately equal as they compile down to very similar bytecode).

If you don't know Scala, I hope that you should still be able to make good progress. I've provided a similar example (**uk.co.bbc.dojo.actors.pi.AkkaPiCalculator**) that you should be able to lift code from without becoming too bogged down in Scala syntax. Worst case, you can just ask me for help. The important thing to take away is an understanding of the underlying concepts, which will then enable you to make use of one of the other many actor model libraries written in your language of choice.

Note that the exercises are quite prescriptive. There's quite a number of fundamental principles that I wanted to get across and so I though this was the best way of doing it. I've included a few semi-serious optional extra exercises at the end, which are more open-ended. If you're already familiar with Akka, I expected that you'll be able to fly through the core exercises (no pun intended) and will have more of a free hand on these.

## Setup
* Ensure SBT is installed. If not, install it via:
```
brew install sbt
```
* Ensure the Java 8 **JDK** (not just the JRE) is installed (it's required by the latest version of Akka). It can be downloaded from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* Clone to repo locally:
```
git clone git@github.com:dojonorth/planet-survey-akka.git
```
* Open the code in your favourite IDE. If you have IntelliJ, then you should just be able to import the build.sbt. At a push, any text editor that lets you navigate the code should be fine.
* Compile and run the tests. If you're using IntelliJ, then it should be easy to do this from there. Otherwise, running the tests directly from the command line is fine. The easist way to check that everything is fine is to navigate to the planet-survey-akka folder and then run:
```
sbt test
```
**NOTE: The first time you run the tests, they'll be slow for a couple of reasons: SBT will have to download and locally cache all of Akka's dependencies and the the Pi Calculation single threaded test that runs is intentionally slow. So don't worry if it appears to have frozen for 30 seconds or so.**

Just the Pi speed tests can be run with:
```
sbt 'test-only uk.co.bbc.dojo.actors.pi.PiCalculationSpeedComparisonSpec'
```
Just the exercise tests with (after running the Pi tests once for reference, this is likely the command you'll want to use, if you're not just running the tests directly from IntelliJ):
```
sbt 'test-only uk.co.bbc.dojo.awaymission.AwayMissionSpec'
```

## The Actor Model
The Actor Model originated in a 1973 paper by [Carl Hewitt](https://en.wikipedia.org/wiki/Carl_Hewitt). The key principles of the actor model (with a slight emphasis on the Akka implementation) are:
* Everything is an Actor - akin to Objects in OO.
* Actors are autonomous objects. They don't share state.
* Actors communicate via asynchronous immutable messages.
* Behaviour is triggered in response to messages.
* Actors (in Akka) have mailboxes that buffer pending messages.

The benefits of this are:
* No shared resource = no blocking = very fast and scalable
* Thread management can be largely abstracted over.
* Highly fault tolerant systems can be developed - encapsulated actors means that failures can be localised and robust recovery strategies can be defined.
* Distributing actors over remote systems is easy.
* Lack of shared state and clear boundaries makes comprehending the system easy (at least, as far as multi-threaded systems go...).

Akka is particularly suited to problems with the following characteristics:
* *A non-trivial concurrent element:* For simple parallelism, as the next section will show, it's probably overkill.
* *Scaling out over distributed machines would be beneficial:* Akka actors are locationally transparent and inherently network-aware, so scaling out across multiple machines should be easy.
* [Here's](http://doc.akka.io/docs/akka/2.4.1/intro/why-akka.html) Akka's take on when to use it.

Akka has excellent documentation. All of these concepts are covered in more detail [here](http://doc.akka.io/docs/akka/2.4.1/scala.html).

I'm not experienced enough to write an exposition on the downsides of Akka. However, I found the main difficulties I encountered writing this little toy exercise revolved around message passing going wrong, leading to the system hanging until it timed out. This is a fundamental concurrency problem, though and not really a failing of Akka.

For a bit more balance, [here](http://noelwelsh.com/programming/2013/03/04/why-i-dont-like-akka-actors/), [here](http://stackoverflow.com/questions/23922530/when-to-use-actors-vs-futures) and [here](http://programmers.stackexchange.com/questions/212754/when-is-it-not-good-to-use-actors-in-akka-erlang) are some discussions on when Akka might not be suitable.

## Calculating Pi In Parallel
This section is a worked example of a relatively simple parallisable problem. The intention is that by working through it, we can build up a good understanding of the problem domain, so that when we move onto the Akka implementation, we can focus on purely on the actor-related aspects of it. The Akka implementation should then also serve as a template that can be referenced in the main exercise.

One way of calculating Pi is using the [Leibniz formula](https://en.wikipedia.org/wiki/Leibniz_formula_for_%CF%80) (another is [throwing sausages](http://www.wikihow.com/Calculate-Pi-by-Throwing-Frozen-Hot-Dogs), but I won't cover that here). Pi is calculated by the summation of an infinite sequence of fractions as per:
```
π = (4/1) - (4/3) + (4/5) - (4/7) + (4/9) - (4/11) + ...
```
This is an example of an [embarrassingly parallel problem](https://en.wikipedia.org/wiki/Embarrassingly_parallel). Each of the terms is independent of the other and so we can potentially farm out blocks of terms to different cores and then sum their results together at the end. For example, we could calculate (4/1) - (4/3) + (4/5) in one core and then (4/7) + (4/9) - (4/11) in another in parallel before adding the results together at the end.

Take a look at **uk.co.bbc.dojo.actors.pi.SingleThreadedPiCalculator** and observe how this uses the 'niz to calculate Pi.

Look at the first test in **uk.co.bbc.dojo.actors.pi.PiCalculationSpeedComparisonSpec**. This uses the 'niz to calculate Pi from the first 2000000000 terms. Run it and observe that it's quite slow. It takes about 20 seconds to run on my Mac.

Now take a look at **uk.co.bbc.dojo.actors.pi.MultiThreadedFuturesPiCalculator**. This uses Scala futures to farm out blocks of a million terms to available cpu cores. Run the second test and observe that it's much faster than the single-threaded test. It takes about 2.7 seconds to run on my Mac. This is about 8x faster than the single threaded one, which makes sense as my Mac has 8 logical cores. Don't worry too much about exactly what's happening here if you're not familiar with Scala. I've included it mainly just provide some contrast with Akka show the simplest (that I can think of) way of scaling the problem out.

Finally, have a look at **uk.co.bbc.dojo.actors.pi.AkkaPiCalculator**. This uses Akka to produce a number of messages that each stipulate a million term block to calculate. Run the third test and observe that the code is almost as fast as the futures implementation. On my Mac it takes about 2.9 seconds to run.

I've added extensive commenting to explain the functionality. The gist is:
* An Akka actor system is created.
* A master is created that is in charge of coordinating the distribution of the calculation and returning the result.
* The master creates 8 Worker actors. The worker actors are used to actually calculate the result of specific ranges of terms.
* All of the messages are sent immediately by the by a Master to the mailboxes of 8 Worker actors (it doesn't wait for them to finish). The mailboxes of the actors buffer the messages and feed the workers' receive methods in order.

A couple of minor quirks of the code are:
* The fact that I didn't divide the terms into 8 evenly-sized blocks. I chose not to highlight message buffering and to maintain similarity with the Futures implementation.
* The master having to store the initial ActorRef of the temporary sender used to fulfill the ask pattern (? operator). More normally, Akka systems purely fire and forget and don't block like this. However, since we want a result out, rather than it being a continuous system, then there has to be a point where we block and await the result.

Although I used this as an example, it isn't actually a case where I would advocate using Akka, since it's so simple: it takes up more lines of code and is slower than the Futures-based solution. Interestingly, there used to be a similar worked example on the Akka website, but they got rid of it - probably because they thought the same.

#The Exercise
The exercise we're going to look at focuses on the voyages of a starship and its long running quest to seek out new life and new civilisations.

We'll go through making a number of tests pass. At the moment, they're all ignored. See **uk.co.bbc.dojo.awaymission.AwayMissionSpec**. In order to make a test run, then change *ignore* to *it*. I.E.
```
ignore("the En Prise should survey a single planet") {
```
becomes:
```
it("the En Prise should survey a single planet") {
```
The system outputs events to the console, which are key in understanding how the Akka system is operating, so even though as we build up functionality, the earlier tests will still pass, I recommend only running one test at a time, so as not to clutter the console. You can do this by explicitly selecting single tests to run in IntelliJ or by only ever having the current test not ignored if you're running them from the command line.

## Part 1 - The Founding of The Corporation

*Well, we had a good innings, but it looks like Earth is finished. Our only hope is to go and find another planet that we can colonise. The countries of the world have come together and agreed to the founding of 'The Galactic Corporation' (aka 'The Corporation'). You are tasked with making it happen.*

* Go to **uk.co.bbc.dojo.awaymission.AwayMission**
* Create an an Akka system called 'The-Corporation'
* Create a **uk.co.bbc.dojo.awaymission.actors.StarshipCommand** actor called 'Admiral-Reith'. This should be a top-level actor i.e. created by calling 'actorOf' directly on the Akka system you've just created.
* Message the StarshipCommand actor with a **uk.co.bbc.dojo.awaymission.akka.actorsSeekOutNewLifeAndNewCivilisations**. Starship Command should return immediately saying that it hasn't found any life.
* Take a look at the logged output when the test runs and see what's happening.

Notes:
* Remember to check **uk.co.bbc.dojo.actors.pi.AkkaPiCalculator** for inspiration.
* Be careful not to remove the 'akka.pattern.ask' import, which is needed for the '?' messaging operator to work.
* Akka forces you to construct hierarchies of actors, such that each actor is supervised by a single parent. This maps well onto a military / corporate structure, so we'll assume a chain of command in our example.
* The name parameter is optional to Akka Systems and Actors, however, providing one is good practice. Be aware that Akka names don't allow certain special characters or spaces. For now, stick to alphanumeric characters, numbers and hyphens.
* The 'receive' method in StarshipCommand is wrapped in a 'LoggingReceive' block. We need this to automatically capture and print out incoming messages.
* The 'actorOf' method actually takes a 'Props' object. I've glossed over this (for more info go [here](http://doc.akka.io/docs/akka/2.4.1/scala/actors.html#Props)) this by providing companion objects for each actor that handle this in their constructors. i.e. you can just call:
```
actorOf(StarshipCommand(), name = ...
```

## Part 2 - Creating Our First Ship
*Without any starships to send out into space, Starship Command is struggling. We need a ship that we can actually send out there to do something! In this part, we'll create our first starship under the stewardship of The Corporate, the BBC En Prise and send it out into the great unknown!*

* Go to **uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand**
* Create a new **uk.co.bbc.dojo.awaymission.actors.Starship** called 'The-BBC-Enprise' (or similar) that is supervised by StarshipCommand - i.e. call 'actorOf' on it's context object. The context object inherits lots of useful state. Find out more about it [here](http://doc.akka.io/docs/akka/2.4.1/scala/actors.html#Actor_API).
* Check Starship for a suitable message type we can pass it containing the planet to explore.
* Message the En Prise with the new message and tell it to go and scan the passed planet (i.e. call the 'checkForAlienLife' method).
* Once the En Prise has scanned the planet we then need it to message back StarshipCommand with the result of the scan. **HINT: Check the context to see how we find the actor reference to respond to.**
* Have Starship Command receive the response and return whether or not the planet is occupied.

Notes:
* It's [good practice](http://doc.akka.io/docs/akka/2.4.1/scala/actors.html#Recommended_Practices) to put the message types that an Actor can receive in it's companion object, so ensure you've declared your messages in the correct places.
* I've included some basic logging. As you go along, you may choose to add more to better follow the system.
* In the test output you'll notice actors have names like *akka://Pi-System/user/Admiral-Reith*. Akka enforces a strict single-parent hierarchy, hence actors can be identified by file system-like paths. I've not really touched on this in the exercise, but lots more info can be found [here](http://doc.akka.io/docs/akka/2.4.1/general/addressing.html#What_is_an_Actor_Path_).
* Note that the final message containing the number of occupied planets is sent to an odd-looking actor: this is a temporary one created to receive the response needed for the ask pattern ('?' operator) used in *uk.co.bbc.dojo.awaymission.AwayMission*.
* The final component that appears in the log is an Akka dispatcher reference. We won't go into this, but it can be thought of as a reference to the thread that the actor is running in. See [here](http://doc.akka.io/docs/akka/2.4.1/scala/dispatchers.html) for more details.

## Part 3 - Scanning Multiple planets
*The En Prise has proved itself! Now it's time to up the ante. We'll send the En Prise off to scan a number of planets; have Starship Command tally the number of occupied ones and then return us the result.*

* Have Starship Command message the En Prise with each of the planets to survey.
* Starship Command must now keep track of each response it receives from the En Prise and only respond to us once it's recieived results for all of planets to scan.

Notes:
* Being an Akka actor means that the En Prise will have a mailbox that buffers messages and feeds them to the receive method, so send all of them to it at the start, don't send them lockstep with responses (see [here](http://doc.akka.io/docs/akka/2.4.1/scala/mailboxes.html#mailboxes-scala) for painful amounts of detail on Akka mailboxes).

## Part 4 - Reporting Failure
*Oh dear, and everything was going so well... Something went wrong while we were scanning the next batch of planets. Now Starship Command is left waiting indefinitely for a response that will never come. We need to improve our protocols to avoid this situation.*

* Before thinking about making the test pass, look at the series of events that happened in the console. After scanning the errant planet, the En Prise still managed to continue its journey. Note though that it moved from *Starship Command* to the next planet along! The reason was Akka's default error handling strategy is to throw away the message that caused the exception, recreate the actor (hidden within the outer 'ActorRef' container, which remains the same) and the process the next message. Hence, the En Prise moving from Starship Command - it was really a different ship that continued - all new ships start out at Starship Command.
* Override the En Prise's *preRestart* method to send an SOS message with the name of the failing planet to Starship Command. See [here](http://doc.akka.io/docs/akka/current/scala/actors.html#Actor_API) for the method signature (make sure to call 'super.preRestart(reason, message)' at the end of the override behaviour - It shouldn't matter in this case, but it's good practice).
* For now, have StarshipCommand assume that if there was someone there to blow up the En Prise, then it probably means that the planet was occupied.
* **HINT: We'll want to include the name of the bad planet int he SOS message. The 'preRestart' method includes the message that was being processed that caused the failure - we know that this must be an 'ExplorePlanet' message, so we can do something along these lines:**
```
message match {
      case Some(deadlyPlanetMessage: ExplorePlanet) => {...}
}
```
Notes:
* See [here](http://doc.akka.io/docs/akka/2.4.1/general/supervision.html#What_Restarting_Means) for more detail on restarts and [here](http://doc.akka.io/docs/akka/2.4.1/scala/fault-tolerance.html#Default_Supervisor_Strategy) for more detail on Akka's default approach to error handling.
* An alternative approach - that you might try if you're curious - would be to override Starship Command's supervisor strategy to specifically deal with the exception type that we're seeing throw, as described [here](http://doc.akka.io/docs/akka/2.4.1/scala/fault-tolerance.html#Default_Supervisor_Strategy), in some ways, that feels neater, but I don't see how we then recover from it and determine which planet it was we failed to scan. The only solution I could come up with was to introduce a try block around the planet scanning and introduce a new exception type that we throw that includes the name of the bad planet so Command can then pick it up. I'd have thought this would be a common problem, which makes me think I might be missing something, but articles that I found, such as [this](http://mattro.be/rts/posts/2015/08/08/fault-tolerance-in-akka/).

## Part 5 - Dealing with Failure
*Another day, another planet explored. It turns out that our assumption that whenever the En Prise is destroyed it was by hostile aliens was a dodgy one. The mighty Clanger Empire has started just destroyed our ships when we're they're scanning unoccupied planets for fun. Starship Command has had enough of living under the yolk of Clanger tyranny and so decides to commission a new ship that can fight back!*

* Create a new ship under the Supervision of Starship Command, but this time equip it with laser cannons! Anticlimactically, this just involves calling the other constructor... I'll let you name it.
* Whenever the En Prise sends an SOS, then message the new ship and tell them to scan that planet instead.

## Part 6 - Expand the Fleet
*The list of planets to explore is never ending! Starship Command has lined up a long list of new planets to explore. These ones have thick atmospheres and are going to take time to scan, but Command wants the results ASAP! As we all know, the way to achieve results when a deadline approaches is drag more people in, so that's what we'll do - let's replace the En Prise with a fleet of three ships!*

* Replace the instantiation of the En Prise with a router that creates **three** (unarmed) ships. Use a RoundRobin router for now. Also, for consistency, call the Router the same name as the En Prise.
* That should work, but it's still too slow. The problem is that the RoundRobin router that we're using is allocating the planets to explore to the Starships up front. However, some planets are taking much longer to survey than others, leaving some of our fleet idling. Replace the RoundRobin strategy with a more efficient one (see [here](http://doc.akka.io/docs/akka/2.4.1/scala/routing.html) for details.)

Notes:
* In the main, routers are transparent between message senders and receivers - they will automatically forward messages between the two parties.

## Part 7 - Beam Me Down (OPTIONAL)
**Warnng: You're on your own from hereon out. These are exercises I've conceived, but not actually attempted myself.**

*Some planets are proving stubbornly resilient to our sensors. It looks like the says of being able to rock up, scan the planet and be back in time for last orders may be gone. We're going to have to send in the Redhshirts!*

* Introduce a new class of planet that is impervious to our sensors.
* These planets include a method to return a number of new locations that need exploring to determine whether life is present or not.
* Create a new hierarchy of Redshirt actors supervised by the En Prise that will investigate these locations.
* Optionally have the Redshirts being brutally killed off by the various alien species they encounter and deal with it.

Notes:
* Previously, we've just create actors and left them running, relying on the Akka system taking them down when we close it. Manually killing off the Redshirts after each survey is discussed [here](http://doc.akka.io/docs/akka/current/scala/actors.html#Stopping_actors).

## Part 8 - The Sky (Universe?) Is The limit (OPTIONAL)
*If you've made it this far, then you're obviously an Akka savant, or are -really- trying to put off something else. I've throw in a few half-baked suggestions for other extensions you might consider*

* Read about Akka remoting and then expand the fleet to multiple machines.
* Akka offers [at most once](http://doc.akka.io/docs/akka/current/general/message-delivery-reliability.html) delivery of messages. Read up on and simulate communication over a unreliable message channel.
* Introduce a UI. Include full 3D rendering of procedurally generated planets and realistic space battles.
