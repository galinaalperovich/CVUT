include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T, Train]: (enter(T,Train,in2)))
)).

fof(appear_always, axiom, (
	(![T, Train]: (enter(T,Train,in1)))
)).


%Entrace in2 should not be closed every time
fof(not_critical_entrance_closed_in2, conjecture, (
	 (?[T]: (open(T,in2)))
)).

