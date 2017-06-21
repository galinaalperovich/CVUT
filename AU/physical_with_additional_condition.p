include ('physical.p').
%Additional condition which say that a train moves from one node to another as soon as possible
fof(go_always, axiom, (
	![X,T]: (go(X,T))
)).

