This is the implementation of Availability Criteria (Satisfiability, Feasibility and Resiliency) Verifiers for Relationship-based Access Control with support for Multiple Ownership (ReBAC/MO). The following are informal examples of availability criteria.

. Satisfiability: "Object o shall be accessible by at least 35 users in the current protection state."

. Resiliency: "Even if I were to recruit 10 additional patients (or lose 10 of my existing patients), object o shall remain accessible by at least 25 users."

. Feasibility: "I shall be able to make object o accessible by at least 50 users if the company hires no more than 10 additional colleagues (or fires no more than 10 existing colleagues)."

The ReBAC/MO consists of the Boolean combinations of atomic policies. The atomic policies are represented by a 2-tuple; comprising a Birooted graph pattern (one root represents the owner, the other one represents the requester) and a vertex in the social graph, to which the owner in the graph pattern will be anchored. The availability criteria then boils down to finding the graph pattern(s) in the social graph that are isomorphic to the graph patterns in the atomic policies. The verifiers use different technologies as to decide availability criteria. Below we briefly explain each verifier.

. In the first verifier we reduce the Availability Criteria (esp. Satisfiability) to Quantified Boolean Formula Satisfiability (QBF) and compile it into a non-prenex non-CNF format (QCIR). Please note, each atomic policy can be seen as an stand-alone Subgraph Isomorphism problem, hence, we use a fairly standard approach to reduce it to a Boolean Satifiability problem. The Boolean combination of Boolean Satisfiability problem (esp. with negations) yields a non-CNF QBF. A non-clausal QBF solver like RAReQS-NN or GhostQ can then be invoked to decide the availability criteria. 

. In the second verifier we employ an approach similar to the previous one. In nutshell, we reduce atomic policies to SAT problems and call a SAT solver (i.e. Sat4J) to solve it. Then we construct the original policy from the atomic policies and check if it is satisifiable.

. In the third verifier we adopt a completely differnet approach. We reduce the availability criteria into an Answer Set Programming problem. Then an ASP solver (i.e. clingo) is invoked to decide the availability criteria.

The paper reporting the formalities for this implementation is still under review. We will provide a link to this paper as soon as it gets accepted!

Enjoy!

Pooya
