include ('physical_with_additional_condition.p').
%Control system of the station

%Only one entrance can decided if it should opens or not at one moment
fof(entrance_deciding_0, axiom, (
	(![T]: ((entranceDeciding(T) = in2) <=> (entranceDeciding(succ(T)) = in1)))
)).


fof(entrance_deciding_1, axiom, (
	(![T]: ((entranceDeciding(T) = in1) <=> (entranceDeciding(succ(T)) = in2)))
)).


fof(entrance_deciding_restr, axiom, (
	(![T]: ((entranceDeciding(T) = in2) | (entranceDeciding(T) = in1)))
)).


%Define state of switches for every time moment
fof(state_switch_s1_to_out1, axiom, (
	(![T]: ?[Train]: ((switch(succ(T), s1) = out1) <=> ((at(T,Train,s1) & (switch(T, s1) = out1)) | (at(T, Train, in1) & gate(Train) = out1 & open(T, in1)))))
)).


fof(state_switch_s1_to_out2, axiom, (
	(![T]: ?[Train]: ((switch(succ(T), s1) = out2) <=> ((at(T,Train,s1) & (switch(T, s1) = out2)) | (at(T, Train, in1) & gate(Train) = out2 & open(T, in1)))))
)).


fof(state_switch_s2_to_out1, axiom, (
	(![T]: ?[Train]: ((switch(succ(T), s2) = out1) <=> ((at(T,Train,s2) & (switch(T, s2) = out1)) | (at(T, Train, in2) & gate(Train) = out1 & open(T, in2)))))
)).


fof(state_switch_s2_to_out2, axiom, (
	(![T]: ?[Train]: ((switch(succ(T), s2) = out2) <=> ((at(T,Train,s2) & (switch(T, s2) = out2)) | (at(T, Train, in2) & gate(Train) = out2 & open(T, in2)))))
)).


%Path is safe for using if there are no trains which can cross the path at some later time
fof(path_0_is_safe, axiom, (
	(![T, Train]: (isSafe(T, in2_s2_out1)<=> ((~at(T, Train, s2)))))
)).


fof(path_1_is_safe, axiom, (
	(![T, Train]: (isSafe(T, in2_s2_out2)<=> ((~at(T, Train, s2)))))
)).


fof(path_2_is_safe, axiom, (
	(![T, Train]: (isSafe(T, in1_s1_out1)<=> ((~at(T, Train, s1)))))
)).


fof(path_3_is_safe, axiom, (
	(![T, Train]: (isSafe(T, in1_s1_out2)<=> ((~at(T, Train, s1)))))
)).


%Path is ready to be activated if it is safe, there is a train at the entrance node, it is time to decide for entrance node and train's target is path's exit node
fof(path_0_is_ready_to_be_activated, axiom, (
	![T]: ?[Train]: (toBeActivated(T, in2_s2_out1) <=> ((isSafe(T, in2_s2_out1)) & (at(T,Train,in2)) & (entranceDeciding(T)=in2) & (gate(Train)=out1)))
)).


fof(path_1_is_ready_to_be_activated, axiom, (
	![T]: ?[Train]: (toBeActivated(T, in2_s2_out2) <=> ((isSafe(T, in2_s2_out2)) & (at(T,Train,in2)) & (entranceDeciding(T)=in2) & (gate(Train)=out2)))
)).


fof(path_2_is_ready_to_be_activated, axiom, (
	![T]: ?[Train]: (toBeActivated(T, in1_s1_out1) <=> ((isSafe(T, in1_s1_out1)) & (at(T,Train,in1)) & (entranceDeciding(T)=in1) & (gate(Train)=out1)))
)).


fof(path_3_is_ready_to_be_activated, axiom, (
	![T]: ?[Train]: (toBeActivated(T, in1_s1_out2) <=> ((isSafe(T, in1_s1_out2)) & (at(T,Train,in1)) & (entranceDeciding(T)=in1) & (gate(Train)=out2)))
)).


%Open the entrance if there is path started with this entrance
fof(open_0, axiom, (
	![T]: (open(succ(T),in2) <=> ((toBeActivated(T, in2_s2_out1)) | (toBeActivated(T, in2_s2_out2)) | (open(T, in2) & (?[Train]: (at(T, Train, in2) & ~go(T, Train))))))
)).


fof(open_1, axiom, (
	![T]: (open(succ(T),in1) <=> ((toBeActivated(T, in1_s1_out1)) | (toBeActivated(T, in1_s1_out2)) | (open(T, in1) & (?[Train]: (at(T, Train, in1) & ~go(T, Train))))))
)).


%Path are different
fof(different_paths, axiom, (
	(in2_s2_out1 != in2_s2_out2) & (in2_s2_out1 != in1_s1_out1) & (in2_s2_out1 != in1_s1_out2) & (in2_s2_out2 != in1_s1_out1) & (in2_s2_out2 != in1_s1_out2) & (in1_s1_out1 != in1_s1_out2)
)).

