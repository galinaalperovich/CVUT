include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in)))
)).


%Two train should not be on v at the same time
fof(not_critical_two_trains_on_node_v, conjecture, (
	(![T, Train1, Train2]: (((Train1 != Train2) & at(T, Train1, v)) => ~at(T, Train2, v)))
)).

