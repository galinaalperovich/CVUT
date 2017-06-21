include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in2)))
)).

fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in1)))
)).


%Train should not be on s2 when it is switched
fof(not_critical_on_switch_s2, conjecture, (
	(![T, Train]: ((at(T, Train, s2) & at(succ(T), Train, s2)) => (switch(T, s2) = switch(succ(T), s2))))
)).

