include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in2)))
)).

fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in1)))
)).


%Two train should not be on s2 at the same time
fof(not_critical_two_trains_on_node_s2, conjecture, (
	(![T, Train1, Train2]: (((Train1 != Train2) & at(T, Train1, s2)) => ~at(T, Train2, s2)))
)).

