include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T, Train]: (enter(T,Train,in)))
)).


%Entrace in should not be closed every time
fof(not_critical_entrance_closed_in, conjecture, (
	 (?[T]: (open(T,in)))
)).

