Ant Challenge Project for Addepar Interview
By: Vidhur Vohra
April 8th, 2012
---

This is my code for the Ant Challenge. I have included JUnit tests which I used during the design/testing phase. Since the game engine does not like compiled Java classes in packages, I had to make an adapter since the JUnit tests can only work from non-default packages.

During the design phase I coded BFS/Djikstra/A* to use as a search algorithm. For the final submission I have decided to use the BFS algorithm.

NOTE: The engine can slow down a good amount if there are many ants on top of each other. I did try to reduce the lag through the use of profilers but could not reduce it to a satisfying amount.

---

Implemented Ant Behaviors:
 - Ants use BFS to search for closest desired type of Cell

 - The path an ant takes from the last time it was at HOME is tracked (fromHomePlan). After the ant has picked up FOOD and wants to return HOME, the length of the new planned path (currentPlan) is checked against the fromHomePlan one. If the length is the same, take the "fromHomePlan." This allows for the ant to always take the same route it took to the food back home and hopefully share knowledge with ants on the way home.

 - The initial 3 ants are 'scouts' that explore the world but do not bring food home immediately. Once a counter runs out they switch "TOFOOD" mode.

 - If a new ant spawns and there is already a certain amount of food on the mound, the spawned ant HALTS. This is due to the fact that the food around the mound has probably already been collected and it would be wiser to wait for the spawned ant's world map to be updated from another ant.

---

The Ants are implemented using a finite state machine. Description of the Ant Modes FSM is below.

SCOUT
	If scoutModeCounter is > 0.
		If plan exists, continue with it
		Find closest UNEXPLORED, return
	ChangeMode(TOFOOD)

EXPLORE
	If map was updated, ChangeMode(TOFOOD)
	If plan exists, continue with it
	If more than 200 food on mound
		if at HOME, wait
		else ChangeMode(TOHOME)
	If can find closest UNEXPLORED, return
	ChangeMode(TOHOME)
	
TOFOOD
	If map has been updated and target tile has no food, re-plan.
	If plan exists, continue with it
	If on food, gather. ChangeMode(TOHOME)
	If can find closest FOOD, return
	ChangeTo(EXPLORE)
	
TOHOME
	If at HOME and not carrying food
		ChangeMode(Explore)
	If at HOME and carrying food. 
		DROPOFF
		If scout, reset counter. 
		ChangeMode(TOFOOD)
	If plan exists, continue with it
	If can find closest HOME,
		If plan to home and plan from home is same, switch.
		return
	If can't find unexplored/food, ERROR
