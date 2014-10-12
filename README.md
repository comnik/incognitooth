incognitooth
============

Decentralized, persistent message passing. Hack-Zurich 2014 entry.

We care about secure, independent communication. Even today, thousands of people
rely on easily monitored, centralized service providers or have no access to 
networking infrastructure at all.

Be it protestors and whistleblowers who need to share information that could
put them at risk, rural communities who want to establish basic networking capabilities
or large crowds trying to organize when faced with catastrophes, the need for
decentralized networks is dearly felt.

Traditionally, Peer-To-Peer networks establish a lasting connection amongst a group
of near-by peers. This enables great things like real-time communication or connection sharing.
Unfortunately, it fails to convey information to peers that are not in reach of
an existing network.

Exploring a possible solution to this problem, we implemented a proof-of-concept for
persistent packages in a Bluetooth-based P2P-Network. Each node stores packets it generated
or recieved until a specific expiration date. Everytime two or more peers come close enough
to each other, they exchange all packets they have stored. Because all packets are encrypted,
no peer can intercept information. By tagging each packet with the public key of its intended
destination, each peer can identify information inteded for itself.
