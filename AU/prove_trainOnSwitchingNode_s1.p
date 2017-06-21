include ('control_system.p').
%Additional condition that trains always appear
fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in2)))
)).

fof(appear_always, axiom, (
	(![T]:?[Train]: (enter(T,Train,in1)))
)).


%Train should not be on s1 when it is switched
fof(not_critical_on_switch_s1, conjecture, (
	(![T, Train]: ((at(T, Train, s1) & at(succ(T), Train, s1)) => (switch(T, s1) = switch(succ(T), s1))))
)).

