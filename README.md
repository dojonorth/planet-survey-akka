# planet-survey-akka
Learn Akka! Discover strange alien species, then get killed by them!

NOTE - THESE NOTES ARE REALLY ROUGH - I'll TIDY THEM UP.

# Setup
TODO: Include some stuff on setting up sbt with brew if people don't have it.
-Install SBT - you can go this using 'brew install sbt;
-For ease of running and editing I'd also suggest importing it into an IDE. IntelliJ should recognise it as a Scala project and read the build.sbt)
-I'm using the latest version of Akka, which needs Java 8 - so you'll need that. Download the DMG here:  http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
-Clone the repo locally and run 'sbt test' to compile and run the work.
-It's written in Scala, but mostly you'll be plugging together existing code based on a given example, so even if you're not familiar with Scala then you should be able to make progress.

# Intro
-Reasoning about concurrent code is very hard - try wriring a complex multi-thread system in Java without any libraries. Likely it'll be one of the hardest things you'll ever have to do.
-However, lots of interest in further parallelising code last 5 or so years. Probably as a consequence of the need to parallelise code more as we begin to reach the limits of single core performance (Moore's Law) improvements and new architectures instead scale out with more logical cores.
-Hence, it's no surprise that in recent years, lots of new abstractions have come along for reasoning about concurrent logic. They give us higher level abstractions that are more easy to comprehend, rather than having to worry about the nitty gritty of managing individual threads and dealing with inter-thread communication.
-One such abstraction is the use of Futures in Scala, that Jens covered in another dojo (TODO: Link).
-I'm going to talk about another - the Actor Model, and specifically, it's most prominent implementation, Akka (http://akka.io).
-Note that I'm *far* from an Akka expert. I read up on it myself for the dojo, so if you notice any inefficiencies or unidiomatic constructs, then flag me up and I'll read up and correct it.

# The Actor Model
- Originated in 1973 - almost as old as me: https://en.wikipedia.org/wiki/Actor_model
- Abstracts over low-level concurrency by introducing the idea of independent actors that communicate via messages.
- Erlang takes idea and really run with it (https://en.wikipedia.org/wiki/Erlang_(programming_language). Used with great success in telecoms industry for last decade or so.
- Key principles:
    - Actors are autonomous objects. They don't share state.
    - Actors communicate via asynchronous immutable messages.
    - Actors have mailboxes that buffer pending messages.
    - Highly fault tolerent - encapsulated actors means that failures are localised and recovery strategies can be defined.
    - No shared resource = no blocking = very fast and scalable.

# A Worked Example
- Take a look at uk.co.bbc.dojo.actors.pi.SingleThreadedPiCalculator.
- This calculates Pi by using the Leibniz formula https://en.wikipedia.org/wiki/Leibniz_formula_for_%CF%80
- (I also discovered whilst reading this how to calculate Pi by throwing hotdogs - check here for a laugh: http://www.wikihow.com/Calculate-Pi-by-Throwing-Frozen-Hot-Dogs)
- Pi is calculated by the summation of an infinite sequence of fractions as per (TODO: Properly markup):
π = (4/1) - (4/3) + (4/5) - (4/7) + (4/9) - (4/11) + ...
- This is an example of an embarassingly parallel problem (https://en.wikipedia.org/wiki/Embarrassingly_parallel). Each of the terms is independent of the other and so we can potentially farm out blocks of terms to different cores and then sum their results together at the end.
- For example, we could calculate (4/1) - (4/3) + (4/5) in one core and then (4/7) + (4/9) - (4/11) in another in parallel before adding the results together at the end.

- Run the first test in PiCalculationSpeedComparisonSpec. This uses the 'niz to calculate Pi from the first 2000000000 terms.
- Observe that it's quite slow. It takes about 20 seconds to run on my Mac.

- Run the second test in PiCalculationSpeedComparisonSpec. This uses Scala futures to farm out blocks of a million terms to available cpu cores.
- Don't worry too much about exactly what's happening here. I've included it mainly just provide some contrast with Akka show the simplest (that I can think of) way of scaling the problem out.
- Observe that it's much faster than the single-threaded test. It takes about 2.7 seconds to run on my Mac. This is about 8x faster than the single threaded one, which makes sense as my Mac has 8 logical cores.
- (The threading is defined by the ExecutionContext - we're using the default one that will parallelise up to the number of logical cores your machine reports it has).

- Run the third test in PiCalculationSpeedComparisonSpec. This uses Akka to produce a number of messages that each stiplulate a million term block to calculate.
- The messages are sent by a Master to the mailboxes of 8 Worker actors who will calculate each block in turn and then return the answer to the master who composes the figures and then returns the result once all have finished.
- (Note that I could have just divided the terms in 8 evenly-sized blocks, but I chose not to, mainly maintain similarity between the Futures implementation).
- Observe that the code is almost as fast as the futures implementation. On my Mac it takes about 2.9 seconds to run.

- Note that this isn't actually a case where I would advocate using Akka (this used to actually be the worked example on the Akka website, but they got rid of it - probably because they thought the same). As you can see, the Akka implementation takes up considerably more lines of code and takes longer to run.
- Instead this example is intended to provide the basic concepts that you'll need to perform the dojo, so take a look at it (I've heavily commented it) and ensure that you have a general feel for what's going on.
- Akka is much more suited to more complex systems. It has a number of key strengths:
    - Actors are inherently network aware. Scaling out across multiple machines should be easy.
    - Coherent and robust error handling.
    - Lack of shared state and clear boundaries makes comprehending the system easy (as far as multi-threaded systems go).
    - Basically, if you're creating a complex parallel system, then Akka is likely a good condidiate
    - See here for Akka's take (http://doc.akka.io/docs/akka/2.4.1/intro/why-akka.html)

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
-What actually happened here is that the En prise was destroyed (threw an unhandled exception) and then Starship Command applied it's default supervision policy, which is to recreate the actor, throw away the bad message and then continue (see how the En Prise traveled from base again to revisit the last planet).
-Note that the test then timed out, as Starship Command never then recieved a response about whether or not Clanger Prime was occupied.
-There are a number of ways we could handle this. For now, override the Starships preResart method (don't forget to call super first, as we don't want to lose the default behaviour!) to see if the killer message was one to explore a planet and message Starship Command with a new SOS message saying what the last visited planet was.
-For now, have StarshipCommand assume that if there was someone there to blow up the En Prise, then it probably means that the planet was occupied, so assume that by default.
TODO: Include link to standard Akka supervision policy.

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
-Retire the En Prise and replace it with a router that creates three (unarmed) ships (see the Pi example for inspiration). Stick with the RoundRobin router for now.
-Well, it's working, but it's so slow! The problem is that the RoundRobin router that we're using is allocating the planets to explore to the Starships up front. However, some planets are taking much longer to survey than others, leaving some of our fleet idling.
-Take a look at the Akka routing strategies and select a different one that will make better use of our fleet's time.
-See: http://doc.akka.io/docs/akka/2.4.1/scala/routing.html
-TODO: Make the assertion assert that the total time should be less than some limit maybe.

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
