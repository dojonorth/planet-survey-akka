# planet-survey-akka
Learn Akka! Discover strange alien species, then get killed by them!

NOTE - THESE NOTES ARE REALLY ROUGH - I'll TIDY THEM UP.

# Setup
TODO: Include some stuff on setting up sbt with brew if people don't have it.
-Clone the repo locally and run 'sbt test' to compile and run the work.
-For ease of running and editing I'd also suggest importing it into an IDE. IntelliJ should recognise it as a Scala project and read the build.sbt)
-Probably needs Java 8, but I'll check.
-It's written in Scala, but mostly you'll be plugging together existing code based on a given example, so even if you're not familiar with Scala then you should be able to make progress.

# Intro
-Reasoning about concurrent code is very hard - try wriring a complex multi-thread system in Java without any libraries. Likely it'll be one of the hardest things you'll ever have to do.
- However, lots of interest in further parallelising code last 5 or so years. Probably as a consequence of the need to parallelise code more as we begin to reach the limits of single core performance improvements and new architectures instead scale out with more logical cores.
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
-TODO: Add links to the Akka Doc (say it's really good) for all these things if people need more info.

PART 1
------
-AwayMission will act as our entry point.
-Create an Akka system called 'The-Corperation'
-Create a StarShipCommand called 'Admiral-Reith'. This should be a top-level actor i.e. create is by calling 'actorOf' directly on the Akka system you've just created.
-Message command and tell it to go and scan the passed planet (Gallifrey, in this case).
TODO: Get rid of the construction of the akks system and the ship. Ensure command returns 0 by default.

PART 2
------
TBC
