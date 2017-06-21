%Order: less; functions: succ and pred
fof(antisymmetry, axiom, (
	![X,Y]: ((less(X,Y) & less(Y,X)) => (X = Y))
)).

fof(transitivity, axiom, (
	![X,Y,Z]: ((less(X,Y) & less(Y, Z)) => less(X, Z))
)).

fof(totality, axiom, (
	![X,Y]: (less(X,Y) | less(Y,X))
)).

fof(succ, axiom, (
	![X]: ((less(X,succ(X))) & (![Y]: (less(Y,X) | less(succ(X), Y))))
)).

fof(pred, axiom, (
	![X]: (((pred(succ(X)) = X) & (succ(pred(X)) = X)))
)).

