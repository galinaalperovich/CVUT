include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in)))
)).


%Train should not be on v when it is switched
fof(not_critical_on_switch_v, conjecture, (
	(![T, Train]: ((at(T, Train, v) & at(succ(T), Train, v)) => (switch(T, v) = switch(succ(T), v))))
)).

