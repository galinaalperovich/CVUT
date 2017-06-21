include ('axioms.p').
%Physical model of the station

%Movements of the trains: predicate at

%Train is on the entrance node <=> it was appeared there at the previous moment or it is on the same node and (doesn't want to go or the signal is blocking it) 
fof(at_in2, axiom, (
	![T,Train]: (
		at(succ(T), Train, in2) <=> ( 
			(enter(T, Train, in2)) | 
			(at(T, Train, in2) & (~go(T,Train) | ~open(T, in2)))
		)
	)
)).

fof(at_in1, axiom, (
	![T,Train]: (
		at(succ(T), Train, in1) <=> ( 
			(enter(T, Train, in1)) | 
			(at(T, Train, in1) & (~go(T,Train) | ~open(T, in1)))
		)
	)
)).


%Train is on the node <=> it was arrived from the previous node at the previous moment or it is on the same node and doesn't want to go
fof(at_out2, axiom, (
	![T,Train]: (
		at(succ(T), Train, out2) <=> (
			(at(T, Train, s1) & (switch(T, s1) = out2) & go(T, Train)) |
			(at(T, Train, s2) & (switch(T, s2) = out2) & go(T, Train))
		)
	)
)).

fof(at_out1, axiom, (
	![T,Train]: (
		at(succ(T), Train, out1) <=> (
			(at(T, Train, s1) & (switch(T, s1) = out1) & go(T, Train)) |
			(at(T, Train, s2) & (switch(T, s2) = out1) & go(T, Train))
		)
	)
)).

fof(at_s1, axiom, (
	![T,Train]: (
		at(succ(T), Train, s1) <=> (
			(at(T, Train, in1) & open(T, in1) & go(T, Train)) |
			(at(T, Train, s1) & ~go(T, Train))
		)
	)
)).

fof(at_s2, axiom, (
	![T,Train]: (
		at(succ(T), Train, s2) <=> (
			(at(T, Train, in2) & open(T, in2) & go(T, Train)) |
			(at(T, Train, s2) & ~go(T, Train))
		)
	)
)).

%Train can be only at one node at one time moment
fof(at_uniq, axiom, (
	![T,Train,N1,N2]: ((at(T, Train, N1) & at(T, Train, N2)) => (N1 = N2))
)).

%Every train departs at some time, so go(T,Train) will = true at some time
fof(train_will_depart, axiom, (
	![T,Train,N]: ?[Next_T]: (at(T,Train,N) => ((less(T,Next_T) & go(Next_T,Train))))
)).

%Every train wasn't  in the node all the time, it appeared in some time
fof(train_appeared_sometime, axiom, (
	![T,Train,N]: ?[Prev_T]: (at(T, Train, N) => ((less(Prev_T,T) & ~at(Prev_T,Train, N))))
)).

%Train will not enter when it is already on the station
fof(at_nondup, axiom, (
	![T,Train,N,OtherN]: (at(T, Train, N) => ~enter(T, Train, OtherN))
)).

%Train will not enter occupied node
fof(input_nonoccup, axiom, (
	![T,Train,OtherTrain,N]: (at(T, Train, N) => ~enter(T, OtherTrain, N))
)).

%Two trains will not enter same node same time
fof(enter_uniq, axiom, (
	![T,Train1,Train2,N]: ((enter(T, Train1, N) & enter(T, Train2, N)) => (Train1 = Train2))
)).

%When the train at exit node => next time it is not there
fof(train_out_of_exit_0, axiom, (
	![T,Train]: (at(T,Train,out2) => ~at(succ(T),Train,out2))
)).

fof(train_out_of_exit_1, axiom, (
	![T,Train]: (at(T,Train,out1) => ~at(succ(T),Train,out1))
)).

%Nodes are different
fof(different_nodes, axiom, (
	(out2 != out1) & (out2 != in2) & (out2 != in1) & (out2 != s1) & (out2 != s2) & (out1 != in2) & (out1 != in1) & (out1 != s1) & (out1 != s2) & (in2 != in1) & (in2 != s1) & (in2 != s2) & (in1 != s1) & (in1 != s2) & (s1 != s2)
)).

%If train is at station, then it is at one of the station nodes
fof(at_restr, axiom, (
	![T, Train, N]: (at(T, Train, N) => ((N = out2) | (N = out1) | (N = in2) | (N = in1) | (N = s1) | (N = s2)))
)).

%Train has gate
fof(gates_restr, axiom, (
	![Train]: ((gate(Train) = out2) | (gate(Train) = out1))
)).

