# planet-survey-akka
Learn Akka! Discover strange new alien species, then get killed by them!

TODO: Say which sections are skippable if you're really YOLO (basically, just jump straight in and go through the worked example.)

## Introduction
Reasoning about concurrent code can be very hard - manually managing interacting threads is likely one of the hardest things you'd ever be asked to do as a programmer. Despite this, the need to parallelise to meet performance goals is ever increasing. This can be attributed to a shift over the last decade or so, whereby CPU performance increases have come much more from increasing numbers of logical cores and not increased single-core performance. Hence, it's no surprise that in recent years, new abstractions have gained popularity that attempt to insulate us from low-level thread management and enable to reason at a a higher level.

One such abstraction is the use of Futures / Promises that was covered in a [previous dojo](https://github.com/dojonorth/promise-lander-kata). I'm going to cover another, the [Actor Model](https://en.wikipedia.org/wiki/Actor_model); specifically, its most prominent implementation [Akka](http://akka.io).

**Disclaimer:** I'm *far* from an Akka expert. I learnt about it myself for the dojo, so if you notice any inefficiencies or unidiomatic practices, then flag me up and I'll look to improve upon my code.

## Dojo Format
I've written the dojo in Scala. A Java implementation also exists, but the Scala version is widely considered to be the nicer one to work with due to language support for pattern matching etc (note that in terms of performance though, both will be approximately equal as they compile down to very similar bytecode).

If you don't know Scala, I hope that you should still be able to make good progress. I've provided a similar example that you should be able to lift code from without getting bogged down in Scala syntax. Worst case, you can just copy and paste from the provided solution as you work through the exercise. The important thing to take away is an understanding of the underlying concepts, which will then enable you to make use of one of the other many actor model libraries written in your language of choice.

Note that the exercises are quite prescriptive. There's quite a number of fundamental principles that I wanted to get across and so I though this was the best way of doing it. I've included a few optional extra exercises at the end, which are more open-ended. If you're already familiar with Akka, I expected that you'll be able to fly through the core exercises (no pun intended) and will have more of a free hand on these.

## Setup
1. Ensure SBT is installed. If not, install it via:
```
brew install sbt
```
2. Ensure the Java 8 **JDK** (not just the JRE) is installed (it's required by the latest version of Akka). It can be downloaded from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
3. Clone to repo locally:
```
git clone git@github.com:dojonorth/planet-survey-akka.git
```
4. Open the code in your favourite IDE. If you have IntelliJ, then you should just be able to import the build.sbt. At a push, any text editor that lets you naviagate the code should be fine.
5. Compile and run the tests. If you're using IntelliJ, then it should be easy to do this from there. Otherwise, running the tests directly from the command line is fine. To run all of the tests then from directly within the planet-survey-akka folder run:
```
sbt test
```
Alternatively, run just the Pi speed tests with:
```
sbt 'test-only uk.co.bbc.dojo.actors.pi.PiCalculationSpeedComparisonSpec'
```
Or the exercise tests with:
```
sbt 'test-only uk.co.bbc.dojo.awaymission.AwayMissionSpec'
```

## The Actor Model
The Actor Model originated in a 1973 paper by [Carl Hewitt](https://en.wikipedia.org/wiki/Carl_Hewitt). Ericsson's use of Actors in [Erlang](https://en.wikipedia.org/wiki/Erlang_(programming_language)) (**Er**icsoon **Lang**uage) in the late 90s helped popularise it.

The key principles of the actor model (with a slight emphasis on the Akka implementation) are:
* Everything is an Actor - akin to Objects in OO.
* Actors are autonomous objects. They don't share state.
* Actors communicate via asynchronous immutable messages.
* Behaviour is triggered in response to messages.
* Actors (in Akka) have mailboxes that buffer pending messages.

The benefits of this are:
* No shared resource = no blocking = very fast and scalable
* Thread management can be largely abstracted over.
* Highly fault tolerant system can be developed - encapsulated actors means that failures can be localised and robust recovery strategies can be defined.
* Distributing actors over remote systems is easy.
* Lack of shared state and clear boundaries makes comprehending the system easy (at least, as far as multi-threaded systems go...).

Akka is particularly suited to problems with the following characteristics:
* *A non-trivial concurrent element:* For simple parallelism, as the next section will show, it's probably overkill.
* *Scaling out over distributed machines would be beneficial:* Akka actors are locationally transparent and inherently network-aware, so scaling out across multiple machines should be easy.
* [Here's](http://doc.akka.io/docs/akka/2.4.1/intro/why-akka.html) Akka's take on when to use it.

Akka has excellent documentation. All of these concepts are covered in more detail [here](http://doc.akka.io/docs/akka/2.4.1/scala.html).

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

Although I used this as an example, it isn't actually a case where I would advocate using Akka, since it's so simple: it takes up more lines of code and is slower than the Futures-baed solution. Interestingly, there used to be a similar worked example on the Akka website, but they got rid of it - probably because they thought the same.

# Over To You
-The exercise we're going to look at focuses on the voyages of a starship and its long running quest to seek out new life and new civilisations.
-Akka forces you to construct heirrarchies of actors which map well onto a military / corporate structure, so we'll assume a chain of command in our example.
-We'll go through making a number of tests pass. At the moment, they're all ignored. See uk.co.bbc.dojo.awaymission.AwayMissionSpec. We're going to focus on the series of event we see in the console for each test, so I recommend runnng them one at a time (TODO: Say about test-only or using SBT) - i.e. even though the first test will still pass once you move onto the second one, don't keep running it as it will muddy the water in the console output.
-As there's so much to try and cram into a Dojo, then it's a bit more perscriptive than normal. At least for the early parts.
-In general, if you don't know Scala and it's the syntax that's holding you back, then try and work out logically what you want to do, then take a look at the solution.
-TODO: Add links to the Akka Doc (say it's really good) for all these things if people need more info.

PART 1
------
-AwayMission will act as our entry point.
-Create an Akka system called 'The-Corperation'
-Create a StarshipCommand called 'Admiral-Reith'. This should be a top-level actor i.e. create is by calling 'actorOf' directly on the Akka system you've just created.
-Message command and tell it to go and scan the passed planet (Gallifrey, in this case).
-Note that Akka names are limited (include link). Basically, no spaces just alphanumeric characters and numbers.
TODO: Get rid of the construction of the akka system and the ship. Ensure command returns -1 by default.
TODO: Try and get a test in to match up with this that returns a zero.

PART 2
------
-Without any starships to send out into space, Starship command is struggling. We need a ship that we can actually send out there to do something!
-Commission The Corperation's first ship the BBC 'En Prise'.
-This should be a child of StarshipCommand and so this time, we will need to call 'actorOf' on Starship command's context object.
-Message the En Prise and tell it to go and scan the passed planet.
-Once the En Prise has scanned the planet we then need it to message StarshipCommand with the result of the scan. Create a new message type for this purpose then send it to StarshipCommand.
-NOTE: It's good practice to put the message types that an Actor can receive in it's companion object, so do this. Hence, it should be StarshipCommand that has the message type declared for it, not the Starship.
-Finally, have StarshipCommand receive the response message and message back directly whether or not the planet is occupied.
-Take a look at the console to see the sequence of messages.

TODO: Get rid of response message.
TODO: Say about props etc. Will hide the details from the dojo users.
ToDO: Want the first planet to be occupied and return a 1.

PART 3
------
-The En Prise has proved itself! It's now ready to scan a whole host of planets! Move onto the next test where we ask them to scan a whole host of planets.
-Have Starship Command message the En Prise with the list of planets and have it scan them all!
-Note that the En Prise (aka a Scala actor) has it's own mailbox, so it will receive all of the messages instantly and then process them in order every time the receive method is called again.
-Extend the message handling in Starship command to keep track of the incomming responses and aggregate them together, only returning when all of the results are in.

PART 4
------
-This interplanetary exploration lark is easy. Run the next test and see what happens...
-Well, that didn't go so well. Take a look at the series of events that happened in the console.
-What actually happened here is that the En prise was destroyed (threw an unhandled exception) and then Starship Command applied it's default supervision policy (http://doc.akka.io/docs/akka/2.4.1/scala/fault-tolerance.html#Default_Supervisor_Strategy), which is to recreate the actor, throw away the bad message and then continue (see how the En Prise traveled from base again to revisit the last planet).
-Note that the test then timed out, as Starship Command never then recieved a response about whether or not Clanger Prime was occupied.

-For now, have StarshipCommand assume that if there was someone there to blow up the En Prise, then it probably means that the planet was occupied, so assume that by default.

PART 5
------
-Another day, another planet explored.
-It turns out that our assumption that whenever the En Prise is destroyed it was by hostile aliens was a dodgy one. The mighty Clanger Empire sometimes just destroyed our ships when we're they're scanning unoccupied planets for fun. The bastards!
-Starship Command has had enough of living under Clanger tyrany and so decides to commission a new ship that can fight back!
-Create a new ship under the Supervision of Statship Command, but this time equip it with laser cannons! (anticlimatically, this means call the other constructor on it...)
-Whenever the En Prise sends an SOS, then message the Merciless and tell them to scan that planet instead.
TODO: Introduce a new exception type that isn't a hostile alien attack so that the assumption makes the next test fail.

PART 6
------
-The list of planets to explore is never ending! Starship Command now want to expand its fleet to three ships!
-Retire the En Prise and replace it with a router that creates three (unarmed) ships (see the Pi example for inspiration). Stick with the RoundRobin router for now. Also for consistency, call the Router the same name as the En Prise
-Well, it's working, but it's so slow! The problem is that the RoundRobin router that we're using is allocating the planets to explore to the Starships up front. However, some planets are taking much longer to survey than others, leaving some of our fleet idling.
-Take a look at the Akka routing strategies and select a different one that will make better use of our fleet's time.
-See: http://doc.akka.io/docs/akka/2.4.1/scala/routing.html

PART 7 (OPTIONAL)
-----------------
-Well, now, you're on your own. However, never fear, if you're already brilliant at Akka and have whistled through the earlier exercises or want to try and drag this out to avoid going back to work, then I've a range of suggestions for extensions for you - you're on your own though:
-At the moment, we can just rock up and survey planets from orbit. However, introduce a new class of planet that is impenetrable to our sensors. In this case, we'll have to send an away team down to investigate.
-Give impenetrable planets a list of survey points that need to be investigated for life and then introduce a new class of actor, The Redshirt, that we will been down to survey these points.
-Experiment with exception handling as the RedShirts are remorselessly slaughtered by aliens as they go about their business.

PART 8 (OPTIONAL)
-----------------
-At the moment, the Redshirts are able to analyse their findings on site. Instead, have them beam their findings back to the ship's science officer for analysis. Where he can then determine whether life is present or not.
-Introduce a time component to the tasks that the Starship undertakes, so that the sciene officer can be analysing data in parallel as the ship moves between planets.
-It turns out that all of thsi data analaysis is far too much work for one man, so introduce a team of researchers, working under the officer who will ahve the work distributed to them.

PART 9 (OPTIONAL)
-----------------
-The sky (universe?) is the limit! Go crazy!
-Expand out the universe over multiple machines using Akka remoting.
-Manually manage transporting the RedShirts down to the planet. When they are injured, then transport them to the sick bay to be healed.
-Introduce a UI! Possibly including full 3D rendering of the ships and planets.
