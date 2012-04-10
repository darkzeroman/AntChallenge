



Ant Modes: 

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