I have some issues uploadoing this and making changes. I did this project
before I started to use Git. I wouldn't worry about the commit history.

Things that need work:
- Flesh out documentation, right now it's difficult because I wanted the
  code to be understandable without looking at the documentation, but
that leads to unnecessary comments.
- I don't like I have to check for null. Thinking about passing an
  object, then manipulate the object for desired results if possible.
  Return true/false if the output is valid? Need to think about this more.

--
Ant Challenge Project for Addepar Interview
By: Vidhur Vohra
Date of Completion: April 9th, 2012
---

Highly recommend to import project with Eclipse!

---

NOTES: 

JUnit tests are under "/tests."

Game engine errors on non-default package classes. So I made adapter since JUnit tests needed defined packages. 

During the design phase I coded BFS/Djikstra/A* to use as a path planning algorithm. For the final submission I have decided to use the BFS algorithm.

Engine can be slow if many ants are on top of each other. I did try to reduce the lag but could not reduce it to a satisfactory amount.

Enums are implicitly static but I used them anyway.  I figured the ants aren't allowed to talk to each other directly, which I did not do.

My commenting can be a bit too much, but that's because this way it's not necessary to keep switching between the code and the FSM description below.

Made on UBUNTU machine with Eclipse.
---

Implemented Ant Behaviors:
 - Ants use BFS to search for closest desired type of Cell
 
 - If the path an ant has taken to a food source is same length as the path back, prefer the path it came on. Taking the same path back hopefully means ants are met along the way home.
 
 - The initial 3 ants are 'scouts' that explore the world but do not bring food home immediately. Once a counter runs out they switch "EXPLORE" mode (which means they will find the closest food and return home). Scout ants will remain that mode until a certain amount of food has been found in the world.

 - If ant spawns and certain amount of food already exists on mound, spawned ant HALTS. Because it's probably better to wait for new information from another ant instead of wandering/exploring. 

 - Only search for food when food sources are updated to lower the number of graph searches.

---

The Ants are implemented using a finite state machine:

SCOUT
	If TotalFoodFound > VALUE: ant is not a scout anymore
	If scoutModeCounter is > 0:
		If plan exists: return nextAction
		If can find closest UNEXPLORED: return nextAction
	ChangeMode(EXPLORE)

EXPLORE
	If food source updated: ChangeMode(TOFOOD)
	If plan exists: return nextAction
	If can find closest UNEXPLORED: return nextAction
	If at home: return HALT
	ChangeMode(TOHOME)
	
TOFOOD
	If food source updated and target tile has no food, re-plan.
	If plan exists: return nextAction
	If on food, gather: ChangeMode(TOHOME)
	If can find closest FOOD: return nextAction
	ChangeTo(EXPLORE)
	
TOHOME
	If at HOME and not carrying food: ChangeMode(Explore)
	If at HOME and carrying food:
		If Scout: ChangeModeWithAction(SCOUT,DROPOFF)
		Else ChangeModeWithAction(DROPOFF,TOFOOD)
	If plan exists: return nextAction
	If can find HOME:
		If plan to home and plan from home is same: switch them.
		return nextAction
	If at HOME and can't find closest food or unexplored: return HALT
	If still in TOHOME mode: throw error.
