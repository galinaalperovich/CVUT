include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in)))
)).


%Two train should not be on in at the same time
fof(not_critical_two_trains_on_node_in, conjecture, (
	(![T, Train1, Train2]: (((Train1 != Train2) & at(T, Train1, in)) => ~at(T, Train2, in)))
)).

